package com.example.quickbox_front;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SignIn_Handler extends AppCompatActivity {
    private JSONObject receivedMessage;

    private WebSocket webSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        loadLocale();
        setContentView(R.layout.signin);

        EditText email = findViewById(R.id.editTextTextEmailAddress);
        EditText password = findViewById(R.id.editTextTextPassword);
        ProgressBar progressBar = findViewById(R.id.progressBarUp);
        Button signin = findViewById(R.id.signinB);
        TextView textcreate = findViewById(R.id.createacc);
        TextView signup = findViewById(R.id.signupB);
        //underline signup text
        signup.setPaintFlags(signup.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        // Connect to WebSocket server
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://" + IPServer.IP + ":8000/ws/signin")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        // WebSocket connection established
                        super.onOpen(webSocket, response);
                        Log.d("WebSocket", "Connection established (Sign In)");
                    }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WebSocket", "Received message: " + text);
                String resultID;
                String resultEmail;
                try {
                    receivedMessage = new JSONObject(text);
                    resultID = receivedMessage.getString("id");
                    resultEmail = receivedMessage.getString("email");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                // Check received message
                if (!resultID.isEmpty()) {
                    // If credentials are correct, start Home_Handler activity
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(SignIn_Handler.this, Home_Handler.class);
                        intent.putExtra("id", resultID);
                        intent.putExtra("email", resultEmail);
                        webSocket.close(1000, "Closing the connection");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Add this line
                    });
                } else {
                    // If credentials are incorrect, show error message
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        signin.setVisibility(View.VISIBLE);
                        signup.setVisibility(View.VISIBLE);
                        textcreate.setVisibility(View.VISIBLE);
                        email.setError(getString(R.string.errorsignin));
                        password.setError(getString(R.string.errorsignin));
                    });
                }
            }

                    @Override
                    public void onClosed(WebSocket webSocket, int code, String reason) {
                        Log.d("WebSocket", "Connection closed (Sign In)");
                    }

                    @Override
                    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                        Log.e("WebSocket", "Connection failed: " + t.getMessage());
                    }
                });

        //check iuf the user write the email and password
        email.setOnFocusChangeListener((v, hasFocus) -> {
                if(email.getText().toString().isEmpty()){
                    email.setError(getString(R.string.emailenter));
            }
        });
        password.setOnFocusChangeListener((v, hasFocus) -> {
            if(password.getText().toString().isEmpty()){
                password.setError(getString(R.string.passwordenter));
            }
        });

        signup.setOnClickListener(v -> {
            Intent intent = new Intent(SignIn_Handler.this, SignUp_Handler.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        signin.setOnClickListener(v -> {
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

        public void closeWebSocketConnection() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing the connection");
        }
    }
}
