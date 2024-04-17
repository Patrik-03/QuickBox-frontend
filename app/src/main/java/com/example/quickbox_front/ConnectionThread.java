package com.example.quickbox_front;

import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;

public class ConnectionThread {
    private volatile boolean running = true;
    @NonNull
    public Thread getThread(SharedPreferences sharedPreferencesConnection, View noConnection) {
        final Handler handler = new Handler();
        return new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    handler.post(() -> {
                        if (sharedPreferencesConnection.getBoolean("connection", false)) {
                            noConnection.setVisibility(View.GONE);
                            running = false;
                        } else {
                            noConnection.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
