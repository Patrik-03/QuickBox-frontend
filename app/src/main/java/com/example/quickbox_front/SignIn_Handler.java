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

public class SignIn_Handler extends AppCompatActivity {
    private String receivedMessage;
    static SignIn_Handler signInHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        signInHandler = this;
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signin);

        TextView email = findViewById(R.id.editTextTextEmailAddress);
        TextView password = findViewById(R.id.editTextTextPassword);
        // Connect to WebSocket server
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
                if (receivedMessage.equals("true")) {
                    // If credentials are correct, start Home_Handler activity
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SignIn_Handler.this, Home_Handler.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    // If credentials are incorrect, show error message
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            email.setError(getString(R.string.errorsignin));
                            password.setError(getString(R.string.errorsignin));
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

        //check iuf the user write the email and password
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                    if(email.getText().toString().isEmpty()){
                        email.setError(getString(R.string.emailenter));
                }
            }
        });
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(password.getText().toString().isEmpty()){
                    password.setError(getString(R.string.passwordenter));
                }
            }
        });


        TextView signup = findViewById(R.id.signupB);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn_Handler.this, SignUp_Handler.class);
                startActivity(intent);
            }
        });

        Button signin = findViewById(R.id.signinB);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send credentials to server
                webSocket.send("{\"signInEmail\": \"" + email.getText().toString() + "\" , \"signInPassword\" : \"" + password.getText().toString() + "\"}");
            }
        });
    }

    public static SignIn_Handler getInstance() {
        return signInHandler;
    }
}
