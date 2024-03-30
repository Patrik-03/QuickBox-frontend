package com.example.quickbox_front;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class History_Handler extends AppCompatActivity {
    Button back;
    Button delete;
    ScrollView scrollView;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);
        EdgeToEdge.enable(this);

        back = findViewById(R.id.backH);
        delete = findViewById(R.id.deleteH);
        scrollView = findViewById(R.id.scrollViewH);
        linearLayout = findViewById(R.id.linearLayoutH);

        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);

        // Create a new Button
        for (int i = 0; i < 20; i++) {
            Button newButton = new Button(this);
            newButton.setText("New Button");

            // Add the new Button to your LinearLayout
            linearLayout.addView(newButton);
        }

        back.setOnClickListener(v -> finish());
    }
}
