package com.example.qrhunterapp_t11;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * Handles settings screen. Users can rename themselves and change their email.
 *
 * @author Afra
 * @reference Firestore documentation
 */
public class SettingsFragment extends Fragment {

    private final CollectionReference usersReference;
    private final CollectionReference QRCodeReference;
    private EditText usernameEditText;
    private EditText emailEditText;
    private String usernameString;
    private String emailString;

    public SettingsFragment(@NonNull FirebaseFirestore db) {
        this.usersReference = db.collection("Users");
        this.QRCodeReference = db.collection("QRCodes");
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        usernameEditText = view.findViewById(R.id.username_edit_edittext);
        emailEditText = view.findViewById(R.id.email_edit_edittext);
        Button confirmButton = view.findViewById(R.id.settings_confirm_button);

        usernameString = prefs.getString("currentUserDisplayName", null);
        emailString = prefs.getString("currentUserEmail", "No email");
        usernameEditText.setText(usernameString);
        emailEditText.setText(emailString);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                usernameString = usernameEditText.getText().toString();
                emailString = emailEditText.getText().toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                // Make sure new username is eligible for change
                usernameCheck(usernameString, usernameEditText, new SettingsCallback() {
                    public void valid(boolean valid) {

                        if (valid) {
                            builder
                                    .setTitle("Confirm username and email change")
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            String user = prefs.getString("currentUser", null);
                                            String oldUsername = prefs.getString("currentUserDisplayName", null);

                                            usersReference.document(user).update("displayName", usernameString);
                                            usersReference.document(user).update("email", emailString);

                                            prefs.edit().putString("currentUserDisplayName", usernameString).commit();
                                            prefs.edit().putString("currentUserEmail", emailString).commit();

                                            // Update username in all user's previous comments
                                            updateUserComments(user, oldUsername, usernameString, new SettingsCallback() {
                                                public void valid(boolean valid) {
                                                    assert (valid);
                                                }
                                            });
                                        }
                                    })
                                    .create();
                            builder.show();
                        }
                    }
                });
            }
        });

        return view;
    }

    /**
     * Checks to see if username is valid and unique
     *
     * @param usernameString   Entered username
     * @param usernameEditText EditText for entered username
     * @param valid            Callback for query
     */
    public void usernameCheck(@NonNull String usernameString, @NonNull EditText usernameEditText, final @NonNull SettingsCallback valid) {

        // Check if username matches Firestore document ID guidelines
        if (usernameString.length() == 0) {
            usernameEditText.setError("Field cannot be blank");
        } else if (usernameString.contains("/")) {
            usernameEditText.setError("Invalid character: '/'");
        } else if (usernameString.equals(".") || usernameString.equals("..")) {
            usernameEditText.setError("Invalid username");
        } else if (usernameString.equals("__.*__")) {
            usernameEditText.setError("Invalid username");
        }

        // Check if username exists already
        else {
            usersReference.whereEqualTo("displayName", usernameString).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            valid.valid(true);
                        } else {
                            usernameEditText.setError("Username is not unique");
                            valid.valid(false);
                        }
                    }
                }
            });
        }
    }

    /**
     * Use the DocumentReference in the user's QR Codes collection to find what
     * QR codes they have commented on, and update their display name for each one
     *
     * @param username           User's username
     * @param oldDisplayUsername User's old display name
     * @param newDisplayUsername User's new display name
     * @param valid              Callback for query
     */
    public void updateUserComments(@NonNull String username, @NonNull String oldDisplayUsername, @NonNull String newDisplayUsername, final @NonNull SettingsCallback valid) {

        ArrayList<DocumentReference> userCommentedListRef = new ArrayList<>();

        // Retrieve DocumentReferences in the user's QR code collection and store them in an array
        usersReference.document(username).collection("User QR Codes")
                .get()
                .addOnSuccessListener(documentReferenceSnapshots -> {
                    for (QueryDocumentSnapshot snapshot : documentReferenceSnapshots) {

                        DocumentReference documentReference = (DocumentReference) snapshot.get("Reference");
                        userCommentedListRef.add(documentReference);
                    }
                    if (!userCommentedListRef.isEmpty()) {
                        // Retrieve matching QR Code data from the QRCodes collection using DocumentReferences
                        QRCodeReference.whereIn(FieldPath.documentId(), userCommentedListRef)
                                .get()
                                .addOnSuccessListener(referencedQRDocumentSnapshots -> {
                                    for (QueryDocumentSnapshot snapshot : referencedQRDocumentSnapshots) {
                                        CollectionReference commentList = snapshot.getReference().collection("commentList");
                                        // Find exactly which comments need to be updated and update them
                                        commentList.whereEqualTo("username", username)
                                                .whereNotEqualTo("displayName", newDisplayUsername)
                                                .get()
                                                .addOnSuccessListener(commentedQRDocumentSnapshots -> {
                                                    ArrayList<DocumentSnapshot> commentedQR;
                                                    commentedQR = (ArrayList) commentedQRDocumentSnapshots.getDocuments();
                                                    for (DocumentSnapshot commented : commentedQR) {
                                                        commented.getReference().update("displayName", newDisplayUsername);
                                                    }
                                                    valid.valid(true);
                                                });
                                    }
                                });
                    }
                });
    }

    /**
     * Callback for querying the database
     *
     * @author Afra
     */
    public interface SettingsCallback {
        void valid(boolean valid);
    }
}
