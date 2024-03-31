package com.example.quickbox_front;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Delivery_Handler extends AppCompatActivity {
    Button back;
    Format_time format_time;
    WebSocket webSocket;
    Boolean isConnected = false;
    TextView from, sentTime, deliveryTime, status, noteText;
    ImageButton map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery);
        EdgeToEdge.enable(this);

        int id = getIntent().getIntExtra("id", 0);
        int user_id = getIntent().getIntExtra("user_id", 0);


        back = findViewById(R.id.backD);
        from = findViewById(R.id.textFrom);
        sentTime = findViewById(R.id.textSentTime);
        deliveryTime = findViewById(R.id.textDelTime);
        status = findViewById(R.id.textStatus);
        noteText = findViewById(R.id.textNoteText);
        map = findViewById(R.id.map);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://" + IPServer.IP + ":8000/ws/deliveries")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // WebSocket connection established
                super.onOpen(webSocket, response);
                Log.d("WebSocket", "Connection established (Delivery details)");
                JSONObject message = new JSONObject();
                try {
                    message.put("id", user_id);
                    message.put("del_id", id);
                    webSocket.send(message.toString());
                } catch (Exception e) {
                    Log.e("WebSocket", "Failed to send message: " + e.getMessage());
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WebSocket", "Received message: " + text);

                try {
                    JSONObject receivedMessage = new JSONObject(text);
                    String fromText = "From: " + receivedMessage.getInt("from");
                    SpannableString spannableStringFrom = new SpannableString(fromText);
                    spannableStringFrom.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold "From:"
                    from.setText(spannableStringFrom);

                    format_time = new Format_time(receivedMessage.getString("sent_time"));
                    sentTime.setText(format_time.getFormattedTime());
                    format_time = new Format_time(receivedMessage.getString("delivery_time"));
                    deliveryTime.setText(format_time.getFormattedTime());

                    String statusText = "Status: " + receivedMessage.getString("status");
                    SpannableString spannableStringStatus = new SpannableString(statusText);
                    spannableStringStatus.setSpan(new StyleSpan(Typeface.BOLD), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold "Status:"
                    status.setText(spannableStringStatus);

                    noteText.setText(receivedMessage.getString("note"));
                    isConnected = true;

                } catch (Exception e) {
                    Log.e("WebSocket", "Failed to parse message: " + e.getMessage());
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("WebSocket", "Connection closed (Delivery details)");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocket", "Connection failed: " + t.getMessage());
            }
        });

        back.setOnClickListener(v -> {
            finish();
            webSocket.close(1000, "User closed the activity");
        });

        map.setOnClickListener(v -> {
            // Open map
        });
    }
}
