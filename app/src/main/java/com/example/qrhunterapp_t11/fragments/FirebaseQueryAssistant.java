package com.example.qrhunterapp_t11.fragments;

import androidx.annotation.NonNull;

import com.example.qrhunterapp_t11.interfaces.QueryCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

public class FirebaseQueryAssistant {
    private CollectionReference usersReference;
    public FirebaseQueryAssistant(){}

    public void deleteQR(@NonNull String username, @NonNull String qrCodeID, @NonNull CollectionReference usersReference, final @NonNull QueryCallback deleted) {

            usersReference.document(username).collection("User QR Codes").document(qrCodeID)
                    .get()
                    .addOnSuccessListener(userQRSnapshot -> {
                        DocumentReference documentReference = (DocumentReference) userQRSnapshot.get("Reference");
                        assert documentReference != null;
                        documentReference
                                .get()
                                .addOnSuccessListener(qrToDelete -> {

                                    // Subtract point value of that code from user's total points
                                    int points = qrToDelete.getLong("points").intValue();
                                    points = -points;
                                    usersReference.document(username).update("totalPoints", FieldValue.increment(points));

                                    // Delete code from user's collection
                                    usersReference.document(username).collection("User QR Codes").document(qrCodeID).delete();

                                    deleted.queryCompleteCheck(true);
                                });
                    });
        }

}
