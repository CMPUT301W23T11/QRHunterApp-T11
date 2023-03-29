package com.example.qrhunterapp_t11.fragments;

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

import com.example.qrhunterapp_t11.R;
import com.example.qrhunterapp_t11.interfaces.QueryCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Handles settings screen. Users can rename themselves and change their email.
 *
 * @author Afra
 * @sources Firestore documentation
 */
public class SettingsFragment extends Fragment {

    private final CollectionReference usersReference;
    private final CollectionReference qrCodeReference;
    private EditText usernameEditText;
    private EditText emailEditText;
    private String usernameString;
    private String emailString;
    private static final String PREFS_CURRENT_USER_EMAIL = "currentUserEmail";
    private static final String PREFS_CURRENT_USER_DISPLAY_NAME = "currentUserDisplayName";
    private static final String DATABASE_DISPLAY_NAME_FIELD = "displayName";
    private SharedPreferences prefs;

    public SettingsFragment(@NonNull FirebaseFirestore db) {
        this.usersReference = db.collection("Users");
        this.qrCodeReference = db.collection("QRCodes");
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        usernameEditText = view.findViewById(R.id.username_edit_edittext);
        emailEditText = view.findViewById(R.id.email_edit_edittext);
        Button confirmButton = view.findViewById(R.id.settings_confirm_button);

        usernameString = prefs.getString(PREFS_CURRENT_USER_DISPLAY_NAME, null);
        emailString = prefs.getString(PREFS_CURRENT_USER_EMAIL, "No email");
        usernameEditText.setText(usernameString);
        emailEditText.setText(emailString);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                usernameString = usernameEditText.getText().toString();
                emailString = emailEditText.getText().toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                // Make sure new username is eligible for change
                usernameCheck(usernameString, usernameEditText, new QueryCallback() {
                    public void queryCompleteCheck(boolean usernameExists) {

                        if (!usernameExists) {
                            builder
                                    .setTitle("Confirm username and email change")
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            String user = prefs.getString("currentUserUsername", null);

                                            usersReference.document(user).update(DATABASE_DISPLAY_NAME_FIELD, usernameString);
                                            usersReference.document(user).update("email", emailString);

                                            prefs.edit().putString(PREFS_CURRENT_USER_DISPLAY_NAME, usernameString).commit();
                                            prefs.edit().putString(PREFS_CURRENT_USER_EMAIL, emailString).commit();

                                            // Update username in all user's previous comments
                                            updateUserComments(user, usernameString, new QueryCallback() {
                                                public void queryCompleteCheck(boolean queryComplete) {
                                                    assert (queryComplete);
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
     * @param usernameExists   Callback for query
     */
    public void usernameCheck(@NonNull String usernameString, @NonNull EditText usernameEditText, final @NonNull QueryCallback usernameExists) {

        // Check if username matches Firestore document ID guidelines
        if (usernameString.length() == 0) {
            usernameEditText.setError("Field cannot be blank");
        } else if (usernameString.contains("/")) {
            usernameEditText.setError("Invalid character: '/'");
        } else if (usernameString.equals(".") || usernameString.equals("..") || usernameString.equals("__.*__")) {
            usernameEditText.setError("Invalid username");
        }

        // Check if username exists already
        else {
            usersReference
                    .whereEqualTo(DATABASE_DISPLAY_NAME_FIELD, usernameString)
                    .get()
                    .addOnSuccessListener(queryResult -> {
                        if (queryResult.isEmpty()) {
                            usernameExists.queryCompleteCheck(false);
                        } else {
                            usernameEditText.setError("Username is not unique");
                            usernameExists.queryCompleteCheck(true);
                        }
                    });
        }
    }

    /**
     * Use the DocumentReference in the user's QR Codes collection to find what
     * QR codes they have commented on, and update their display name for each one
     *
     * @param username           User's username
     * @param newDisplayUsername User's new display name
     * @param queryCompleteCheck Callback for query
     */
    public void updateUserComments(@NonNull String username, @NonNull String newDisplayUsername, final @NonNull QueryCallback queryCompleteCheck) {

        ArrayList<DocumentReference> userCommentedListRef = new ArrayList<>();

        // Retrieve DocumentReferences in the user's QR code collection and store them in an array
        usersReference.document(username).collection("User QR Codes")
                .get()
                .addOnSuccessListener(documentReferences -> {
                    for (QueryDocumentSnapshot reference : documentReferences) {

                        DocumentReference documentReference = (DocumentReference) reference.get("Reference");
                        userCommentedListRef.add(documentReference);
                    }
                    if (!userCommentedListRef.isEmpty()) {

                        // Retrieve matching QR Code data from the QRCodes collection using DocumentReferences
                        qrCodeReference.whereIn(FieldPath.documentId(), userCommentedListRef)
                                .get()
                                .addOnSuccessListener(referencedQRDocuments -> {
                                    for (QueryDocumentSnapshot referencedQR : referencedQRDocuments) {

                                        // Get collection reference to specific commentList to check
                                        CollectionReference commentList = referencedQR.getReference().collection("commentList");

                                        // Find exactly which comments need to be updated and update them
                                        commentList
                                                .whereEqualTo("username", username)
                                                .whereNotEqualTo(DATABASE_DISPLAY_NAME_FIELD, newDisplayUsername) // Filter out documents that don't need updating
                                                .get()
                                                .addOnSuccessListener(commentedQRDocuments -> {
                                                    ArrayList<DocumentSnapshot> commentedQR;
                                                    commentedQR = (ArrayList) commentedQRDocuments.getDocuments();
                                                    for (DocumentSnapshot commented : commentedQR) {
                                                        commented.getReference().update(DATABASE_DISPLAY_NAME_FIELD, newDisplayUsername);
                                                    }
                                                    queryCompleteCheck.queryCompleteCheck(true);
                                                });
                                    }
                                });
                    }
                });
    }
}
