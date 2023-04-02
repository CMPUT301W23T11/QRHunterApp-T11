package com.example.qrhunterapp_t11;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.app.Activity;
import android.content.Intent;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.qrhunterapp_t11.activities.MainActivity;
import com.example.qrhunterapp_t11.fragments.FirebaseQueryAssistant;
import com.example.qrhunterapp_t11.interfaces.QueryCallback;
import com.example.qrhunterapp_t11.interfaces.QueryCallbackWithObject;
import com.example.qrhunterapp_t11.objectclasses.Preference;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.example.qrhunterapp_t11.objectclasses.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
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
 * Link to qrCode being used in this test: https://en.wikipedia.org/wiki/QR_code
 * Some manual testing required: tester must accept permissions and scan the provided qr Code
 */

//TODO: Test the following situations:
// 1. User has QR code in collection with NULL LOCATION:
//      1.1 They scan the same qrcode and refuse to update location
//      1.2 They scan the same qrcode and update location with a null location (no change)
//      1.3 They scan the same qrcode and update location with a non null location (change) ****
// 2. User has QR code in collection with NONNULL LOCATION:
//      2.1 They scan the same qrcode and refuse to update location
//      2.2 They scan the same qrcode and update location with a null location (no change)
//      2.3 They scan the same qrcode and update location with a non null location that is the same location as the original (within 30m) (no change)
//      2.3 They scan the same qrcode and update location with a non null location that is a different location than the original (further than 30m) (change)
//
public class CameraFragmentScanDuplicatesTest {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private final Random rand = new Random();
    private final String testUsername = "testUser" + rand.nextInt(1000000);
    private final FirebaseQueryAssistant firebaseQueryAssistant = new FirebaseQueryAssistant(db);

    private final CollectionReference qrReference = db.collection("QRCodes");
    private Solo solo;
    private boolean docExists;
    private QRCode qrCode;
    //private SharedPreferences prefs;
    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<MainActivity>(MainActivity.class) {

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
    private String name;

    private QRCode mockQRCode(String valueString) {
        return new QRCode(valueString);
    }

    /**
     * Runs before all tests and creates solo instance.
     */
    @Before
    public final void setUp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        rule.launchActivity(intent);
        Activity activity = rule.getActivity();

        User user = new User(testUsername, testUsername, 0, 0, 0, "No email");
        usersReference.document(testUsername).set(user);
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), activity);

    }

    /**
     * Clear SharedPreferences and close the activity after each test
     */
    @After
    public final void tearDown() {
        Activity activity = rule.getActivity();
        Preference.clearPrefs();
        usersReference.document(testUsername).delete();
        solo.finishOpenedActivities();
    }


    /**
     * Tests the following:
     * 1. User has QR code in collection with NULL LOCATION:
     *       1.1 They scan the same qrcode and refuse to update location
     *       1.2 They scan the same qrcode and update location with a null location (no change)
     *       1.3 They scan the same qrcode and update location with a non null location (change)
     */
    @Test
    public void takePhotoShareLocation() {
        String testId = "9a7cd5efda286fbcdd26f89e64a360c560208248b301ff49ad670cb5552790ff";

        // scan qr code the first time with null location
        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Points", 1, 10000)); // wait 7 sec for points prompt to appear
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button1));
        solo.clickOnView(solo.getView(R.id.captureButton));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        solo.clickOnText("Comments:");
        String numerator = solo.getView(R.id.numeratorTV).toString();
        System.out.println(numerator);

        firebaseQueryAssistant.checkDocExists(testId, qrReference, new QueryCallback() {
            public void queryCompleteCheck(boolean docExists) {
                assertTrue(docExists);
            }
        });

        //scan the qr code the second time with null location
        solo.clickOnView(solo.getView(R.id.addFab));
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        solo.clickOnText("Comments:");
        assertTrue(solo.waitForText("1/1", 1, 7000)); // confirm the second photo has not been added

        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        solo.clickOnView(solo.getView(R.id.captureButton));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnText("NO");
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        assertTrue(solo.waitForText("1/1", 1, 7000)); // confirm the second photo has not been added

    }



}
