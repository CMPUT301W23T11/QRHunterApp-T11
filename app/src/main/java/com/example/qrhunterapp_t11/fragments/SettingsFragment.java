package com.example.qrhunterapp_t11.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.example.qrhunterapp_t11.objectclasses.Preference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles settings screen. Users can rename themselves and change their email.
 *
 * @author Afra
 * @sources Firestore documentation
 * @sources <pre>
 * * <ul>
 * * <li><a href="https://www.javatpoint.com/java-email-validation">Validating email input using regex</a></li>
 * * </ul>
 * * </pre>
 */
public class SettingsFragment extends Fragment {

    private final CollectionReference usersReference;
    private final CollectionReference qrCodesReference;
    private EditText usernameEditText;
    private EditText emailEditText;
    private String usernameString;
    private String emailString;

    public SettingsFragment(@NonNull FirebaseFirestore db) {
        this.usersReference = db.collection("Users");
        this.qrCodesReference = db.collection("QRCodes");
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        //prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        usernameEditText = view.findViewById(R.id.username_edit_edittext);
        emailEditText = view.findViewById(R.id.email_edit_edittext);
        Button confirmUsernameButton = view.findViewById(R.id.change_username_button);
        Button confirmEmailButton = view.findViewById(R.id.change_email_button);

        usernameString = Preference.getPrefsString(Preference.PREFS_CURRENT_USER_DISPLAY_NAME, null);
        emailString = Preference.getPrefsString(Preference.PREFS_CURRENT_USER_EMAIL, null);
        usernameEditText.setText(usernameString);
        emailEditText.setText(emailString);

        // Changes the user's username
        confirmUsernameButton.setOnClickListener(view1 -> {

            usernameString = usernameEditText.getText().toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            // Make sure new username is eligible for change
            usernameCheck(usernameString, usernameEditText, new QueryCallback() {
                public void queryCompleteCheck(boolean usernameExists) {

                    if (!usernameExists) {
                        builder
                                .setTitle("Confirm username change")
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        usernameEditText.setText(Preference.getPrefsString(Preference.PREFS_CURRENT_USER_DISPLAY_NAME, null));
                                    }
                                })
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String user = Preference.getPrefsString(Preference.PREFS_CURRENT_USER, null);

                                        usersReference.document(user).update(Preference.DATABASE_DISPLAY_NAME_FIELD, usernameString);

                                        Preference.setPrefsString(Preference.PREFS_CURRENT_USER_DISPLAY_NAME, usernameString);

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
        });

        // Changes the user's email
        confirmEmailButton.setOnClickListener(view12 -> {
            emailString = emailEditText.getText().toString();

            AlertDialog.Builder emailBuilder = new AlertDialog.Builder(getContext());

            // Checks to make sure the user inputs a valid email
            if (validEmail(emailString)) {
                emailBuilder
                        .setTitle("Confirm email change")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                emailEditText.setText(Preference.getPrefsString(Preference.PREFS_CURRENT_USER_EMAIL, null));
                            }
                        })
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String user = Preference.getPrefsString(Preference.PREFS_CURRENT_USER, null);

                                if (!Objects.equals(emailString, "")) {
                                    usersReference.document(user).update("email", emailString);
                                } else {
                                    usersReference.document(user).update("email", null);
                                }

                                Preference.setPrefsString(Preference.PREFS_CURRENT_USER_EMAIL, emailString);

                            }
                        })
                        .create();
                emailBuilder.show();
            }

        });

        return view;
    }

    /**
     * Checks to see if inputted email is in a valid format
     *
     * @param emailString Entered email
     */
    public boolean validEmail(@NonNull String emailString) {
        System.out.println(emailString);
        if (emailString.equals("")) {
            return true;
        } else {
            // String to see the correct format of an email, allowed characters, and restricted character format
            String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z\\d-]+\\.)+[a-zA-Z]{2,6}$";
            Pattern pattern = Pattern.compile(regex);

            // Checks if provided email matches the regex email format string
            Matcher matcher = pattern.matcher(emailString);
            if (matcher.matches()) {
                return true;
            } else {
                emailEditText.setError("Invalid email");
                return false;
            }
        }

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
                    .whereEqualTo(Preference.DATABASE_DISPLAY_NAME_FIELD, usernameString)
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

        usersReference.document(username)
                .get()
                .addOnSuccessListener(user -> {
                    ArrayList<String> qrCodesCommentedOn = (ArrayList<String>) user.get("commentedOn");
                    for (String qrCodeCommentedOn : qrCodesCommentedOn) {
                        qrCodesReference.document(qrCodeCommentedOn).collection("commentList")
                                .whereEqualTo("username", username)
                                .whereNotEqualTo("displayName", newDisplayUsername)
                                .get()
                                .addOnSuccessListener(qrCodeToUpdate -> {
                                    System.out.println("ooooooooooooooo" + qrCodeToUpdate.getDocuments());
                                });
                    }
                });

    }
}
