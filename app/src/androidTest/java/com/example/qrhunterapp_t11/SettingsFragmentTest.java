package com.example.qrhunterapp_t11;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Intent;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.qrhunterapp_t11.activities.MainActivity;
import com.example.qrhunterapp_t11.interfaces.QueryCallback;
import com.example.qrhunterapp_t11.objectclasses.Preference;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.example.qrhunterapp_t11.objectclasses.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Test class for the settings fragment.
 *
 * @author Afra
 * @sources <a href="https://stackoverflow.com/questions/37101241/how-to-listen-android-activitytestrules-beforeactivitylaunched-method-in-an-and">The journey to this answer took me to the 5th circle of hell and back</a>
 */
public class SettingsFragmentTest {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private final CollectionReference qrCodesReference = db.collection("QRCodes");
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
    private Solo solo;
    private String username;

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
     * Test behaviour when attempting to change to a blank or taken username
     */
    @Test
    public void testUsernameInvalid() {
        String testUserDupe = "testUserDupe";
        addTestUserToDB(testUserDupe);

        solo.clickOnView(solo.getView(R.id.settings));

        solo.clickOnView(solo.getView(R.id.username_edit_edittext));
        solo.clearEditText(0);
        solo.enterText(0, "");
        solo.clickOnView(solo.getView(R.id.change_username_button));
        EditText usernameEditText = solo.getEditText(0);
        assertEquals("Field cannot be blank", usernameEditText.getError());

        solo.clickOnView(solo.getView(R.id.username_edit_edittext));
        solo.clearEditText(0);
        solo.enterText(0, testUserDupe);
        solo.clickOnView(solo.getView(R.id.change_username_button));
        usernameEditText = solo.getEditText(0);
        assertEquals("Username is not unique", usernameEditText.getError());

        usersReference.document(testUserDupe).delete();
    }

    /**
     * Test behaviour when attempting to change email
     */
    @Test
    public void testEmailChange() {

        solo.clickOnView(solo.getView(R.id.settings));
        solo.clickOnView(solo.getView(R.id.email_edit_edittext));

        // Random string
        solo.clearEditText(1);
        solo.enterText(1, "randomText");
        solo.clickOnView(solo.getView(R.id.change_email_button));
        EditText emailEditText = solo.getEditText(1);
        assertEquals("Invalid email", emailEditText.getError());

        // Just email domain with @
        solo.clearEditText(1);
        solo.enterText(1, "@gmail.com");
        solo.clickOnView(solo.getView(R.id.change_email_button));
        emailEditText = solo.getEditText(1);
        assertEquals("Invalid email", emailEditText.getError());

        // Random string with @ symbol
        solo.clearEditText(1);
        solo.enterText(1, "randomText@");
        solo.clickOnView(solo.getView(R.id.change_email_button));
        emailEditText = solo.getEditText(1);
        assertEquals("Invalid email", emailEditText.getError());

        // Random string with @ symbol and partial domain
        solo.clearEditText(1);
        solo.enterText(1, "randomText@gmail");
        solo.clickOnView(solo.getView(R.id.change_email_button));
        emailEditText = solo.getEditText(1);
        assertEquals("Invalid email", emailEditText.getError());

        // Random string with @ symbol and partial domain with dot
        solo.clearEditText(1);
        solo.enterText(1, "randomtext@gmail.");
        solo.clickOnView(solo.getView(R.id.change_email_button));
        emailEditText = solo.getEditText(1);
        assertEquals("Invalid email", emailEditText.getError());

        // Backwards order
        solo.clearEditText(1);
        solo.enterText(1, "@gmail.comrandomText");
        solo.clickOnView(solo.getView(R.id.change_email_button));
        emailEditText = solo.getEditText(1);
        assertEquals("Invalid email", emailEditText.getError());

        // Correct email but with space
        solo.clearEditText(1);
        solo.enterText(1, "random Text@gmail.com");
        solo.clickOnView(solo.getView(R.id.change_email_button));
        emailEditText = solo.getEditText(1);
        assertEquals("Invalid email", emailEditText.getError());

        // Correct email
        String correctEmailString = "randomText@gmail.com";
        solo.clearEditText(1);
        solo.enterText(1, correctEmailString);
        solo.clickOnView(solo.getView(R.id.change_email_button));
        solo.clickOnText("Confirm", 2);

        usersReference.document(testUsername)
                .get()
                .addOnSuccessListener(user -> {
                    assertEquals(user.get("email"), correctEmailString);
                });

    }

    /**
     * Test behaviour when attempting to change to a unique username
     */
    @Test
    public void testUsernameChange() {
        String testUserUnique = "testUserUnique";

        // Make sure testUserUnique displayName is unique
        checkUniqueDisplayName(testUserUnique, new QueryCallback() {
            public void queryCompleteCheck(boolean unique) {
                assertTrue(unique);

            }
        });

        QRCode qrCode1 = mockQRCode();
        QRCode qrCode2 = mockQRCode();

        for (int i = 0; i < 2; i++) {
            HashMap<String, String> comment = new HashMap<>();
            comment.put("commentString", "test comment" + i);
            comment.put("displayName", testUsername);
            comment.put("username", testUsername);
            qrCodesReference.document(qrCode1.getID()).collection("commentList").add(comment);

            comment.put("commentString", "test comment" + (3 * i));
            qrCodesReference.document(qrCode2.getID()).collection("commentList").add(comment);

            comment.put("username", "falseTestUser");
            qrCodesReference.document(qrCode1.getID()).collection("commentList").add(comment);
            qrCodesReference.document(qrCode2.getID()).collection("commentList").add(comment);
        }

        solo.clickOnView(solo.getView(R.id.settings));

        solo.clickOnView(solo.getView(R.id.username_edit_edittext));
        solo.clearEditText(0);

        solo.enterText(0, testUserUnique);
        solo.clickOnView(solo.getView(R.id.change_username_button));
        solo.clickOnText("Confirm", 2);

        // I want to talk to whoever decided queries should be asynchronous.
        // Just have a friendly chat.
        solo.sleep(3000);
        // Just a friendly, pleasant conversation.

        // Make sure displayName was successfully changed
        checkDisplayNameChanged(qrCode1, qrCode2, new QueryCallback() {
            @Override
            public void queryCompleteCheck(boolean queryComplete) {
                assert (queryComplete);
                deleteMockQRCode(qrCode1);
                deleteMockQRCode(qrCode2);
                usersReference.document(testUserUnique).delete();
            }
        });
    }


    /**
     * Adds a test user to the database.
     * testUser should always be a new addition to the database
     */
    public void addTestUserToDB(String username) {
        ArrayList<String> qrCodeIDs = new ArrayList<>();
        ArrayList<String> qrCodeHashes = new ArrayList<>();
        ArrayList<String> commentedOn = new ArrayList<>();

        User user = new User(username, username, 0, 0, 0, "", qrCodeIDs, qrCodeHashes, commentedOn);

        usersReference.document(username).set(user);

    }

    /**
     * Checks if the given displayName exists in the database
     */
    public void checkUniqueDisplayName(String displayName, final QueryCallback uniqueUsername) {
        usersReference
                .whereEqualTo("displayName", displayName)
                .get()
                .addOnSuccessListener(results -> {
                    uniqueUsername.queryCompleteCheck(results.isEmpty());
                });
    }

    /**
     * Checks if the given displayName was correctly changed
     */
    public void checkDisplayNameChanged(QRCode qrCode1, QRCode qrCode2, final QueryCallback queryComplete) {
        usersReference.document(testUsername)
                .get()
                .addOnSuccessListener(user -> {
                    assertEquals(user.get("displayName"), Preference.getPrefsString("currentUserDisplayName", null));

                    qrCodesReference.document(qrCode1.getID()).collection("commentList")
                            .get()
                            .addOnSuccessListener(qrCode1Comments -> {
                                for (QueryDocumentSnapshot comment : qrCode1Comments) {
                                    if (comment.getData().get("username").equals("falseTestUser")) {
                                        assertEquals(testUsername, comment.getData().get("displayName"));
                                    } else if (comment.getData().get("username").equals(testUsername)) {
                                        assertEquals("testUserUnique", comment.getData().get("displayName"));
                                    }
                                }

                                qrCodesReference.document(qrCode2.getID()).collection("commentList")
                                        .get()
                                        .addOnSuccessListener(qrCode2Comments -> {
                                            for (QueryDocumentSnapshot comment : qrCode2Comments) {
                                                if (comment.getData().get("username").equals("falseTestUser")) {
                                                    assertEquals(testUsername, comment.getData().get("displayName"));
                                                } else if (comment.getData().get("username").equals(testUsername)) {
                                                    assertEquals("testUserUnique", comment.getData().get("displayName"));
                                                }
                                            }

                                            queryComplete.queryCompleteCheck(true);
                                        });
                            });
                });
    }

    /**
     * Checks if the given email was correctly changed
     */
    public void checkEmailChanged(String email, final QueryCallback queryComplete) {
        System.out.println(testUsername);
        System.out.println(Preference.getPrefsString("currentUserUsername", null));
        usersReference.document(testUsername)
                .get()
                .addOnSuccessListener(user -> {
                    assertEquals(user.get("email"), email);
                    queryComplete.queryCompleteCheck(true);
                });
    }

    /**
     * Create a mock QR Code
     *
     * @return QRCode object
     */
    private QRCode mockQRCode() {
        final int randomNum = new Random().nextInt(10000);
        QRCode qrCode = new QRCode("test" + randomNum);

        qrCodesReference.document(qrCode.getID()).set(qrCode);
        usersReference.document(testUsername).update("qrCodeHashes", FieldValue.arrayUnion(qrCode.getHash()));
        usersReference.document(testUsername).update("qrCodeIDs", FieldValue.arrayUnion(qrCode.getID()));

        return qrCode;
    }

    /**
     * Delete mock QR Code
     *
     * @param qrCode QRCode object to delete
     */
    private void deleteMockQRCode(QRCode qrCode) {
        qrCodesReference.document(qrCode.getID()).delete();
        usersReference.document(testUsername).update("qrCodeHashes", FieldValue.arrayRemove(qrCode.getHash()));
        usersReference.document(testUsername).update("qrCodeIDs", FieldValue.arrayRemove(qrCode.getID()));
    }
}