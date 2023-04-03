package com.example.qrhunterapp_t11;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import android.app.Activity;
import android.content.Intent;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.qrhunterapp_t11.activities.MainActivity;
import com.example.qrhunterapp_t11.objectclasses.Preference;
import com.example.qrhunterapp_t11.objectclasses.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

/**
 * Test class for SearchFragment; leaderboard and clicking on user's profiles.
 * <p>
 * author: Aidan Lynch, adapted parts of afra's SettingFragmentTest
 */
public class SearchFragmentTest {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private final CollectionReference qrCodesReference = db.collection("QRCodes");
    private final Random rand = new Random();
    private final String testUsername = "testUser" + rand.nextInt(1000000);
    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<MainActivity>(MainActivity.class, true, true) {
        // Set SharedPreferences to initialize a new user before the activity is launched
        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();
            String username;
            String displayName;

            Preference.init(getApplicationContext());
            Preference.clearPrefs();
            Preference.setPrefsBool("loggedIn", true);
            Preference.setPrefsString("currentUserUsername", testUsername);
            Preference.setPrefsString("currentUserDisplayName", testUsername);

            username = Preference.getPrefsString("currentUserUsername", null);
            displayName = Preference.getPrefsString("currentUserDisplayName", null);

            assertEquals(testUsername, username);
            assertEquals(testUsername, displayName);
        }
    };
    private Solo solo;

    /**
     * Runs before all tests and creates solo instance.
     *
     * @throws Exception
     */

    @Before
    public final void setUp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        rule.launchActivity(intent);
        Activity activity = rule.getActivity();

        ArrayList<String> qrCodeIDs = new ArrayList<>();
        ArrayList<String> qrCodeHashes = new ArrayList<>();
        ArrayList<String> commentedOn = new ArrayList<>();
        User user = new User(testUsername, testUsername, 0, 0, 0, "", qrCodeIDs, qrCodeHashes, commentedOn);
        usersReference.document(testUsername).set(user);
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), activity);
    }

    /**
     * Check we can switch between filters on the leaderboard.
     */
    @Test
    public void flourish() {
        solo.clickOnView(solo.getView(R.id.search));
        assertTrue(solo.waitForText("Leaderboard", 1, 5000)); // confirm we're on leaderboard page
        solo.clickOnText("Most Points"); // click on spinner
        solo.clickOnText("Most Scans"); // select option in spinner
        assertTrue(solo.waitForText("Scans", 1, 5000)); // confirm we've switched to "Most Scans"
        solo.clickOnText("Most Scans"); // click on spinner
        solo.clickOnText("Top QR Code", 2, true);
        assertTrue(solo.waitForText("Top Code", 1, 5000)); // confirm we've switched to "Top QR Code"
        // clicking on "Top QR Code (Regional) is bugged
    }

    /**
     * Check we can view a user's profile through the leaderboard.
     */
    @Test
    public void viewUserProfile() {
        solo.clickOnView(solo.getView(R.id.search));
        assertTrue(solo.waitForText("Leaderboard", 1, 5000)); // confirm we're on leaderboard page

        solo.clickOnText("04"); // click on a user to view their profile
        assertTrue(solo.waitForText("STATS", 1, 5000)); // confirm we're on the user's profile
        solo.sleep(2000);
        solo.clickOnText("Points:"); // click on a QR code to view it
        assertTrue(solo.waitForText("Total Scans:", 1, 5000)); // confirm we're viewing the QR code
    }

    /**
     * Check if we can search for a player using the search bar.
     */
    @Test
    public void searchForPlayer() {
        solo.clickOnView(solo.getView(R.id.search));
        assertTrue(solo.waitForText("Leaderboard", 1, 5000)); // confirm we're on leaderboard page

        solo.clickOnView(solo.getView(R.id.search_id));
        solo.enterText(0, "user0");
        solo.sendKey(Solo.ENTER);
        assertTrue(solo.waitForText("user0", 1, 5000)); // confirm we're on the user's profile
    }

    /**
     * Confirm the player is ranked in the leaderboard; was originally going to check if their ranking updated
     * when they added a QR code, but MockQR object doesn't appear to register with the recyclerview...
     */
    @Test
    public void playerInLeaderboard() {
        solo.clickOnView(solo.getView(R.id.search));
        assertTrue(solo.waitForText("Leaderboard", 1, 5000)); // confirm we're on leaderboard page
        assertTrue(solo.searchText(testUsername, 0, true)); // doesn't look like it's scrolling, but it is
    }

    /**
     * Clear SharedPreferences and close the activity after each test
     */
    @After
    public final void tearDown() {
        Preference.clearPrefs();
        usersReference.document(testUsername).delete();
        solo.finishOpenedActivities();
    }
}
