package com.example.quickbox_front;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class Language_Handler extends AppCompatActivity {
    Home_Handler homeHandler;
    Profile_Handler profileHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        homeHandler = new Home_Handler();
        profileHandler = new Profile_Handler();
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.language);
        EdgeToEdge.enable(this);

        Button back = findViewById(R.id.backL);
        Button english = findViewById(R.id.english);
        Button slovak = findViewById(R.id.slovak);
        ProgressBar progressBar = findViewById(R.id.progressBarL);
        TextView language = findViewById(R.id.currL);

        if(Locale.getDefault().getLanguage().equals("en")) {
            language.setTranslationY(0.283F);
        }
        else if (Locale.getDefault().getLanguage().equals("sk")) {
            language.setTranslationY(0.148F);
        }

        back.setOnClickListener(v -> {
            finish();
        });

        english.setOnClickListener(v -> {
            if (Locale.getDefault().getLanguage().equals("en")) {
                return;
            }
            else if (Locale.getDefault().getLanguage().equals("sk")) {
                progressBar.setVisibility(View.VISIBLE);
                language.setTranslationY(0.283F);
                setLocale("en");
                restartApp();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        slovak.setOnClickListener(v -> {
            if (Locale.getDefault().getLanguage().equals("sk")) {
                return;
            }
            else if (Locale.getDefault().getLanguage().equals("en")) {
                progressBar.setVisibility(View.VISIBLE);
                language.setTranslationY(0.148F);
                setLocale("sk");
                restartApp();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);

        // Save the selected language
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    public void loadLocale() {
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = preferences.getString("My_Lang", "");
        setLocale(language);
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
        startActivity(intentP);
        Intent intent = new Intent(getApplicationContext(), Language_Handler.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
