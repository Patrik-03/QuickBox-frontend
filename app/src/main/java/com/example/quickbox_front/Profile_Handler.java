package com.example.quickbox_front;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Locale;

public class Profile_Handler extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        loadLocale();
        setContentView(R.layout.profile);

        String idH = getIntent().getStringExtra("id");
        String emailH = getIntent().getStringExtra("email");
        Bitmap qr_codeH = getIntent().getParcelableExtra("qr_code");

        Button back = findViewById(R.id.backP);
        TextView id = findViewById(R.id.nameP);
        TextView email = findViewById(R.id.emailP);
        Button history = findViewById(R.id.history);
        Button language = findViewById(R.id.language);
        Button signout = findViewById(R.id.signout);
        ImageView qr_code = findViewById(R.id.qr_code);
        ImageView noConnection = findViewById(R.id.no_connection);

        Log.e("Profile_Handler", "Received QR code: " + qr_codeH);

        id.setText("ID: " + idH);
        email.setText(emailH);
        qr_code.setImageBitmap(qr_codeH);

        back.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.enter_animation_back, R.anim.exit_animation_back);
        });
        SharedPreferences sharedPreferencesConnection = getSharedPreferences("Connection", MODE_PRIVATE);
        ConnectionThread connectionThread = new ConnectionThread();
        Thread thread = connectionThread.getThread(sharedPreferencesConnection, noConnection);
        thread.start();

        signout.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            sharedPreferencesConnection.getAll().clear();
            editor.putBoolean("isUserLoggedIn", false);
            editor.clear();
            editor.apply();
            Intent intentB = new Intent("com.example.quickbox_front.USER_SIGNED_OUT");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentB);
            // Start the sign-in activity and clear all other activities from the stack
            Intent intent = new Intent(Profile_Handler.this, SignIn_Handler.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Finish the current activity
            overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation);
        });


        history.setOnClickListener(v -> {
            Intent intent = new Intent(Profile_Handler.this, History_Handler.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("id", idH);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation);
        });

        language.setOnClickListener(v -> {
            Intent intent = new Intent(Profile_Handler.this, Language_Handler.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("id", idH);
            intent.putExtra("email", emailH);
            intent.putExtra("qr_code", qr_codeH);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation);
        });
    }
    public void loadLocale() {
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = preferences.getString("My_Lang", "");
        setLocale(language);
    }

    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }
}