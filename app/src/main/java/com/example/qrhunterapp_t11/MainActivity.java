package com.example.qrhunterapp_t11;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Main app activity. Logged in users will see their player profile screen first, and
 * can click on the toolbar at the bottom to switch to other parts of the app.
 *
 * @author Afra, Josh, Kristina
 * @reference <a href="https://www.geeksforgeeks.org/how-to-create-fragment-using-bottom-navigation-in-social-media-android-app/">How to use fragments with a bottom navigation bar</a>
 * @reference <a href="https://youtu.be/x6-_va1R788">How to set up and align a floating action button on the BottomNavigationView</a>
 */
public class MainActivity extends AppCompatActivity implements ViewQR.ViewQRDialogListener{

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private BottomNavigationView bottomToolbar;
    private final ProfileFragment profileFragment = new ProfileFragment(db);
    private final SettingsFragment settingsFragment = new SettingsFragment();
    private final CameraFragment cameraFragment = new CameraFragment();
    private final MapFragment mapFragment = new MapFragment();
    @Override
    public void ViewCode(QRCode qrCode) {}

    /**
    * Called after the activity launches and sets the activity content to the provided layout resource
     * initializes the bottomNavigationView and the floatingActionButton
     * @param savedInstanceState If the activity is being re-initialized after
     *      *                           previously being shut down then this Bundle contains the data it most
     *      *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomToolbar = findViewById(R.id.bottomToolbar);
        FloatingActionButton addFab = findViewById(R.id.addFab);

        // sets the toolbar to be on profile item.
        bottomToolbar.setSelectedItemId(R.id.profile);
        // sets the profile page to be the first screen displayed after the main screen opens
        getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, profileFragment).commit();

        // floating action button that moves the fragment to the camera fragment
        addFab.setOnClickListener(view -> {
            // sets the toolbar back to profile item
            bottomToolbar.setSelectedItemId(R.id.profile);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, cameraFragment).commit();

        });

        // Changes the fragment based on which item is clicked on the toolbar
        bottomToolbar.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.profile: // changes the main screen to the profile
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, profileFragment).commit();

                    return true;

                case R.id.camera_placeholder:
                    // when add button clicked, the selected item in the toolbar will be set back to the profile
                    bottomToolbar.setSelectedItemId(R.id.profile);
                    return true;

                case R.id.settings: // changes the main screen to settings
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, settingsFragment).commit();

                    return true;

                case R.id.map: // changes the main screen to the map
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, mapFragment).commit();

                    return true;

                // use 'case R.id.search:' for search/leaderboard fragment

            }
            /*
             - add cases for search after creating their fragments
            */

            return false;
        });
    }
}