package com.example.quickboxwear;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class DataLayerListenerService extends WearableListenerService{

    SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        DataClient dataClient = Wearable.getDataClient(this);

        Log.d("WebSocket", "Service started");
        preferences = getSharedPreferences("quickboxWear", MODE_PRIVATE);
    }

    private static final String MESSAGE_PATH = "/deploy";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("WebSocket", "onMessageReceived(): " + messageEvent);
        Log.d("WebSocket", new String(messageEvent.getData()));
        if (MESSAGE_PATH.equals(messageEvent.getPath())) {
            Log.d("WebSocket", "Data received");
            String data = new String(messageEvent.getData());
            Log.d("WebSocket", "Data: " + data);
            preferences.edit().putString("activeDeliveries", data).apply();

            // Send a broadcast to MainActivity to update the UI
            Intent intent = new Intent("ACTIVE_DELIVERIES_UPDATED");
            sendBroadcast(intent);

        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}