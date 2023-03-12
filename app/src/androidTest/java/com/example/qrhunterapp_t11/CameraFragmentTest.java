package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Intent tests for the camera fragment.
 *
 * @author Aidan Lynch - writing the tests.
 * @author afra - set up querying information from the database.
 *
 * @reference code mostly repurposed from lab 7.
 */
public class CameraFragmentTest {
    public interface Callback {
        void dataValid(boolean valid);
    }

    private Solo solo;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference QRCodesReference = db.collection("QRCodes");
    private boolean docexists;

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

    //TODO add start function that accepts initial prompts or something, hopefully that is enough
    //TODO initial QR setting for camera?

    /**
     * Attempt to scan a QR code, take a photo, and share location.
     */
    @Test
    public void takePhotoShareLocation() {
        String testHash = "8227ad036b504e39fe29393ce170908be2b1ea636554488fa86de5d9d6cd2c32";

        deleteDoc(testHash, QRCodesReference); // delete the test doc to see change in collection size

        checkDocExists(testHash, QRCodesReference, new Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertFalse(docexists);

            }
        });


        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Take Photo", 1, 7000)); // wait 7 sec for photo prompt to appear
        solo.clickOnText("Yes");
        solo.clickOnView(solo.getView(R.id.captureButton));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnText("Yes");
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page

        checkDocExists(testHash, QRCodesReference, new Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertTrue(docexists);
            }
        });



    }

    /**
     * Attempt to scan a QR code, take a photo, and reject location.
     */
    @Test
    public void takePhotoRejectLocation() {
        String testHash = "8227ad036b504e39fe29393ce170908be2b1ea636554488fa86de5d9d6cd2c32";

        deleteDoc(testHash, QRCodesReference); // delete the test doc to see change in collection size

        checkDocExists(testHash, QRCodesReference, new Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertFalse(docexists);

            }
        });

        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Take Photo", 1, 7000)); // wait 7 sec for photo prompt to appear
        solo.clickOnText("Yes");
        solo.clickOnView(solo.getView(R.id.captureButton));
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnText("No");
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page

        checkDocExists(testHash, QRCodesReference, new Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertTrue(docexists);
            }
        });
    }

    /**
     * Attempt to scan a QR code, reject a photo, and accept location.
     */
    @Test
    public void rejectPhotoAcceptLocation() {
        String testHash = "8227ad036b504e39fe29393ce170908be2b1ea636554488fa86de5d9d6cd2c32";

        deleteDoc(testHash, QRCodesReference); // delete the test doc to see change in collection size

        checkDocExists(testHash, QRCodesReference, new Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertFalse(docexists);

            }
        });


        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Take Photo", 1, 7000)); // wait 7 sec for photo prompt to appear
        solo.clickOnText("No");
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnText("Yes");
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page

        checkDocExists(testHash, QRCodesReference, new Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertTrue(docexists);
            }
        });
    }

    /**
     * Attempt to scan a QR code, reject photo, and reject location.
     */
    @Test
    public void rejectPhotoRejectLocation() {
        String testHash = "8227ad036b504e39fe29393ce170908be2b1ea636554488fa86de5d9d6cd2c32";

        deleteDoc(testHash, QRCodesReference); // delete the test doc to see change in collection size

        checkDocExists(testHash, QRCodesReference, new Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertFalse(docexists);

            }
        });


        solo.clickOnView(solo.getView(R.id.addFab));
        assertTrue(solo.waitForText("Take Photo", 1, 7000)); // wait 7 sec for photo prompt to appear
        solo.clickOnText("No");
        assertTrue(solo.waitForText("Share Geolocation", 1, 7000)); // wait 7 sec for location prompt to appear
        solo.clickOnText("No");
        assertTrue(solo.waitForText("STATS", 1, 7000)); // confirm we're back on the profile page

        checkDocExists(testHash, QRCodesReference, new Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertTrue(docexists);
            }
        });
    }

    /**
     * Closes the activity after each test
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    /**
     * Helper function to delete the test QR code document
     *
     * @param docToDelete document that should be deleted
     * @param cr CollectionReference to the collection being accessed
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
     * Helper function to check if a QR code document exists
     *
     * @param docToCheck document that should be checked for
     * @param cr CollectionReference to the collection being accessed
     * @reference https://firebase.google.com/docs/firestore/query-data/get-data - used without major modification
     */
    public void checkDocExists(String docToCheck, CollectionReference cr, final Callback dataValid) {
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

}

