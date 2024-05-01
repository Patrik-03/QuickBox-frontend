package com.example.quickbox_front;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class History_Handler extends AppCompatActivity {
    Button back, delete;
    ImageView noConnection;
    ScrollView scrollView;
    LinearLayout linearLayout;
    TextView noDeliveries;
    ProgressBar progressBar;
    WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);
        EdgeToEdge.enable(this);

        back = findViewById(R.id.backH);
        delete = findViewById(R.id.deleteH);
        scrollView = findViewById(R.id.scrollViewH);
        linearLayout = findViewById(R.id.linearLayoutH);
        noDeliveries = findViewById(R.id.noHistory);
        progressBar = findViewById(R.id.progressBarUp);
        noConnection = findViewById(R.id.no_connection);
        String user_id = getIntent().getStringExtra("id");
        scrollView.setSmoothScrollingEnabled(true);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);
        SharedPreferences sharedPreferencesHistory = getSharedPreferences("HistoryID", MODE_PRIVATE);
        SharedPreferences sharedPrefHistory = getSharedPreferences("History", MODE_PRIVATE);
        SharedPreferences sharedPreferencesConnection = getSharedPreferences("Connection", MODE_PRIVATE);

        if (sharedPreferencesHistory.getAll().isEmpty()) {
            noDeliveries.setVisibility(TextView.VISIBLE);
            delete.setVisibility(Button.GONE);
        }
        else {
            noDeliveries.setVisibility(TextView.GONE);
            delete.setVisibility(Button.VISIBLE);
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://" + IPServer.IP + ":8000/ws/deliveries_history")
                .build();

        if (!sharedPreferencesConnection.getBoolean("connection", false)) {
            String history = sharedPreferencesHistory.getString("historyID", null);
            if (history != null) {
                delete.setVisibility(Button.VISIBLE);
                noDeliveries.setVisibility(TextView.GONE);
                try {
                    JSONObject jsonObject = new JSONObject(history);
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject receivedMessage = jsonArray.getJSONObject(i);
                        Integer id = receivedMessage.getInt("id");
                        Button button = getButton(id);
                        runOnUiThread(() -> linearLayout.addView(button));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                noDeliveries.setVisibility(TextView.VISIBLE);
                delete.setVisibility(Button.GONE);
            }
        }

        if (sharedPreferencesConnection.getBoolean("connection", false)) {
            webSocket = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                    super.onClosed(webSocket, code, reason);
                    Log.d("WebSocket", "Connection closed (History details)");
                }

                @Override
                public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                    super.onClosing(webSocket, code, reason);
                }

                @Override
                public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, okhttp3.Response response) {
                    super.onFailure(webSocket, t, response);
                    Log.e("WebSocket", "Connection failed", t);
                    if (response != null) {
                        Log.e("WebSocket", "Response: " + response);
                    }
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    super.onMessage(webSocket, text);
                    Log.d("WebSocket", "Received message: " + text);
                    try {
                        // Parse the JSON string into a JSONObject
                        JSONObject jsonObject = new JSONObject(text);
                        sharedPrefHistory.getAll().clear();
                        JSONArray jsonArray = jsonObject.getJSONArray("items");
                        JSONObject receivedMessage2 = jsonArray.getJSONObject(0);
                        String type = receivedMessage2.getString("type");
                        Log.d("WebSocket", "Received type: " + type);
                        if (type.equals("delete")) {
                            runOnUiThread(() -> {
                                linearLayout.removeAllViews();
                                progressBar.setVisibility(ProgressBar.GONE);
                                noDeliveries.setVisibility(TextView.VISIBLE);
                                linearLayout.invalidate();
                            });
                        } else if (type.equals("history")) {
                            if (receivedMessage2.getString("id").equals("0")) {
                                runOnUiThread(() -> {
                                    delete.setVisibility(Button.GONE);
                                    noDeliveries.setVisibility(TextView.VISIBLE);
                                });
                            } else {
                                runOnUiThread(() -> {
                                    delete.setVisibility(Button.VISIBLE);
                                    noDeliveries.setVisibility(TextView.GONE);
                                });
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject receivedMessage = jsonArray.getJSONObject(i);
                                    Integer id = receivedMessage.getInt("id");
                                    Log.d("WebSocket", "Received ID: " + id);
                                    Button button = getButton(id);
                                    sharedPreferencesHistory.edit().putString("historyID", jsonObject.toString()).apply();
                                    runOnUiThread(() -> linearLayout.addView(button));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
                    super.onOpen(webSocket, response);
                    Log.d("WebSocket", "Connection established (History details)");
                    JSONObject message = new JSONObject();
                    try {
                        if (user_id != null) {
                            message.put("id", user_id);
                            message.put("type", "history");
                        }
                        webSocket.send(message.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else {
            ConnectionThread connectionThread = new ConnectionThread();
            Thread thread = connectionThread.getThread(sharedPreferencesConnection, noConnection);
            thread.start();
        }

        back.setOnClickListener(v -> {
            if (sharedPreferencesConnection.getBoolean("connection", false)){
                webSocket.close(1000, "Closing the connection");
            }
            finish();
            overridePendingTransition(R.anim.enter_animation_back, R.anim.exit_animation_back);
        });
        delete.setOnClickListener(v -> {
            if (sharedPreferencesConnection.getBoolean("connection", false)) {
                delete.setVisibility(Button.GONE);
                progressBar.setVisibility(ProgressBar.VISIBLE);
                sharedPreferencesHistory.edit().clear().apply();
                sharedPrefHistory.edit().clear().apply();
                JSONObject message = new JSONObject();
                try {
                    message.put("id", user_id);
                    message.put("type", "delete");
                    webSocket.send(message.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Button getButton(Integer id) {
        ContextThemeWrapper newContext = new ContextThemeWrapper(History_Handler.this, R.style.materialButtonStyle);
        Button button = new Button(newContext);
        button.setText("ID: " + id);
        button.setAutoSizeTextTypeUniformWithConfiguration(1, 50, 1, TypedValue.COMPLEX_UNIT_SP);
        button.setBackgroundResource(R.drawable.round_corners);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,  // Width
                LinearLayout.LayoutParams.WRAP_CONTENT   // Height
        );
        params.setMargins(0, 0, 0, 30);
        button.setLayoutParams(params);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(History_Handler.this, Delivery_Handler.class);
            intent.putExtra("id", id);
            intent.putExtra("type", "history");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation);
        });
        return button;
    }
}
