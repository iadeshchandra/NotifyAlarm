package com.yourname.notifalarm;

public class AlarmEvent {
    public String appName;
    public String keyword;
    public String title;
    public String text;
    public long timestamp;
    public boolean snoozed;

    public AlarmEvent(String appName, String keyword, String title, String text) {
        this.appName   = appName;
        this.keyword   = keyword;
        this.title     = title;
        this.text      = text;
        this.timestamp = System.currentTimeMillis();
        this.snoozed   = false;
    }

    public String getTimeAgo() {
        long diff = System.currentTimeMillis() - timestamp;
        long mins = diff / 60000;
        if (mins < 1)  return "Just now";
        if (mins < 60) return mins + "m ago";
        long hrs = mins / 60;
        if (hrs < 24)  return hrs + "h ago";
        return (hrs / 24) + "d ago";
    }

    public String getFormattedTime() {
        java.text.SimpleDateFormat sdf =
            new java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
}
