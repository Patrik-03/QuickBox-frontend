package com.example.quickbox_front;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Profile_Handler extends AppCompatActivity {
    static Profile_Handler profileHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        Button edit = findViewById(R.id.history);
        Button language = findViewById(R.id.language);
        Button signout = findViewById(R.id.signout);
        ImageView qr_code = findViewById(R.id.qr_code);

        qr_code.setImageBitmap(bitmap);

        back.setOnClickListener(v -> {
            finish();
        });
    }
    public static Profile_Handler getProfileHandler() {
        return profileHandler;
    }

    public Bitmap byteArrayToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}
