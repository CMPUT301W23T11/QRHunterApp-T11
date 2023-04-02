package com.example.qrhunterapp_t11.fragments;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.qrhunterapp_t11.interfaces.QueryCallback;
import com.example.qrhunterapp_t11.interfaces.QueryCallbackWithObject;
import com.example.qrhunterapp_t11.objectclasses.QRCode;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Assistant class for common queries to the firestore database
 *
 * @author Everyone
 */

public class FirebaseQueryAssistant {
    private CollectionReference qrCodesReference;
    private CollectionReference usersReference;
    private final FirebaseFirestore db;


    public FirebaseQueryAssistant(@NonNull FirebaseFirestore db){
        this.db = db;
        this.qrCodesReference = db.collection("QRCodes");
        this.usersReference = db.collection("Users");
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

        usersReference.document(username).collection("User QR Codes")
                .get()
                .addOnSuccessListener(userQRCodes ->
                        hasCodes.queryCompleteCheck(!userQRCodes.isEmpty()));
    }

    /**
     * Helper function to check if a user has a QR Code in their collection with the same hash as qr param
     *
     * @param qrInput QR Code that is having its hash value checked
     * @param username  User whose collection is being checked
     */

    public void checkUserHasHash(@NonNull QRCode qrInput, @NonNull String username, final @NonNull QueryCallbackWithObject docExists) {
        ArrayList<DocumentReference> listOfUsersReferencedCodes = new ArrayList<DocumentReference>();
        System.out.println("HERE1query");


        // Retrieve DocumentReferences in the user's QR code collection and store them in an array
        usersReference.document(username).collection("User QR Codes")
                .get()
                .addOnSuccessListener(documentReferences -> {
                    for (QueryDocumentSnapshot reference : documentReferences) {

                        DocumentReference documentReference = (DocumentReference) reference.get("Reference");
                        listOfUsersReferencedCodes.add(documentReference);
                    }
                    if (!listOfUsersReferencedCodes.isEmpty()) {
                        // Retrieve matching QR Code data from the QRCodes collection using DocumentReferences
                        qrCodesReference.whereIn(FieldPath.documentId(), listOfUsersReferencedCodes)
                                .get()
                                .addOnSuccessListener(referencedQRDocuments -> {
                                    boolean hashExists = false;
                                    QRCode qrOutput = null;

                                    for (QueryDocumentSnapshot referencedQR : referencedQRDocuments) {
                                        if (referencedQR.get("hash").equals( qrInput.getHash())){
                                            qrOutput = referencedQR.toObject(QRCode.class);
                                            hashExists = true;
                                        }
                                    }
                                    docExists.queryCompleteCheckObject(hashExists, qrOutput);
                                });
                    }else{
                        docExists.queryCompleteCheckObject(false, null);
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
        usersReference.document(username).collection("User QR Codes").document(qrCodeID)
                .get()
                .addOnSuccessListener(userQRCode ->
                        userHasQRCode.queryCompleteCheck(userQRCode.exists())
                );
    }

    /**
     * This function calculates the distance between two locations on earth (input
     * via decimal latitude longitude coordinates) using the Haversine formula;
     * if the distance between the two points is less than the input threshold,
     * returns true, else false
     * <p>
     * In the context of a freshly scanned QRCode, if the hash function of the new code
     * matches the hash of a QRCode already in the db, this function determines if they should
     * be considered unique objects or the same QRcode (sharing comments, photos etc...)
     * if the function returns true using the new QRCode and the QRCode object already in the database,
     * no new document will be inserted (user profile will reference pre-existing QRCode), otherwise
     * a new entry will be created
     *
     * @param qr          QRCode -
     * @param dbQR        QRCode -
     * @param givenRadius Double - the maximum distance allowed between the two points IN METERS
     * @return true if distance shorter than uniqueness threshold, else false if 2 separate instances
     * @sources <pre>
     * <ul>
     * <li><a href="https://www.trekview.org/blog/2021/reading-decimal-gps-coordinates-like-a-computer/">How to read lat/long</a></li>
     * <li><a href="https://en.wikipedia.org/wiki/Haversine_formula">How to calculate distance between to locations on earth using lat/long</a></li>
     * <li><a href="https://linuxhint.com/import-math-in-java/">How use Math library</a></li>
     * <li><a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Math.html">cos, sin, arcsin</a></li>
     * <li><a href="https://www.movable-type.co.uk/scripts/latlong.html"> Verified test cases w/ this calculator</a></li>
     * </ul>
     * </pre>
     */

    public static boolean isSameLocation(@Nullable QRCode qr, @Nullable QRCode dbQR, double givenRadius) {
        //TODO INPUT VALIDATION:
        // some coordinates shouldn't make sense, iirc long can't have larger magnitude than +-180?
        // and +-90 for lat?

        // input validation
        // hashes are same, no location data for either, treat as same QRCode object
        if ((qr.getLatitude() == null) && (qr.getLongitude() == null) && (dbQR.getLatitude() == null) && (dbQR.getLongitude() == null)) {
            return true;
            // at least one of the qrs is null but not both, treat as separate objects
        } else if ((qr.getLatitude() == null) || (qr.getLongitude() == null) || (dbQR.getLatitude() == null) || (dbQR.getLongitude() == null)) {
            return false;
        }
        double lat1 = qr.getLatitude();
        double lng1 = qr.getLongitude();
        double lat2 = dbQR.getLatitude();
        double lng2 = dbQR.getLongitude();

        System.out.printf("lat1 %.20f\n", lat1);
        System.out.printf("lng2 %.20f\n", lng1);
        System.out.printf("lat2 %.20f\n", lat2);
        System.out.printf("lng2 %.20f\n", lng2);

        //COORDINATES HARDCODED FOR TESTING
        //double maxDistance = 30;    // in meters
        //double lat1 = 38.8977;
        //double lng1 = -77.0365;

        // latitude & longitude of second QRCode
        //double lat2 = 48.8584;
        //double lng2 = 2.2945;

        // convert degrees to radians
        // phi = latitude, lambda = longitude
        double phi1 = (lat1 * Math.PI) / 180.0;
        double lambda1 = (lng1 * Math.PI) / 180.0;

        double phi2 = (lat2 * Math.PI) / 180.0;
        double lambda2 = (lng2 * Math.PI) / 180.0;

        // Calculate haversine(theta), the central angle between both locations relative to earth's center
        // Haversine(theta) = sin^2((phi2-phi1)/2)+cos(phi1)cos(phi2)sin^2((lambda2-lambda1)/2)
        double haversine = (Math.pow(Math.sin((phi2 - phi1) / 2), 2) + Math.cos(phi1) * Math.cos(phi2) * (Math.pow(Math.sin((lambda2 - lambda1) / 2), 2)));

        // Calculate distance between both points using haversine
        // 6371.0 is the Earth's radius in kilometers
        // Distance = 2r*arcsin(sqr(haversine(theta)))
        double distance = (2 * 6371.0) * (Math.asin(Math.sqrt(haversine)));

        //System.out.printf("%f\n", haversine);
        System.out.printf("%.20f\n", distance);

        //convert distance to meters and compare with maxDistance
        distance *= 1000;
        System.out.printf("distance in meters: %.20f\n", distance);

        if (distance <= givenRadius) {
            System.out.printf("Same\n");
            return true;
        } else {
            System.out.printf("Different\n");
            return false;
        }
    }

    /**
     * @param qrCode           QR code to find matches of in db
     * @param qrCodeExists Query callback
     * @sources <a href="https://firebase.google.com/docs/firestore/query-data/queries#java_6">Firestore documentation</a>
     */
    public void checkQRCodeExists(QRCode qrCode, double MAX_RADIUS, final @NonNull QueryCallbackWithObject qrCodeExists) {
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
                        isSame = isSameLocation(qrCode, dbQR, MAX_RADIUS);
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
     * @param qrCode           QR code to find matches of in db
     * @param username         Username the qrCode is being added to
     * @param resizedImageUrl  URL of image the user took of qrCode
     * @param radius           Maximum radius for two codes to be considered the same object (meters)
     * @sources <a href="https://firebase.google.com/docs/firestore/query-data/queries#java_6">Firestore documentation</a>
     */
    public void addQR(@NonNull String username, @NonNull QRCode qrCode, String resizedImageUrl, @NonNull double radius) {
        String qrCodeID = qrCode.getID();

        Map<String, Object> qrCodeRef = new HashMap<>();

        // Check if qrCode within location threshold already exists in db in QRCodes collection
        checkQRCodeExists(qrCode, radius, new QueryCallbackWithObject() {
            public void queryCompleteCheckObject(boolean qrExists,QRCode dbQR) {
                qrCodeRef.put("Reference", qrCodesReference.document(qrCodeID));

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
                            usersReference.document(username).collection("User QR Codes").document(qrCodeID).set(qrCodeRef);
                            usersReference.document(username).update("totalScans", FieldValue.increment(1));
                            usersReference.document(username).update("totalPoints", FieldValue.increment(qrCode.getPoints()));
                            if (resizedImageUrl != null) {
                                qrCodesReference.document(qrCodeID).update("photoList", FieldValue.arrayUnion(resizedImageUrl));
                                //QRCodesReference.document(QRCodeId).update("photoList", FieldValue.arrayRemove(resizedImageUrl));
                            }
                        }
                        // If user does not have this qrCode but it already exists in qrCode collection, increase its total scans
                        if ((qrExists) && (!qrRefExists)) {
                            qrCodesReference.document(qrCodeID).update("numberOfScans", FieldValue.increment(1));
                        }
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
     * @param deleted  Callback for query
     * @sources Firestore documentation
     * //TODO delete from database if no users have that qrcode?
     */
    public void deleteQR(@NonNull String username, @NonNull String qrCodeID, final @NonNull QueryCallback deleted) {

            usersReference.document(username).collection("User QR Codes").document(qrCodeID)
                    .get()
                    .addOnSuccessListener(userQRSnapshot -> {
                        System.out.println("HERE3query");
                        DocumentReference documentReference = (DocumentReference) userQRSnapshot.get("Reference");
                        assert documentReference != null;
                        documentReference
                                .get()
                                .addOnSuccessListener(qrToDelete -> {
                                    System.out.println("HERE4query");

                                    // Subtract point value of that code from user's total points
                                    int points = qrToDelete.getLong("points").intValue();
                                    points = -points;
                                    usersReference.document(username).update("totalPoints", FieldValue.increment(points));

                                    // Delete code from user's collection
                                    usersReference.document(username).collection("User QR Codes").document(qrCodeID).delete();

                                    deleted.queryCompleteCheck(true);
                                    System.out.println("HERE5query");
                                });
                    });
        }

}
