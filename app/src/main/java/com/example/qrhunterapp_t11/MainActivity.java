package com.example.qrhunterapp_t11;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    BottomNavigationView bottomToolbar;
    ProfileFragment profileFragment = new ProfileFragment(db);
    SettingsFragment settingsFragment = new SettingsFragment(db);
    CameraFragment cameraFragment = new CameraFragment();
    LoginFragment loginFragment = new LoginFragment(db);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        // When the app is started, sets the profile fragment to be opened first if user is logged in
        // Otherwise it starts with the login fragment
        /*
            https://www.geeksforgeeks.org/how-to-create-fragment-using-bottom-navigation-in-social-media-android-app/
            * How to use fragments with a bottom navigation bar
        */
        if (prefs.getBoolean("notLoggedIn", true)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, loginFragment).commit();
        }
        else {
            bottomToolbar = findViewById(R.id.bottomToolbar);
            bottomToolbar.setSelectedItemId(R.id.profile);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, profileFragment).commit();

            // Changes the fragment based on which item is clicked on the toolbar
            bottomToolbar.setOnItemSelectedListener(item -> {
                switch (item.getItemId()) {
                    case R.id.profile:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, profileFragment).commit();

                        return true;
                    case R.id.settings:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, settingsFragment).commit();

                        return true;
                    case R.id.camera:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, cameraFragment).commit();

                        return true;

                    case R.id.map:
                        MapFragment mapFragment = new MapFragment();

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
}