package com.example.qrhunterapp_t11;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Logic for the camera fragment, which is responsible for managing everything that pertains to scanning and adding a new QR code.
 * Calls all the necessary activities for achieving this (the scan QR and take photo activities).
 *
 * @author Aidan Lynch
 */
public class CameraFragment extends Fragment {
    ActivityResultLauncher<ScanOptions> barLauncher;
    ActivityResultLauncher<Intent> photoLauncher;
    QRCode qrCode;

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

    // https://stackoverflow.com/questions/70211364/attempting-to-launch-an-unregistered-activityresultlauncher

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
                //TODO JOSH SARAH - resultString is a string of whatever was in the QR code; supply it as an argument into your hash function/constructor
                // resultString will probably need to be turned into a class attribute so it can be accessed later in runtime (once the location is obtained),
                // since we don't have that at this point in time

                // NEW ADDITIONS
                String hash = null;
                try {
                    hash = QRCode.strToHash(resultString);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                qrCode = new QRCode(hash);

                // create custom dialog to display QR score
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.qr_scored_dialog, null);
                TextView scoredTV = dialogView.findViewById(R.id.scoredTV); // NEW ADDITION
                builder.setView(dialogView);
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
     */
    private void promptForPhoto() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(getContext())
                        .setTitle("Take Photo")
                        .setMessage("Take photo of object or location?")
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
        //startActivity(intent);
    }

    /**
     * Creates a dialog asking whether the user would like to share their geolocation.
     *
     * @reference Daily Coding - https://www.youtube.com/watch?v=DfDj9EadOLk - how to use activityresultlauncher to execute code after an activity closes
     * @reference Oleksandra - https://stackoverflow.com/a/63883427/14445107 - where to initialize an activityresultlauncher
     */
    private boolean mLocationPermissionGranted = false;
    public static final int permissionsRequestAccessFineLocation = 9003;
    public boolean isLocationEnabled() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, permissionsRequestAccessFineLocation);
            return false;
        } else {
            return true;
        }
    }
    private void promptForLocation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Share Geolocation")
                .setMessage("Let others find this location on the map?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("LocationPrompt", "User accepted geolocation prompt.");
                        Toast.makeText(getContext(), "User accepted geolocation prompt", Toast.LENGTH_SHORT).show(); // remove
                        //TODO DANIEL - get current location
                        // SARAH + JOSH - then proceed to create QR with location (probably want to check whether the QR already exists in DB first)
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("LocationPrompt", "User rejected geolocation prompt.");
                        Toast.makeText(getContext(), "User rejected geolocation prompt", Toast.LENGTH_SHORT).show(); // remove
                        //TODO SARAH + JOSH - create QR without location (I assume using null for location)
                    }
                })
                .show();
    }

}