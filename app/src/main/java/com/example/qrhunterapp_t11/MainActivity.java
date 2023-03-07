package com.example.qrhunterapp_t11;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    BottomNavigationView bottomToolbar;
    FloatingActionButton addFab;
    ProfileFragment profileFragment = new ProfileFragment(db);
    SettingsFragment settingsFragment = new SettingsFragment(db);
    CameraFragment cameraFragment = new CameraFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
            https://www.geeksforgeeks.org/how-to-create-fragment-using-bottom-navigation-in-social-media-android-app/
            - How to use fragments with a bottom navigation bar
        */

        bottomToolbar = findViewById(R.id.bottomToolbar);
        addFab = findViewById(R.id.addFab);
        bottomToolbar.setSelectedItemId(R.id.profile);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, profileFragment).commit();


        /*
            https://youtu.be/x6-_va1R788
            - how to set up and align a floating action button on the BottomNavigationView
         */
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomToolbar.setSelectedItemId(R.id.camera_placeholder);
                getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, cameraFragment).commit();

            }
        });


        // Changes the fragment based on which item is clicked on the toolbar
        bottomToolbar.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.profile:
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, profileFragment).commit();

                    return true;

                case R.id.camera_placeholder:
                    return true;

                case R.id.settings:
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, settingsFragment).commit();

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