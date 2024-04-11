package com.example.quickbox_front;

import android.app.NotificationManager;

public class Notification extends android.app.Notification {
    private String title;
    private String message;
    private String time;

    public Notification(String title, String message, NotificationManager time) {
        this.title = title;
        this.message = message;
        this.time = String.valueOf(time);
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }
}
