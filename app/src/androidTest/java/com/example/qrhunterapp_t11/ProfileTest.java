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
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.example.qrhunterapp_t11.objectclasses.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

/**
 * Intent tests for the Profile Fragment and its interaction with the QRCodeView dialog.
 *
 * @author Sarah Thomson, Aidan Lynch, Afra
 * @reference code mostly repurposed from Aidan Lynch and lab 7.
 * @reference Afra - setUp(), tearDown(), ActivityTestRule initialization
 * @reference <a href="https://stackoverflow.com/questions/50035752/how-to-get-list-of-documents-from-a-collection-in-firestore-android">for help retrieving all documents answer by Ivan Banha CC BY-SA 3.0.</a>
 */
public class ProfileTest {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private final Random rand = new Random();
    private final String testUsername = "testUser" + rand.nextInt(1000000);
    //private SharedPreferences prefs;
    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<MainActivity>(MainActivity.class) {

        // Set SharedPreferences to initialize a new user before the activity is launched
        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();
            //prefs = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
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
    private boolean docExists;
    private QRCode qrCode;
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

        ArrayList<String> qrCodeIDs = new ArrayList<>();
        ArrayList<String> qrCodeHashes = new ArrayList<>();
        ArrayList<String> commentedOn = new ArrayList<>();
        User user = new User(testUsername, testUsername, 0, 0, 0, "", qrCodeIDs, qrCodeHashes, commentedOn);
        CollectionReference qrReference = db.collection("QRCodes");
        usersReference.document(testUsername).set(user);

        // add new QR code
        final int randomNum = new Random().nextInt(10000);
        qrCode = mockQRCode(String.valueOf(randomNum));
        name = qrCode.getName();

        qrReference.document(qrCode.getID()).set(qrCode);

        usersReference.document(testUsername).update("qrCodeHashes", FieldValue.arrayUnion(qrCode.getHash()));
        usersReference.document(testUsername).update("qrCodeIDs", FieldValue.arrayUnion(qrCode.getID()));
        qrReference.document(qrCode.getID()).update("inCollection", FieldValue.arrayUnion(testUsername));
        usersReference.document(testUsername).update("totalScans", FieldValue.increment(1));
        usersReference.document(testUsername).update("totalPoints", FieldValue.increment(qrCode.getPoints()));

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
     * Verifies that when you click an item in the recyclerView the dialog for it appears
     */
    @Test
    public void checkListClick() {
        // Asserts that the current activity is the MainActivity. Otherwise, show Wrong Activity
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        // Refresh the profile
        solo.clickOnView(solo.getView(R.id.settings));
        Assert.assertTrue(solo.waitForText("Settings", 1, 1000));
        solo.clickOnView(solo.getView(R.id.profile));

        solo.sleep(20);
        // Name of qrCode should appear
        assertTrue(solo.waitForText(name, 1, 2000));
        // There should only be 1 qrCode in collection
        assertTrue(solo.waitForText("1", 1, 10000));
        // Points should appear 1. in recycler view 2. in highest score QR 3. in lowest score QR 4. in total points
        assertTrue(solo.waitForText(String.valueOf(qrCode.getPoints()), 4, 10000));
        solo.clickOnText(qrCode.getName());
        // wait for "Add Comment" to know the qrView dialog has opened
        assertTrue(solo.waitForText("Add comment", 1, 10000));
        // should display the qRCode name as a header
        assertTrue(solo.waitForText(name, 1, 10000));
        // points should be displayed in dialog
        assertTrue(solo.waitForText(String.valueOf(qrCode.getPoints()), 1, 10000));
        // click OK
        solo.clickOnText("OK");
    }

    /**
     * Verifies comments can be added and that they will show up in the comment box after being sent
     */
    @Test
    public void checkCommentAdd() {
        // Asserts that the current activity is the MainActivity. Otherwise, show Wrong Activity
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        // Refresh the profile
        solo.clickOnView(solo.getView(R.id.settings));
        Assert.assertTrue(solo.waitForText("Settings", 1, 1000));
        solo.clickOnView(solo.getView(R.id.profile));
        solo.sleep(20);

        // Name of qrCode should appear
        assertTrue(solo.waitForText(name, 1, 2000));
        // There should only be 1 qrCode in collection
        assertTrue(solo.waitForText("1", 1, 10000));
        solo.clickOnText(qrCode.getName());
        // wait for "Add Comment" to know the qrView dialog has opened
        assertTrue(solo.waitForText("Add comment", 1, 10000));
        solo.enterText(0, "great catch king 😂");
        solo.clickOnView(solo.getView(R.id.imageViewSend));
        solo.enterText(0, "great catch king 😂");
        solo.clickOnView(solo.getView(R.id.imageViewSend));
        // Verify comments have been sent to the comment box
        assertTrue(solo.waitForText("great catch king 😂", 2, 10000));
        assertTrue(solo.waitForText(testUsername, 2, 10000));
        // click OK
        solo.clickOnText("OK");
    }

    /**
     * Verifies scoreboard is updated accordingly when a new QR Code is added
     */
    @Test
    public void checkPointAddition() {
        // Asserts that the current activity is the MainActivity. Otherwise, show Wrong Activity
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        CollectionReference qrReference = db.collection("QRCodes");

        // Refresh the profile
        solo.clickOnView(solo.getView(R.id.settings));
        Assert.assertTrue(solo.waitForText("Settings", 1, 1000));
        solo.clickOnView(solo.getView(R.id.profile));
        solo.sleep(20);

        // Name of qrCode should appear
        assertTrue(solo.waitForText(name, 1, 2000));
        // There should only be 1 qrCode in collection
        assertTrue(solo.waitForText("1", 1, 10000));
        // Points should appear 1. in recycler view 2. in highest score QR 3. in lowest score QR 4. in total points
        assertTrue(solo.waitForText(String.valueOf(qrCode.getPoints()), 4, 10000));

        // Create another QR object
        QRCode qrCode1 = mockQRCode("Test");
        qrReference.document(qrCode1.getID()).set(qrCode1);

        usersReference.document(testUsername).update("qrCodeHashes", FieldValue.arrayUnion(qrCode1.getHash()));
        usersReference.document(testUsername).update("qrCodeIDs", FieldValue.arrayUnion(qrCode1.getID()));
        qrReference.document(qrCode1.getID()).update("inCollection", FieldValue.arrayUnion(testUsername));
        usersReference.document(testUsername).update("totalScans", FieldValue.increment(1));
        usersReference.document(testUsername).update("totalPoints", FieldValue.increment(qrCode1.getPoints()));

        // Refresh the profile
        solo.clickOnView(solo.getView(R.id.settings));
        Assert.assertTrue(solo.waitForText("Settings", 1, 1000));
        solo.clickOnView(solo.getView(R.id.profile));
        solo.sleep(20);

        // Check if the total has been updated
        assertTrue(solo.waitForText(String.valueOf(qrCode1.getPoints() + qrCode.getPoints()), 1, 10000));
        if (qrCode1.getPoints() != qrCode.getPoints()) {
            // Check if highest and lowest have been updated
            assertTrue(solo.waitForText(String.valueOf(Math.max(qrCode1.getPoints(), qrCode.getPoints())), 1, 10000));
            assertTrue(solo.waitForText(String.valueOf(Math.min(qrCode1.getPoints(), qrCode.getPoints())), 1, 10000));
        }

        // Check if there are now 2 QRCodes shown on scoreboard
        assertTrue(solo.waitForText("2", 1, 10000));
    }

    /**
     * Verifies that a qrCode can be deleted from the profile correctly by a Long click when the positive button on the dialog confirmation box is
     */
    @Test
    public void deleteLongClickPositive() {
        // Asserts that the current activity is the MainActivity. Otherwise, show Wrong Activity
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        // Refresh the profile
        solo.clickOnView(solo.getView(R.id.settings));
        Assert.assertTrue(solo.waitForText("Settings", 1, 1000));
        solo.clickOnView(solo.getView(R.id.profile));

        // Long click to delete
        solo.clickLongOnText(qrCode.getName());
        Assert.assertTrue(solo.waitForText("Delete", 1, 1000));
        solo.clickOnView(solo.getView(android.R.id.button1));
        solo.sleep(2000);
        Assert.assertFalse(solo.waitForText(String.valueOf(qrCode.getPoints()), 1, 100));
        Assert.assertFalse(solo.waitForText(qrCode.getName(), 1, 100));
        Assert.assertTrue(solo.waitForText("0", 4, 100));

    }

    /**
     * Verifies that a qrCode will not be deleted from the profile correctly by a Long click when the negative button on the dialog confirmation box is clicked
     */
    @Test
    public void deleteLongClickNegative() {
        // Asserts that the current activity is the MainActivity. Otherwise, show Wrong Activity
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        CollectionReference qrReference = usersReference.document(testUsername).collection("QR Codes");

        // Refresh the profile
        solo.clickOnView(solo.getView(R.id.settings));
        Assert.assertTrue(solo.waitForText("Settings", 1, 1000));
        solo.clickOnView(solo.getView(R.id.profile));

        // Long click to delete
        solo.clickLongOnText(qrCode.getName());
        Assert.assertTrue(solo.waitForText("Delete", 1, 1000));
        solo.clickOnText("Cancel");
        solo.sleep(20);
        Assert.assertTrue(solo.waitForText(String.valueOf(qrCode.getPoints()), 4, 100));
        Assert.assertTrue(solo.waitForText(qrCode.getName(), 1, 100));
        Assert.assertTrue(solo.waitForText("1", 1, 100));

    }

}