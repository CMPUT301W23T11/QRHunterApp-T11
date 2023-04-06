package com.example.qrhunterapp_t11.activities;

import android.location.Address;
import android.location.Geocoder;
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
import com.example.qrhunterapp_t11.objectclasses.Preference;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.example.qrhunterapp_t11.objectclasses.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Main app activity. Default startup screen is the player profile.
 * Users can click on the toolbar at the bottom to switch to other parts of the app.
 *
 * @author Afra
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
        Preference.init(getApplicationContext());

        // If the user is logging in for the first time, create a new user
        if (!Preference.getPrefsBool("loggedIn", false)) {
            firstTimeLaunch(new MainActivityCallback() {
                public void setNumUsers(int numUsers) {

                    String username = "user" + (numUsers + 1);
                    Preference.setPrefsString(Preference.PREFS_CURRENT_USER, username);
                    Preference.setPrefsString(Preference.PREFS_CURRENT_USER_DISPLAY_NAME, username);
                    Preference.setPrefsBool("loggedIn", true);

                    ArrayList<String> qrCodeIDs = new ArrayList<>();
                    ArrayList<String> qrCodeHashes = new ArrayList<>();
                    ArrayList<String> commentedOn = new ArrayList<>();

                    User user = new User(username, username, 0, 0, 0, "", qrCodeIDs, qrCodeHashes, commentedOn);
                    usersReference.document(username).set(user);
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, new ProfileFragment(db, Preference.getPrefsString(Preference.PREFS_CURRENT_USER, null), Preference.getPrefsString(Preference.PREFS_CURRENT_USER_DISPLAY_NAME, null))).commit();

                }
            });
        } else {

            populateApp();

            getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, new ProfileFragment(db, Preference.getPrefsString(Preference.PREFS_CURRENT_USER, null), Preference.getPrefsString(Preference.PREFS_CURRENT_USER_DISPLAY_NAME, null))).commit();
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
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_screen, new ProfileFragment(db, Preference.getPrefsString(Preference.PREFS_CURRENT_USER, null), Preference.getPrefsString(Preference.PREFS_CURRENT_USER_DISPLAY_NAME, null))).commit();

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
     * Helper function to populate a small cluster of area in the app
     */
    public void populateApp() {

        ArrayList<QRCode> qrCodes = new ArrayList<>();
        int numQRCodesToAddToDB = 10;

        // Set number of QR Codes to add to db
        for (int i = 0; i < numQRCodesToAddToDB; i++) {

            // Set maximum distance away from centre of region for cluster
            int randomClusterBoundsLat = new Random().nextInt(100) - 50;
            int randomClusterBoundsLong = new Random().nextInt(100) - 50;

            // For U of A region
//            double latitude = Double.parseDouble("53.5" + (265 + randomClusterBoundsLat));
//            double longitude = Double.parseDouble("-113.5" + (258 + randomClusterBoundsLong));

            // For Googleplex region
            double latitude = Double.parseDouble("37.4" + (221 + randomClusterBoundsLat));
            double longitude = Double.parseDouble("-122.0" + (841 + randomClusterBoundsLong));

            QRCode qrCode = new QRCode("randomshit" + (randomClusterBoundsLat * i) + "hopefullyrandomenough" + (randomClusterBoundsLong * (i + 1)));

            Geocoder geocoder = new Geocoder(this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Set some of the QR Code's data
            qrCode.setID(latitude, longitude);
            qrCode.setLatitude(latitude);
            qrCode.setLongitude(longitude);
            ArrayList<String> inCollection = new ArrayList<>();
            qrCode.setInCollection(inCollection);

            if (!addresses.isEmpty()) {
                qrCode.setCountry(addresses.get(0).getCountryName());
                qrCode.setAdminArea(addresses.get(0).getAdminArea());
                qrCode.setSubAdminArea(addresses.get(0).getSubAdminArea());
                qrCode.setLocality(addresses.get(0).getLocality());
                qrCode.setSubLocality(addresses.get(0).getSubLocality());

                String postalCode = addresses.get(0).getPostalCode();
                qrCode.setPostalCode(postalCode);
                qrCode.setPostalCodePrefix(postalCode.substring(0, 3));
            }

            // Add QR Code to DB and local array
            db.collection("QRCodes").document(qrCode.getID()).set(qrCode);
            qrCodes.add(qrCode);
        }

        // Set number of users to add, be careful when setting values here
        for (int i = 3; i < 5; i++) {

            // Initialize values for user
            String username = "user" + i;
            String displayName = username;
            int totalPoints = 0;
            int totalScans = 0;
            int topQRCode = 0;
            String email = "";
            ArrayList<String> qrCodeHashes = new ArrayList<>();
            ArrayList<String> qrCodeIDs = new ArrayList<>();
            ArrayList<String> commentedOn = new ArrayList<>();

            // Add a random selection of QR Codes from the array to the user's collection
            int rangeQRCodesForUser = new Random().nextInt(qrCodes.size());
            Collections.shuffle(qrCodes);

            for (int j = 0; j < rangeQRCodesForUser; j++) {

                QRCode currentQRCode = qrCodes.get(j);
                int currentQRCodePoints = currentQRCode.getPoints();

                // Increment user's stats/collection
                totalPoints += currentQRCodePoints;
                totalScans += 1;
                if (currentQRCodePoints > topQRCode) {
                    topQRCode = currentQRCodePoints;
                }
                qrCodeHashes.add(currentQRCode.getHash());
                qrCodeIDs.add(currentQRCode.getID());

                // Update qrCode document's inCollection field, and its numberOfScans field
                db.collection("QRCodes").document(currentQRCode.getID()).update("inCollection", FieldValue.arrayUnion(username));
                db.collection("QRCodes").document(currentQRCode.getID()).update("numberOfScans", FieldValue.increment(1));
            }

            User user = new User(displayName, username, totalPoints, totalScans, topQRCode, email, qrCodeIDs, qrCodeHashes, commentedOn);

            usersReference.document(username).set(user);
        }
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