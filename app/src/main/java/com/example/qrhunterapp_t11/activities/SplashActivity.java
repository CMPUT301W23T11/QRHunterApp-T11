package com.example.qrhunterapp_t11.activities;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.qrhunterapp_t11.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;


/**
 * Handles logic for showing a splashscreen (the built-in android splashscreen library sucks).
 *
 * @referece Coding With Tea - https://www.youtube.com/watch?v=VcrzLcokvvc- how to implement the logic and layout of the splashscreen. Used without major modification.
 * @reference lottieicon - https://lottiefiles.com/78072-map-pin-location - created the map loading animation.
 */
public class SplashActivity extends AppCompatActivity {

    TextView appName;
    LottieAnimationView mapAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        appName = findViewById(R.id.app_name);
        mapAnimation = findViewById(R.id.splash_animation);

        //appName.animate().translationY(-400).setDuration(2700).setStartDelay(0);
        //mapAnimation.animate().translationX(2000).setDuration(2000).setStartDelay(2900);
        appName.animate().translationY(-500).setDuration(2000).setStartDelay(350);
        mapAnimation.animate().translationY(-500).setDuration(2000).setStartDelay(0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        }, 4500);
    }
}