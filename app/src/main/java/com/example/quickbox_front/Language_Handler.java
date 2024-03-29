package com.example.quickbox_front;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class Language_Handler extends AppCompatActivity {
    Home_Handler homeHandler = new Home_Handler();
    Profile_Handler profileHandler = new Profile_Handler();
    String idH;
    String emailH;
    Bitmap qr_codeH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.language);
        EdgeToEdge.enable(this);

        Button back = findViewById(R.id.backD);
        Button english = findViewById(R.id.english);
        Button slovak = findViewById(R.id.slovak);
        ProgressBar progressBar = findViewById(R.id.progressBarL);
        TextView languageSK = findViewById(R.id.currLS);
        TextView languageEN = findViewById(R.id.currLE);

        idH = getIntent().getStringExtra("id");
        emailH = getIntent().getStringExtra("email");
        qr_codeH = getIntent().getParcelableExtra("qr_code");

        progressBar.setVisibility(View.INVISIBLE);

        if (Locale.getDefault().getLanguage().equals("sk")) {
            languageSK.setVisibility(View.VISIBLE);
            languageEN.setVisibility(View.INVISIBLE);
        } else if (Locale.getDefault().getLanguage().equals("en")) {
            languageEN.setVisibility(View.VISIBLE);
            languageSK.setVisibility(View.INVISIBLE);
        }

        back.setOnClickListener(v -> finish());

        english.setOnClickListener(v -> {
            if (!Locale.getDefault().getLanguage().equals("en")) {
                progressBar.setVisibility(View.VISIBLE);
                setLocale("en");
                restartApp();
            }
        });

        slovak.setOnClickListener(v -> {
            if (!Locale.getDefault().getLanguage().equals("sk")) {
                progressBar.setVisibility(View.VISIBLE);
                setLocale("sk");
                restartApp();
            }
        });
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        conf.setLocale(locale);
        res.updateConfiguration(conf, res.getDisplayMetrics());

        // Save the selected language
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    public void loadLocale() {
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = preferences.getString("My_Lang", "");
        if (!language.isEmpty()) {
            setLocale(language);
        }
    }

    private void restartApp() {
        finish();
        homeHandler.finish();
        profileHandler.finish();
        Intent intentH = new Intent(getApplicationContext(), Home_Handler.class);
        intentH.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentH);
        Intent intentP = new Intent(getApplicationContext(), Profile_Handler.class);
        intentP.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentP.putExtra("id", idH);
        intentP.putExtra("email", emailH);
        intentP.putExtra("qr_code", qr_codeH);
        startActivity(intentP);
        Intent intent = new Intent(getApplicationContext(), Language_Handler.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
