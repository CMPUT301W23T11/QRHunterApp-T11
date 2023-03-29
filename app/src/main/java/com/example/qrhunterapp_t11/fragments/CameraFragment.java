package com.example.qrhunterapp_t11.fragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static nl.dionsegijn.konfetti.core.Position.Relative;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import com.example.qrhunterapp_t11.interfaces.QueryCallback;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;

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
    private String currentUserDisplayName;
    private String currentUserUsername;
    private final FirebaseFirestore db;
    private final CollectionReference qrCodesReference;
    private final CollectionReference usersReference;
    private static final String locationPrompt = "LocationPrompt";

    boolean qrExists;
    boolean qrRefExists;
    String qrCodeID;

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
     * This function calculates the distance between two locations on earth (input
     * via decimal latitude longitude coordinates) using the Haversine formula;
     * if the distance between the two points is less than the input threshold,
     * returns true, else false
     * <p>
     * In the context of a freshly scanned QRCode, if the hash function of the new code
     * matches the hash of a QRCode already in the db, this function determines if they should
     * be considered unique objects or the same QRcode (sharing comments, photos etc...)
     * if the function returns true using the new QRCode and the QRCode object already in the database,
     * no new document will be inserted (user profile will reference pre-existing QRCode), otherwise
     * a new entry will be created
     *
     * @param lat1        Double -latitude coordinate of first point
     * @param lng1        Double -longitude  coordinate of first point
     * @param lat2        Double -latitude coordinate of second point
     * @param lng2        Double -longitude coordinate of second point
     * @param maxDistance Double - the maximum distance allowed between the two points IN METERS
     * @return true if distance shorter than uniqueness threshold, else false if 2 separate instances
     * @sources <pre>
     * <ul>
     * <li><a href="https://www.trekview.org/blog/2021/reading-decimal-gps-coordinates-like-a-computer/">How to read lat/long</a></li>
     * <li><a href="https://en.wikipedia.org/wiki/Haversine_formula">How to calculate distance between to locations on earth using lat/long</a></li>
     * <li><a href="https://linuxhint.com/import-math-in-java/">How use Math library</a></li>
     * <li><a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Math.html">cos, sin, arcsin</a></li>
     * <li><a href="https://www.movable-type.co.uk/scripts/latlong.html"> Verified test cases w/ this calculator</a></li>
     * </ul>
     * </pre>
     */

    public static boolean isSameLocation(QRCode qr, QRCode dbqr, double maxDistance) {
        //TODO INPUT VALIDATION:
        // some coordinates shouldn't make sense, iirc long can't have larger magnitude than +-180?
        // and +-90 for lat?

        final double RADIUS = 6371.0;     // earth's radius in kilometers
        // input validation
        // hash's are same, no location data for either, treat as same QRCode object
        if( (qr.getLatitude() == null) && (qr.getLongitude()==null) && (dbqr.getLatitude()==null) && (dbqr.getLongitude()==null) ) {
            return true;
        // at least one of the qr's is null but not both, treat as separate objects
        } else if ( (qr.getLatitude() == null) || (qr.getLongitude()==null) || (dbqr.getLatitude()==null) || (dbqr.getLongitude()==null) ) {
            return false;
        }

        final double radius = 6371.0;     // earth's radius in kilometers

        double lat1 = qr.getLatitude();
        double lng1 = qr.getLongitude();
        double lat2 = dbqr.getLatitude();
        double lng2 = dbqr.getLongitude();
        System.out.printf("lat1 %.20f\n", lat1);
        System.out.printf("lng2 %.20f\n", lng1);
        System.out.printf("lat2 %.20f\n", lat2);
        System.out.printf("lng2 %.20f\n", lng2);


        //COORDINATES HARDCODED FOR TESTING
        //double maxDistance = 30;    // in meters
        //double lat1 = 38.8977;
        //double lng1 = -77.0365;

        // latitude & longitude of second QRCode
        //double lat2 = 48.8584;
        //double lng2 = 2.2945;

        // convert degrees to radians
        // phi = latitude, lambda = longitude
        double phi1 = (lat1 * Math.PI) / 180.0;
        double lambda1 = (lng1 * Math.PI) / 180.0;

        double phi2 = (lat2 * Math.PI) / 180.0;
        double lambda2 = (lng2 * Math.PI) / 180.0;

        // Calculate haversine(theta), the central angle between both locations relative to earth's center
        // Haversine(theta) = sin^2((phi2-phi1)/2)+cos(phi1)cos(phi2)sin^2((lambda2-lambda1)/2)
        double haversine = (Math.pow(Math.sin((phi2 - phi1) / 2), 2) + Math.cos(phi1) * Math.cos(phi2) * (Math.pow(Math.sin((lambda2 - lambda1) / 2), 2)));

        // Calculate distance between both points using haversine
        // Distance = 2r*arcsin(sqr(haversine(theta)))
        double distance = (2 * RADIUS) * (Math.asin(Math.sqrt(haversine)));

        //System.out.printf("%f\n", haversine);
        System.out.printf("%.20f\n", distance);

        //convert distance to meters and compare with maxDistance
        distance *= 1000;
        System.out.printf("distance in meters: %.20f\n", distance);

        if (distance <= maxDistance) {
            System.out.printf("Same\n");
            return true;
        } else {
            System.out.printf("Different\n");
            return false;
        }
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
        scanCode(); // Start scanning a QR code
    }

    /**
     * Called when fragment is being initialized. Creates a dialog that displays the score of the scanned QR code. The dialog disappears automatically
     * after a few seconds.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     * @sources <pre>
     * <ul>
     * <li><a href="https://www.youtube.com/watch?v=W4qqTcxqq48">how to create a custom dialog</a></li>
     * <li><a href="https://xjaphx.wordpress.com/2011/07/13/auto-close-dialog-after-a-specific-time/">how to have dialog automatically close after a few seconds</a></li>
     * <li><a href="https://stackoverflow.com/a/54166609/14445107">How to remove dim from dialog</a></li>
     * </ul>
     * </pre>
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        currentUserDisplayName = prefs.getString("currentUserDisplayName", null);
        currentUserUsername = prefs.getString("currentUserUsername", null);

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
     * @sources <a href="https://www.youtube.com/watch?v=jtT60yFPelI">how to configure the QR camera scanner and obtain the QR contents from the CaptureAct</a>
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
     * @sources <pre>
     * <ul>
     * <li><a href="https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android">how to create an AlertDialog</a></li>
     * <li><a href="https://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare">updating UI elements from within a thread using runOnUiThread()</a></li>
     * <li><a href="https://stackoverflow.com/a/19064968/14445107">how to prevent users from touching outside a dialog box to escape it</a></li>
     * </ul>
     * </pre>
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
            fusedLocationClient
                    .getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            // Location data is available
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            Log.d(locationPrompt, "Latitude: " + latitude + ", Longitude: " + longitude);

                            // Set longitude and latitude and store
                            qrCode.setLatitude(latitude);
                            qrCode.setLongitude(longitude);
                            qrCode.setID(latitude, longitude);
                        } else {
                            // Location data is not available
                            Log.d(locationPrompt, "ERROR Location data is not available.");
                            // Stores QRCode into db with just hash as document id and location = null
                        }
                        addQRCode();
                        returnToProfile();
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
     * Helper function that starts the TakePhotoActivity if the user accepts the photo prompt.
     *
     * @sources <a href="https://stackoverflow.com/questions/28619113/start-a-new-activity-from-fragment">how to start an activity from within a fragment</a>
     */
    private void takePhoto() {
        Intent intent = new Intent(getActivity(), TakePhotoActivity.class);
        photoLauncher.launch(intent);
    }

    /**
     * Prompts the user as to whether they would like to share their geolocation for a QR code. If they click "yes", the QR code will be created with location, and
     * if they press "no" without location.
     *
     * @sources <pre>
     * <ul>
     * <li><a href="https://www.youtube.com/watch?v=DfDj9EadOLk">how to use activityresultlauncher to execute code after an activity closes</a></li>
     * <li><a href="https://stackoverflow.com/a/63883427/14445107">where to initialize an activityresultlauncher</a></li>
     * </ul>
     * </pre>
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
     * @sources <a href="https://stackoverflow.com/a/60055145/14445107">using getParentFragmentManager() instead of getFragmentManager()</a>
     */
    private void returnToProfile() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() { // Wait 500ms before returning to profile; if app returns to quickly the addition will not be registered in Firestore yet (the RecyclerView will update too early)
                FragmentTransaction trans = getParentFragmentManager().beginTransaction();
                trans.replace(R.id.main_screen, new ProfileFragment(db, currentUserDisplayName, currentUserUsername));
                trans.commit();
            }
        }, 500);
    }

    /**
     * Helper function to check if a QR code document exists
     *
     * @param docToCheck document that should be checked for
     * @param cr         CollectionReference to the collection being accessed
     * @sources <a href="https://firebase.google.com/docs/firestore/query-data/get-data">used without major modification</a>
     */
    public void checkDocExists(@NonNull String docToCheck, @NonNull CollectionReference cr, final @NonNull QueryCallback docExists) {
        DocumentReference docRef = cr.document(docToCheck);
        docRef
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {
                        Log.d("DocExist", "DocumentSnapshot data: " + doc.getData());
                        docExists.queryCompleteCheck(true);
                    } else {
                        Log.d("DocExist", "No such document");
                        docExists.queryCompleteCheck(false);
                    }
                });
    }

    /**
     *
     * @param qr - qr code to find matches of in db
     * @param cr - collection reference where to search for matches
     * @param queryCompleteCheck
     * @reference https://firebase.google.com/docs/firestore/query-data/queries#java_6
     */

    public void checkQRCodeExists(@NonNull QRCode qr, @NonNull CollectionReference cr, final @NonNull QueryCallback queryCompleteCheck) {
        String hashValue = qr.getHash();
        double maxRadius = 30; //max distance from a qrlocation in meters
        cr.whereEqualTo("hash", hashValue).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    boolean isSame = false;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("QRExist", document.getId() + " => " + document.getData());
                        QRCode dbqr = document.toObject(QRCode.class);      // rebuilds a QRCode object from db information
                        isSame = isSameLocation(qr, dbqr, maxRadius);
                        if(isSame == true) {                             // locations within threshold, treat as same qr, break from loop
                            qrCodeID = dbqr.getID();
                            //queryCompleteCheck.queryCompleteCheck(true);
                            Log.d("QRExist", "locations close enough, count as equal object");
                            break;
                        }
                        Log.d("QRExists", "location distance too far, not a match");
                    }
                    queryCompleteCheck.queryCompleteCheck(isSame);       // no matches in db withing distance threshold
                    if(isSame == false) {
                        Log.d("QRExists", "no matches within distance, create a new object");
                    }
                }
                else {
                    Log.d("QRExist", "Error getting documents: ", task.getException());
                    queryCompleteCheck.queryCompleteCheck(false);
                }
            }
        });
    }

    /**
     * Helper function to add QRCode object to QRCodes and Users collections
     */
    private void addQRCode() {
        String qrCodeID = qrCode.getID();

        Map<String, Object> qrCodeRef = new HashMap<>();
        qrCodeRef.put("Reference", qrCodesReference.document(qrCodeID));

        // Check if qrCode within location threshold already exists in db in QRCodes collection
        checkDocExists(qrCodeID, qrCodesReference, new QueryCallback() {
            public void queryCompleteCheck(boolean qrExists) {

                // Check if reference to qrCode exists in db in Users collection
                checkDocExists(qrCodeID, usersReference.document(currentUserUsername).collection("User QR Codes"), new QueryCallback() {
                    public void queryCompleteCheck(boolean qrRefExists) {

                        // If qrCode does not exist, add it to QRCode collection
                        if (!qrExists) {
                            qrCodesReference.document(qrCodeID).set(qrCode);
                            if (resizedImageUrl != null) {
                                qrCodesReference.document(qrCodeID).update("photoList", FieldValue.arrayUnion(resizedImageUrl));
                                //QRCodesReference.document(QRCodeId).update("photoList", FieldValue.arrayRemove(resizedImageUrl));
                            }
                        }
                        // If user does not already have this qrCode, add a reference to it, increment their total scans and points, add new photo to qrCode
                        if (!qrRefExists) {
                            usersReference.document(currentUserUsername).collection("User QR Codes").document(qrCodeID).set(qrCodeRef);
                            usersReference.document(currentUserUsername).update("totalScans", FieldValue.increment(1));
                            usersReference.document(currentUserUsername).update("totalPoints", FieldValue.increment(qrCode.getPoints()));
                            if (resizedImageUrl != null) {
                                qrCodesReference.document(qrCodeID).update("photoList", FieldValue.arrayUnion(resizedImageUrl));
                                //QRCodesReference.document(QRCodeId).update("photoList", FieldValue.arrayRemove(resizedImageUrl));
                            }

                        }
                        // If user does not have this qrCode but it already exists in qrCode collection, increase its total scans
                        if ((qrExists) && (!qrRefExists)) {
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
     * @sources <a href="https://github.com/DanielMartinus/Konfetti/blob/main/samples/xml-java/src/main/java/nl/dionsegijn/xml/java/MainActivity.java">Used without major modification</a>
     */
    public void createKonfetti() {
        KonfettiView konfettiView;
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
     * @return string containing the url of the resized image, that will be used later when retrieving it for viewing in the QR view.
     * @sources <a href="https://stackoverflow.com/a/18521373/14445107">How to insert a string in the middle of another; used without major modification</a>
     */
    private String getResizeImageUrl(String rawImageUrl) {
        int index = rawImageUrl.indexOf(".jpg");
        String urlFirstHalf = rawImageUrl.substring(0, index);
        String urlSecondHalf = rawImageUrl.substring(index);
        return urlFirstHalf + "_504x416" + urlSecondHalf; //TODO probably shouldn't use a string literal; make a constant or something
    }
}