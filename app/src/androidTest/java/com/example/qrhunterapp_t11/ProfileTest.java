package com.example.qrhunterapp_t11;

import static junit.framework.TestCase.assertTrue;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.robotium.solo.Solo;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

/**
 * Intent tests for the Profile Fragment and its interaction with the ViewQR dialog.
 *
 * @author  Sarah Thomson, Aiden Lynch
 *
 * @reference code mostly repurposed from Aiden Lynch and lab 7.
 * @reference https://stackoverflow.com/questions/50035752/how-to-get-list-of-documents-from-a-collection-in-firestore-android for help retrieving all documents answer by Ivan Banha CC BY-SA 3.0.
 */
public class ProfileTest {
    private Solo solo;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersReference = db.collection("Users");
    private boolean docexists;
    private QRCode qrCode;
    ArrayList<String> docs = new ArrayList<>();


    private SharedPreferences prefs;

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
    public void checkListClick() {
        // Asserts that the current activity is the MainActivity. Otherwise, show “Wrong Activity”
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        Activity activity = rule.getActivity();
        prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String currentUser = prefs.getString("currentUser", null);
        CollectionReference qrReference = db.collection("Users").document(currentUser).collection("QR Codes");
        final int randomNum = new Random().nextInt(10000);
        qrCode = mockQR(String.valueOf(randomNum));
        String name = qrCode.getName();

        // Check that current user exists
        checkDocExists(currentUser, usersReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertTrue(docexists);
            }
        });

        // Delete all of the user's current qrCodes
        getAll(qrCode.getHash(), qrReference, new Callback2() {
            @Override
            public void collect(QuerySnapshot querySnapshot) {
                for (DocumentSnapshot documentSnapshot : querySnapshot){
                    deleteDoc(documentSnapshot.getId(), qrReference);
                }
            }

        });

        // add new QR code
        addDoc(qrCode, qrReference);

        // check if new QrCode was added
        checkDocExists( qrCode.getHash(), qrReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertTrue(docexists);
            }
        });

        //Name of qrCode should appear
        assertTrue(solo.waitForText(name,1, 10000));
        //Should only be 1 qrCode in collection
        assertTrue(solo.waitForText("1",1, 10000));
        //points should appear 1. in recycler view 2. in highest score QR 3. in lowest score QR 4. total points
        assertTrue(solo.waitForText(String.valueOf(qrCode.getPoints()),4, 10000));
        solo.clickInRecyclerView(0);
        // wait for "Add Comment" to know the qrView dialog has opened
        assertTrue(solo.waitForText("Add Comment",1, 10000));
        // should display the qRCode name as a header
        //assertTrue(solo.waitForText(name,1, 10000));
        // points should be displayed in dialog
        //assertTrue(solo.waitForText(String.valueOf(qrCode.getPoints()),1, 10000));
        /*
        For some reason robotium does not recognize the positive button on the dialog
         */
        //solo.clickOnView(solo.getView(android.R.id.button1));
        //
        //solo.clickOnButton("OK");
        //solo.clickOnText("OK");
        //Thread.sleep(250);
        //solo.goBack();
        //solo.clickOnView(solo.getButton("OK"));
        //assertTrue(solo.waitForText("Highest",1, 10000));
    }

    //@Test
    public void checkCommentAdd() {

        // Asserts that the current activity is the MainActivity. Otherwise, show “Wrong Activity”
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        Activity activity = rule.getActivity();
        prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String currentUser = prefs.getString("currentUser", null);
        CollectionReference qrReference = db.collection("Users").document(currentUser).collection("QR Codes");
        final int randomNum = new Random().nextInt(10000);
        qrCode = mockQR(String.valueOf(randomNum));
        String name = qrCode.getName();

        // Check that current user exists
        checkDocExists(currentUser, usersReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertTrue(docexists);
            }
        });

        // Delete all of the user's current qrCodes
        getAll(qrCode.getHash(), qrReference, new Callback2() {
            @Override
            public void collect(QuerySnapshot querySnapshot) {
                for (DocumentSnapshot documentSnapshot : querySnapshot){
                    deleteDoc(documentSnapshot.getId(), qrReference);
                }
            }

        });

        // add new QR code
        addDoc(qrCode, qrReference);

        // check if new QrCode was added
        checkDocExists( qrCode.getHash(), qrReference, new ProfileTest.Callback() {
            public void dataValid(boolean valid) {
                docexists = valid;
                assertTrue(docexists);
            }
        });

        solo.clickInRecyclerView(0);
        assertTrue(solo.waitForText("Add Comment",1, 10000));

        solo.enterText(0, "hello");













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

    public void getAll(String  docToCheck, CollectionReference cr, final ProfileTest.Callback2 collect){
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
     * @param cr          CollectionReference to the collection being accessed
     * @reference https://firebase.google.com/docs/firestore/manage-data/delete-data - used without major modification
     * @reference Aiden Lynch's CameraFragmentTest
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