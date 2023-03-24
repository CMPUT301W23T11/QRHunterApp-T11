package com.example.qrhunterapp_t11.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.qrhunterapp_t11.R;
import com.example.qrhunterapp_t11.fragments.CameraFragment;
import com.example.qrhunterapp_t11.fragments.MapFragment;
import com.example.qrhunterapp_t11.fragments.ProfileFragment;
import com.example.qrhunterapp_t11.fragments.SearchFragment;
import com.example.qrhunterapp_t11.fragments.SettingsFragment;
import com.example.qrhunterapp_t11.objectclasses.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Main app activity. Default startup screen is the player profile.
 * Users can click on the toolbar at the bottom to switch to other parts of the app.
 *
 * @author Afra Rahmanfard
 * @author Kristina
 * @sources: <pre>
 * <ul>
 * <li> <a href="https://www.geeksforgeeks.org/how-to-create-fragment-using-bottom-navigation-in-social-media-android-app/">How to use fragments with a bottom navigation bar</a></li>
 * <li> <a href="https://youtu.be/x6-_va1R788">How to set up and align a floating action button on the BottomNavigationView</a></li>
 * <li> <a href="https://firebase.google.com/docs/firestore/query-data/aggregation-queries#java">For aggregation queries</a></li>
 * </ul>
 * </pre>
 */
public class MainActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private final SettingsFragment settingsFragment = new SettingsFragment(db);
    private final CameraFragment cameraFragment = new CameraFragment(db);
    private final MapFragment mapFragment = new MapFragment(db);
    private final SearchFragment searchFragment = new SearchFragment(db);
    private int numUsers;
    private static final String PREFS_CURRENT_USER = "currentUserUsername";
    private static final String PREFS_CURRENT_USER_DISPLAY_NAME = "currentUserDisplayName";
    private SharedPreferences prefs;

    /**
     * Called after the activity launches and sets the activity content to the provided layout resource
     * initializes the bottomNavigationView and the floatingActionButton
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        // If the user is logging in for the first time, create a new user
        if (!prefs.getBoolean("loggedIn", false)) {
            firstTimeLaunch(new MainActivityCallback() {
                public void setNumUsers(int numUsers) {

                    String username = "user" + (numUsers + 1);

                    prefs.edit().putString(PREFS_CURRENT_USER, username).commit();
                    prefs.edit().putString(PREFS_CURRENT_USER_DISPLAY_NAME, username).commit();
                    prefs.edit().putBoolean("loggedIn", true).commit();

                    User user = new User(username, username, 0, 0, 0, "No email");

                    usersReference.document(username).set(user);
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, new ProfileFragment(db, prefs.getString(PREFS_CURRENT_USER_DISPLAY_NAME, null), prefs.getString(PREFS_CURRENT_USER, null))).commit();

                }
            });
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, new ProfileFragment(db, prefs.getString(PREFS_CURRENT_USER_DISPLAY_NAME, null), prefs.getString(PREFS_CURRENT_USER, null))).commit();

        }

        BottomNavigationView bottomToolbar = findViewById(R.id.bottomToolbar);
        FloatingActionButton addFab = findViewById(R.id.addFab);

        // Sets the toolbar to be on profile item
        bottomToolbar.setSelectedItemId(R.id.profile);

        // Floating action button that moves the fragment to the camera fragment
        addFab.setOnClickListener(view -> {
            // Sets the toolbar back to profile item
            bottomToolbar.setSelectedItemId(R.id.profile);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, cameraFragment).commit();

        });

        // Changes the fragment based on which item is clicked on the toolbar
        bottomToolbar.setOnItemSelectedListener(item -> {
            switch (item.getTitle().toString()) {
                case "Profile": // Changes the main screen to the profile
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, new ProfileFragment(db, prefs.getString(PREFS_CURRENT_USER_DISPLAY_NAME, null), prefs.getString(PREFS_CURRENT_USER, null))).commit();

                    return true;

                case "Settings": // Changes the main screen to settings
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, settingsFragment).commit();

                    return true;

                case "Map": // Changes the main screen to the map
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, mapFragment).commit();

                    return true;

                case "Search": // Changes the main screen to search
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, searchFragment).commit();

                    return true;

            }
            /*
             - add cases for search after creating their fragments
            */

            return false;
        });
    }

    /**
     * Counts number of users in database to determine with what name to initialize a new user
     *
     * @param setNumUsers Callback that will set numUsers to the number of users in the database
     */
    public void firstTimeLaunch(final @NonNull MainActivityCallback setNumUsers) {
        usersReference
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> {
                    numUsers = (int) snapshot.getCount();
                    setNumUsers.setNumUsers(numUsers);
                });
    }

    /**
     * Callback for querying database
     *
     * @author Afra Rahmanfard
     */
    public interface MainActivityCallback {
        void setNumUsers(int numUsers);
    }
}