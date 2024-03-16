package com.example.quickbox_front;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SignIn_Handler extends AppCompatActivity {
    private ArrayList<String> receivedMessage = new ArrayList<>();
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
                .url("ws://192.168.1.33:8000/ws")
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
                        // save the result of the request in a variable
                        JSONObject jsonObject = null;
                        String content = "";
                        try {
                            jsonObject = new JSONObject(text);
                            content = jsonObject.getString("content");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        receivedMessage.add(content);
                        Log.d("WebSocket", "Received: " + receivedMessage);
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
                    email.setError("Email is required");
                }
            }
        });
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(password.getText().toString().isEmpty()){
                    password.setError("Password is required");
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
                webSocket.send("{\"type\": \"signInEmail\", \"content\": \"" + email.getText().toString() + "\"}");
                webSocket.send("{\"type\": \"signInPassword\", \"content\": \"" + password.getText().toString() + "\"}");
                // if the receivedMessage list contains only true values, then the user is signed in
                Log.d("WebSocket", "message: " + receivedMessage);
                if (receivedMessage.size() == 2 && receivedMessage.get(0).equals("true") && receivedMessage.get(1).equals("true")) {
                    Intent intent = new Intent(SignIn_Handler.this, Home_Handler.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public static SignIn_Handler getInstance() {
        return signInHandler;
    }
}
