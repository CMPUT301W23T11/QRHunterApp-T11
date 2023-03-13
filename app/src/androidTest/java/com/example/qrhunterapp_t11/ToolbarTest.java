package com.example.qrhunterapp_t11;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Activity;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.robotium.solo.Solo;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ToolbarTest {
    private Solo solo;


    @Rule
    public ActivityTestRule<MainActivity> rule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    /**
     * Runs before all tests and creates solo instance.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception{
        solo = new Solo(InstrumentationRegistry.getInstrumentation(),rule.getActivity());
    }
    /**
     * Gets the Activity
     * @throws Exception
     */
    @Test
    public void start() throws Exception{
        Activity activity = rule.getActivity();
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
        assertTrue(solo.waitForText("STATS",1, 6000));

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
        solo.waitForText("STATS",1, 4000);

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
        solo.waitForText("STATS",1, 4000);
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
        solo.waitForText("STATS",1, 4000);
        solo.clickOnView(solo.getView(R.id.search));
        assertTrue(solo.waitForText("STATS", 1, 6000));
    }


    /**
     * Tests the map button on the toolbar
     * clicks on the map button on the toolbar
     * asserts that the page was changed from the profile page using assertFalse
     */
    @Test
    public void testMapClick() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS",1, 4000);

        solo.clickOnView(solo.getView(R.id.map));
        solo.sleep(5000);
        // checks that the page changed from the profile page
        assertFalse(solo.waitForText("STATS",1, 6000));
    }

    /**
     * Tests that clicking the map button from map doesn't change the page
     * After going to the map page click on the map button
     * check that the page doesn't go to any of the other pages using assertFalse
     */
    @Test
    public void testMapClickAgain() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS",1, 4000);
        solo.clickOnView(solo.getView(R.id.map));

        // clicks on map after on map page
        solo.clickOnView(solo.getView(R.id.map));
        // asserts that it did not change to any other page
        assertFalse(solo.waitForText("STATS", 1, 2000));
        assertFalse(solo.waitForText("settings", 1, 2000));
        assertFalse(solo.waitForText("Scored", 1, 2000));
        assertFalse(solo.waitForText("search", 1, 2000));

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
        solo.waitForText("STATS",1, 4000);
        solo.clickOnView(solo.getView(R.id.map));

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
     * Test the add button on the toolbar
     * clicks on the add floating action button
     * Asserts that the main screen will change to the camera
     * credited by Aiden Lynch
     */
    @Test
    public void testCameraClick() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForText("STATS",1, 4000);
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
     * Closes the activity after each test
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }
}
