package com.example.qrhunterapp_t11;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SettingsFragmentTest {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private Solo solo;
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
    public void setUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
    }

    @Test
    public void testUsernameChange() {
        MainActivity activity = (MainActivity) solo.getCurrentActivity();
        prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        setPrefs();

        String username = prefs.getString("currentUser", null);
        String displayName = prefs.getString("currentUserDisplayName", null);

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
    public void addTestUserToDB() {


    }

    /**
     * Checks if the given username exists in the database.
     */
    public void checkUniqueUsername(String username, final testCallback uniqueUsername) {
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
     * Callback for querying database
     * @author Afra
     */
    public interface testCallback {
        void uniqueUsername(boolean valid);
    }
}

