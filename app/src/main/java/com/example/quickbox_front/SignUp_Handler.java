package com.example.quickbox_front;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SignUp_Handler extends AppCompatActivity {
    private JSONObject receivedMessage;
    IPServer ipServer = new IPServer();
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SignIn_Handler signInHandler = new SignIn_Handler();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        loadLocale();
        setContentView(R.layout.signup);

        TextView name = findViewById(R.id.name);
        TextView email = findViewById(R.id.email);
        TextView password = findViewById(R.id.passw);
        TextView city = findViewById(R.id.city);
        TextView street = findViewById(R.id.street);
        TextView street_number = findViewById(R.id.street_num);
        Button back = findViewById(R.id.backB);
        Button create = findViewById(R.id.create);
        ProgressBar progressBar = findViewById(R.id.progressBarUp);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://" + ipServer.getIp() + ":8000/ws/signup")
                .build();

        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // WebSocket connection established
                super.onOpen(webSocket, response);
                Log.d("WebSocket", "Connection established (Sign Up)");
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
                if (result.equals("ok") && email.getText().toString().contains("@") && email.getText().toString().contains(".")){
                    // If credentials are correct, start Home_Handler activity
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        signInHandler.finish();
                        Intent intent = new Intent(SignUp_Handler.this, Home_Handler.class);
                        intent.putExtra("email", email.getText().toString());
                        signInHandler.closeWebSocketConnection();
                        webSocket.close(1000, "Closing the connection");
                        startActivity(intent);
                        finish();
                    });
                } else if (!email.getText().toString().contains("@") || !email.getText().toString().contains(".")
                        || result.equals("ok")) {
                    progressBar.setVisibility(View.GONE);
                    create.setVisibility(View.VISIBLE);
                    email.setError(getString(R.string.wrongfromat));
                } else {
                    // If credentials are incorrect, show error message
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        create.setVisibility(View.VISIBLE);
                        TextView error = findViewById(R.id.error);
                        error.setText(getString(R.string.errorsignup));
                    });
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("WebSocket", "Connection closed (Sign Up)");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocket", "Connection failed: " + t.getMessage());
            }
        });
        email.setOnFocusChangeListener((v, hasFocus) -> {
            if(!email.getText().toString().contains("@") || !email.getText().toString().contains(".")){
                email.setError(getString(R.string.wrongfromat));
            }
        });

        back.setOnClickListener(v -> {
            webSocket.close(1000, "Closing the connection");
            finish();
        });

        create.setOnClickListener(v -> {
            create.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            JSONObject json = new JSONObject();
            try {
                json.put("name", name.getText().toString());
                json.put("email", email.getText().toString());
                json.put("password", password.getText().toString());
                json.put("city", city.getText().toString());
                json.put("street", street.getText().toString());
                json.put("street_number", street_number.getText().toString());
                webSocket.send(json.toString());
            } catch (Exception e) {
                Log.e("WebSocket", "Error: " + e.getMessage());
            }
        });
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
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
}
