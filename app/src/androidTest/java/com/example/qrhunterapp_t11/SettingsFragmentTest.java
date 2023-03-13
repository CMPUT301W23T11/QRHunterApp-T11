package com.example.qrhunterapp_t11;

import static org.junit.Assert.assertNull;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

public class SettingsFragmentTest {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private Solo solo;
    private SharedPreferences prefs;
    String username;

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

    /**
     * Gets the Activity
     *
     * @throws Exception
     */
    @Test
    public void start() throws Exception {
        Activity activity = rule.getActivity();
    }

    @Test
    public void checkList() {
        // Asserts that the current activity is the MainActivity
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        MainActivity activity = (MainActivity) solo.getCurrentActivity();

        prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        username = prefs.getString("currentUser", null);

    }

    @Test
    public void testUserCreation() {
        username = prefs.getString("currentUser", null);
        String username = prefs.getString("currentUser", null);
        assertNull(username);
        prefs.edit().putString("currentUser", "testUser").commit();
        username = prefs.getString("currentUser", null);
        System.out.println(username);
    }
}

