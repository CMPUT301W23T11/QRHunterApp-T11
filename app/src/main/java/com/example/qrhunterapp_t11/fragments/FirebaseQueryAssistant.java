package com.example.qrhunterapp_t11.fragments;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.qrhunterapp_t11.interfaces.QueryCallback;
import com.example.qrhunterapp_t11.interfaces.QueryCallbackWithQRCode;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Assistant class for common queries to the firestore database
 *
 * @author Everyone
 */

public class FirebaseQueryAssistant {
    private final CollectionReference qrCodesReference;
    private final CollectionReference usersReference;
    private final FirebaseFirestore db;


    /**
     * Constructor takes the db as argument
     *
     * @param db
     */
    public FirebaseQueryAssistant(@NonNull FirebaseFirestore db) {
        this.db = db;
        this.qrCodesReference = db.collection("QRCodes");
        this.usersReference = db.collection("Users");
    }

    /**
     * This function calculates the distance between two QRCode locations
     * <p>
     * In the context of a freshly scanned QRCode, if the hash function of the new code
     * matches the hash of a QRCode already in the db, this function determines if they should
     * be considered unique objects or the same QRcode (sharing comments, photos etc...)
     * if the function returns true using the new QRCode and the QRCode object already in the database,
     * no new document will be inserted (user profile will reference pre-existing QRCode), otherwise
     * a new entry will be created
     *
     * @param qrCode1     First QRCode
     * @param qrCode2     Second QRCode
     * @param givenRadius Double - the maximum distance allowed between the two points IN METERS
     * @return true if distance shorter than uniqueness threshold, else false if 2 separate instances
     * @sources <a href="https://developer.android.com/reference/android/location/Location">Getting distance between locations</a>
     */

    public static boolean qrCodesWithinRadius(@Nullable QRCode qrCode1, @Nullable QRCode qrCode2, double givenRadius) {

        // Input validation

        // Hashes are same, no location data for either, treat as same QRCode object
        if ((qrCode1.getLatitude() == null) && (qrCode2.getLatitude() == null)) {
            return true;
            // at least one of the qrs is null but not both, treat as separate objects
        } else if ((qrCode1.getLatitude() == null) || (qrCode2.getLatitude() == null)) {
            return false;
        }
        double lat1 = qrCode1.getLatitude();
        double lng1 = qrCode1.getLongitude();
        double lat2 = qrCode2.getLatitude();
        double lng2 = qrCode2.getLongitude();

        Location qrCode1Location = new Location("");
        Location qrCode2Location = new Location("");

        qrCode1Location.setLatitude(lat1);
        qrCode1Location.setLongitude(lng1);
        qrCode2Location.setLatitude(lat2);
        qrCode2Location.setLongitude(lng2);

        double distance = qrCode1Location.distanceTo(qrCode2Location);

        if (distance <= givenRadius) {
            System.out.printf("Same\n");
            return true;
        } else {
            System.out.printf("Different\n");
            return false;
        }
    }

    /**
     * Query database to check if QR code has any comments or not
     *
     * @param qrCodeID    QR to check for comments
     * @param hasComments Callback function
     */
    public void hasCommentsCheck(@NonNull String qrCodeID, final @NonNull QueryCallback hasComments) {

        qrCodesReference.document(qrCodeID).collection("commentList")
                .get()
                .addOnSuccessListener(qrCodeCommentList ->
                        hasComments.queryCompleteCheck(!qrCodeCommentList.isEmpty())
                );
    }

    /**
     * Query database to check if user has any QR codes in their collection or not
     *
     * @param username Current user's username
     * @param hasCodes Callback function
     */
    public void hasQRCodesCheck(@NonNull String username, final @NonNull QueryCallback hasCodes) {

        usersReference.document(username)
                .get()
                .addOnSuccessListener(user -> {
                    System.out.println(username);
                    if (user.exists()) {
                        ArrayList<String> userQRHashes = (ArrayList<String>) user.get("qrCodeHashes");
                        hasCodes.queryCompleteCheck(!userQRHashes.isEmpty());
                    }
                });
    }

    /**
     * Helper function to check if a user has a QR Code in their collection with the same hash as qr param
     *
     * @param qrInput  QR Code that is having its hash value checked
     * @param username User whose collection is being checked
     */

    public void checkUserHasHash(@NonNull QRCode qrInput, @NonNull String username, final @NonNull QueryCallbackWithQRCode docExists) {

        qrCodesReference
                .whereEqualTo("hash", qrInput.getHash())
                .get()
                .addOnSuccessListener(matchingQRCodes -> {
                    if (matchingQRCodes.isEmpty()) {
                        docExists.queryCompleteCheckObject(false, null);
                        return;
                    } else {
                        for (QueryDocumentSnapshot qrCodeDocument : matchingQRCodes) {
                            qrCodesReference.document(qrCodeDocument.getId()).collection("In Collection")
                                    .whereEqualTo("username", username)
                                    .get()
                                    .addOnSuccessListener(matchingUsers -> {
                                        if (!matchingUsers.isEmpty()) {
                                            QRCode qrCode = qrCodeDocument.toObject(QRCode.class);
                                            docExists.queryCompleteCheckObject(true, qrCode);
                                            return;
                                        } else {
                                            docExists.queryCompleteCheckObject(false, null);
                                            return;
                                        }
                                    });
                            break;
                        }
                    }
                });

    }

    /**
     * Check if the given user has the given QR Code
     *
     * @param username      Username to check
     * @param qrCodeID      QR Code to check
     * @param userHasQRCode Callback for query
     * @sources <a href="https://firebase.google.com/docs/firestore/query-data/get-data">used without major modification</a>
     */
    public void checkUserHasQR(@NonNull String qrCodeID, @NonNull String username, final @NonNull QueryCallback userHasQRCode) {
        qrCodesReference.document(qrCodeID)
                .get()
                .addOnSuccessListener(qrCode -> {
                            ArrayList<String> users = (ArrayList<String>) qrCode.get("inCollection");
                            userHasQRCode.queryCompleteCheck(users.contains(username));
                        }
                );
    }

    /**
     * @param qrCode       QR code to find matches of in db
     * @param qrCodeExists Query callback
     * @sources <a href="https://firebase.google.com/docs/firestore/query-data/queries#java_6">Firestore documentation</a>
     */
    public void checkQRCodeExists(@NonNull QRCode qrCode, double MAX_RADIUS, final @NonNull QueryCallbackWithQRCode qrCodeExists) {
        String hashValue = qrCode.getHash();

        qrCodesReference
                .whereEqualTo("hash", hashValue)
                .get()
                .addOnSuccessListener(matchingQRCodes -> {

                    QRCode dbQR = null;
                    boolean isSame = false;
                    for (QueryDocumentSnapshot document : matchingQRCodes) {
                        Log.d("QRExist", document.getId() + " => " + document.getData());
                        dbQR = document.toObject(QRCode.class);      // rebuilds a QRCode object from db information
                        isSame = qrCodesWithinRadius(qrCode, dbQR, MAX_RADIUS);
                        if (isSame) {                             // locations within threshold, treat as same qr, break from loop
                            String qrCodeID = dbQR.getID();
                            //queryCompleteCheck.queryCompleteCheck(true);
                            Log.d("QRExist", "locations close enough, count as equal object");
                            break;
                        }
                        Log.d("QRExists", "location distance too far, not a match");
                    }
                    if (!isSame) {       // no matches in db within distance threshold
                        Log.d("QRExists", "no matches within distance, create a new object");
                    }
                    qrCodeExists.queryCompleteCheckObject(isSame, dbQR);

                });
    }

    /**
     * Adds a QR Code to the database if it does not exist, or gives a reference of it to the user by username.
     *
     * @param qrCode          QR code to find matches of in db
     * @param username        Username the qrCode is being added to
     * @param resizedImageUrl URL of image the user took of qrCode
     * @param radius          Maximum radius for two codes to be considered the same object (meters)
     * @sources <a href="https://firebase.google.com/docs/firestore/query-data/queries#java_6">Firestore documentation</a>
     */
    public void addQR(@NonNull String username, @NonNull QRCode qrCode, @Nullable String resizedImageUrl, double radius) {
        String qrCodeID = qrCode.getID();

        // Check if qrCode within location threshold already exists in db in QRCodes collection
        checkQRCodeExists(qrCode, radius, new QueryCallbackWithQRCode() {
            public void queryCompleteCheckObject(boolean qrExists, QRCode dbQR) {

                // Check if reference to qrCode exists in db in Users collection
                checkUserHasQR(qrCodeID, username, new QueryCallback() {
                    public void queryCompleteCheck(boolean qrRefExists) {

                        // If qrCode does not exist, add it to QRCode collection
                        if (!qrExists) {
                            qrCodesReference.document(qrCodeID).set(qrCode);
                            if (resizedImageUrl != null) {
                                qrCodesReference.document(qrCodeID).update("photoList", FieldValue.arrayUnion(resizedImageUrl));
                                //QRCodesReference.document(QRCodeId).update("photoList", FieldValue.arrayRemove(resizedImageUrl));
                            }
                        }
                        // If user does not already have this qrCode, add a reference to it, increment their total scans and points, add new photo to qrCode
                        if (!qrRefExists) {
                            usersReference.document(username).update("qrCodeHashes", FieldValue.arrayUnion(qrCode.getHash()));
                            usersReference.document(username).update("qrCodeIDs", FieldValue.arrayUnion(qrCodeID));
                            usersReference.document(username).update("totalScans", FieldValue.increment(1));
                            usersReference.document(username).update("totalPoints", FieldValue.increment(qrCode.getPoints()));
                            if (resizedImageUrl != null) {
                                qrCodesReference.document(qrCodeID).update("photoList", FieldValue.arrayUnion(resizedImageUrl));
                            }
                        }
                        // If user does not have this qrCode but it already exists in qrCode collection, increase its total scans
                        if ((qrExists) && (!qrRefExists)) {
                            qrCodesReference.document(qrCodeID).update("numberOfScans", FieldValue.increment(1));
                        }
                        qrCodesReference.document(qrCodeID).update("inCollection", FieldValue.arrayUnion(username));
                    }
                });
            }
        });
    }

    /**
     * Delete given QR Code from user's collection
     *
     * @param username User's username
     * @param qrCodeID QR Code to delete
     * @sources Firestore documentation
     */
    public void deleteQR(@NonNull String username, @NonNull String qrCodeID) {

        usersReference.document(username).update("qrCodeIDs", FieldValue.arrayRemove(qrCodeID));
        usersReference.document(username).update("qrCodeHashes", FieldValue.arrayRemove(qrCodeID.substring(0, 64)));

        qrCodesReference.document(qrCodeID).update("inCollection", FieldValue.arrayRemove(username));
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


}
