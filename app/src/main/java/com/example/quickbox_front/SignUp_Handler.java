package com.example.quickbox_front;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SignUp_Handler extends AppCompatActivity {
    private String receivedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://192.168.1.33:8000/ws/signin")
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
                receivedMessage = text;

                // Check received message
                if (receivedMessage.equals("ok")) {
                    // If credentials are correct, start Home_Handler activity
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SignUp_Handler.this, Home_Handler.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    // If credentials are incorrect, show error message
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView error = findViewById(R.id.error);
                            error.setText("Invalid credentials");
                        }
                    });
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                // WebSocket connection closed
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocket", "Connection failed: " + t.getMessage());
            }
        });

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
