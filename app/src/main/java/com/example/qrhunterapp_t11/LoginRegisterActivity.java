// This file handles switching from the register/login activity to the main app activity

package com.example.qrhunterapp_t11;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.firestore.FirebaseFirestore;

public class LoginRegisterActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LoginFragment loginFragment = new LoginFragment(db);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        if (prefs.getBoolean("notLoggedIn", true)) {
            setContentView(R.layout.activity_login_register);
            getSupportFragmentManager().beginTransaction().replace(R.id.login_register_screen, loginFragment).commit();

        } else {
            Intent intent = new Intent(LoginRegisterActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}