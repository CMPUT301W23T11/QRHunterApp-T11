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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Handles settings screen. Users can rename themselves and change their email.
 *
 * @author Afra
 * @reference Firestore documentation
 */
public class SettingsFragment extends Fragment {

    private final CollectionReference usersReference;
    private EditText usernameEditText;
    private EditText emailEditText;
    private String usernameString;
    private String emailString;
    private boolean validUsername;

    public SettingsFragment(@NonNull FirebaseFirestore db) {
        this.usersReference = db.collection("Users");
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
                usernameCheck(usernameString, usernameEditText, new SettingsCallback() {
                    public void usernameValid(boolean valid) {
                        validUsername = valid;

                        if (validUsername) {
                            builder
                                    .setTitle("Confirm username and email change")
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            String user = prefs.getString("currentUser", null);
                                            usersReference.document(user).update("Display Name", usernameString);
                                            usersReference.document(user).update("Email", emailString);

                                            prefs.edit().putString("currentUserDisplayName", usernameString).commit();
                                            prefs.edit().putString("currentUserEmail", emailString).commit();
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
     * @param usernameValid    Callback for query
     */
    public void usernameCheck(@NonNull String usernameString, @NonNull EditText usernameEditText, final @NonNull SettingsCallback usernameValid) {

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
            usersReference.whereEqualTo("Display Name", usernameString).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            usernameValid.usernameValid(true);
                        } else {
                            usernameEditText.setError("Username is not unique");
                            usernameValid.usernameValid(false);
                        }
                    }
                }
            });
        }
    }

    /**
     * Callback for querying the database
     *
     * @author Afra
     */
    public interface SettingsCallback {
        void usernameValid(boolean valid);
    }
}
