package com.example.qrhunterapp_t11;

import static junit.framework.TestCase.assertTrue;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import android.app.Activity;
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

/**
 * Intent tests for the Profile Fragment and its interaction with the ViewQR dialog.
 *
 * @author  Sarah Thomson
 *
 * @reference code mostly repurposed from Aiden Lynch and lab 7.
 */
public class ProfileTest {
    private Solo solo;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersReference = db.collection("Users");
    private boolean docexists;
    private QRCode qrCode;

    private QRCode mockQR(String valueString) {
        return new QRCode(valueString);
    }

    public interface Callback {
        void dataValid(boolean valid);
    }

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

    /**
     * Click an item in the recyclerView and check if dialog for it appears
     */
    @Test
    public void checkList() {
        // Asserts that the current activity is the MainActivity. Otherwise, show “Wrong Activity”
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        MainActivity activity = (MainActivity) solo.getCurrentActivity();
        qrCode = mockQR("Test this string");

        addDoc("usertest", usersReference);

        checkDocExists("usertest", usersReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertTrue(docexists);
            }
        });

        CollectionReference qrReference =  db.collection("Users").document("usertest").collection("QR Codes");

        addDoc(qrCode.getHash(), qrReference);

        checkDocExists("qrReference", usersReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertTrue(docexists);
            }
        });

        //solo.clickOnView(solo.getView(R.id.collectionRecyclerView));
        solo.clickInList(0);
    }

    /**
     * Helper function to check if a QR code document exists
     *
     * @param docToCheck document that should be checked for
     * @param cr CollectionReference to the collection being accessed
     * @reference https://firebase.google.com/docs/firestore/query-data/get-data - used without major modification
     * @reference Aiden Lynch's CameraFragmentTest for this code
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
     * @param docToAdd document that should be added
     * @param cr          CollectionReference to the collection being accessed
     * @reference https://firebase.google.com/docs/firestore/manage-data/delete-data - used without major modification
     * @reference Aiden Lynch's CameraFragmentTest
     */
    public void addDoc(String docToAdd, CollectionReference cr) {
        cr.document(docToAdd).set(docToAdd)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
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