package com.example.quickbox_front;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SignIn_Handler extends AppCompatActivity {
    private JSONObject receivedMessage;
    static SignIn_Handler signInHandler;
    static WebSocket webSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        signInHandler = this;
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signin);

        TextView email = findViewById(R.id.editTextTextEmailAddress);
        TextView password = findViewById(R.id.editTextTextPassword);
        ProgressBar progressBar = findViewById(R.id.progressBarUp);
        Button signin = findViewById(R.id.signinB);
        TextView textcreate = findViewById(R.id.createacc);
        TextView signup = findViewById(R.id.signupB);
        //underline signup text
        signup.setPaintFlags(signup.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        // Connect to WebSocket server
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://192.168.1.33:8000/ws/signin")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        // WebSocket connection established
                        super.onOpen(webSocket, response);
                        Log.d("WebSocket", "Connection established");
                    }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WebSocket", "Received message: " + text);
                String result;
                try {
                    receivedMessage = new JSONObject(text);
                    result = receivedMessage.getString("result");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                // Check received message
                if (result.equals("True")) {
                    // If credentials are correct, start Home_Handler activity
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(SignIn_Handler.this, Home_Handler.class);
                            startActivity(intent);
                            webSocket.close(1000, "Goodbye, World!");
                            finish();
                        }
                    });
                } else {
                    // If credentials are incorrect, show error message
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            signin.setVisibility(View.VISIBLE);
                            signup.setVisibility(View.VISIBLE);
                            textcreate.setVisibility(View.VISIBLE);
                            email.setError(getString(R.string.errorsignin));
                            password.setError(getString(R.string.errorsignin));
                        }
                    });
                }
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

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn_Handler.this, SignUp_Handler.class);
                startActivity(intent);
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signin.setVisibility(View.GONE);
                signup.setVisibility(View.GONE);
                textcreate.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("signInEmail", email.getText().toString());
                    jsonObject.put("signInPassword", password.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                // Send credentials to server
                webSocket.send(jsonObject.toString());
            }
        });
    }
    public static void closeWebSocketConnection() {
        if (webSocket != null) {
            webSocket.close(1000, "Goodbye, World!");
        }
    }
    public static SignIn_Handler getInstance() {
        return signInHandler;
    }
}
