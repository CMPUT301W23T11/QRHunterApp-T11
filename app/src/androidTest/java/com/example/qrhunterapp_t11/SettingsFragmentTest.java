package com.example.qrhunterapp_t11;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.qrhunterapp_t11.activities.MainActivity;
import com.example.qrhunterapp_t11.interfaces.QueryCallback;
import com.example.qrhunterapp_t11.objectclasses.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

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
    private final Random rand = new Random();
    private final String testUsername = "testUser" + rand.nextInt(1000000);
    private Solo solo;
    private String username;
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
            prefs.edit().putBoolean("loggedIn", true).commit();
            prefs.edit().putString("currentUserUsername", testUsername).commit();
            prefs.edit().putString("currentUserDisplayName", testUsername).commit();

            username = prefs.getString("currentUserUsername", null);
            displayName = prefs.getString("currentUserDisplayName", null);

            assertEquals(testUsername, username);
            assertEquals(testUsername, displayName);
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
        prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        usersReference.document(testUsername).delete();
        solo.finishOpenedActivities();
    }

    /**
     * Test behaviour when attempting to change to a taken username
     */
    @Test
    public void testUsernameChangeNotUnique() {
        String testUserDupe = "testUserDupe";
        addTestUserToDB(testUserDupe);

        solo.clickOnView(solo.getView(R.id.settings));
        solo.clickOnView(solo.getView(R.id.username_edit_edittext));
        solo.clearEditText(0);
        solo.enterText(0, testUserDupe);
        solo.clickOnView(solo.getView(R.id.change_username_button));

        EditText usernameEditText = solo.getEditText(0);
        assertEquals("Username is not unique", usernameEditText.getError());

        usersReference.document(testUserDupe).delete();
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
        usersReference.document(prefs.getString("currentUserUsername", null))
                .get()
                .addOnSuccessListener(user -> {
                    assertEquals(user.get("displayName"), username);
                    usersReference.document(testUserUnique).delete();
                });
    }

    /**
     * Adds a test user to the database.
     * testUser should always be a new addition to the database
     */
    public void addTestUserToDB(String username) {

        User user = new User(username, username, 0, 0, 0, "No email");

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
}