package com.example.quickbox_front;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Home_Handler extends AppCompatActivity {
    static Home_Handler homeHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);
        ImageButton profile = findViewById(R.id.profileH);
        ImageButton map = findViewById(R.id.mapH);
        TextView name = findViewById(R.id.nameH);
        String email = "";
        Byte[] qr_code = new Byte[0];
        ViewPager viewPager = findViewById(R.id.viewPager);
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.map);
        images.add(R.drawable.map);
        images.add(R.drawable.map);
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(this, images);
        viewPager.setAdapter(myPagerAdapter);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://192.168.1.33:8000/ws/home")
                .build();

        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // WebSocket connection established
                super.onOpen(webSocket, response);
                Log.d("WebSocket", "Connection established");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WebSocket", "Received message: " + text);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("WebSocket", "Connection closed");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocket", "Connection failed: " + t.getMessage());
            }
        });

        profile.setOnClickListener(v -> {
            Intent intent = new Intent(this, Profile_Handler.class);
            intent.putExtra("name", name.getText().toString());
            intent.putExtra("email", email);
            intent.putExtra("qr_code", qr_code);
            startActivity(intent);
        });
    }
    public static Home_Handler getInstance(){
        return homeHandler;
    }
}
