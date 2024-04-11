package com.example.quickbox_front;

public class CarouselItem {
    private Integer id;
    private String time;
    private String status;

    public CarouselItem(Integer id, String status, String time) {
        this.id = id;
        this.time = time;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}