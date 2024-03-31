package com.example.quickbox_front;

public class CarouselItem {
    private Integer id;
    private Integer user_id;
    private String time;
    private String status;

    public CarouselItem(Integer id, Integer user_id, String time, String status) {
        this.id = id;
        this.user_id = user_id;
        this.time = time;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}