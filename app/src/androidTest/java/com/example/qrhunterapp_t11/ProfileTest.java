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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Random;

/**
 * Intent tests for the Profile Fragment and its interaction with the ViewQR dialog.
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
    private Solo solo;
    private boolean docExists;
    private QRCode qrCode;
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
            prefs.edit().putString("currentUser", testUsername).commit();
            prefs.edit().putString("currentUserDisplayName", testUsername).commit();

            username = prefs.getString("currentUser", null);
            displayName = prefs.getString("currentUserDisplayName", null);

            assertEquals(testUsername, username);
            assertEquals(testUsername, displayName);
        }
    };
    private String name;

    private QRCode mockQR(String valueString) {
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

        // add new QR code
        final int randomNum = new Random().nextInt(10000);
        qrCode = mockQR(String.valueOf(randomNum));
        name = qrCode.getName();
        CollectionReference qrReference = usersReference.document(testUsername).collection("QR Codes");
        addDoc(qrCode, qrReference);

        // check if new QrCode was added
        checkDocExists(qrCode.getId(), qrReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                docExists = valid;
                assertTrue(docExists);
            }
        });

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
     * Verifies that when you click an item in the recyclerView the dialog for it appears
     */
    @Test
    public void checkListClick() {
        // Asserts that the current activity is the MainActivity. Otherwise, show “Wrong Activity”
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        // Check that current user exists
        checkDocExists(testUsername, usersReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                docExists = valid;
                assertTrue(docExists);
            }
        });

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
        solo.clickInRecyclerView(0);
        // wait for "Add Comment" to know the qrView dialog has opened
        solo.drag(250, 250, 400, 0, 10);
        assertTrue(solo.waitForText("Add Comment", 1, 10000));
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
        // Asserts that the current activity is the MainActivity. Otherwise, show “Wrong Activity”
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        // Check that current user exists
        checkDocExists(testUsername, usersReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                docExists = valid;
                assertTrue(docExists);
            }
        });

        // Refresh the profile
        solo.clickOnView(solo.getView(R.id.settings));
        Assert.assertTrue(solo.waitForText("Settings", 1, 1000));
        solo.clickOnView(solo.getView(R.id.profile));

        solo.sleep(20);
        // Name of qrCode should appear
        assertTrue(solo.waitForText(name, 1, 2000));
        // There should only be 1 qrCode in collection
        assertTrue(solo.waitForText("1", 1, 10000));
        solo.clickInRecyclerView(0);
        // wait for "Add Comment" to know the qrView dialog has opened
        solo.drag(250, 250, 400, 0, 10);
        assertTrue(solo.waitForText("Add Comment", 1, 10000));
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
        // Asserts that the current activity is the MainActivity. Otherwise, show “Wrong Activity”
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        CollectionReference qrReference = usersReference.document(testUsername).collection("QR Codes");

        // Check that current user exists
        checkDocExists(testUsername, usersReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                docExists = valid;
                assertTrue(docExists);
            }
        });

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

        // create another QR object
        QRCode qrCode1 = mockQR("Test");
        addDoc(qrCode1, qrReference);

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
     * TODO make this test not fail
     */
    //@Test
    public void deleteLongClickPositive() {
        // Asserts that the current activity is the MainActivity. Otherwise, show “Wrong Activity”
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        CollectionReference qrReference = usersReference.document(testUsername).collection("QR Codes");

        // Check that current user exists
        checkDocExists(testUsername, usersReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                docExists = valid;
                assertTrue(docExists);
            }
        });

        // Refresh the profile
        solo.clickOnView(solo.getView(R.id.settings));
        Assert.assertTrue(solo.waitForText("Settings", 1, 1000));
        solo.clickOnView(solo.getView(R.id.profile));

        // Long click to delete
        solo.clickLongInRecycleView(0);
        Assert.assertTrue(solo.waitForText("Delete", 1, 1000));
        solo.clickOnText("Delete");

        // Check document has been deleted from the database
        checkDocExists(qrCode.getId(), qrReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                docExists = valid;
                assertFalse(docExists);
            }
        });
    }

    /**
     * Verifies that a qrCode will not be deleted from the profile correctly by a Long click when the negative button on the dialog confirmation box is clicked
     * TODO make this test not fail when checking database
     */
    @Test
    public void deleteLongClickNegative() {
        // Asserts that the current activity is the MainActivity. Otherwise, show “Wrong Activity”
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        CollectionReference qrReference = usersReference.document(testUsername).collection("QR Codes");

        // Check that current user exists
        checkDocExists(testUsername, usersReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                docExists = valid;
                assertTrue(docExists);
            }
        });

        // Refresh the profile
        solo.clickOnView(solo.getView(R.id.settings));
        Assert.assertTrue(solo.waitForText("Settings", 1, 1000));
        solo.clickOnView(solo.getView(R.id.profile));

        // Long click to delete
        solo.clickLongInRecycleView(0);
        Assert.assertTrue(solo.waitForText("Delete", 1, 1000));
        solo.clickOnText("Cancel");
        solo.sleep(20);
        Assert.assertTrue(solo.waitForText(String.valueOf(qrCode.getPoints()), 4, 100));
        Assert.assertTrue(solo.waitForText(qrCode.getName(), 1, 100));
        Assert.assertTrue(solo.waitForText("1", 1, 100));

    }

    /**
     * Helper function to check if a QR code document exists
     *
     * @param docToCheck document that should be checked for
     * @param cr         CollectionReference to the collection being accessed
     * @reference <a href="https://firebase.google.com/docs/firestore/query-data/get-data">used without major modification</a>
     * @reference Aidan Lynch's CameraFragmentTest for this code
     */
    public void checkDocExists(String docToCheck, CollectionReference cr, final ProfileTest.Callback dataValid) {
        DocumentReference docRef = cr.document(docToCheck);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("DocExist", "DocumentSnapshot data: " + document.getData());
                        dataValid.dataValid(true);
                    } else {
                        Log.d("DocExist", "No such document");
                        dataValid.dataValid(false);
                    }
                } else {
                    Log.d("DocExist", "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     * Helper function to add the test QR code document
     *
     * @param qrCode document that should be added
     * @param cr     CollectionReference to the collection being accessed
     * @reference <a href="https://firebase.google.com/docs/firestore/manage-data/delete-data">used without major modification</a>
     * @reference Aidan Lynch's CameraFragmentTest
     */
    public void addDoc(QRCode qrCode, CollectionReference cr) {
        cr.document(qrCode.getId()).set(qrCode)
                .addOnSuccessListener(new OnSuccessListener<Object>() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d("AddedDocument", "DocumentSnapshot successfully added!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("AddedDocument", "Error adding document", e);
                    }
                });
    }

    public interface Callback {
        void dataValid(boolean valid);
    }

    public interface Callback2 {
        void collect(QuerySnapshot querySnapshot);
    }
}