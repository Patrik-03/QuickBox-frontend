package com.example.quickbox_front;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Language_Handler extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.language);

        Button back = findViewById(R.id.backB);
        Button english = findViewById(R.id.english);
        Button slovak = findViewById(R.id.slovak);

        back.setOnClickListener(v -> {
            finish();
        });

        english.setOnClickListener(v -> {
            // Change language to English
        });

        slovak.setOnClickListener(v -> {
            // Change language to Slovak
        });
    }
}
