package com.example.qrhunterapp_t11.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.qrhunterapp_t11.R;
import com.example.qrhunterapp_t11.activities.CaptureAct;
import com.example.qrhunterapp_t11.activities.TakePhotoActivity;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.lang.Math;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;
import static nl.dionsegijn.konfetti.core.Position.Relative;

/**
 * Logic for the camera fragment, which is responsible for managing everything that pertains to scanning and adding a new QR code.
 * Calls all the necessary activities for achieving this (the scan QR and take photo activities) .
 *
 * @author Aidan Lynch - methods related to QR scanning, photo-taking and main camera fragment screen logic.
 * @author Daniel Guo - methods related to geolocation and obtaining permissions for location.
 * @author Josh Lucas and Afra - methods for creating a new QR object
 */
public class CameraFragment extends Fragment {
    private static final int permissionsRequestLocation = 100;
    private final boolean mIsPreciseLocationEnabled = false;
    public static final int permissionsRequestAccessFineLocation = 9003;
    public static final int permissionsRequestAccessCoarseLocation = 9004;
    private ActivityResultLauncher<ScanOptions> barLauncher;
    private ActivityResultLauncher<Intent> photoLauncher;
    private QRCode qrCode;
    private String imageUrl;
    private String resizedImageUrl;
    private SharedPreferences prefs;
    private KonfettiView konfettiView;
    private final FirebaseFirestore db;
    private final CollectionReference qrCodesReference;
    private final CollectionReference usersReference;
    private static final String locationPrompt = "LocationPrompt";

    boolean qrExists;
    boolean qrRefExists;

    public CameraFragment(@NonNull FirebaseFirestore db) {
        this.db = db;
        this.qrCodesReference = db.collection("QRCodes");
        this.usersReference = db.collection("Users");
    }


    /**
     * Inflates the layout for the camera fragment.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return a View containing the inflated layout.
     */
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    /**
     * Once the layout view is initialized, call the function to scan a code.
     * Having a view for the camera fragment may be redundant at the moment (since it's never really used),
     * but will keep this for now in case we need to display something in the layout later.
     * <p>
     * Also for some reason scanCode() must be called here or the app will crash.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scanCode(); // start scanning a QR code
    }

    /**
     * Called when fragment is being initialized. Creates a dialog that displays the score of the scanned QR code. The dialog disappears automatically
     * after a few seconds.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     * @reference <a href="https://www.youtube.com/watch?v=W4qqTcxqq48">how to create a custom dialog</a>
     * @reference <a href="https://xjaphx.wordpress.com/2011/07/13/auto-close-dialog-after-a-specific-time/">how to have dialog automatically close after a few seconds</a>
     * @reference Erfan - https://stackoverflow.com/a/54166609/14445107 - how to remove dim from dialog
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        photoLauncher = registerForActivityResult( // should be okay to initialize before scanner
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent intent = result.getData();
                        assert intent != null;
                        Bundle extras = intent.getExtras();
                        imageUrl = extras.getString("url");

                        resizedImageUrl = getResizeImageUrl(imageUrl); //TODO get true url of image

                        promptForLocation(); // prompt for location once the TakePhotoActivity has finished
                    }
                }
        );

        barLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // create a builder for the alert dialog
                String resultString = result.getContents(); // access QR code contents

                // object instantiated
                qrCode = new QRCode(resultString);

                // create custom dialog to display QR score
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.qr_scored_dialog, null);
                TextView scoredTV = dialogView.findViewById(R.id.scoredTV);
                builder.setView(dialogView);
                builder.setCancelable(false);
                String scored = qrCode.getPoints() + " Points";
                scoredTV.setText(scored);

                final AlertDialog alertDialog = builder.create();
                alertDialog.show(); // create and display the dialog
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Objects.requireNonNull(alertDialog.getWindow()).setDimAmount(0);
                }

                createKonfetti(); // party rock is in the house tonight

                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    public void run() {
                        alertDialog.dismiss();
                        timer.cancel();
                        promptForPhoto(); // prompt the user for a photo of the QR object or location once the score dialog disappears


                    }
                }, 5000); // set a timer to automatically close the dialog after 5 seconds
            }
        });
    }

    /**
     * Function to initialize QR scanner options, and order the QR scanner to start scanning using the CaptureAct.
     *
     * @reference <a href="https://www.youtube.com/watch?v=jtT60yFPelI">how to configure the QR camera scanner and obtain the QR contents from the CaptureAct</a>
     */
    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up for flash");
        options.setOrientationLocked(true);
        options.setBeepEnabled(false);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    /**
     * Creates a dialog for whether the user would like to take a photo of the object or location of the QR code.
     * If the user selects "no", this step will be skipped and the user's geo-location will be prompted next.
     *
     * @reference <a href="https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android">how to create an AlertDialog</a>
     * @reference <a href="https://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare">updating UI elements from within a thread using runOnUiThread()</a>
     * @reference <a href="https://stackoverflow.com/a/19064968/14445107">how to prevent users from touching outside a dialog box to escape it</a>
     */
    private void promptForPhoto() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(getContext())
                        .setTitle("Take Photo")
                        .setMessage("Take photo of object or location?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new
                                DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d("PhotoPrompt", "User accepted photo prompt.");
                                        takePhoto();
                                    }
                                })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("PhotoPrompt", "User rejected photo prompt.");
                                promptForLocation(); // skip straight to geolocation prompt
                            }
                        })
                        .show();
            }
        });
    }

    /**
     * Helper function that starts the TakePhotoActivity if the user accepts the photo prompt.
     *
     * @reference <a href="https://stackoverflow.com/questions/28619113/start-a-new-activity-from-fragment">how to start an activity from within a fragment</a>
     */
    private void takePhoto() {
        Intent intent = new Intent(getActivity(), TakePhotoActivity.class);
        photoLauncher.launch(intent);
    }

    /**
     * Connects the GoogleApiClient and initiates the permissions check
     */
    private void connectGoogleApiClient() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(requireContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        permissions();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    }
                })
                .build();
        googleApiClient.connect();
    }

    /**
     * Retrieves the current location and logs the latitude and longitude of the location.
     * Adds QRCode to db with location and returns to profile
     * Adds QRCode to db and returns to profile
     */
    private void getCurrentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // Location data is available
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        Log.d(locationPrompt, "Latitude: " + latitude + ", Longitude: " + longitude);

                        //set longitude and latitude and store
                        qrCode.setLatitude(latitude);
                        qrCode.setLongitude(longitude);
                        qrCode.setID(latitude, longitude);
                        addQRCode();
                        returnToProfile();
                    } else {
                        // Location data is not available
                        Log.d(locationPrompt, "ERROR Location data is not available.");
                        //stores QRCode into db with just hash as document id and location = null
                        addQRCode();
                        returnToProfile();
                    }
                }
            });
        }
    }

    /**
     * Initiates the location permission check and logs if permission is already granted
     */
    private void permissions() {
        boolean isFineLocationGranted = ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean isCoarseLocationGranted = ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (isFineLocationGranted) {
            Log.d(locationPrompt, "PERMISSION ALREADY GAVE F.");
            getCurrentLocation();
        } else if (isCoarseLocationGranted) {
            Log.d(locationPrompt, "PERMISSION ALREADY GAVE C.");
            getCurrentLocation();
        } else {
            Log.d(locationPrompt, "ASKING FOR PERMISSION.");
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, permissionsRequestLocation);
        }
    }

    /**
     * Handles the user's response to the location permission request.
     * Calls getCurrentLocation() if permission is granted, otherwise adds QRCode to db with location=null and returns to profile.
     *
     * @param requestCode  The request code of the permission request.
     * @param permissions  The requested permissions.
     * @param grantResults The results of the permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case permissionsRequestLocation:
                boolean isFineLocationGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean isCoarseLocationGranted = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (isFineLocationGranted) {
                    Log.d(locationPrompt, "Execute if permission granted f.");
                    getCurrentLocation();
                } else if (isCoarseLocationGranted) {
                    Log.d(locationPrompt, "Execute if permission granted c.");
                    getCurrentLocation();
                } else {
                    // Permission is not granted
                    Log.d(locationPrompt, "Execute if permission not granted.");
                    //stores QRCode into db with just hash as document id and location = null
                    addQRCode();
                    returnToProfile();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Prompts the user as to whether they would like to share their geolocation for a QR code. If they click "yes", the QR code will be created with location, and
     * if they press "no" without location.
     *
     * @reference <a href="https://www.youtube.com/watch?v=DfDj9EadOLk">how to use activityresultlauncher to execute code after an activity closes</a>
     * @reference <a href="https://stackoverflow.com/a/63883427/14445107">where to initialize an activityresultlauncher</a>
     */
    private void promptForLocation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Share Geolocation")
                .setMessage("Let others find this location on the map?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(locationPrompt, "User accepted geolocation prompt.");
                        permissions();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(locationPrompt, "User rejected geolocation prompt.");
                        //stores QRCode into db with just hash as document id and location = null
                        addQRCode();

                        returnToProfile();
                    }
                })
                .show();
    }

    /**
     * Helper function to return to profile screen once user has finished adding a QR code. Otherwise if a user tried
     * to add another QR code immediately after scanning one, since they're technically still in the CameraFragment,
     * nothing would happen.
     *
     * @reference <a href="https://stackoverflow.com/a/60055145/14445107">using getParentFragmentManager() instead of getFragmentManager()</a>
     */
    private void returnToProfile() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() { // wait 500ms before returning to profile; if app returns to quickly the addition will not be registered in Firestore yet (the RecyclerView will update too early)
                FragmentTransaction trans = getParentFragmentManager().beginTransaction();
                trans.replace(R.id.main_screen, new ProfileFragment(db, prefs.getString("currentUserDisplayName", null), prefs.getString("currentUserUsername", null)));
                trans.commit();
            }
        }, 500);
    }

    /**
     * When new QRCodes are scanned, if the resulting hash already exists in
     * the database, this function calculates the distance between the two locations
     * using the Haversine formula, if the distance between the two points is less
     * than the set threshold, the scanned QRCode is considered the same as QRCode object
     * already in the database, and no new document will be inserted; user profile
     * will reference pre-existing QRCode
     *
     * @return boolean - returns true if distance shorter than uniqueness threshold, else false if 2 separate instances
     * @references https://www.trekview.org/blog/2021/reading-decimal-gps-coordinates-like-a-computer/ David G, August 27, 2021 how to read lat/long
     *             https://en.wikipedia.org/wiki/Haversine_formula  how to calculate distance between to locations on earth using lat/long
     *             https://linuxhint.com/import-math-in-java/   how use Math library
     *             https://docs.oracle.com/javase/8/docs/api/java/lang/Math.html    cos, sin, arcsin
     */

    public static boolean isSameLocation() {
        //TODO INPUT VALIDATION:
        // some coordinates shouldn't make sense, iirc long can't have larger magnitude than +-180?
        // and +- for lat?


        double maxDistance = 30;    // in meters
        double radius = 6371.0;     // earths radius in kilometers

        //TODO do we want to pass to QRCode objects or just the lat/long values of two objects?
        // latitude & longitude of first QRCode
        // otherwise function itself works, calculations are verified correct

        //TODO ** CURRENT COORDINATES HARDCODED FOR TESTING
        double lat1 = 38.8977;
        double lng1 = -77.0365;

        // latitude & longitude of second QRCode
        double lat2 = 48.8584;
        double lng2 = 2.2945;

        // convert degrees to radians
        // phi = latitude, lambda = longitude
        double phi1 = (lat1*Math.PI)/180.0;
        double lambda1 = (lng1*Math.PI)/180.0;

        double phi2 = (lat2*Math.PI)/180.0;
        double lambda2 = (lng2*Math.PI)/180.0;

        //calculate haversine(theta), the central angle between both locations relative to earth's center
        // haversine(theta) = sin^2((phi2-phi1)/2)+cos(phi1)cos(phi2)sin^2((lamda2-lamda1)/2)
        double haversine = ( Math.pow( Math.sin((phi2-phi1)/2) ,2) + Math.cos(phi1)*Math.cos(phi2)*( Math.pow(Math.sin( (lambda2-lambda1)/2), 2 ) ));

        //calculate distance between both points using haversine
        // distance = 2r*arcsin(sqr(haversine(theta)))
        double distance = (2*radius)*(Math.asin(Math.sqrt(haversine)));

        //System.out.printf("%f\n", haversine);
        System.out.printf("%f\n", distance);

        //convert distance to meters and compare with maxDistance
        distance = distance*1000;
        System.out.printf("distance in meters: %f\n", distance);

        if(distance <= maxDistance) {
            System.out.printf("Same\n");
            return true;
        }
        else    {
            System.out.printf("Different\n");
            return false;
        }
    }



    /**
     * Helper function to check if a QR code document exists
     *
     * @param docToCheck document that should be checked for
     * @param cr         CollectionReference to the collection being accessed
     * @reference <a href="https://firebase.google.com/docs/firestore/query-data/get-data">used without major modification</a>
     * @reference Aidan Lynch's CameraFragmentTest for this code
     */
    public void checkDocExists(@NonNull String docToCheck, @NonNull CollectionReference cr, final @NonNull Callback dataValid) {
        DocumentReference docRef = cr.document(docToCheck);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("DocExist", "DocumentSnapshot data: " + document.getData());
                        dataValid.dataValid(true);
                    } else {
                        Log.d("DocExist", "No such document");
                        dataValid.dataValid(false);
                    }
                } else {
                    Log.d("DocExist", "get failed with ", task.getException());
                }
            }
        });
    }

    public interface Callback {
        void dataValid(boolean valid);
    }

    /**
     * Helper function to add QRCode object to QRCodes and Users collections
     */
    private void addQRCode() {
        String currentUser = prefs.getString("currentUserUsername", null);
        String qrCodeID = qrCode.getID();

        Map<String, Object> qrCodeRef = new HashMap<>();
        DocumentReference qrCodeDocumentRef = qrCodesReference.document(qrCodeID);
        qrCodeRef.put("Reference", qrCodeDocumentRef);

        // Check if qrCode exists in db in QRCodes collection
        checkDocExists(qrCodeID, qrCodesReference, new Callback() {
            public void dataValid(boolean valid) {
                qrExists = valid;
                System.out.println(valid);

                // Check if reference to qrCode exists in db in Users collection
                checkDocExists(qrCodeID, usersReference.document(currentUser).collection("User QR Codes"), new Callback() {
                    public void dataValid(boolean valid) {
                        qrRefExists = valid;
                        System.out.println(valid);

                        // If qrCode does not exist, add it to QRCode collection
                        if (!qrExists){
                            qrCodesReference.document(qrCodeID).set(qrCode);
                            if (resizedImageUrl != null){
                                qrCodesReference.document(qrCodeID).update("photoList", FieldValue.arrayUnion(resizedImageUrl));
                                //QRCodesReference.document(QRCodeId).update("photoList", FieldValue.arrayRemove(resizedImageUrl));
                            }
                        }
                        // If user does not already have this qrCode, add a reference to it, increment their total scans, add new photo to qrCode
                        if(!qrRefExists){
                            System.out.println("HEUHURLSHRPIUSHEPRIHSEPOIHRPOISHEPROIPSOEHRPOISHEPRIHP");
                            usersReference.document(currentUser).collection("User QR Codes").document(qrCodeID).set(qrCodeRef);
                            usersReference.document(currentUser).update("totalScans", FieldValue.increment(1));
                            usersReference.document(currentUser).update("totalPoints", FieldValue.increment(qrCode.getPoints()));
                            if (resizedImageUrl != null){
                                qrCodesReference.document(qrCodeID).update("photoList", FieldValue.arrayUnion(resizedImageUrl));
                                //QRCodesReference.document(QRCodeId).update("photoList", FieldValue.arrayRemove(resizedImageUrl));
                            }

                        }
                        // If user does not have this qrCode but it already exists in qrCode collection, increase its total scans
                        if ((qrExists) && (!qrRefExists)){
                            qrCodesReference.document(qrCodeID).update("numberOfScans", FieldValue.increment(1));
                        }
                    }
                });
            }
        });
    }

    /**
     * Creates some confetti when you scan a QR code :)
     *
     * @reference Daniel Martinus - https://github.com/DanielMartinus/Konfetti/blob/main/samples/xml-java/src/main/java/nl/dionsegijn/xml/java/MainActivity.java - used without major modification
     */
    public void createKonfetti() {
        konfettiView = getActivity().findViewById(R.id.konfetti_view);
        EmitterConfig emitterConfig = new Emitter(6, TimeUnit.SECONDS).perSecond(125);
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .angle(Angle.RIGHT - 65)
                        .spread(Spread.WIDE)
                        .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE))
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(10f, 30f)
                        .position(new Relative(0.0, 0.3))
                        .build(),
                new PartyFactory(emitterConfig)
                        .angle(Angle.LEFT + 65)
                        .spread(Spread.WIDE)
                        .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE))
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(10f, 30f)
                        .position(new Relative(1.0, 0.3))
                        .build()
        );
    }

    /**
     * Gets the true url of the resized image, since firebase does not do this for some reason. Simply adds "_504x416" inside the url,
     * which is the dimensions of the resized image.
     *
     * @param rawImageUrl the original url of the uploaded image, which does not provide the proper path to the resized image.
     * @return a string containing the url of the resized image, that will be used later when retrieving it for viewing in the QR view.
     * @reference Lee Meador - https://stackoverflow.com/a/18521373/14445107 - how to insert a string in the middle of another; used without major modification
     */
    private String getResizeImageUrl(String rawImageUrl) {
        int index = rawImageUrl.indexOf(".jpg");
        String urlFirstHalf = rawImageUrl.substring(0, index);
        String urlSecondHalf = rawImageUrl.substring(index);
        return urlFirstHalf + "_504x416" + urlSecondHalf; //TODO probably shouldn't use a string literal; make a constant or something
    }
}