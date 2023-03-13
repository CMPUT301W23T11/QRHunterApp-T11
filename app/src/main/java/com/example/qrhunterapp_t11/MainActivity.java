package com.example.qrhunterapp_t11;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Main app activity. Default startup screen is the player profile.
 * Users can click on the toolbar at the bottom to switch to other parts of the app.
 *
 * @author Afra, Kristina
 * @reference <a href="https://www.geeksforgeeks.org/how-to-create-fragment-using-bottom-navigation-in-social-media-android-app/">How to use fragments with a bottom navigation bar</a>
 * @reference <a href="https://youtu.be/x6-_va1R788">How to set up and align a floating action button on the BottomNavigationView</a>
 * @reference <a href="https://firebase.google.com/docs/firestore/query-data/aggregation-queries#java">For aggregation queries</a>
 */
public class MainActivity extends AppCompatActivity implements ViewQR.ViewQRDialogListener {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private final ProfileFragment profileFragment = new ProfileFragment(db);
    private final SettingsFragment settingsFragment = new SettingsFragment(db);
    private final CameraFragment cameraFragment = new CameraFragment(db);
    private final MapFragment mapFragment = new MapFragment();
    private BottomNavigationView bottomToolbar;
    private int numUsers;

    @Override
    public void ViewCode(@NonNull QRCode qrCode) {
    }

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomToolbar = findViewById(R.id.bottomToolbar);
        FloatingActionButton addFab = findViewById(R.id.addFab);

        // sets the toolbar to be on profile item.
        bottomToolbar.setSelectedItemId(R.id.profile);
        SharedPreferences prefs = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        // If the user is logging in for the first time, create a new user
        if (!prefs.getBoolean("LoggedIn", false)) {
            firstTimeLaunch(new MainActivityCallback() {
                public void setNumUsers(int numUsers) {

                    String username = "user" + (numUsers + 1);

                    prefs.edit().putString("currentUser", username).commit();
                    prefs.edit().putString("currentUserDisplayName", username).commit();
                    prefs.edit().putBoolean("LoggedIn", true).commit();

                    Map<String, Object> user = new HashMap<>();
                    user.put("Username", username);
                    user.put("Display Name", username);

                    usersReference.document(username).set(user);
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, profileFragment).commit();
                }
            });
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, profileFragment).commit();
        }

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

    /**
     * Counts number of users in database to determine with what name to initialize a new user
     *
     * @param setNumUsers Callback that will set numUsers to the number of users in the database
     */
    public void firstTimeLaunch(final @NonNull MainActivityCallback setNumUsers) {
        AggregateQuery countQuery = usersReference.count();
        countQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                if (task.isSuccessful()) {
                    AggregateQuerySnapshot snapshot = task.getResult();
                    numUsers = (int) snapshot.getCount();
                    setNumUsers.setNumUsers(numUsers);
                }
            }
        });
    }

    /**
     * Callback for querying database
     *
     * @author Afra
     */
    public interface MainActivityCallback {
        void setNumUsers(int numUsers);
    }
}