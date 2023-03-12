package com.example.qrhunterapp_t11;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles settings screen.
 * Users can toggle geolocation on and off, or log out.
 *
 * @author Afra, Kristina
 * @reference <a href="https://www.tutlane.com/tutorial/android/android-switch-on-off-button-with-examples">For handling switch events</a>
 */
public class SettingsFragment extends Fragment {

    private final CollectionReference usersReference;
    private boolean validUsername;

    public SettingsFragment(FirebaseFirestore db) {
        this.usersReference = db.collection("Users");
    }
    public interface settingsCallback {
        void usernameValid(boolean valid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        EditText usernameEditText = view.findViewById(R.id.username_edit_edittext);
        EditText emailEditText = view.findViewById(R.id.email_edit_edittext);
        Button confirmButton = view.findViewById(R.id.settings_confirm_button);

        String usernameString = prefs.getString("currentUserDisplayName", null);
        String emailString = prefs.getString("currentUserEmail", "No email");
        usernameEditText.setText(usernameString);
        emailEditText.setText(emailString);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                EditText newUsernameEditText = view.findViewById(R.id.username_edit_edittext);
                EditText newEmailEditText = view.findViewById(R.id.email_edit_edittext);

                String newUsernameString = String.valueOf(newUsernameEditText);
                String newEmailString = String.valueOf(newEmailEditText);

                usernameCheck(newUsernameString, newUsernameEditText, new settingsCallback() {
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
                                            usersReference.document(user).update("Display Name", newUsernameString);

                                            prefs.edit().putString("currentUserDisplayName", newUsernameString).commit();
                                            prefs.edit().putString("currentUserEmail", newEmailString).commit();
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

    public void usernameCheck(String usernameString, EditText usernameEditText, final settingsCallback usernameValid) {

        // Check for empty field
        if (usernameString.length() == 0) {
            usernameEditText.setError("Field cannot be blank");
        }

        // Check if username exists already
        else {
            DocumentReference usernameReference = usersReference.document(usernameString);
            usernameReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            usernameEditText.setError("Username is not unique");
                            usernameValid.usernameValid(false);
                        } else {
                            usernameValid.usernameValid(true);
                        }
                    }
                }
            });
        }
    }
}
