package com.example.qrhunterapp_t11;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Handles switching from the register/login activity to the main app activity.
 * The class switches to the main activity depending on the login state of the user,
 * which is indicated in either the login or registration fragment.
 *
 * @author Afra
 * @reference <a href="https://stackoverflow.com/a/66270738">For changing fragments</a>
 * @see RegistrationFragment
 * @see LoginFragment
 */
public class LoginRegisterActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final LoginFragment loginFragment = new LoginFragment(db);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        // If user has not previously logged in then prompt for login/registration
        if (prefs.getBoolean("notLoggedIn", true)) {
            setContentView(R.layout.activity_login_register);
            getSupportFragmentManager().beginTransaction().replace(R.id.login_register_screen, loginFragment).commit();

        }
        // Otherwise, the user is already logged in
        else {
            Intent intent = new Intent(LoginRegisterActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}