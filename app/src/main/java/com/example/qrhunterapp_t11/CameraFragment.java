package com.example.qrhunterapp_t11;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Logic for the camera fragment, which is responsible for managing everything that pertains to scanning and adding a new QR code.
 * Calls all the necessary activities for achieving this (the scan QR and take photo activities).
 *
 * @author Aidan Lynch - methods related to QR scanning, photo-taking and main camera fragment screen logic.
 * @author Daniel Guo - methods related to geolocation and obtaining permissions for location.
 * @author Josh Lucas and Afra - methods for creating a new QR object
 */
public class CameraFragment extends Fragment {
    ActivityResultLauncher<ScanOptions> barLauncher;
    ActivityResultLauncher<Intent> photoLauncher;
    QRCode qrCode;
    SharedPreferences prefs;

    //https://firebase.google.com/docs/firestore/manage-data/add-data //TODO put this in a javadoc somewhere as an @reference?
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference QRCodesReference = db.collection("QRCodes");
    CollectionReference usersReference = db.collection("Users");


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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
     * @reference Pro Grammer - https://www.youtube.com/watch?v=W4qqTcxqq48 - how to create a custom dialog
     * @reference Pete Houston - https://xjaphx.wordpress.com/2011/07/13/auto-close-dialog-after-a-specific-time/ - how to have dialog automatically close after a few seconds
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
                        promptForLocation(); // prompt for location once the TakePhotoActivity has finished
                    }
                }
        );

        barLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // create a builder for the alert dialog
                String resultString = result.getContents(); // how to access QR code contents; score dialog shows placeholder value for now
                // resultString is a string of whatever was in the QR code; supply it as an argument into your hash function/constructor

                // object instantiated
                qrCode = new QRCode(resultString);

                // create custom dialog to display QR score
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.qr_scored_dialog, null);
                TextView scoredTV = dialogView.findViewById(R.id.scoredTV); // NEW ADDITION
                builder.setView(dialogView);
                builder.setCancelable(false);
                scoredTV.setText("Scored " + String.valueOf(qrCode.getPoints()) + " Points"); // NEW ADDITION

                final AlertDialog alertDialog = builder.create();
                alertDialog.show(); // create and display the dialog

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
     * @reference Cambo Tutorial - https://www.youtube.com/watch?v=jtT60yFPelI - how to configure the QR camera scanner and obtain the QR contents from the CaptureAct
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
     * @reference David Hedlund - https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android - how to create an AlertDialog
     * @reference EboMike - https://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare - updating UI elements from within a thread using runOnUiThread()
     * @reference Rick - https://stackoverflow.com/a/19064968/14445107 - how to prevent users from touching outside a dialog box to escape it
     */
    private void promptForPhoto() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(getContext())
                        .setTitle("Take Photo")
                        .setMessage("Take photo of object or location?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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
     * @reference Paul Thompson - https://stackoverflow.com/questions/28619113/start-a-new-activity-from-fragment - how to start an activity from within a fragment
     */
    private void takePhoto() {
        Intent intent = new Intent(getActivity(), TakePhotoActivity.class);

        photoLauncher.launch(intent);
    }



    private static final int PERMISSIONS_REQUEST_LOCATION = 100; //TODO move to top of class for cleanliness?
    private GoogleApiClient googleApiClient; //TODO move to top of class for cleanliness?

    /**
     *Connects the GoogleApiClient and initiates the permissions check
     */
    private void connectGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(requireContext())
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
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                    }
                })
                .build();
        googleApiClient.connect();
    }

    /**
     *Retrieves the current location and logs the latitude and longitude of the location.
     *Adds QRCode to db with location and returns to profile
     */
    private void getCurrentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // Location data is available
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Log.d("LocationPrompt", "Latitude: " + latitude + ", Longitude: " + longitude);
                        //stores QRCode into db with just hash as document id and location = null
                        addQRCode();
                        returnToProfile();
                    } else {
                        // Location data is not available
                        Log.d("LocationPrompt", "ERROR Location data is not available.");
                        //stores QRCode into db with just hash as document id and location = null
                        addQRCode();
                        returnToProfile();
                    }
                }
            });
        }
    }

    /**
     *Initiates the location permission check and logs if permission is already granted
     */
    private void permissions() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            Log.d("LocationPrompt", "PERMISSION ALREADY GAVE.");
            getCurrentLocation();
        } else {
            // Permission is not granted
            // Ask for the permission, calls onRequestPermissionsResult after selection
            Log.d("LocationPrompt", "ASKING FOR PERMISSION.");
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
    }

    /**
     *Handles the user's response to the location permission request.
     *Calls getCurrentLocation() if permission is granted, otherwise adds QRCode to db with location=null and returns to profile.
     *
     *@param requestCode The request code of the permission request.
     *@param permissions The requested permissions.
     *@param grantResults The results of the permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                Log.d("LocationPrompt", "Execute if permission granted.");
                getCurrentLocation();
            } else {
                // Permission is not granted
                Log.d("LocationPrompt", "Execute if permission not granted.");
                //stores QRCode into db with just hash as document id and location = null
                addQRCode();
                returnToProfile();
            }
        }
    }

    /**
     * Prompts the user as to whether they would like to share their geolocation for a QR code. If they click "yes", the QR code will be created with location, and
     * if they press "no" without location.
     *
     * @reference Daily Coding - https://www.youtube.com/watch?v=DfDj9EadOLk - how to use activityresultlauncher to execute code after an activity closes
     * @reference Oleksandra - https://stackoverflow.com/a/63883427/14445107 - where to initialize an activityresultlauncher
     */
    private void promptForLocation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Share Geolocation")
                .setMessage("Let others find this location on the map?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("LocationPrompt", "User accepted geolocation prompt.");
                        permissions();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("LocationPrompt", "User rejected geolocation prompt.");
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
     * @reference Yoju - https://stackoverflow.com/a/60055145/14445107 - using getParentFragmentManager() instead of getFragmentManager()
     */
    private void returnToProfile() {
        FragmentTransaction trans = getParentFragmentManager().beginTransaction();
        trans.replace(R.id.main_screen, new ProfileFragment(db));
        trans.commit();
    }

    /**
     * Helper function to add QRCode object to QRCodes and Users collections
     */
    private void addQRCode() {
        String currentUser = prefs.getString("currentUser", null);
        String id = qrCode.getHash();

        QRCodesReference.document(id).set(qrCode);
        usersReference.document(currentUser).collection("QR Codes").document(id).set(qrCode);
    }
}