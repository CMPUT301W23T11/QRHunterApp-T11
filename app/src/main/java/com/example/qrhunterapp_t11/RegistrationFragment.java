package com.example.qrhunterapp_t11;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles registration functionality for the app.
 *
 * @author afra
 * @reference <a href="https://stackoverflow.com/a/66270738">For changing fragments</a>
 * @see LoginRegisterActivity
 */
public class RegistrationFragment extends Fragment {

    Button registerButton;
    FirebaseFirestore db;
    CollectionReference usersReference;
    boolean validUsername;
    boolean validPassword;
    boolean validEmail;
    EditText registerUsernameEditText;
    EditText registerEmailEditText;
    EditText registerPasswordEditText;
    EditText registerConfirmPasswordEditText;

    /**
     * Constructor for fragment. Also instantiates a reference to the users collection for ease of access.
     *
     * @param db Firestore database instance
     */
    public RegistrationFragment(FirebaseFirestore db) {
        this.db = db;
        this.usersReference = db.collection("Users");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration_screen, container, false);
        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        registerButton = view.findViewById(R.id.registerbuttonregisterscreen);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                registerUsernameEditText = getView().findViewById(R.id.usernameregisterscreen);
                registerEmailEditText = getView().findViewById(R.id.emailregisterscreen);
                registerPasswordEditText = getView().findViewById(R.id.passwordregisterscreen);
                registerConfirmPasswordEditText = getView().findViewById(R.id.confirmpasswordregisterscreen);

                String registerUsername = registerUsernameEditText.getText().toString();
                String registerEmail = registerEmailEditText.getText().toString();
                String registerPassword = registerPasswordEditText.getText().toString();
                String registerConfirmPassword = registerConfirmPasswordEditText.getText().toString();

                /* Run the validity checks, starting with the username check. If all info is valid, add user to the database,
                 log them in, and update the prefs file so that they don't have to log in again next time.
                 */
                usernameExistsCheck(registerUsername, registerUsernameEditText, new Callback() {
                    public void dataValid(boolean valid) {
                        validUsername = valid;

                        // Check if email exists
                        emailExistsCheck(registerEmail, registerEmailEditText, new Callback() {
                            public void dataValid(boolean valid) {
                                validEmail = valid;

                                // Check if password is valid
                                validPassword = passwordIsValid(registerPassword, registerConfirmPassword, registerPasswordEditText, registerConfirmPasswordEditText);

                                // Add user info to database
                                if (validUsername && validPassword && validEmail) {
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("Username", registerUsername);
                                    user.put("Password", registerPassword);
                                    user.put("Email", registerEmail);

                                    usersReference.document(registerUsername).set(user);
                                    usersReference.document(registerUsername).collection("QR Codes");

                                    prefs.edit().putBoolean("notLoggedIn", false).commit();
                                    prefs.edit().putString("loginUsername", registerUsername).commit();

                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                });
            }
        });

        return view;
    }

    /**
     * Method to confirm if password is valid and relatively secure.
     *
     * @param registerPassword                Password the user entered into the password field
     * @param registerConfirmPassword         Password the user entered into the confirm password field
     * @param registerPasswordEditText        EditText for password field
     * @param registerConfirmPasswordEditText EditText for confirm password field
     * @return true if all checks pass, false otherwise
     */
    public boolean passwordIsValid(String registerPassword, String registerConfirmPassword, EditText registerPasswordEditText, EditText registerConfirmPasswordEditText) {

        // Check for empty field
        if (registerPassword.length() == 0) {
            registerPasswordEditText.setError("Field cannot be blank");
        }
        // Check for length
        if (registerPassword.length() < 8 && registerPassword.length() > 0) {
            registerPasswordEditText.setError("Password is too short");
        }
        // Check for matching passwords
        if (!registerPassword.equals(registerConfirmPassword)) {
            registerConfirmPasswordEditText.setError("Passwords do not match");
        }
        // Check for password containing necessary character types
        if (registerPassword.length() >= 8) {
            Pattern letter = Pattern.compile("[a-zA-z]");
            Pattern digit = Pattern.compile("[0-9]");
            Pattern special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");

            Matcher hasLetter = letter.matcher(registerPassword);
            Matcher hasDigit = digit.matcher(registerPassword);
            Matcher hasSpecial = special.matcher(registerPassword);

            if (!hasLetter.find() || !hasDigit.find() || !hasSpecial.find()) {
                registerPasswordEditText.setError("Invalid password");
            }
        }
        if (registerPasswordEditText.getError() == null) {
            return true;
        }
        return false;
    }

    /**
     * Method to check if user already exists with entered email or not.
     *
     * @param registerEmail         Email the user entered into the email field
     * @param registerEmailEditText EditText for email field
     * @param dataValid             Callback value since the database is being queried
     * @see Callback
     */
    public void emailExistsCheck(String registerEmail, EditText registerEmailEditText, final Callback dataValid) {

        if (registerEmail.length() == 0) {
            registerEmailEditText.setError("Field cannot be blank");
        }
        if (registerEmail.length() > 0) {
            usersReference
                    .whereEqualTo("Email", registerEmail)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot document = task.getResult();
                                if (document.isEmpty()) {
                                    dataValid.dataValid(true);
                                } else {
                                    registerEmailEditText.setError("User already exists with this email");
                                    dataValid.dataValid(false);
                                }
                            }
                        }
                    });
        }
    }

    /**
     * Method to check if entered username is already taken or not.
     *
     * @param registerUsername         Username the user entered into the username field
     * @param registerUsernameEditText EditText for username field
     * @param dataValid                Callback value since the database is being queried
     */
    public void usernameExistsCheck(String registerUsername, EditText registerUsernameEditText, final Callback dataValid) {

        if (registerUsername.length() == 0) {
            registerUsernameEditText.setError("Field cannot be blank");
        }
        if (registerUsername.length() < 6 && registerUsername.length() > 0) {
            registerUsernameEditText.setError("Username is too short");
        }
        if (registerUsername.length() >= 6) {
            DocumentReference usernameReference = usersReference.document(registerUsername);
            usernameReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            registerUsernameEditText.setError("Username is not unique");
                            dataValid.dataValid(false);
                        } else {
                            dataValid.dataValid(true);
                        }
                    }
                }
            });
        }
    }
}