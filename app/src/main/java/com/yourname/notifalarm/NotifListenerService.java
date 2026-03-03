package com.yourname.notifalarm;

import android.app.Notification;
import android.content.*;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.*;

public class NotifListenerService extends NotificationListenerService {

    private SharedPreferences prefs;
    private HistoryManager historyManager;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        historyManager = new HistoryManager(prefs);
    }

    /**
     * Called AUTOMATICALLY by Android every time any app posts a notification.
     * Works even when screen is off, app is closed, or phone is locked.
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;
        if (!prefs.getBoolean(AppConstants.KEY_MASTER_ON, true)) return;
        if (AlarmService.isRunning) return;
        if (!isWithinActiveHours()) return;

        String pkg = sbn.getPackageName();
        // Ignore our own notifications
        if (pkg.equals(getPackageName())) return;
        if (!isAppEnabled(pkg)) return;

        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;

        String title   = extras.getString(Notification.EXTRA_TITLE, "");
        String text    = extras.getString(Notification.EXTRA_TEXT, "");
        String bigText = extras.getString(Notification.EXTRA_BIG_TEXT, "");
        String full    = (title + " " + text + " " + bigText).toLowerCase().trim();
        if (full.isEmpty()) return;

        // Check ignored senders
        if (isIgnored(title)) return;

        // Match keywords — UNLIMITED
        Set<String> keywords = prefs.getStringSet(
                AppConstants.KEY_KEYWORDS, AppConstants.defaultKeywords());
        for (String kw : keywords) {
            if (!kw.isEmpty() && full.contains(kw.toLowerCase())) {
                triggerAlarm(getAppName(pkg), kw, title, text);
                return;
            }
        }
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private boolean isAppEnabled(String pkg) {
        Set<String> enabled = prefs.getStringSet(
                AppConstants.KEY_ENABLED_APPS, AppConstants.defaultEnabledApps());
        return enabled.contains("ALL") || enabled.contains(pkg);
    }

    private boolean isIgnored(String sender) {
        Set<String> ignored = prefs.getStringSet(AppConstants.KEY_IGNORE_APPS, new HashSet<>());
        if (sender == null) return false;
        for (String ig : ignored) {
            if (sender.toLowerCase().contains(ig.toLowerCase())) return true;
        }
        return false;
    }

    private boolean isWithinActiveHours() {
        if (!prefs.getBoolean(AppConstants.KEY_ACTIVE_HOURS_ON, false)) return true;
        int start = prefs.getInt(AppConstants.KEY_HOUR_START, 8);
        int end   = prefs.getInt(AppConstants.KEY_HOUR_END, 22);
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (start <= end) return hour >= start && hour < end;
        return hour >= start || hour < end; // overnight e.g. 22–6
    }

    private String getAppName(String pkg) {
        for (String[] app : AppConstants.SUPPORTED_APPS) {
            if (app[0].equals(pkg)) return app[1];
        }
        return pkg;
    }

    private void triggerAlarm(String appName, String keyword, String title, String text) {
        // Save to history
        AlarmEvent event = new AlarmEvent(appName, keyword, title, text);
        historyManager.addEvent(event);

        // Increment counter
        int count = prefs.getInt(AppConstants.KEY_ALARM_COUNT, 0);
        prefs.edit().putInt(AppConstants.KEY_ALARM_COUNT, count + 1).apply();

        // Start alarm
        Intent intent = new Intent(this, AlarmService.class);
        intent.setAction(AppConstants.ACTION_START_ALARM);
        intent.putExtra("source",  appName);
        intent.putExtra("keyword", keyword);
        intent.putExtra("title",   title);
        intent.putExtra("text",    text);
        startService(intent);

        // Refresh UI if open
        sendBroadcast(new Intent(AppConstants.ACTION_UI_REFRESH));
    }
}
