// References:
// https://stackoverflow.com/a/66270738

package com.example.qrhunterapp_t11;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginFragment extends Fragment {
    Button signInButton;
    Button registerButton;
    private final CollectionReference usersReference;

    public LoginFragment(CollectionReference usersReference) {
        this.usersReference = usersReference;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_screen, container, false);
        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        signInButton = view.findViewById(R.id.loginbuttonloginscreen);
        registerButton = view.findViewById(R.id.registerbuttonloginscreen);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validUsername;
                boolean validPassword;

                EditText loginUsernameEditText = getView().findViewById(R.id.usernameloginscreen);
                EditText loginPasswordEditText = getView().findViewById(R.id.passwordloginscreen);

                String loginUsername = loginUsernameEditText.getText().toString();
                String loginPassword = loginPasswordEditText.getText().toString();

                // Some input validation

                // Check if username exists
                validUsername = usernameExistsCheck(loginUsername, loginUsernameEditText);

                // Check if document exists with matching username and password
                validPassword = passwordMatchesCheck(loginUsername, loginPassword, loginPasswordEditText);

                if (validUsername && validPassword) {
                    prefs.edit().putBoolean("notLoggedIn", false).commit();

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_screen, new ProfileFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_screen, new RegistrationFragment(usersReference))
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }
    public boolean usernameExistsCheck(String loginUsername, EditText loginUsernameEditText) {
        boolean valid = false;

        if(loginUsername.length() == 0) {
            loginUsernameEditText.setError("Field cannot be blank");
        }
        if (loginUsername.length() > 0) {
            DocumentReference usernameReference = usersReference.document(loginUsername);
            usernameReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            loginUsernameEditText.setError("Username not found");
                        }
                    }
                }
            });
        }
        if (loginUsernameEditText.getError() == null) {
            valid = true;
        }
        return valid;
    }
    public boolean passwordMatchesCheck(String loginUsername, String loginPassword, EditText loginPasswordEditText) {
        boolean valid = false;

        if(loginPassword.length() == 0) {
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
                                if (document.isEmpty()) {
                                    loginPasswordEditText.setError("Incorrect password");
                                }
                            }
                        }
                    });
        }
        if (loginPasswordEditText.getError() == null) {
            valid = true;
        }
        return valid;
    }
}
