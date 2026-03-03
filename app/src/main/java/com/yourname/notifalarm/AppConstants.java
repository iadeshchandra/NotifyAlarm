package com.yourname.notifalarm;

import java.util.*;

public class AppConstants {
    public static final String PREF_NAME           = "NotifAlarmPrefs";
    public static final String KEY_KEYWORDS        = "keywords";
    public static final String KEY_MASTER_ON       = "master_on";
    public static final String KEY_ALARM_COUNT     = "alarm_count";
    public static final String KEY_REPEAT_MS       = "repeat_ms";
    public static final String KEY_VIBRATE         = "vibrate";
    public static final String KEY_SOUND_ENABLED   = "sound_enabled";
    public static final String KEY_ENABLED_APPS    = "enabled_apps";
    public static final String KEY_HISTORY         = "alarm_history";
    public static final String KEY_ACTIVE_HOURS_ON = "active_hours_on";
    public static final String KEY_HOUR_START      = "hour_start";
    public static final String KEY_HOUR_END        = "hour_end";
    public static final String KEY_SNOOZE_MIN      = "snooze_min";
    public static final String KEY_DARK_MODE       = "dark_mode";
    public static final String KEY_IGNORE_APPS     = "ignore_apps";

    public static final String ACTION_START_ALARM   = "com.yourname.notifalarm.START_ALARM";
    public static final String ACTION_STOP_ALARM    = "com.yourname.notifalarm.STOP_ALARM";
    public static final String ACTION_SNOOZE_ALARM  = "com.yourname.notifalarm.SNOOZE_ALARM";
    public static final String ACTION_ALARM_STARTED = "com.yourname.notifalarm.ALARM_STARTED";
    public static final String ACTION_ALARM_STOPPED = "com.yourname.notifalarm.ALARM_STOPPED";
    public static final String ACTION_UI_REFRESH    = "com.yourname.notifalarm.UI_REFRESH";

    // Supported apps: {packageName, displayName, emoji}
    public static final String[][] SUPPORTED_APPS = {
        {"com.fiverr.fiverr",                  "Fiverr",             "🟢"},
        {"com.upwork.android.apps.main",        "Upwork",             "🟢"},
        {"com.google.android.gm",               "Gmail",              "📧"},
        {"com.whatsapp",                        "WhatsApp",           "💬"},
        {"com.whatsapp.w4b",                    "WhatsApp Business",  "💼"},
        {"org.telegram.messenger",              "Telegram",           "✈️"},
        {"com.facebook.orca",                   "Messenger",          "💙"},
        {"com.linkedin.android",                "LinkedIn",           "🔵"},
        {"com.slack",                           "Slack",              "🟣"},
        {"com.microsoft.teams",                 "Microsoft Teams",    "🔷"},
        {"com.instagram.android",               "Instagram",          "📸"},
        {"com.twitter.android",                 "Twitter / X",        "🐦"},
    };

    public static Set<String> defaultKeywords() {
        return new HashSet<>(Arrays.asList(
            "new message", "new order", "order placed",
            "buyer request", "new inquiry", "hired you",
            "new proposal", "contract", "milestone",
            "sent you an offer", "interview", "custom offer",
            "revision request", "order completed",
            "payment received", "new client"
        ));
    }

    public static Set<String> defaultEnabledApps() {
        return new HashSet<>(Arrays.asList(
            "com.fiverr.fiverr",
            "com.upwork.android.apps.main"
        ));
    }
}
