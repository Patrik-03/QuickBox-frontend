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

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Locale;

public class Profile_Handler extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Home_Handler homeHandler = new Home_Handler();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        loadLocale();
        setContentView(R.layout.profile);

        String nameH = getIntent().getStringExtra("name");
        String emailH = getIntent().getStringExtra("email");
        Bitmap qr_codeH = getIntent().getParcelableExtra("qr_code");

        Button back = findViewById(R.id.backP);
        TextView name = findViewById(R.id.nameP);
        TextView email = findViewById(R.id.emailP);
        Button history = findViewById(R.id.history);
        Button language = findViewById(R.id.language);
        Button signout = findViewById(R.id.signout);
        ImageView qr_code = findViewById(R.id.qr_code);

        Log.e("Profile_Handler", "Received QR code: " + qr_codeH);

        name.setText(nameH);
        email.setText(emailH);
        qr_code.setImageBitmap(qr_codeH);

        back.setOnClickListener(v -> {
            finish();
        });

        signout.setOnClickListener(v -> {
            Intent intent = new Intent(Profile_Handler.this, SignIn_Handler.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            homeHandler.finish();
            homeHandler.closeWebSocketConnectionHome();
        });

        history.setOnClickListener(v -> {
        });

        language.setOnClickListener(v -> {
            Intent intent = new Intent(Profile_Handler.this, Language_Handler.class);
            startActivity(intent);
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

    public String readQRCode(Bitmap qr_code) {
        try {
            // Convert the Bitmap to a BinaryBitmap for decoding
            int[] intArray = new int[qr_code.getWidth() * qr_code.getHeight()];
            qr_code.getPixels(intArray, 0, qr_code.getWidth(), 0, 0, qr_code.getWidth(), qr_code.getHeight());
            LuminanceSource source = new RGBLuminanceSource(qr_code.getWidth(), qr_code.getHeight(), intArray);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // Decode the QR code
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}