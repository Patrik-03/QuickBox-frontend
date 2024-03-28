package com.example.quickbox_front;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

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
        EdgeToEdge.enable(this);

        Button back = findViewById(R.id.backD);
        FloatingActionButton qr_scan = findViewById(R.id.qrScan);
        EditText receiveID = findViewById(R.id.receivID);
        EditText note = findViewById(R.id.note);
        CheckBox economy = findViewById(R.id.economy);
        CheckBox standard = findViewById(R.id.standard);
        CheckBox fast = findViewById(R.id.fast);
        Button create = findViewById(R.id.createD);
        SharedPreferences sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
        String id = sharedPreferences.getString("id", "");

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
                Log.d("WebSocket", "Received message: " + text);

                // Handle received message
                JSONObject jsonObject = null;
                String from;
                String sent_time;
                String delivery_time;
                String delivery_type;
                String status;
                String note;
                try {
                    jsonObject = new JSONObject(text);
                    from = jsonObject.getString("from");
                    sent_time = jsonObject.getString("sent_time");
                    delivery_time = jsonObject.getString("delivery_time");
                    delivery_type = jsonObject.getString("delivery_type");
                    status = jsonObject.getString("status");
                    note = jsonObject.getString("note");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                if (!from.isEmpty() && !sent_time.isEmpty() && !delivery_time.isEmpty() && !delivery_type.isEmpty() && !status.isEmpty() && !note.isEmpty()) {
                    Intent intent = new Intent(CreateDel_Handler.this, Home_Handler.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else {
                    //create pop up message that delivery was not created

                }
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

        create.setOnClickListener(v -> {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("receiveID", receiveID.getText().toString());
                jsonObject.put("fromID", id);
                if (economy.isChecked()) {
                    jsonObject.put("deliveryType", "economy");
                } else if (standard.isChecked()) {
                    jsonObject.put("deliveryType", "standard");
                } else if (fast.isChecked()) {
                    jsonObject.put("deliveryType", "fast");
                }
                jsonObject.put("note", note.getText().toString());
                webSocket.send(jsonObject.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
