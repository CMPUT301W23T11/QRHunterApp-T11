package com.example.qrhunterapp_t11;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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
}

//TODO figure out how to have the next dialog box (prompt for photo) automatically appear after the score dialog closes.
//TODO provide proper doc comments once basic structure finished