package com.example.quickbox_front;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
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
    Button back;
    Button delete;
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
        String user_id = getIntent().getStringExtra("id");

        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://" + IPServer.IP + ":8000/ws/deliveries_history")
                .build();

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

                            // Get the "items" JSONArray from the JSONObject
                            JSONArray jsonArray = jsonObject.getJSONArray("items");
                            JSONObject receivedMessage2 = jsonArray.getJSONObject(0);
                            String type = receivedMessage2.getString("type");
                            if (type.equals("delete")) {
                                runOnUiThread(() -> {
                                    linearLayout.removeAllViews();
                                    progressBar.setVisibility(ProgressBar.GONE);
                                    noDeliveries.setVisibility(TextView.VISIBLE);
                                });
                            }
                            else if (type.equals("history")) {
                                if (receivedMessage2.getString("id").equals("0")){
                                    runOnUiThread(() -> noDeliveries.setVisibility(TextView.VISIBLE));
                                }
                                else {
                                    runOnUiThread(() -> noDeliveries.setVisibility(TextView.GONE));
                                    delete.setVisibility(Button.VISIBLE);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject receivedMessage = jsonArray.getJSONObject(i);
                                        Integer id = receivedMessage.getInt("id");
                                        Log.d("WebSocket", "Received ID: " + id);
                                        Button button = getButton(id);
                                        runOnUiThread(() -> linearLayout.addView(button));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @NonNull
                    private Button getButton(Integer id) {
                        Button button = new Button(History_Handler.this);
                        button.setText("ID: " + id);
                        button.setAutoSizeTextTypeUniformWithConfiguration(1, 40, 1, TypedValue.COMPLEX_UNIT_SP);
                        button.setOnClickListener(v -> {
                            Intent intent = new Intent(History_Handler.this, Delivery_Handler.class);
                            intent.putExtra("id", id);
                            intent.putExtra("user_id", Integer.valueOf(user_id));
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        });
                        return button;
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

        back.setOnClickListener(v -> finish());
        delete.setOnClickListener(v -> {
            delete.setVisibility(Button.GONE);
            progressBar.setVisibility(ProgressBar.VISIBLE);
            JSONObject message = new JSONObject();
            try {
                message.put("id", user_id);
                message.put("type", "delete");
                webSocket.send(message.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
