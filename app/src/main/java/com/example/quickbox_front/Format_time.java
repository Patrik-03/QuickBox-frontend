package com.example.quickbox_front;

public class Format_time {
    private String time;

    public Format_time(String time) {
        this.time = time;
    }

    public String getFormattedTime() {
        if (time == null) {
            return "";
        }
        String[] timeSplit = time.split("T");
        if (timeSplit.length < 2) {
            return time; // or some default value
        }
        String[] dateSplit = timeSplit[0].split("-");
        if (dateSplit.length < 3) {
            return time; // or some default value
        }
        String[] timeSplit2 = timeSplit[1].split(":");
        if (timeSplit2.length < 2) {
            return time; // or some default value
        }
        String formattedTime = dateSplit[2] + "." + dateSplit[1] + "." + dateSplit[0] + "  " + timeSplit2[0] + ":" + timeSplit2[1];
        return formattedTime;
    }
}
