package com.example.quickbox_front;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
            boolean isUserLoggedIn = sharedPreferences.getBoolean("isUserLoggedIn", false);

            if (isUserLoggedIn) {
                startActivity(new Intent(SplashScreenActivity.this, Home_Handler.class));
            } else {
                startActivity(new Intent(SplashScreenActivity.this, SignIn_Handler.class));
            }
            finish();
        }, 1000);
    }
}