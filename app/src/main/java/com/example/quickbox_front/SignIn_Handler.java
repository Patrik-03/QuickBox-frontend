package com.example.quickbox_front;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;



public class SignIn_Handler extends AppCompatActivity {
    static SignIn_Handler signInHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        signInHandler = this;
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signin);

        Button signup = (Button) findViewById(R.id.signupB);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn_Handler.this, SignUp_Handler.class);
                startActivity(intent);
            }
        });
    }
    public static SignIn_Handler getInstance(){
        return signInHandler;
    }
}