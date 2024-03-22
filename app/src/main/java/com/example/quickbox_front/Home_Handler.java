package com.example.quickbox_front;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Home_Handler extends AppCompatActivity {
    private WebSocket webSocket;
    IPServer ipServer = new IPServer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        loadLocale();
        setContentView(R.layout.home);

        ImageButton profile = findViewById(R.id.profileH);
        ImageButton map = findViewById(R.id.mapH);
        TextView name = findViewById(R.id.nameH);
        String email = getIntent().getStringExtra("email");
        String nameR = "";
        byte[] qr_codeR = new byte[0];

        ViewPager viewPager = findViewById(R.id.viewPager);

        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.map);
        images.add(R.drawable.map);
        images.add(R.drawable.map);
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(this, images);
        viewPager.setAdapter(myPagerAdapter);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://" + ipServer.getIp() + ":8000/ws/home")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // WebSocket connection established
                super.onOpen(webSocket, response);
                Log.d("WebSocket", "Connection established (Home)");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("email", email);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                webSocket.send(jsonObject.toString());
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WebSocket", "Received message: " + text);

            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("WebSocket", "Connection closed (Home)");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocket", "Connection failed: " + t.getMessage());
            }
        });

        profile.setOnClickListener(v -> {
            Intent intent = new Intent(Home_Handler.this, Profile_Handler.class);
            intent.putExtra("name", nameR);
            intent.putExtra("email", email);
            intent.putExtra("qr_code", qr_codeR);
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
    public void closeWebSocketConnectionHome() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing the connection");
        }
    }
}