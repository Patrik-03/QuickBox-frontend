package com.example.quickbox_front;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Map;

public class Delivery_Handler extends AppCompatActivity {
    Button back;
    Format_time format_time;
    TextView from, sentTime, deliveryTime, status, noteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery);
        EdgeToEdge.enable(this);

        int id = getIntent().getIntExtra("id", 0);
        String type = getIntent().getStringExtra("type");

        back = findViewById(R.id.backD);
        from = findViewById(R.id.textFrom);
        sentTime = findViewById(R.id.textSentTime);
        deliveryTime = findViewById(R.id.textDelTime);
        status = findViewById(R.id.textStatus);
        noteText = findViewById(R.id.textNoteText);

        if (type.equals("active")) {
            SharedPreferences sharedPreferencesDeliveries = getSharedPreferences("Deliveries", MODE_PRIVATE);

            for (int i = 0; i < sharedPreferencesDeliveries.getAll().size(); i++) {
                try {
                    JSONObject jsonObject = new JSONObject(sharedPreferencesDeliveries.getString("idHome" + i, ""));
                    if (jsonObject.getInt("id") == id) {
                        String fromText = "From: " + jsonObject.getString("from");
                        SpannableString spannableStringFrom = new SpannableString(fromText);
                        spannableStringFrom.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold "From:"
                        from.setText(spannableStringFrom);

                        format_time = new Format_time(jsonObject.getString("sent_time"));
                        sentTime.setText(format_time.getFormattedTime());
                        format_time = new Format_time(jsonObject.getString("delivery_time"));
                        deliveryTime.setText(format_time.getFormattedTime());
                        String statusG = "Status: " + jsonObject.getString("status");

                        String statusText;
                        switch (statusG) {
                            case "Status: Sent":
                                statusText = "Status: " + getString(R.string.sent);
                                break;
                            case "Status: Received by courier":
                                statusText = "Status: " + getString(R.string.received);
                                break;
                            case "Status: On the go":
                                statusText = "Status: " + getString(R.string.ontheGo);
                                break;
                            case "Status: Nearby":
                                statusText = "Status: " + getString(R.string.nearby);
                                break;
                            case "Status: Delivered":
                                statusText = "Status: " + getString(R.string.delivered);
                                break;
                            default:
                                statusText = "Status: Unknown";
                                break;
                        }
                        SpannableString spannableStringStatus = new SpannableString(statusText);
                        spannableStringStatus.setSpan(new StyleSpan(Typeface.BOLD), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold "Status:"
                        status.setText(spannableStringStatus);
                        noteText.setText(jsonObject.getString("note"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            SharedPreferences sharedPreferencesHistory = getSharedPreferences("History", MODE_PRIVATE);
            Map<String, ?> allEntries = sharedPreferencesHistory.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().startsWith("idHistory" + id)) {
                    try {
                        Log.d("WebSocket", entry.getValue().toString());
                        JSONObject jsonObject = new JSONObject(entry.getValue().toString());
                        if (jsonObject.getInt("id") == id) {
                            String fromText = "From: " + jsonObject.getString("from");
                            SpannableString spannableStringFrom = new SpannableString(fromText);
                            spannableStringFrom.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold "From:"
                            from.setText(spannableStringFrom);

                            format_time = new Format_time(jsonObject.getString("sent_time"));
                            sentTime.setText(format_time.getFormattedTime());
                            format_time = new Format_time(jsonObject.getString("delivery_time"));
                            deliveryTime.setText(format_time.getFormattedTime());
                            String statusG = "Status: " + jsonObject.getString("status");

                            String statusText;
                            switch (statusG) {
                                case "Status: Sent":
                                    statusText = "Status: " + getString(R.string.sent);
                                    break;
                                case "Status: Received by courier":
                                    statusText = "Status: " + getString(R.string.received);
                                    break;
                                case "Status: On the go":
                                    statusText = "Status: " + getString(R.string.ontheGo);
                                    break;
                                case "Status: Nearby":
                                    statusText = "Status: " + getString(R.string.nearby);
                                    break;
                                case "Status: Delivered":
                                    statusText = "Status: " + getString(R.string.delivered);
                                    break;
                                default:
                                    statusText = "Status: Unknown";
                                    break;
                            }
                            SpannableString spannableStringStatus = new SpannableString(statusText);
                            spannableStringStatus.setSpan(new StyleSpan(Typeface.BOLD), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold "Status:"
                            status.setText(spannableStringStatus);
                            noteText.setText(jsonObject.getString("note"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        back.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.enter_animation_back, R.anim.exit_animation_back);
        });
    }
}
