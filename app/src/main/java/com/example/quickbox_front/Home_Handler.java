package com.example.quickbox_front;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Home_Handler extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE_NOT = 1;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE_FOREGROUND = 1;
    private WebSocket webSocket;
    DataClient dataClient;
    Handler handler = new Handler(Looper.getMainLooper());
    WebSocketManager webSocketManager = WebSocketManager.getInstance();
    private SwipeRefreshLayout swipeRefreshLayout;
    Boolean nameSet = false;
    Float longitude, latitude;
    ImageView no_connection;
    String idGet;
    WebSocketListener webSocketListener;
    WebSocketForegroundService webSocketService;
    boolean openConnection = false;
    private boolean isBound = false;


    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        loadLocale();
        setContentView(R.layout.home);
        // Check request notifications permission
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != getPackageManager().PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE_NOT);
        }
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != getPackageManager().PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
        }
        //request background service permission
        if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != getPackageManager().PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.FOREGROUND_SERVICE}, PERMISSION_REQUEST_CODE_FOREGROUND);
        }

        ImageButton profile = findViewById(R.id.profileH);
        Button create = findViewById(R.id.createDel);
        CardView map = findViewById(R.id.mapH);

        no_connection = findViewById(R.id.no_connection);

        // Bind to the WebSocketForegroundService
        Intent serviceIntent = new Intent(this, WebSocketForegroundService.class);

        dataClient = Wearable.getDataClient(Home_Handler.this);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        SharedPreferences sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences sharedPreferencesDeliveries = getSharedPreferences("Deliveries", MODE_PRIVATE);
        SharedPreferences sharedPreferencesMap = getSharedPreferences("Map", MODE_PRIVATE);
        SharedPreferences sharedPreferencesHistory = getSharedPreferences("History", MODE_PRIVATE);
        SharedPreferences sharedPreferencesConnection = getSharedPreferences("Connection", MODE_PRIVATE);

        String email = sharedPreferences.getString("email", "");
        idGet = sharedPreferences.getString("id", "");

        longitude = sharedPreferences.getFloat("longitude", 0);
        latitude = sharedPreferences.getFloat("latitude", 0);

        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Not used in this case
            }

            @Override
            public void onPageSelected(int position) {
                // Not used in this case
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    swipeRefreshLayout.setEnabled(false); // Disable swipe refresh during dragging
                } else if (state == ViewPager.SCROLL_STATE_IDLE) {
                    swipeRefreshLayout.setEnabled(true);  // Re-enable swipe refresh when idle
                }
            }
        });


        List<CarouselItem> items = new ArrayList<>();

        if (sharedPreferencesDeliveries.getAll().isEmpty()) {
            items.add(new CarouselItem(0, "No delivery", "No delivery"));
        } else {
            for (int i = 0; i < sharedPreferencesDeliveries.getAll().size(); i++) {
                try {
                    JSONObject jsonObject = new JSONObject(sharedPreferencesDeliveries.getString("idHome" + i, ""));
                    Integer id = jsonObject.getInt("id");
                    String delivery_time = jsonObject.getString("delivery_time");
                    String status = jsonObject.getString("status");
                    items.add(new CarouselItem(id, status, delivery_time));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        runOnUiThread(() -> {
            MyPagerAdapter myPagerAdapter = new MyPagerAdapter(Home_Handler.this, items);
            viewPager.setAdapter(myPagerAdapter);
        });
        openConnection = true;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://" + IPServer.IP + ":8000/ws/home")
                .build();

        webSocket = client.newWebSocket(request, webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                webSocketManager.startWebSocket(idGet, sharedPreferencesMap);
                startForegroundService(serviceIntent);
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
                openConnection = true;
                runOnUiThread(() -> no_connection.setVisibility(View.GONE));
                sharedPreferencesConnection.getAll().clear();
                sharedPreferencesConnection.edit().putBoolean("connection", true).apply();
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    if (jsonObject.has("name")) {
                        try {
                            String nameGet = jsonObject.getString("name");
                            String qrCode = jsonObject.getString("qr_code");
                            sharedPreferences.edit().putString("name", nameGet).apply();
                            sharedPreferences.edit().putString("qr_code", qrCode).apply();
                            nameSet = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (jsonObject.has("items")) {
                        try {
                            JSONArray jsonArray = jsonObject.getJSONArray("items");
                            sendDataToWearable(jsonArray.toString());

                            items.clear();
                            sharedPreferencesDeliveries.edit().clear().apply();

                            if (jsonArray.length() == 0) {
                                items.add(new CarouselItem(0, "No delivery", "No delivery"));
                            } else {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject delivery = jsonArray.getJSONObject(i);
                                    Integer id = delivery.getInt("id");
                                    String from = delivery.getString("from");
                                    Integer user_id = Integer.valueOf(idGet);
                                    String sent_time = delivery.getString("sent_time");
                                    String delivery_time = delivery.getString("delivery_time");
                                    String status = delivery.getString("status");
                                    String note = delivery.getString("note");

                                    // Add the image to the images list
                                    items.add(new CarouselItem(id, status, delivery_time));
                                    // Save the delivery ID to SharedPreferences
                                    JSONObject jsonObject1 = new JSONObject();
                                    jsonObject1.put("id", id);
                                    jsonObject1.put("user_id", user_id);
                                    jsonObject1.put("from", from);
                                    jsonObject1.put("sent_time", sent_time);
                                    jsonObject1.put("delivery_time", delivery_time);
                                    jsonObject1.put("status", status);
                                    jsonObject1.put("note", note);
                                    sharedPreferencesDeliveries.edit()
                                            .putString("idHome" + i, String.valueOf(jsonObject1))
                                            .apply();
                                }
                            }
                            runOnUiThread(() -> {
                                MyPagerAdapter myPagerAdapter = new MyPagerAdapter(Home_Handler.this, items);
                                viewPager.setAdapter(myPagerAdapter);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (jsonObject.has("type")) {
                        String title = "QuickBox";
                        String id = jsonObject.getString("delivery_id");
                        String status = jsonObject.getString("status");
                        String message = "Delivery #" + id + " is now: " + status;

                        if (status.equals("Delivered")) {
                            for (int i = 0; i < sharedPreferencesDeliveries.getAll().size(); i++) {
                                try {
                                    JSONObject jsonObject1 = new JSONObject(sharedPreferencesDeliveries.getString("idHome" + i, ""));
                                    if (jsonObject1.getInt("id") == Integer.parseInt(id)) {
                                        jsonObject1.put("status", "Delivered"); // replace status with delivered
                                        sharedPreferencesHistory.edit()
                                                .putString("idHistory" + id, String.valueOf(jsonObject1))
                                                .apply();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        Intent intent = new Intent(getApplicationContext(), Home_Handler.class);
                        showNotification(getApplicationContext(), title, message, intent, 0);
                        refreshData();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("WebSocket", "Connection closed (Home)");
                openConnection = false;  // Add this line
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocket", "Connection failed: " + t.getMessage());
                runOnUiThread(() -> no_connection.setVisibility(View.VISIBLE));
                webSocketManager.stopWebSocket();
                handler.removeCallbacksAndMessages(null);
                sharedPreferencesConnection.getAll().clear();
                sharedPreferencesConnection.edit().putBoolean("connection", false).apply();
                openConnection = false;  // Add this line
                Runnable runnableCode = new Runnable() {
                    @Override
                    public void run() {
                        // Try to reconnect only if the connection was not previously open
                        if (!openConnection) {
                            Home_Handler.this.webSocket = client.newWebSocket(request, Home_Handler.this.webSocketListener);
                            handler.postDelayed(this, 5000);
                        }
                    }
                };
                handler.post(runnableCode);
            }
        });


        profile.setOnClickListener(v -> {
            Intent intent2 = new Intent(Home_Handler.this, Profile_Handler.class);
            intent2.putExtra("id", idGet);
            intent2.putExtra("email", email);
            // Retrieve Base64 string from SharedPreferences
            String qrCode = sharedPreferences.getString("qr_code", "");
            Bitmap qrCodeBitmap = convertBase64StringToBitmap(qrCode);
            intent2.putExtra("qr_code", qrCodeBitmap);
            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent2, 1);
            overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation);
        });

        create.setOnClickListener(v -> {
            Intent intent3 = new Intent(Home_Handler.this, CreateDel_Handler.class);
            intent3.putExtra("email", email);
            intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent3);
            overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation);
        });

        map.setOnClickListener(v -> {
            Intent intent4 = new Intent(Home_Handler.this, Map_Handler.class);
            intent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent4.putExtra("longitude", longitude);
            intent4.putExtra("latitude", latitude);
            startActivity(intent4);
            overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation);
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            WebSocketForegroundService.LocalBinder binder = (WebSocketForegroundService.LocalBinder) service;
            webSocketService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            isBound = false;
        }
    };

    public Bitmap convertBase64StringToBitmap(String base64String) {
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void sendDataToWearable(String data) {
        DataClient dataClient = Wearable.getDataClient(this);
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/deliveries");
        putDataMapReq.getDataMap().putString("message", data);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest().setUrgent();
        Task<DataItem> putDataTask = dataClient.putDataItem(putDataReq);
        putDataTask.addOnSuccessListener(dataItem -> Log.d("WebSocket", "Data item set: " + dataItem));
        putDataTask.addOnFailureListener(e -> Log.e("WebSocket", "Failed to set data item: " + e.getMessage()));
    }
        public void showNotification(Context context, String title, String message, Intent intent, int reqCode) {

        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        String CHANNEL_ID = "QuickBox";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence name = "QuickBox";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        notificationManager.createNotificationChannel(mChannel);
        notificationManager.notify(reqCode, notificationBuilder.build());
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

    private void refreshData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", idGet);
            jsonObject.put("del_id", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        webSocket.send(jsonObject.toString());
        JSONObject jsonObject1 = new JSONObject();
        try {
            jsonObject1.put("id", idGet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        swipeRefreshLayout.setRefreshing(false);
    }
}