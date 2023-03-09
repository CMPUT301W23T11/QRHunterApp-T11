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

/**
 * Handles login functionality for the app.
 *
 * @author afra
 * @reference <a href="https://stackoverflow.com/a/66270738">For changing fragments</a>
 * @see LoginRegisterActivity
 */
public class LoginFragment extends Fragment {
    Button signInButton;
    Button registerButton;
    FirebaseFirestore db;
    CollectionReference usersReference;
    boolean validUsername;
    boolean validPassword;
    EditText loginUsernameEditText;
    EditText loginPasswordEditText;

    public LoginFragment(FirebaseFirestore db) {
        this.db = db;
        this.usersReference = db.collection("Users");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_screen, container, false);
        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        signInButton = view.findViewById(R.id.loginbuttonloginscreen);
        registerButton = view.findViewById(R.id.registerbuttonloginscreen);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginUsernameEditText = getView().findViewById(R.id.usernameloginscreen);
                loginPasswordEditText = getView().findViewById(R.id.passwordloginscreen);

                String loginUsername = loginUsernameEditText.getText().toString();
                String loginPassword = loginPasswordEditText.getText().toString();

                // Some input validation

                // Check if username exists
                usernameExistsCheck(loginUsername, loginUsernameEditText, new Callback() {
                    public void dataValid(boolean valid) {
                        validUsername = valid;

                        // Check if document exists with matching username and password
                        if (validUsername) {
                            passwordMatchesCheck(loginUsername, loginPassword, loginPasswordEditText, new Callback() {
                                public void dataValid(boolean valid) {
                                    validPassword = valid;
                                    if (validUsername && validPassword) {
                                        prefs.edit().putBoolean("notLoggedIn", false).commit();
                                        prefs.edit().putString("loginUsername", loginUsername).commit();

                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.login_register_screen, new RegistrationFragment(db))
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    public void usernameExistsCheck(String loginUsername, EditText loginUsernameEditText, final Callback dataValid) {

        if (loginUsername.length() == 0) {
            loginUsernameEditText.setError("Field cannot be blank");
        }
        if (loginUsername.length() > 0) {
            DocumentReference usernameReference = usersReference.document(loginUsername);
            usernameReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            dataValid.dataValid(true);
                        } else {
                            loginUsernameEditText.setError("Username not found");
                            dataValid.dataValid(false);
                        }
                    }
                }
            });
        }
    }

    public void passwordMatchesCheck(String loginUsername, String loginPassword, EditText loginPasswordEditText, final Callback dataValid) {

        if (loginPassword.length() == 0) {
            loginPasswordEditText.setError("Field cannot be blank");
        }
        if (loginPassword.length() > 0) {
            usersReference
                    .whereEqualTo("Username", loginUsername)
                    .whereEqualTo("Password", loginPassword)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot document = task.getResult();
                                if (!document.isEmpty()) {
                                    dataValid.dataValid(true);
                                } else {
                                    loginPasswordEditText.setError("Incorrect password");
                                    dataValid.dataValid(false);
                                }
                            }
                        }
                    });
        }
    }
}
