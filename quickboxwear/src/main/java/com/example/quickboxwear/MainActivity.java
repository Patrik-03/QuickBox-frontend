package com.example.quickboxwear;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity implements DataClient.OnDataChangedListener {

    private LinearLayout mainLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout);

        mainLayout = findViewById(R.id.linearLayout);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        Wearable.getDataClient(this).addListener(this);

    }
    @Override
    protected void onDestroy() {
        Wearable.getDataClient(this).removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("JSON1", "Data changed");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                event.getDataItem();
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().equals("/active_deliveries")) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    Log.d("JSON1", dataMap.toString());
                    String data = dataMap.getString("message");
                    progressBar.setVisibility(ProgressBar.GONE);
                    displayActiveDeliveries(data);
                }
            }
        }
    }

    private void displayActiveDeliveries(String data) {
        mainLayout.removeAllViews();
        try {
            JSONObject jsonArray = new JSONObject(data);
            Log.d("WebS", jsonArray.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject delivery = jsonArray.getJSONObject(String.valueOf(i));
                String id = delivery.getString("id");
                String status = delivery.getString("status");
                String deliveryTime = delivery.getString("delivery_time");
                String deliveryInfo = "ID: " + id + "\nStatus: " + status + "\nDelivery Time: " + getFormattedTime(deliveryTime);

                TextView textView = new TextView(this);
                textView.setTextColor(Color.BLACK);
                TextView title = new TextView(this);
                title.setText(deliveryInfo);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(300, 200);
                title.setLayoutParams(layoutParams);
                if (i == jsonArray.length() - 1) {
                    layoutParams.setMargins(0, 0, 0, 60);
                } else {
                    layoutParams.setMargins(0, 0, 0, 20);
                }
                title.setPadding(10, 10, 10, 10);
                title.setAutoSizeTextTypeUniformWithPresetSizes(new int[]{25}, 0);
                title.setTextColor(Color.BLACK);
                title.setBackground(getDrawable(R.drawable.item));
                mainLayout.addView(title);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public String getFormattedTime(String time) {
        String[] timeSplit = time.split("T");
        String[] dateSplit = timeSplit[0].split("-");
        String[] timeSplit2 = timeSplit[1].split(":");
        String formattedTime = dateSplit[2] + "." + dateSplit[1] + "." + dateSplit[0] + "  " + timeSplit2[0] + ":" + timeSplit2[1];
        return formattedTime;
    }
}

