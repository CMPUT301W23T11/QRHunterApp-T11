package com.example.qrhunterapp_t11;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static junit.framework.TestCase.assertTrue;

import static org.junit.Assert.assertEquals;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Intent tests for the Profile Fragment and its interaction with the ViewQR dialog.
 *
 * @author Sarah Thomson, Aidan Lynch
 * @reference code mostly repurposed from Aidan Lynch and lab 7.
 * @reference https://stackoverflow.com/questions/50035752/how-to-get-list-of-documents-from-a-collection-in-firestore-android for help retrieving all documents answer by Ivan Banha CC BY-SA 3.0.
 */
public class ProfileTest {
    private Solo solo;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");
    private boolean docExists;
    private QRCode qrCode;
    private SharedPreferences prefs;
    private final Random rand = new Random();
    private final String testUser = "testUser" + rand.nextInt(1000);

    private String name;

    private QRCode mockQR(String valueString) {
        return new QRCode(valueString);
    }

    public interface Callback {
        void dataValid(boolean valid);
    }

    public interface Callback2 {
        void collect(QuerySnapshot querySnapshot);
    }

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

        // add new QR code
        final int randomNum = new Random().nextInt(10000);
        qrCode = mockQR(String.valueOf(randomNum));
        name = qrCode.getName();
        CollectionReference qrReference = usersReference.document(testUser).collection("QR Codes");
        addDoc(qrCode, qrReference);

        // check if new QrCode was added
        checkDocExists(qrCode.getHash(), qrReference, new ProfileTest.Callback() {
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
        usersReference.document(testUser).delete();
        solo.finishOpenedActivities();
    }

    /**
     * Click an item in the recyclerView and check if dialog for it appears
     */
    @Test
    public void checkListClick() {
        // Asserts that the current activity is the MainActivity. Otherwise, show “Wrong Activity”
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        // Check that current user exists
        checkDocExists(testUser, usersReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                System.out.println("here");
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
        assertTrue(solo.waitForText("Add Comment", 1, 10000));
        // should display the qRCode name as a header
        assertTrue(solo.waitForText(name,1, 10000));
        // points should be displayed in dialog
        assertTrue(solo.waitForText(String.valueOf(qrCode.getPoints()),1, 10000));
        // click OK
        solo.clickOnText("OK");
    }

    @Test
    public void checkCommentAdd() {
        // Asserts that the current activity is the MainActivity. Otherwise, show “Wrong Activity”
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        // Check that current user exists
        checkDocExists(testUser, usersReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                System.out.println("here");
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
        assertTrue(solo.waitForText("Add Comment", 1, 10000));
        solo.enterText(0, "great catch king 😂");
        solo.clickOnView(solo.getView(R.id.imageViewSend));
        solo.enterText(0, "great catch king 😂");
        solo.clickOnView(solo.getView(R.id.imageViewSend));
        // Verify comments have been sent to the comment box
        assertTrue(solo.waitForText("great catch king 😂",2, 10000));
        // click OK
        solo.clickOnText("OK");

    }

    @Test
    public void checkPointAddition(){
        // Asserts that the current activity is the MainActivity. Otherwise, show “Wrong Activity”
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        CollectionReference qrReference = usersReference.document(testUser).collection("QR Codes");

        // Check that current user exists
        checkDocExists(testUser, usersReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                System.out.println("here");
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

        QRCode qrCode1 = mockQR("Test");
        addDoc(qrCode1, qrReference);
        solo.clickOnView(solo.getView(R.id.settings));
        Assert.assertTrue(solo.waitForText("Settings", 1, 1000));
        solo.clickOnView(solo.getView(R.id.profile));
        assertTrue(solo.waitForText(String.valueOf(qrCode1.getPoints() + qrCode.getPoints()), 1, 10000));
        if(qrCode1.getPoints() != qrCode.getPoints()){
            assertTrue(solo.waitForText(String.valueOf(Math.max(qrCode1.getPoints() , qrCode.getPoints())), 1, 10000));
            assertTrue(solo.waitForText(String.valueOf(Math.min(qrCode1.getPoints() , qrCode.getPoints())), 1, 10000));
        }

        //
        assertTrue(solo.waitForText("2", 1, 10000));
        // Click OK
        solo.clickOnText("OK");

    }

    /**
     * Helper function to check if a QR code document exists
     *
     * @param docToCheck document that should be checked for
     * @param cr         CollectionReference to the collection being accessed
     * @reference https://firebase.google.com/docs/firestore/query-data/get-data - used without major modification
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

    public void getAll(String docToCheck, CollectionReference cr, final ProfileTest.Callback2 collect) {
        cr
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            collect.collect(task.getResult());

                        } else {
                            Log.d("DocExist", "get failed with ", task.getException());
                        }
                    }
                });
    }


    /**
     * Helper function to delete the test QR code document
     *
     * @param docToDelete document that should be deleted
     * @param cr          CollectionReference to the collection being accessed
     * @reference https://firebase.google.com/docs/firestore/manage-data/delete-data - used without major modification
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
     * Helper function to add the test QR code document
     *
     * @param qrCode document that should be added
     * @param cr     CollectionReference to the collection being accessed
     * @reference https://firebase.google.com/docs/firestore/manage-data/delete-data - used without major modification
     * @reference Aidan Lynch's CameraFragmentTest
     */
    public void addDoc(QRCode qrCode, CollectionReference cr) {
        cr.document(qrCode.getHash()).set(qrCode)
                .addOnSuccessListener(new OnSuccessListener() {
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
}