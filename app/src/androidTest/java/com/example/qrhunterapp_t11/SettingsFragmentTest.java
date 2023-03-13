package com.example.qrhunterapp_t11;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragmentTest {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private Solo solo;
    private boolean uniqueUser;
    private SharedPreferences prefs;

    @Rule
    public ActivityTestRule<MainActivity> rule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    /**
     * Runs before all tests and creates solo instance.
     *
     * @throws Exception
     */
    @Before
    public final void setUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        Activity activity = rule.getActivity();
        prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        //setPrefs();
    }

    /**
     * Runs after tests and clears SharedPreferences and removes user from database.
     */
    @After
    public final void cleanUp() {
        //prefs.edit().clear().commit();
        checkUniqueUsername("testUser", new checkUniqueUsernameCallback() {
            public void uniqueUsername(boolean unique) {

                if (!unique) {
                    usersReference.document("testUser").delete();
                }
            }
        });
    }

    /**
     * Test behaviour when attempting to leave username field blank
     */
    @Test
    public void testUsernameChangeBlank() {
        solo.clickOnView(solo.getView(R.id.settings));

        solo.clickOnView(solo.getView(R.id.username_edit_edittext));
        solo.clearEditText(0);
        solo.enterText(0, "");

        solo.clickOnView(solo.getView(R.id.settings_confirm_button));

        EditText usernameEditText = solo.getEditText(0);
        assertEquals("Field cannot be blank", usernameEditText.getError());

        solo.enterText(0, "testUser");
    }

    /**
     * Test behaviour when attempting to change to a taken username
     */
    @Test
    public void testUsernameChangeNotUnique() {
        solo.clickOnView(solo.getView(R.id.settings));

        solo.clickOnView(solo.getView(R.id.username_edit_edittext));
        solo.clearEditText(0);

        String testUser = "testUser";

        addTestUserToDB(testUser, new addTestUserToDBCallback() {
            @Override
            public void completedQuery(boolean complete) {
                assertTrue(complete);
                assertTrue(uniqueUser);

                solo.enterText(0, testUser);
                solo.clickOnView(solo.getView(R.id.settings_confirm_button));

                EditText usernameEditText = solo.getEditText(0);
                assertEquals("Username is not unique", usernameEditText.getError());
            }
        });
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

        checkUniqueUsername("testUserUnique", new checkUniqueUsernameCallback() {
            public void uniqueUsername(boolean unique) {
                uniqueUser = unique;
                assertTrue(uniqueUser);

                Map<String, Object> user = new HashMap<>();
                user.put("Username", "testUserUnique");
                user.put("Display Name", "testUserUnique");

                usersReference.document("testUserUnique").set(user);

                // Make sure user was successfully added
                checkUniqueUsername("testUserUnique", new checkUniqueUsernameCallback() {
                    public void uniqueUsername(boolean unique) {
                        assertFalse(unique);
                    }
                });
            }
        });
    }


    /**
     * Sets SharedPreferences strings for username and display name
     */
    public void setPrefs() {
        String username = prefs.getString("currentUser", null);
        String displayName = prefs.getString("currentUserDisplayName", null);
        assertNotNull(username);
        assertNotNull(displayName);
        prefs.edit().putString("currentUser", "testUser").commit();
        prefs.edit().putString("currentUserDisplayName", "testUser").commit();
        username = prefs.getString("currentUser", null);
        displayName = prefs.getString("currentUserDisplayName", null);
        assertEquals("testUser", username);
        assertEquals("testUser", displayName);
    }

    /**
     * Adds a test user to the database.
     * testUser should always be a new addition to the database
     */
    public void addTestUserToDB(String username, final addTestUserToDBCallback completedQuery) {
        checkUniqueUsername(username, new checkUniqueUsernameCallback() {
            public void uniqueUsername(boolean unique) {
                uniqueUser = unique;
                System.out.println("wooooooooooooooo " + unique);
                if (uniqueUser) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("Username", username);
                    user.put("Display Name", username);

                    usersReference.document(username).set(user);

                    // Make sure user was successfully added
                    checkUniqueUsername(username, new checkUniqueUsernameCallback() {
                        public void uniqueUsername(boolean unique) {
                            assertFalse(unique);
                            completedQuery.completedQuery(true);
                        }
                    });
                }
            }
        });
    }

    /**
     * Checks if the given username exists in the database.
     */
    public void checkUniqueUsername(String username, final checkUniqueUsernameCallback uniqueUsername) {
        DocumentReference usernameReference = usersReference.document(username);
        usernameReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        uniqueUsername.uniqueUsername(false);
                    } else {
                        uniqueUsername.uniqueUsername(true);
                    }
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
        void uniqueUsername(boolean unique);
    }

    /**
     * Callback for addTestUserToDB method
     *
     * @author Afra
     */
    public interface addTestUserToDBCallback {
        void completedQuery(boolean complete);
    }
}

