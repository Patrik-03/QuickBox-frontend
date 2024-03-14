package com.example.quickbox_front;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SignUp_Handler extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup);

        Button back = (Button) findViewById(R.id.backB);
        Button create = (Button) findViewById(R.id.create);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn_Handler.getInstance().finish();
                Intent intent = new Intent(SignUp_Handler.this, Home_Handler.class);
                startActivity(intent);
                finish();
            }
        });

        TextView name = (TextView) findViewById(R.id.name);
        TextView email = (TextView) findViewById(R.id.email);
        TextView password = (TextView) findViewById(R.id.passw);
        TextView city = (TextView) findViewById(R.id.city);
        TextView street = (TextView) findViewById(R.id.street);

    }
}
