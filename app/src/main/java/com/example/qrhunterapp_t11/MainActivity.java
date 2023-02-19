package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomToolbar;

    ProfileFragment profileFragment = new ProfileFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    CameraFragment cameraFragment = new CameraFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        bottomToolbar = findViewById(R.id.bottomToolbar);

        // When the app is started, sets the profile fragment to be opened first
        /*
            https://www.geeksforgeeks.org/how-to-create-fragment-using-bottom-navigation-in-social-media-android-app/
            * How to use fragments with a bottom navigation bar
        */
        getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, profileFragment).commit();

        // Changes the fragment based on which item is clicked on the toolbar
        bottomToolbar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.profile) { // TODO convert to switch statement?
                getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, profileFragment).commit();
            }
            else if (item.getItemId() == R.id.settings) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, settingsFragment).commit();
            }
            else if (item.getItemId() == R.id.camera) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, cameraFragment).commit();
            }
            /*
             * add cases for map, search after creating their fragments
            */
            return false;
        });


    }
}