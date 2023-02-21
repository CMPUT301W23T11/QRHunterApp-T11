package com.example.qrhunterapp_t11;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Timer;
import java.util.TimerTask;

public class CameraFragment extends Fragment {
    ActivityResultLauncher<ScanOptions> barLauncher;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scanCode(); // does placement matter? probably not
    }

    // https://stackoverflow.com/questions/70211364/attempting-to-launch-an-unregistered-activityresultlauncher
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        barLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // create a builder for the alert dialog
                //String resultString = result.getContents(); // how to access QR code contents; score dialog shows placeholder value for now
                //TODO compute hash and create new instance of QR code in database if it's unique (ie. hook into whatever josh and sarah come up with for that)

                //https://www.youtube.com/watch?v=W4qqTcxqq48
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.qr_scored_dialog, null);
                builder.setView(dialogView); // inflate the custom layout and set the dialog's view

                final AlertDialog alertDialog = builder.create();
                alertDialog.show(); // create and display the dialog

                // https://xjaphx.wordpress.com/2011/07/13/auto-close-dialog-after-a-specific-time/
                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    public void run() {
                        alertDialog.dismiss();
                        timer.cancel();
                        promptForPhoto();
                    }
                }, 5000); // set a timer to automatically close the dialog after 5 seconds
            }
        });
    }
    // https://www.youtube.com/watch?v=jtT60yFPelI
    private void scanCode() { // set options for scanning, and start scanning for QR code
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up for flash");
        options.setOrientationLocked(true);
        options.setBeepEnabled(false);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    // https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
    // https://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare
    private void promptForPhoto() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                //Toast.makeText(getContext(), "Prompt for photo", Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(getContext())
                        .setTitle("Take Photo")
                        .setMessage("Take photo of object or location?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d("PhotoPrompt", "User accepted photo prompt.");
                                takePhoto();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("PhotoPrompt", "User rejected photo prompt.");
                            }
                        })
                        .show();
            }
        });
    }

    private void takePhoto() {
        Log.d("PhotoPrompt", "User accepted photo prompt.");
        Intent intent = new Intent(getActivity(), TakePhotoActivity.class); // https://stackoverflow.com/questions/28619113/start-a-new-activity-from-fragment
        startActivity(intent);
    }
}

//TODO provide proper doc comments once basic structure finished