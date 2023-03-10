package com.example.qrhunterapp_t11;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Toolbar button tests
 * Tests every button on the toolbar and their interactions with the main screen
 *
 * @author Kristina
 * @reference Afra - setUp(), tearDown(), ActivityTestRule initialization
 */
public class ToolbarTest {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private final Random rand = new Random();
    private Solo solo;
    private final String testUser = "testUser" + rand.nextInt(10000);
    private SharedPreferences prefs;
    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<MainActivity>(MainActivity.class) {

        // Set SharedPreferences to initialize a new user before the activity is launched
        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();
            prefs = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
            String username;
            String displayName;

            prefs.edit().clear().commit();
            prefs.edit().putBoolean("LoggedIn", true).commit();
            prefs.edit().putString("currentUser", testUser).commit();
            prefs.edit().putString("currentUserDisplayName", testUser).commit();

            username = prefs.getString("currentUser", null);
            displayName = prefs.getString("currentUserDisplayName", null);

            assertEquals(testUser, username);
            assertEquals(testUser, displayName);
        }
    };

    /**
     * Runs before all tests and creates solo instance.
     */
    @Before
    public final void setUp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        rule.launchActivity(intent);
        Activity activity = rule.getActivity();

        Map<String, Object> user = new HashMap<>();
        user.put("Username", testUser);
        user.put("Display Name", testUser);

        usersReference.document(testUser).set(user);

        solo = new Solo(InstrumentationRegistry.getInstrumentation(), activity);
    }

    /**
     * Clear SharedPreferences and close the activity after each test
     */
    @After
    public final void tearDown() {
        Activity activity = rule.getActivity();
        prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        usersReference.document(testUser).delete();
        solo.finishOpenedActivities();
    }

    /**
     * Tests the profile button on the toolbar from the profile page
     * Asserts that the first screen to pop up is the profile screen using assertTrue
     * clicks the profile button on the toolbar
     * assert that the main screen did not change from the profile page using assertTrue
     */
    @Test
    public void testProfileClick() {
        // asserts that the activity starts on the main activity
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        // asserts that the page starts on the profile page
        assertTrue(solo.waitForText("STATS", 1, 6000));

        solo.clickOnView(solo.getView(R.id.profile));
        // asserts that the page stays on the profile page
        assertTrue(solo.waitForText("STATS", 1, 6000));
    }

    /**
     * Tests the settings button on the toolbar
     * Clicks on the settings button on the toolbar
     * asserts that the main screen switched from the profile screen to the settings screen using assertTrue
     */
    @Test
    public void testSettingsClick() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS", 1, 4000);

        solo.clickOnView(solo.getView(R.id.settings));
        // asserts that settings button works
        assertTrue(solo.waitForText("Settings", 1, 6000));
    }

    /**
     * Tests that pressing the settings button from the settings page does nothing
     * goes to settings then clicks on settings button
     * asserts that screen stays on settings page using assertTrue
     */
    @Test
    public void testSettingsClickAgain() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS", 1, 4000);
        solo.clickOnView(solo.getView(R.id.settings));
        solo.waitForText("Settings", 1, 4000);

        // checks that pressing the settings button stays on the settings page
        solo.clickOnView(solo.getView(R.id.settings));
        assertTrue(solo.waitForText("Settings", 1, 4000));

    }

    /**
     * Tests that the profile button works from the settings screen
     * goes to settings then clicks on profile button
     * asserts that the screen is changed to profile using assertTrue
     */
    @Test
    public void testProfileFromSettingsClick() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS", 1, 4000);
        solo.clickOnView(solo.getView(R.id.settings));
        solo.waitForText("Settings", 1, 4000);

        // checks that the profile button works
        solo.clickOnView(solo.getView(R.id.profile));
        assertTrue(solo.waitForText("STATS", 1, 6000));

    }

    /**
     * Tests that the map button works from the settings screen
     * goes to settings then clicks on map button
     * asserts that the screen is changed to map using assertTrue
     */
    @Test
    public void testMapFromSettingsClick() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS", 1, 4000);
        solo.clickOnView(solo.getView(R.id.settings));
        solo.waitForText("Settings", 1, 4000);

        // checks that the map button works
        solo.clickOnView(solo.getView(R.id.map));
        assertFalse(solo.waitForFragmentByTag("settings", 6000));

    }

    /**
     * Tests that search button does nothing yet from settings screen
     * goes to settings then clicks on search button
     * asserts that settings stayed on settings screen using assertTrue
     */
    @Test
    public void testSearchFromSettingsClick() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS", 1, 4000);
        solo.clickOnView(solo.getView(R.id.settings));
        solo.waitForText("Settings", 1, 4000);

        // checks that the search button does nothing yet
        solo.clickOnView(solo.getView(R.id.search));
        assertTrue(solo.waitForText("Settings", 1, 4000));

    }

    /**
     * Tests that the add button works from the settings page
     * goes to the settings page and clicks on the add button
     * asserts that the camera opens using assertTrue
     * partially credited by Aiden Lynch
     */
    @Test
    public void testAddFromSettingsClick() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS", 1, 4000);
        solo.clickOnView(solo.getView(R.id.settings));
        solo.waitForText("Settings", 1, 4000);

        // checks that the add button works
        solo.clickOnView(solo.getView(R.id.addFab));
        // gotten from CameraFragmentTest.java
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnText("Yes");
        solo.clickOnView(solo.getView(R.id.captureButton));
        solo.waitForText("Share Geolocation", 1, 7000); // wait 7 sec for location prompt to appear
        solo.clickOnText("No");
        solo.waitForText("STATS", 1, 7000);
    }

    /**
     * Tests the Search button on the toolbar
     * Clicks on the search button on the toolbar
     * asserts that the search button does not do anything yet and stays on the profile page using assertTrue
     */
    @Test
    public void testSearchClick() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS", 1, 4000);
        solo.clickOnView(solo.getView(R.id.search));
        assertTrue(solo.waitForText("STATS", 1, 6000));
    }


    /**
     * Tests that the settings button works from the map screen
     * goes to map then clicks on settings button
     * asserts that the screen is changed to settings using assertTrue
     */
    @Test
    public void testSettingsFromMapClick() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS", 1, 4000);
        solo.clickOnView(solo.getView(R.id.map));

        // checks that pressing the setting button works
        solo.clickOnView(solo.getView(R.id.settings));
        assertTrue(solo.waitForText("Settings", 1, 8000));

    }

    /**
     * Tests that the profile button works from the map screen
     * goes to map then clicks on profile button
     * asserts that the screen is changed to profile using assertTrue
     */
    @Test
    public void testProfileFromMapClick() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS", 1, 4000);
        solo.clickOnView(solo.getView(R.id.map));

        // checks that the profile button works
        solo.clickOnView(solo.getView(R.id.profile));
        assertTrue(solo.waitForText("STATS", 1, 8000));

    }

    /**
     * Tests that search button does nothing yet from map screen
     * goes to map then clicks on search button
     * asserts that the map did not go to a search screen using assertFalse
     */
    @Test
    public void testSearchFromMapClick() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS", 1, 4000);
        solo.clickOnView(solo.getView(R.id.map));

        // checks that the search button does nothing yet
        solo.clickOnView(solo.getView(R.id.search));
        assertFalse(solo.waitForText("search", 1, 4000));

    }

    /**
     * Tests that the add button works from the map screen
     * goes to the Map then clicks on the add button
     * asserts that the screen changed to the camera using assertTrue
     * partially credited by Aiden Lynch
     */
    @Test
    public void testAddFromMapClick() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS", 1, 4000);
        solo.clickOnView(solo.getView(R.id.map));

        // checks that the add button works
        solo.clickOnView(solo.getView(R.id.addFab));
        // gotten from CameraFragmentTest.java
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 10 sec for photo prompt to appear
        solo.clickOnText("Yes");
        solo.clickOnView(solo.getView(R.id.captureButton));
        solo.waitForText("Share Geolocation", 1, 7000); // wait 7 sec for location prompt to appear
        solo.clickOnText("No");
        solo.waitForText("STATS", 1, 7000);
    }

    /**
     * Test the add button on the toolbar
     * clicks on the add floating action button
     * Asserts that the main screen will change to the camera
     * credited by Aiden Lynch
     */
    @Test
    public void testCameraClick() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS", 1, 4000);
        solo.clickOnView(solo.getView(R.id.addFab));
        // gotten from CameraFragmentTest.java
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 10 sec for photo prompt to appear
        solo.clickOnText("Yes");
        solo.clickOnView(solo.getView(R.id.captureButton));
        solo.waitForText("Share Geolocation", 1, 7000); // wait 7 sec for location prompt to appear
        solo.clickOnText("No");
        solo.waitForText("STATS", 1, 7000);
    }

}
