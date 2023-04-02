package com.example.qrhunterapp_t11;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.qrhunterapp_t11.activities.MainActivity;
import com.example.qrhunterapp_t11.fragments.FirebaseQueryAssistant;
import com.example.qrhunterapp_t11.interfaces.QueryCallback;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.example.qrhunterapp_t11.objectclasses.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Random;

/**
 * Intent tests for the camera fragment.
 *
 * @author Aidan Lynch - writing the tests.
 * @author Afra - set up querying information from the database, setUp(), tearDown(), ActivityTestRule initialization.
 * @sources code mostly repurposed from lab 7.
 */
public class CameraFragmentTest {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private final CollectionReference qrCodesReference = db.collection("QRCodes");
    private final Random rand = new Random();
    private final String testUsername = "testUser" + rand.nextInt(1000000);
    private Solo solo;
    private boolean docExists;
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
        prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        usersReference.document(testUsername).delete();
        solo.finishOpenedActivities();
    }

    /**
     * test isSameLocation function
     */
    @Test
    public void testIsSameLocation() {
        QRCode qrCode1 = mockQRCode("QR 1");
        QRCode qrCode2 = mockQRCode("QR 2");
        FirebaseQueryAssistant firebaseQueryAssistant = new FirebaseQueryAssistant(db);


        // initial case, both coordinates null, small threshold 30m
        Assert.assertEquals(true, FirebaseQueryAssistant.isSameLocation(qrCode1, qrCode2, 30));

        // initial case, both coordinates null, big threshold 5km
        Assert.assertEquals(true, FirebaseQueryAssistant.isSameLocation(qrCode1, qrCode2, 5000));

        // test exact same location small threshold 30m
        qrCode1.setLatitude(37.4219983);
        qrCode1.setLongitude(-122.084);
        qrCode2.setLatitude(37.4219983);
        qrCode2.setLongitude(-122.084);
        Assert.assertEquals(true, FirebaseQueryAssistant.isSameLocation(qrCode1, qrCode2, 30));

        // test exact same location big threshold 5km
        qrCode1.setLatitude(37.4219983);
        qrCode1.setLongitude(-122.084);
        qrCode2.setLatitude(37.4219983);
        qrCode2.setLongitude(-122.084);
        Assert.assertEquals(true, FirebaseQueryAssistant.isSameLocation(qrCode1, qrCode2, 5000));

        // test close points, within a small distance threshold 30m
        qrCode1.setLatitude(53.439966);
        qrCode1.setLongitude(-113.567222);
        qrCode2.setLatitude(53.439966);
        qrCode2.setLongitude(-113.567122);
        Assert.assertEquals(true, FirebaseQueryAssistant.isSameLocation(qrCode1, qrCode2, 30));

        // test close points, outside a small distance threshold 30m
        qrCode1.setLatitude(53.439966);
        qrCode1.setLongitude(-113.567222);
        qrCode2.setLatitude(53.439966);
        qrCode2.setLongitude(-113.569222);
        Assert.assertEquals(false, FirebaseQueryAssistant.isSameLocation(qrCode1, qrCode2, 30));

        // test close points within a big distance threshold 5km
        qrCode1.setLatitude(53.439966);
        qrCode1.setLongitude(-113.567222);
        qrCode2.setLatitude(53.439966);
        qrCode2.setLongitude(-113.567122);
        Assert.assertEquals(true, FirebaseQueryAssistant.isSameLocation(qrCode1, qrCode2, 5000));

        // test far points, outside a small distance threshold 30m
        qrCode1.setLatitude(53.439966);
        qrCode1.setLongitude(-113.567222);
        qrCode2.setLatitude(38.897957);
        qrCode2.setLongitude(-77.036560);
        Assert.assertEquals(false, FirebaseQueryAssistant.isSameLocation(qrCode1, qrCode2, 30));

        // test far points within a big distance threshold 5km
        qrCode1.setLatitude(53.439966);
        qrCode1.setLongitude(-113.567222);
        qrCode2.setLatitude(53.449966);
        qrCode2.setLongitude(-113.567122);
        Assert.assertEquals(true, FirebaseQueryAssistant.isSameLocation(qrCode1, qrCode2, 5000));

        // test far points outside a big distance threshold 5km
        qrCode1.setLatitude(53.439966);
        qrCode1.setLongitude(-113.567222);
        qrCode2.setLatitude(38.897957);
        qrCode2.setLongitude(-77.036560);
        Assert.assertEquals(false, FirebaseQueryAssistant.isSameLocation(qrCode1, qrCode2, 5000));

        // test a 0 distance threshold
        qrCode1.setLatitude(53.439966);
        qrCode1.setLongitude(-113.567222);
        qrCode2.setLatitude(38.897957);
        qrCode2.setLongitude(-77.036560);
        Assert.assertEquals(false, FirebaseQueryAssistant.isSameLocation(qrCode1, qrCode2, 0));

    }

    /**
     * Attempt to scan a QR code, take a photo, and share location.
     */
    @Test
    public void takePhotoShareLocation() {
        String testHash = "8227ad036b504e39fe29393ce170908be2b1ea636554488fa86de5d9d6cd2c32";

        deleteDoc(testHash, qrCodesReference); // delete the test doc to see change in collection size

        checkDocExists(testHash, qrCodesReference, new QueryCallback() {
            public void queryCompleteCheck(boolean docExists) {
                assertFalse(docExists);
            }
        });

        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnText("Yes");
        solo.clickOnView(solo.getView(R.id.captureButton));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnText("Yes");
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page

        checkDocExists(testHash, qrCodesReference, new QueryCallback() {
            public void queryCompleteCheck(boolean docExists) {
                assertTrue(docExists);
            }
        });
    }

    //TODO add start function that accepts initial prompts or something, hopefully that is enough
    //TODO initial QR setting for camera?

    /**
     * Attempt to scan a QR code, take a photo, and reject location.
     */
    @Test
    public void takePhotoRejectLocation() {
        String testHash = "8227ad036b504e39fe29393ce170908be2b1ea636554488fa86de5d9d6cd2c32";

        deleteDoc(testHash, qrCodesReference); // delete the test doc to see change in collection size

        checkDocExists(testHash, qrCodesReference, new QueryCallback() {
            public void queryCompleteCheck(boolean docExists) {
                assertFalse(docExists);

            }
        });

        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnText("Yes");
        solo.clickOnView(solo.getView(R.id.captureButton));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnText("No");
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page

        checkDocExists(testHash, qrCodesReference, new QueryCallback() {
            public void queryCompleteCheck(boolean docExists) {
                assertTrue(docExists);
            }
        });
    }

    /**
     * Attempt to scan a QR code, reject a photo, and accept location.
     */
    @Test
    public void rejectPhotoAcceptLocation() {
        String testHash = "8227ad036b504e39fe29393ce170908be2b1ea636554488fa86de5d9d6cd2c32";

        deleteDoc(testHash, qrCodesReference); // delete the test doc to see change in collection size

        checkDocExists(testHash, qrCodesReference, new QueryCallback() {
            public void queryCompleteCheck(boolean docExists) {
                assertFalse(docExists);
            }
        });


        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnText("No");
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnText("Yes");
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page

        checkDocExists(testHash, qrCodesReference, new QueryCallback() {
            public void queryCompleteCheck(boolean docExists) {
                assertTrue(docExists);
            }
        });
    }

    /**
     * Attempt to scan a QR code, reject photo, and reject location.
     */
    @Test
    public void rejectPhotoRejectLocation() {
        String testHash = "8227ad036b504e39fe29393ce170908be2b1ea636554488fa86de5d9d6cd2c32";

        deleteDoc(testHash, qrCodesReference); // delete the test doc to see change in collection size

        checkDocExists(testHash, qrCodesReference, new QueryCallback() {
            public void queryCompleteCheck(boolean docExists) {
                assertFalse(docExists);
            }
        });


        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Take Photo", 1, 10000)); // wait 7 sec for photo prompt to appear
        solo.clickOnText("No");
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnText("No");
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page

        checkDocExists(testHash, qrCodesReference, new QueryCallback() {
            public void queryCompleteCheck(boolean docExists) {
                assertTrue(docExists);
            }
        });
    }

    /**
     * Helper function to delete the test QR code document
     *
     * @param docToDelete document that should be deleted
     * @param cr          CollectionReference to the collection being accessed
     * @reference <a href="https://firebase.google.com/docs/firestore/manage-data/delete-data">used without major modification</a>
     */
    public void deleteDoc(String docToDelete, CollectionReference cr) {
        cr.document(docToDelete)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DeleteDocument", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DeleteDocument", "Error deleting document", e);
                    }
                });
    }

    /**
     * Helper function to check if a QR code document exists
     *
     * @param docToCheck document that should be checked for
     * @param cr         CollectionReference to the collection being accessed
     * @reference <a href="https://firebase.google.com/docs/firestore/query-data/get-data">used without major modification</a>
     */
    public void checkDocExists(String docToCheck, CollectionReference cr, final QueryCallback docExists) {
        DocumentReference docRef = cr.document(docToCheck);
        docRef.get().addOnSuccessListener(result -> {

            if (result.exists()) {
                Log.d("DocExist", "DocumentSnapshot data: " + result.getData());
                docExists.queryCompleteCheck(true);
            } else {
                Log.d("DocExist", "No such document");
                docExists.queryCompleteCheck(false);
            }

        });
    }
}
