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
import com.example.qrhunterapp_t11.objectclasses.Preference;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.example.qrhunterapp_t11.objectclasses.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

/**
 * -------------------------IMPORTANT-------------------------
 * Link to qrCode being used in this test: https://en.wikipedia.org/wiki/QR_code THIS IS THE QRCODE THAT MUST BE SCANNED IN EACH TEST (easiest to add to the wall in the virtual scene)
 * -----------------------------------------------------------
 * Some manual testing required: tester must accept permissions and scan the provided qr Code
 * Test the following situations:
 * 0. User's qrCode photo appears on the qrCode view in their account
 * 1. User has QR code in collection with NULL LOCATION:
 * 1.1 They scan the same qrcode and refuse to update location
 * 1.2 They scan the same qrcode and update location with a null location (no change)
 * 1.3 They scan the same qrcode and update location with a non null location (change) ****
 * 2. User has QR code in collection with NONNULL LOCATION:
 * 2.1 They scan the same qrcode and refuse to update location
 * 2.2 They scan the same qrcode and update location with a null location (no change)
 * 2.3 They scan the same qrcode and update location with a non null location that is the same location as the original (within 30m) (no change)
 *
 * @author Sarah - for tests
 * @author Aidan - for some code from his CameraFragment tests
 * @author Afra - set up querying information from the database, setUp(), tearDown(), ActivityTestRule initialization.
 **/

public class CameraFragmentScanDuplicatesTest {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private final Random rand = new Random();
    private final String testUsername = "testUser" + rand.nextInt(1000000);
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
    private final FirebaseQueryAssistant firebaseQueryAssistant = new FirebaseQueryAssistant(db);
    private final CollectionReference qrReference = db.collection("QRCodes");
    private Solo solo;
    private String name;

    /**
     * Runs before all tests and creates solo instance.
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
     * Tests to make sure a new photo appears in the qrcode view after the user takes a photo
     */
    @Test
    public void photoAppears() {
        String testId = "9a7cd5efda286fbcdd26f89e64a360c560208248b301ff49ad670cb5552790ff";
        final int[] photoSize = new int[1];
        qrReference.document(testId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                QRCode qrCode1 = documentSnapshot.toObject(QRCode.class);
                photoSize[0] = qrCode1.getPhotoList().size();
            }
        });

        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Points", 1, 10000)); // wait 7 sec for points prompt to appear
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button1));
        solo.clickOnView(solo.getView(R.id.captureButton));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        solo.clickOnText("Comments:");
        solo.sleep(1000);
        String newPhotoSize = String.valueOf(++photoSize[0]);
        assertTrue(solo.waitForText(newPhotoSize, 1, 7000)); // confirm a photo has been added
        solo.clickOnView(solo.getView(android.R.id.button1));
    }

    /**
     * Tests the following:
     * User has QR code in collection with NULL LOCATION:
     * They scan the same qrcode and refuse to update location (no change)
     */
    @Test
    public void nullLocationNoUpdateLocation() {
        String testId = "9a7cd5efda286fbcdd26f89e64a360c560208248b301ff49ad670cb5552790ff";
        String testComment = "test" + rand.nextInt(1000000);

        // scan qr code the first time with null location
        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Points", 1, 10000)); // wait 7 sec for points prompt to appear
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        //solo.clickOnView(solo.getView(R.id.captureButton));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        solo.clickOnText("Comments:");
        solo.sleep(1000);
        solo.drag(250, 250, 400, 0, 10);
        solo.enterText(0, testComment);
        solo.clickOnView(solo.getView(R.id.imageViewSend));
        solo.clickOnView(solo.getView(android.R.id.button1));

        firebaseQueryAssistant.checkDocExists(testId, qrReference, new QueryCallback() {
            public void queryCompleteCheck(boolean docExists) {
                assertTrue(docExists);
            }
        });

        //scan the same qr code the second time with null location
        solo.clickOnView(solo.getView(R.id.addFab));
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        solo.clickOnText("Comments:");
        solo.drag(250, 250, 400, 0, 10);
        assertTrue(solo.waitForText(testComment, 1, 7000)); // confirm the qrcode is the same as the first one by checking that the original comment is there
        solo.clickOnView(solo.getView(android.R.id.button1));

        firebaseQueryAssistant.deleteDoc(testId, qrReference);

    }

    /**
     * Tests the following:
     * User has QR code in collection with NULL LOCATION:
     * They scan the same qrcode and update location with a null location (no change)
     */
    @Test
    public void nullLocationUpdateLocationWithNull() {

        String testId = "9a7cd5efda286fbcdd26f89e64a360c560208248b301ff49ad670cb5552790ff";
        String testComment = "test" + rand.nextInt(1000000);

        // scan qr code the first time with null location
        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Points", 1, 10000)); // wait 7 sec for points prompt to appear
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        //solo.clickOnView(solo.getView(R.id.captureButton));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        solo.clickOnText("Comments:");
        solo.sleep(1000);
        solo.drag(250, 250, 400, 0, 10);
        solo.enterText(0, testComment);
        solo.clickOnView(solo.getView(R.id.imageViewSend));
        solo.clickOnView(solo.getView(android.R.id.button1));

        //scan the same qr code the second time with null location
        solo.clickOnView(solo.getView(R.id.addFab));
        solo.clickOnView(solo.getView(android.R.id.button1));
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        solo.clickOnText("Comments:");
        solo.drag(250, 250, 400, 0, 10);
        assertTrue(solo.waitForText(testComment, 1, 7000)); // confirm the qrcode is the same as the first one by checking that the original comment is there
        solo.clickOnView(solo.getView(android.R.id.button1));

        firebaseQueryAssistant.deleteDoc(testId, qrReference);


    }

    /**
     * Tests the following:
     * User has QR code in collection with NULL LOCATION:
     * They scan the same qrcode and update location with a non null location (change)
     */
    @Test
    public void nullLocationUpdateLocationWithLocation() {

        String testId = "9a7cd5efda286fbcdd26f89e64a360c560208248b301ff49ad670cb5552790ff";
        String testComment = "test" + rand.nextInt(1000000);
        solo.clickOnView(solo.getView(R.id.map)); // Go to map to avoid emulator bug with not allowing location data
        solo.sleep(5000);
        solo.clickOnView(solo.getView(R.id.profile));


        // scan qr code the first time with null location
        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Points", 1, 10000)); // wait 7 sec for points prompt to appear
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        //solo.clickOnView(solo.getView(R.id.captureButton));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        solo.clickOnText("Comments:");
        solo.sleep(1000);
        solo.drag(250, 250, 400, 0, 10);
        solo.enterText(0, testComment);
        solo.clickOnView(solo.getView(R.id.imageViewSend));
        solo.clickOnView(solo.getView(android.R.id.button1));

        //scan the same qr code the second time with location data
        solo.clickOnView(solo.getView(R.id.addFab));
        solo.clickOnView(solo.getView(android.R.id.button1));
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button1));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        assertFalse(solo.waitForText("Comments:", 2, 7000)); // confirm we're back on the profile page and the old qr has been replaced
        solo.clickOnText("Comments:");
        solo.drag(250, 250, 400, 0, 10);
        assertFalse(solo.waitForText(testComment, 1, 7000)); // confirm the qrcode has been replaced by the old one by checking the comments
        solo.clickOnView(solo.getView(android.R.id.button1));

        firebaseQueryAssistant.deleteDoc(testId, qrReference);

    }

    /**
     * Tests the following:
     * User has QR code in collection with LOCATION:
     * They scan the same qrcode and refuse to update location (no change)
     */
    @Test
    public void LocationNoUpdateLocation() {
        String testId = "9a7cd5efda286fbcdd26f89e64a360c560208248b301ff49ad670cb5552790ff";
        String testComment = "test" + rand.nextInt(1000000);

        // scan qr code the first time with null location
        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Points", 1, 10000)); // wait 7 sec for points prompt to appear
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        //solo.clickOnView(solo.getView(R.id.captureButton));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button1));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        solo.clickOnText("Comments:");
        solo.sleep(1000);
        solo.drag(250, 250, 400, 0, 10);
        solo.enterText(0, testComment);
        solo.clickOnView(solo.getView(R.id.imageViewSend));
        solo.clickOnView(solo.getView(android.R.id.button1));

        //scan the same qr code the second time and refuse location
        solo.clickOnView(solo.getView(R.id.addFab));
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        solo.clickOnText("Comments:");
        solo.drag(250, 250, 400, 0, 10);
        assertTrue(solo.waitForText(testComment, 1, 7000)); // confirm the qrcode is the same as the first one by checking that the original comment is there
        solo.clickOnView(solo.getView(android.R.id.button1));

        firebaseQueryAssistant.deleteDoc(testId, qrReference);

    }


    /**
     * Tests the following:
     * User has QR code in collection with LOCATION:
     * They scan the same qrcode and update location with a null location (no change)
     */
    @Test
    public void LocationUpdateLocationWithNull() {

        String testId = "9a7cd5efda286fbcdd26f89e64a360c560208248b301ff49ad670cb5552790ff";
        String testComment = "test" + rand.nextInt(1000000);

        // scan qr code the first time with null location
        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Points", 1, 10000)); // wait 7 sec for points prompt to appear
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button1));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        solo.clickOnText("Comments:");
        solo.sleep(1000);
        solo.drag(250, 250, 400, 0, 10);
        solo.enterText(0, testComment);
        solo.clickOnView(solo.getView(R.id.imageViewSend));
        solo.clickOnView(solo.getView(android.R.id.button1));

        //scan the same qr code the second time with null location
        solo.clickOnView(solo.getView(R.id.addFab));
        solo.clickOnView(solo.getView(android.R.id.button1));
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        solo.clickOnText("Comments:");
        solo.drag(250, 250, 400, 0, 10);
        assertTrue(solo.waitForText(testComment, 1, 7000)); // confirm the qrcode is the same as the first one by checking that the original comment is there
        solo.clickOnView(solo.getView(android.R.id.button1));

        firebaseQueryAssistant.deleteDoc(testId, qrReference);


    }

    /**
     * Tests the following:
     * User has QR code in collection with LOCATION:
     * They scan the same qrcode and update location with the same non null location (no change)
     */
    @Test
    public void LocationUpdateNoUpdateLocation() {

        String testId = "9a7cd5efda286fbcdd26f89e64a360c560208248b301ff49ad670cb5552790ff";
        String testComment = "test" + rand.nextInt(1000000);
        solo.clickOnView(solo.getView(R.id.map)); // Go to map to avoid emulator bug with not allowing location data
        solo.sleep(5000);
        solo.clickOnView(solo.getView(R.id.profile));


        // scan qr code the first time with location
        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Points", 1, 10000)); // wait 7 sec for points prompt to appear
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button1));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        solo.clickOnText("Comments:");
        solo.sleep(1000);
        solo.drag(250, 250, 400, 0, 10);
        solo.enterText(0, testComment);
        solo.clickOnView(solo.getView(R.id.imageViewSend));
        solo.clickOnView(solo.getView(android.R.id.button1));

        //scan the same qr code the second time with same location data
        solo.clickOnView(solo.getView(R.id.addFab));
        solo.clickOnView(solo.getView(android.R.id.button1));
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnView(solo.getView(android.R.id.button1));
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page
        assertFalse(solo.waitForText("Comments:", 2, 7000)); // confirm we're back on the profile page and the old qr has been replaced
        solo.clickOnText("Comments:");
        solo.drag(250, 250, 400, 0, 10);
        assertTrue(solo.waitForText(testComment, 1, 7000)); // confirm the qrcode has not been replaced by the old one by checking the comments
        solo.clickOnView(solo.getView(android.R.id.button1));

        firebaseQueryAssistant.deleteDoc(testId, qrReference);

    }


}
