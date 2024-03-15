package com.example.quickbox_front;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


@SuppressLint("CustomSplashScreen") // Suppress the warning about the splash screen
public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        // Transition to next activity immediately, without delay
        Intent intent = new Intent(SplashActivity.this, SignIn_Handler.class);
        startActivity(intent);
        finish();
    }
}

