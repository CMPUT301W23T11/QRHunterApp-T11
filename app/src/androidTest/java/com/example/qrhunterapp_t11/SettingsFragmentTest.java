package com.example.qrhunterapp_t11;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Test class for the settings fragment.
 *
 * @author Afra
 * @reference Aidan's CameraFragmentTest class
 */
public class SettingsFragmentTest {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private final Random rand = new Random();
    private final String testUser = "testUser" + rand.nextInt(1000);
    private Solo solo;
    private boolean uniqueUser;
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
     * Test behaviour when attempting to change to a taken username
     */
    @Test
    public void testUsernameChangeNotUnique() {
        String testUserDuplicate = "testUserDuplicate";
        addTestUserToDB(testUserDuplicate);

        solo.clickOnView(solo.getView(R.id.settings));
        solo.clickOnView(solo.getView(R.id.username_edit_edittext));
        solo.clearEditText(0);
        solo.enterText(0, testUserDuplicate);
        solo.clickOnView(solo.getView(R.id.settings_confirm_button));

        EditText usernameEditText = solo.getEditText(0);
        assertEquals("Username is not unique", usernameEditText.getError());

        usersReference.document("testUserDuplicate").delete();
    }

    /**
     * Test behaviour when attempting to change to a unique username
     */
    @Test
    public void testUsernameChange() {
        solo.clickOnView(solo.getView(R.id.settings));

        solo.clickOnView(solo.getView(R.id.username_edit_edittext));
        solo.clearEditText(0);

        solo.enterText(0, "testUserUnique");
        solo.clickOnView(solo.getView(R.id.settings_confirm_button));
        solo.clickOnText("Confirm");

        checkUniqueDisplayName("testUserUnique", new checkUniqueUsernameCallback() {
            public void uniqueDisplayName(boolean unique) {
                uniqueUser = unique;
                assertTrue(uniqueUser);

                usersReference.document(testUser).update("Display Name", "testUserUnique");

                // Make sure user was successfully added
                checkUniqueDisplayName("testUserUnique", new checkUniqueUsernameCallback() {
                    public void uniqueDisplayName(boolean unique) {
                        assertTrue(unique);
                    }
                });
            }
        });
        usersReference.document("testUserUnique").delete();
    }

    /**
     * Adds a test user to the database.
     * testUser should always be a new addition to the database
     */
    public void addTestUserToDB(String username) {

        Map<String, Object> user = new HashMap<>();
        user.put("Username", username);
        user.put("Display Name", username);

        usersReference.document(username).set(user);

    }

    /**
     * Checks if the given username exists in the database.
     */
    public void checkUniqueDisplayName(String username, final checkUniqueUsernameCallback uniqueUsername) {
        usersReference.whereEqualTo("Display Name", username).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    uniqueUsername.uniqueDisplayName(task.getResult().isEmpty());
                }
            }
        });
    }

    /**
     * Callback for checkUniqueUsername query
     *
     * @author Afra
     */
    public interface checkUniqueUsernameCallback {
        void uniqueDisplayName(boolean unique);
    }
}

