package com.example.quickbox_front;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Profile_Handler extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Home_Handler homeHandler = new Home_Handler();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile);

        String nameH = getIntent().getStringExtra("name");
        String emailH = getIntent().getStringExtra("email");
        byte[] qr_codeH = getIntent().getByteArrayExtra("qr_code");
        Bitmap bitmap = byteArrayToBitmap(qr_codeH);

        Button back = findViewById(R.id.backP);
        TextView name = findViewById(R.id.nameP);
        TextView email = findViewById(R.id.emailP);
        Button history = findViewById(R.id.history);
        Button language = findViewById(R.id.language);
        Button signout = findViewById(R.id.signout);
        ImageView qr_code = findViewById(R.id.qr_code);

        name.setText(nameH);
        email.setText(emailH);
        qr_code.setImageBitmap(bitmap);

        back.setOnClickListener(v -> {
            finish();
        });

        signout.setOnClickListener(v -> {
            Intent intent = new Intent(Profile_Handler.this, SignIn_Handler.class);
            startActivity(intent);
            finish();
            homeHandler.finish();
        });
    }

    public Bitmap byteArrayToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}