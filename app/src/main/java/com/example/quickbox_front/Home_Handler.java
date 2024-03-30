package com.example.quickbox_front;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Home_Handler extends AppCompatActivity {
    private WebSocket webSocket;
    Boolean openConnection = false;
    Boolean nameSet = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        loadLocale();
        setContentView(R.layout.home);

        ImageButton profile = findViewById(R.id.profileH);
        Button create = findViewById(R.id.createDel);
        ImageButton map = findViewById(R.id.mapH);
        TextView name = findViewById(R.id.nameH);

        SharedPreferences sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences sharedPreferencesDeliveries = getSharedPreferences("Deliveries", MODE_PRIVATE);

        String email = sharedPreferences.getString("email", "");
        String idGet = sharedPreferences.getString("id", "");

        Bitmap qr_code = generateQRCode(idGet);

        ViewPager viewPager = findViewById(R.id.viewPager);

        List<CarouselItem> items = new ArrayList<>();
        if (sharedPreferences.getString("name", "").isEmpty()) {
            name.setText(getString(R.string.welcome) + email);
        } else {
            name.setText(getString(R.string.welcome) + sharedPreferences.getString("name", ""));
        }
        if (sharedPreferencesDeliveries.getAll().isEmpty()) {
            Log.d("WebSocket", "No deliveries");
            items.add(new CarouselItem("0", "No deliveries", ""));
        } else {
            Log.d("WebSocket", "Retrieving data from SharedPreferences");
            for (int i = 0; i < sharedPreferencesDeliveries.getAll().size(); i++) {
                try {
                    JSONObject jsonObject = new JSONObject(sharedPreferencesDeliveries.getString("idHome" + i, ""));
                    String id = jsonObject.getString("id");
                    String delivery_time = jsonObject.getString("delivery_time");
                    String status = jsonObject.getString("status");
                    items.add(new CarouselItem(id, delivery_time, status));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.d("WebSocket", items.toString());
            runOnUiThread(() -> {
                MyPagerAdapter myPagerAdapter = new MyPagerAdapter(Home_Handler.this, items);
                viewPager.setAdapter(myPagerAdapter);
            });
        }

        if (!openConnection) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("ws://" + IPServer.IP + ":8000/ws/home")
                    .build();

            webSocket = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    // WebSocket connection established
                    super.onOpen(webSocket, response);
                    Log.d("WebSocket", "Connection established (Home)");
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("id", idGet);
                        jsonObject.put("del_id", 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    webSocket.send(jsonObject.toString());
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    Log.d("WebSocket", "Received message: " + text);
                    if (!nameSet) {
                        try {
                            JSONObject jsonObject = new JSONObject(text);
                            String nameGet = jsonObject.getString("name");
                            sharedPreferences.edit().putString("name", nameGet).apply();
                            name.setText(getString(R.string.welcome) + nameGet);
                            nameSet = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        try {
                            // Parse the JSON string into a JSONObject
                            JSONObject jsonObject = new JSONObject(text);

                            // Get the "items" JSONArray from the JSONObject
                            JSONArray jsonArray = jsonObject.getJSONArray("items");

                            // Clear the images list
                            items.clear();
                            sharedPreferencesDeliveries.edit().clear().apply();
                            // Iterate over the array
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject delivery = jsonArray.getJSONObject(i);

                                String id = delivery.getString("id");
                                String delivery_time = delivery.getString("delivery_time");
                                String status = delivery.getString("status");

                                // Add the image to the images list
                                items.add(new CarouselItem(id, delivery_time, status));
                                // Save the delivery ID to SharedPreferences
                                JSONObject jsonObject1 = new JSONObject();
                                jsonObject1.put("id", id);
                                jsonObject1.put("delivery_time", delivery_time);
                                jsonObject1.put("status", status);
                                boolean success = sharedPreferencesDeliveries.edit()
                                        .putString("idHome" + i, String.valueOf(jsonObject1))
                                        .commit();  // Use commit() instead of apply()

                                if (!success) {
                                    Log.e("WebSocket", "Failed to save data to SharedPreferences");
                                }
                                else {
                                    Log.d("WebSocket", "Saved data to SharedPreferences");
                                    Log.d("WebSocket", sharedPreferencesDeliveries.getAll().toString());
                                }
                            }

                            // Update the UI on the main thread
                            runOnUiThread(() -> {
                                MyPagerAdapter myPagerAdapter = new MyPagerAdapter(Home_Handler.this, items);
                                viewPager.setAdapter(myPagerAdapter);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    Log.d("WebSocket", "Connection closed (Home)");
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    Log.e("WebSocket", "Connection failed: " + t.getMessage());
                }
            });
        }

        profile.setOnClickListener(v -> {
            Intent intent = new Intent(Home_Handler.this, Profile_Handler.class);
            intent.putExtra("id", idGet);
            intent.putExtra("email", email);
            intent.putExtra("qr_code", qr_code);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, 1);
        });

        create.setOnClickListener(v -> {
            Intent intent = new Intent(Home_Handler.this, CreateDel_Handler.class);
            intent.putExtra("email", email);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                webSocket.close(1000, "Closing the connection");
                finish();
            }
        }
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
    private Bitmap generateQRCode(String email) {
        try {
            String qrCodeData = email;
            String charset = "UTF-8";


            Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            BitMatrix matrix = new MultiFormatWriter().encode(
                    new String(qrCodeData.getBytes(charset), charset),
                    BarcodeFormat.QR_CODE, 350, 350, hintMap);

            int width = matrix.getWidth();
            int height = matrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void closeWebSocketConnectionHome() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing the connection");
        }
    }
}