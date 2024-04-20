package com.example.quickbox_front;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketForegroundService extends Service {
    private final IBinder binder = new LocalBinder();
    private WebSocket webSocket;
    public static final String ACTION_STOP_SERVICE = "com.example.quickbox_front.STOP_SERVICE";

    private final BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
                stopForeground(true); // Remove the ongoing notification
                stopSelf();
                System.exit(0);
            }
        }
    };

    public class LocalBinder extends Binder {
        WebSocketForegroundService getService() {
            return WebSocketForegroundService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(stopServiceReceiver, new IntentFilter(ACTION_STOP_SERVICE));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startWebSocket();
        return START_STICKY;
    }

    private void startWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://" + IPServer.IP + ":8000/ws/home")
                .build();

        WebSocketListener webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                super.onOpen(webSocket, response);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);

                try {
                    JSONObject jsonObject = new JSONObject(text);
                    if (jsonObject.has("type")) {
                        Log.d("WebSocket", "Received location: " + jsonObject.toString());
                        String title = "QuickBox";
                        String id = jsonObject.getString("delivery_id");
                        String status = jsonObject.getString("status");
                        String message = "Delivery #" + id + " is now: " + status;

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(WebSocketForegroundService.this, "ForegroundServiceChannel")
                                .setContentTitle(title)
                                .setContentText(message)
                                .setSmallIcon(R.drawable.notification_icon)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                        NotificationManager manager = getSystemService(NotificationManager.class);
                        manager.notify(1, builder.build()); // Use a unique notification ID
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                super.onFailure(webSocket, t, response);
            }
        };

        webSocket = client.newWebSocket(request, webSocketListener);

        NotificationChannel serviceChannel = new NotificationChannel(
                "ForegroundServiceChannel",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);

        Intent stopServiceIntent = new Intent(ACTION_STOP_SERVICE);
        PendingIntent stopServicePendingIntent = PendingIntent.getBroadcast(this, 0, stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ForegroundServiceChannel")
                .setContentTitle("QuickBox")
                .setContentText(getString(R.string.backRunnig))
                .setSmallIcon(R.drawable.notification_icon)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .addAction(R.drawable.notification_icon, "Stop", stopServicePendingIntent); // Add the stop button

        startForeground(1, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(stopServiceReceiver);
        if (webSocket != null) {
            webSocket.cancel();
        }
    }
}