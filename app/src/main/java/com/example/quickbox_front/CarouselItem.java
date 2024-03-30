package com.example.quickbox_front;

public class CarouselItem {
    private String id;
    private String time;
    private String status;

    public CarouselItem(String id, String time, String status) {
        this.id = id;
        this.time = time;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}