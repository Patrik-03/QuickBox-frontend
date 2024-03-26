package com.example.quickbox_front;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class CreateDel_Handler extends AppCompatActivity {
    private WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_delivery);

        Button back = findViewById(R.id.backD);
        FloatingActionButton qr_scan = findViewById(R.id.qrScan);
        EditText receiveID = findViewById(R.id.receivID);
        EditText note = findViewById(R.id.note);
        CheckBox economy = findViewById(R.id.economy);
        CheckBox standard = findViewById(R.id.standard);
        CheckBox fast = findViewById(R.id.fast);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://" + IPServer.IP + ":8000/ws/create_delivery")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                // WebSocket connection established
                super.onOpen(webSocket, response);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // Receiving messages
                super.onMessage(webSocket, text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                // WebSocket is about to close
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                // WebSocket connection closed
                super.onClosed(webSocket, code, reason);
            }
        });


        back.setOnClickListener(v -> {
            finish();
        });

        qr_scan.setOnClickListener(v -> {
            // Scan QR code
        });

        economy.setOnClickListener(v -> {
            if (economy.isChecked()) {
                standard.setChecked(false);
                fast.setChecked(false);
            }
        });

        standard.setOnClickListener(v -> {
            if (standard.isChecked()) {
                economy.setChecked(false);
                fast.setChecked(false);
            }
        });

        fast.setOnClickListener(v -> {
            if (fast.isChecked()) {
                economy.setChecked(false);
                standard.setChecked(false);
            }
        });
    }
}
