package com.example.qrhunterapp_t11.fragments;

import static nl.dionsegijn.konfetti.core.Position.Relative;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import com.example.qrhunterapp_t11.interfaces.QueryCallbackWithQRCode;
import com.example.qrhunterapp_t11.objectclasses.Preference;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
 * @author Sarah Thomson - Adding a new qrCode to db and checking if that hash already exists in the db after it is scanned.
 */
public class CameraFragment extends Fragment {
    static final double MAX_RADIUS = 30; // Max distance from a qrlocation in meters
    private static final int permissionsRequestLocation = 100;
    private static final String locationPrompt = "LocationPrompt";
    private final FirebaseFirestore db;
    private final CollectionReference qrCodesReference;
    private final CollectionReference usersReference;
    private final FirebaseQueryAssistant firebaseQueryAssistant;
    private ActivityResultLauncher<ScanOptions> barLauncher;
    private ActivityResultLauncher<Intent> photoLauncher;
    private QRCode qrCode;
    private String imageUrl = null;
    private String resizedImageUrl;
    private String currentUserDisplayName;
    private String currentUserUsername;
    private QRCode savedQR = null;


    public CameraFragment(@NonNull FirebaseFirestore db) {
        this.db = db;
        this.firebaseQueryAssistant = new FirebaseQueryAssistant(db);
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
        scanCode(); // Start scanning a QR code
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

                            // Set longitude and latitude, regional data, and store
                            Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
                            List<Address> addresses;
                            try {
                                // Get more data about the QR Code's location based on latitude and longitude
                                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            qrCode.setID(latitude, longitude);
                            qrCode.setLatitude(latitude);
                            qrCode.setLongitude(longitude);

                            if (!addresses.isEmpty()) {
                                qrCode.setCountry(addresses.get(0).getCountryName());
                                qrCode.setAdminArea(addresses.get(0).getAdminArea());
                                qrCode.setSubAdminArea(addresses.get(0).getSubAdminArea());
                                qrCode.setLocality(addresses.get(0).getLocality());
                                qrCode.setSubLocality(addresses.get(0).getSubLocality());

                                String postalCode = addresses.get(0).getPostalCode();
                                qrCode.setPostalCode(postalCode);
                                // Convert code to prefix (For most countries this is just the first three digits)
                                qrCode.setPostalCodePrefix(postalCode.substring(0, 3));
                            }
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
                    // Stores QRCode in db with hash as document id and location = null
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
                        // Stores QRCode in db with hash as document id and location = null
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
                trans.replace(R.id.main_screen, new ProfileFragment(db, currentUserUsername, currentUserDisplayName));
                trans.commit();
            }
        }, 500);
    }

    /**
     * Called when fragment is being initialized. Creates a dialog that displays the score of the scanned QR code. The dialog disappears automatically after a few seconds.
     * Or, if the user already has a QR code with the same hash, give user a choice to update its location.
     * Location will only be updated if it is originally null, or if the newly scanned QR code is far enough away from the old one to be considered a new object.
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
        //prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        currentUserDisplayName = Preference.getPrefsString(Preference.PREFS_CURRENT_USER_DISPLAY_NAME, null);
        currentUserUsername = Preference.getPrefsString(Preference.PREFS_CURRENT_USER, null);
        resizedImageUrl = null; // for some reason resizedImageUrl appears to persist between scans; if you add a QR with a photo, and then immediately add a QR
        // without a photo, the second QR will re-use the the photo from the first QR code. Clearing resizedImageUrl here appears to fix this.

        photoLauncher = registerForActivityResult( // should be okay to initialize before scanner
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent intent = result.getData();
                        assert intent != null;
                        Bundle extras = intent.getExtras();
                        imageUrl = extras.getString("url");

                        resizedImageUrl = getResizeImageUrl(imageUrl);

                        promptForLocation(); // prompt for location once the TakePhotoActivity has finished
                    }
                }
        );

        barLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // create a builder for the alert dialog
                String resultString = result.getContents(); // access QR code contents

                // Object instantiated
                qrCode = new QRCode(resultString);

                //Check if the user already has a QR Code object with this hash value in their collection
                firebaseQueryAssistant.checkUserHasHash(qrCode, currentUserUsername, new QueryCallbackWithQRCode() {
                    @Override
                    public void queryCompleteCheckObject(boolean hashExists, QRCode qr) {
                        // If user already has this qRCode, alert user that they cannot get the points for the same code again
                        if (hashExists) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("You scanned the same QR twice!");
                            builder.setMessage("No points have been added to your account.\n\n Update the location of this QR?\n");
                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    savedQR = qr;
                                    promptForPhoto();
                                }
                            });
                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    returnToProfile();
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        } else {

                            // If the user does not already have the scanned qRCode in their collection, show points and the ask to take a photo...
                            // Create custom dialog to display QR score
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.qr_scored_dialog, null);
                            TextView scoredTV = dialogView.findViewById(R.id.scoredTV);
                            builder.setView(dialogView);
                            builder.setCancelable(false);
                            String scored = qrCode.getPoints() + " Points";
                            scoredTV.setText(scored);
                            final AlertDialog alertDialog = builder.create();
                            alertDialog.show(); // Create and display the dialog
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                Objects.requireNonNull(alertDialog.getWindow()).setDimAmount(0);
                            }
                            createKonfetti(); // party rock is in the house tonight
                            // *its party rockers in the hous tonihgt
                            // party rock is

                            final Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                public void run() {
                                    alertDialog.dismiss();
                                    timer.cancel();
                                    promptForPhoto(); // prompt the user for a photo of the QR object or location once the score dialog disappears
                                }
                            }, 7000); // set a timer to automatically close the dialog after 7 seconds
                        }
                    }
                });
            }
        });
    }

    /**
     * Helper function to add QRCode object to QRCodes and Users collections
     */
    private void addQRCode() {
        float[] results = new float[1];
        boolean addNewlyScannedQR = true;

        // If a user is updating the location reference of a QR Code they already scanned before
        if (savedQR != null) {

            // If new version is scanned without location do nothing
            if (qrCode.getLatitude() == null) {
                savedQR = null;
                addNewlyScannedQR = false;
                // If the user's new location is the same as the old QR Code's location do nothing
            } else if ((savedQR.getLatitude() != null) && (qrCode.getLatitude() != null)) {

                android.location.Location.distanceBetween(qrCode.getLatitude(), qrCode.getLongitude(), savedQR.getLatitude(), savedQR.getLongitude(), results);
                if (results[0] < MAX_RADIUS) {
                    savedQR = null;
                    addNewlyScannedQR = false;
                }
            }
        }

        // If the user is updating their scanned qrCode's old location
        if ((savedQR != null) && (addNewlyScannedQR)) {
            // Delete the old qrCode reference from the user's collection
            firebaseQueryAssistant.deleteQR(currentUserUsername, savedQR.getID(), new QueryCallback() {
                @Override
                public void queryCompleteCheck(boolean queryComplete) {
                    assert queryComplete;
                }
            });
        }
        // Executes if the newly scanned QR Code should be added to the database
        if (addNewlyScannedQR) {
            firebaseQueryAssistant.addQR(currentUserUsername, qrCode, resizedImageUrl, MAX_RADIUS);
        }
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