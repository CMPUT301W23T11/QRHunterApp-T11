package com.example.qrhunterapp_t11.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.qrhunterapp_t11.R;

import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Handles logic for showing a splashscreen (the built-in android splashscreen library sucks).
 *
 * @sources <pre>
 * <ul>
 * <li><a href="https://www.youtube.com/watch?v=VcrzLcokvvc">How to implement the logic and layout of the splashscreen</a></li>
 * <li><a href="https://lottiefiles.com/78072-map-pin-location">Created the map loading animation</a></li>
 * </ul>
 * </pre>
 */
public class SplashActivity extends AppCompatActivity {

    TextView appName;
    LottieAnimationView mapAnimation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        appName = findViewById(R.id.app_name);
        mapAnimation = findViewById(R.id.splash_animation);

        //appName.animate().translationY(-400).setDuration(2700).setStartDelay(0);
        //mapAnimation.animate().translationX(2000).setDuration(2000).setStartDelay(2900);
        appName.animate().translationY(-400).setDuration(2000).setStartDelay(350);
        mapAnimation.animate().translationY(-400).setDuration(2000).setStartDelay(0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        }, 4500);
    }
}