package com.example.quickbox_front;

public class Format_time {
    private String time;

    public Format_time(String time) {
        this.time = time;
    }

    public String getFormattedTime() {
        String[] timeSplit = time.split("T");
        String[] dateSplit = timeSplit[0].split("-");
        String[] timeSplit2 = timeSplit[1].split(":");
        String formattedTime = dateSplit[2] + "." + dateSplit[1] + "." + dateSplit[0] + "  " + timeSplit2[0] + ":" + timeSplit2[1];
        return formattedTime;
    }
}
