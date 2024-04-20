package com.example.quickboxwear;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {

    private LinearLayout mainLayout;
    SharedPreferences preferences;
    private BroadcastReceiver receiver;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout);
        new ConnectTask().execute();

        startService(new Intent(this, DataLayerListenerService.class));
        mainLayout = findViewById(R.id.linearLayout);

        preferences = getSharedPreferences("quickboxWear", MODE_PRIVATE);

        String activeDeliveries = preferences.getString("activeDeliveries", null);
        if (activeDeliveries != null) {
            displayActiveDeliveries(activeDeliveries);
        }

        // Register the receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String activeDeliveries = preferences.getString("activeDeliveries", null);
                if (activeDeliveries != null) {
                    displayActiveDeliveries(activeDeliveries);
                }
            }
        };
        registerReceiver(receiver, new IntentFilter("ACTIVE_DELIVERIES_UPDATED"), Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onDestroy() {
        // Unregister the receiver
        unregisterReceiver(receiver);
        stopService(new Intent(this, DataLayerListenerService.class));
        super.onDestroy();
    }

    private void displayActiveDeliveries(String data) {
        mainLayout.removeAllViews();
        try {
            JSONObject jsonArray = new JSONObject(data);
            Log.d("WebS", jsonArray.toString());
            preferences.edit().putString("activeDeliveries", jsonArray.toString()).apply();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject delivery = jsonArray.getJSONObject(String.valueOf(i));
                String id = delivery.getString("id");
                String status = delivery.getString("status");
                String deliveryTime = delivery.getString("delivery_time");
                String deliveryInfo = "ID: " + id + "\nStatus: " + status + "\nDelivery Time: " + getFormattedTime(deliveryTime);

                // Create a new TextView for each delivery
                TextView deliveryTextView = new TextView(this);
                deliveryTextView.setTextColor(Color.BLACK);
                deliveryTextView.setText(deliveryInfo);
                deliveryTextView.setPadding(10, 10, 10, 10);
                deliveryTextView.setBackground(getDrawable(R.drawable.item));

                // Set layout parameters
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layoutParams.setMargins(0, 0, 0, 20); // Add some margin between items
                deliveryTextView.setLayoutParams(layoutParams);

                // Add the TextView to the main layout
                mainLayout.addView(deliveryTextView);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFormattedTime(String time) {
        String[] timeSplit = time.split("T");
        String[] dateSplit = timeSplit[0].split("-");
        String[] timeSplit2 = timeSplit[1].split(":");
        return dateSplit[2] + "." + dateSplit[1] + "." + dateSplit[0] + "  " + timeSplit2[0] + ":" + timeSplit2[1];
    }

    private class ConnectTask extends AsyncTask<Void, Void, ConnectionResult> {
        @Override
        protected ConnectionResult doInBackground(Void... voids) {
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                    .addApi(Wearable.API)
                    .build();

            return googleApiClient.blockingConnect();
        }

        @Override
        protected void onPostExecute(ConnectionResult connectionResult) {
            if (connectionResult.isSuccess()) {
                // The Wearable API connection is successful
                Log.d("WebS", "Connection successful");
            } else {
                // The Wearable API connection is not successful
                Log.d("WebS", "Connection failed");
            }
        }
    }
}
