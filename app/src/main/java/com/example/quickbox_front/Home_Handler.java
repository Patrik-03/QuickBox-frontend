package com.example.quickbox_front;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Home_Handler extends AppCompatActivity {
    static Home_Handler homeHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        homeHandler = this;
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);
    }
    public static Home_Handler getInstance(){
        return homeHandler;
    }
}
