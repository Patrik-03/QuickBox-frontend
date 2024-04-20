package com.example.quickboxwear;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class DataLayerListenerService extends WearableListenerService implements DataClient.OnDataChangedListener{

    SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        DataClient dataClient = Wearable.getDataClient(this);

        Log.d("WebSocket", "Service started");
        preferences = getSharedPreferences("quickboxWear", MODE_PRIVATE);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("WebSocket", "Data changed");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().equals("/deliveries")) {
                    Log.d("WebSocket", "Data received");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    String data = dataMap.getString("message");
                    Log.d("WebSocket", "Data: " + data);
                    preferences.edit().putString("activeDeliveries", data).apply();

                    // Send a broadcast to MainActivity to update the UI
                    Intent intent = new Intent("ACTIVE_DELIVERIES_UPDATED");
                    sendBroadcast(intent);
                }
            }
        }
    }
}