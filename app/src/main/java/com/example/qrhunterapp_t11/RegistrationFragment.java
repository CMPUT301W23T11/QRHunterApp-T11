package com.example.qrhunterapp_t11;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationFragment extends Fragment {

    Button registerButton;
    private final CollectionReference usersReference;

    public RegistrationFragment(CollectionReference usersReference) {
        this.usersReference = usersReference;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.registration_screen, container, false);
        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        registerButton = view.findViewById(R.id.registerbuttonregisterscreen);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validUsername;
                boolean validPassword;
                boolean validEmail;

                EditText registerUsernameEditText = getView().findViewById(R.id.usernameregisterscreen);
                EditText registerEmailEditText = getView().findViewById(R.id.emailregisterscreen);
                EditText registerPasswordEditText = getView().findViewById(R.id.passwordregisterscreen);
                EditText registerConfirmPasswordEditText = getView().findViewById(R.id.confirmpasswordregisterscreen);

                String registerUsername = registerUsernameEditText.getText().toString();
                String registerEmail = registerEmailEditText.getText().toString();
                String registerPassword = registerPasswordEditText.getText().toString();
                String registerConfirmPassword = registerConfirmPasswordEditText.getText().toString();

                // Some input validation

                // Check if username exists
                validUsername = usernameExistsCheck(registerUsername, registerUsernameEditText);

                // Check if password is valid
                validPassword = passwordIsValid(registerPassword, registerConfirmPassword, registerPasswordEditText, registerConfirmPasswordEditText);

                // Check if email exists
                validEmail = emailExistsCheck(registerEmail, registerEmailEditText);

                // Add user info to database
                if (validUsername && validPassword && validEmail) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("Username", registerUsername);
                    user.put("Password", registerPassword);
                    user.put("Email", registerEmail);

                    usersReference.document(registerUsername).set(user);

                    prefs.edit().putBoolean("notLoggedIn", false).apply();

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_screen, new ProfileFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        return view;
    }

    public boolean passwordIsValid(String registerPassword, String registerConfirmPassword, EditText registerPasswordEditText, EditText registerConfirmPasswordEditText) {
        boolean valid = false;

        if (registerPassword.length() == 0) {
            registerPasswordEditText.setError("Field cannot be blank");
        }
        if (registerPassword.length() < 8 && registerPassword.length() > 0) {
            registerPasswordEditText.setError("Password is too short");
        }
        if (!registerPassword.equals(registerConfirmPassword)) {
            registerConfirmPasswordEditText.setError("Passwords do not match");
        }
        if(registerPassword.length() >= 8) {
            Pattern letter = Pattern.compile("[a-zA-z]");
            Pattern digit = Pattern.compile("[0-9]");
            Pattern special = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~-]");

            Matcher hasLetter = letter.matcher(registerPassword);
            Matcher hasDigit = digit.matcher(registerPassword);
            Matcher hasSpecial = special.matcher(registerPassword);

            if(!hasLetter.find() || !hasDigit.find() || !hasSpecial.find()) {
                registerPasswordEditText.setError("Invalid password");
            }
            else {
                valid = true;
            }
        }
        return valid;
    }
    public boolean emailExistsCheck(String registerEmail, EditText registerEmailEditText) {
        boolean valid = false;

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
                                if (!document.isEmpty()) {
                                    registerEmailEditText.setError("User already exists with this email");
                                }
                            }
                        }
                    });
        }
        if (registerEmailEditText.getError() == null) {
            valid = true;
        }
        return valid;
    }
    public boolean usernameExistsCheck(String registerUsername, EditText registerUsernameEditText) {
        boolean valid = false;

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
                        }
                    }
                }
            });
        }
        if (registerUsernameEditText.getError() == null) {
            valid = true;
        }
        return valid;
    }
}
