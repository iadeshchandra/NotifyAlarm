package com.yourname.notifalarm;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {

    public static volatile boolean isRunning  = false;
    public static volatile boolean isSnoozed  = false;
    public static String lastSource   = "";
    public static String lastKeyword  = "";

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private BroadcastReceiver stopReceiver;
    private Handler repeatHandler;
    private Runnable repeatRunnable;
    private PowerManager.WakeLock wakeLock;

    private static final String CHANNEL_ID = "AlarmChannel";
    private static final int    NOTIF_ID   = 3001;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        registerStopReceiver();
        repeatHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        String action = intent.getAction();
        if (AppConstants.ACTION_START_ALARM.equals(action) && !isRunning) {
            lastSource  = intent.getStringExtra("source");
            lastKeyword = intent.getStringExtra("keyword");
            String title = intent.getStringExtra("title");
            String text  = intent.getStringExtra("text");
            startAlarm(title, text);
        } else if (AppConstants.ACTION_SNOOZE_ALARM.equals(action)) {
            snoozeAlarm();
        }
        return START_STICKY;
    }

    // ── START ─────────────────────────────────────────────────────────────────

    private void startAlarm(String title, String text) {
        isRunning = true;
        isSnoozed = false;

        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        boolean soundOn  = prefs.getBoolean(AppConstants.KEY_SOUND_ENABLED, true);
        boolean vibrOn   = prefs.getBoolean(AppConstants.KEY_VIBRATE, true);
        int repeatMs     = prefs.getInt(AppConstants.KEY_REPEAT_MS, 2000);

        // Wake lock: keep CPU alive on locked screen
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NotifAlarm::Lock");
        if (!wakeLock.isHeld()) wakeLock.acquire(10 * 60 * 1000L);

        startForeground(NOTIF_ID, buildNotification(title, text));

        if (soundOn) playSound(repeatMs);

        if (vibrOn) {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                long[] pat = {0, 600, 300, 600, 800};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    vibrator.vibrate(VibrationEffect.createWaveform(pat, 0));
                else
                    vibrator.vibrate(pat, 0);
            }
        }

        sendBroadcast(new Intent(AppConstants.ACTION_ALARM_STARTED));
        sendBroadcast(new Intent(AppConstants.ACTION_UI_REFRESH));
    }

    private void playSound(int repeatMs) {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
            if (mediaPlayer == null) {
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                mediaPlayer = MediaPlayer.create(this, uri);
            }
            if (mediaPlayer != null) {
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.setLooping(false);
                mediaPlayer.start();
                repeatRunnable = new Runnable() {
                    @Override public void run() {
                        if (isRunning && !isSnoozed && mediaPlayer != null) {
                            try { mediaPlayer.seekTo(0); mediaPlayer.start(); } catch (Exception ignored) {}
                            repeatHandler.postDelayed(this, repeatMs);
                        }
                    }
                };
                repeatHandler.postDelayed(repeatRunnable, repeatMs);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── SNOOZE ────────────────────────────────────────────────────────────────

    private void snoozeAlarm() {
        isSnoozed = true;
        if (mediaPlayer != null) { try { mediaPlayer.pause(); } catch (Exception ignored) {} }
        if (vibrator != null) vibrator.cancel();
        if (repeatRunnable != null) repeatHandler.removeCallbacks(repeatRunnable);

        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        int snoozeMin = prefs.getInt(AppConstants.KEY_SNOOZE_MIN, 5);

        // Resume alarm after snooze duration
        repeatHandler.postDelayed(() -> {
            if (isSnoozed && isRunning) {
                isSnoozed = false;
                int repeatMs = prefs.getInt(AppConstants.KEY_REPEAT_MS, 2000);
                playSound(repeatMs);
                if (prefs.getBoolean(AppConstants.KEY_VIBRATE, true) && vibrator != null) {
                    long[] pat = {0, 600, 300, 600, 800};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        vibrator.vibrate(VibrationEffect.createWaveform(pat, 0));
                    else
                        vibrator.vibrate(pat, 0);
                }
            }
        }, snoozeMin * 60 * 1000L);

        sendBroadcast(new Intent(AppConstants.ACTION_UI_REFRESH));
    }

    // ── STOP ─────────────────────────────────────────────────────────────────

    void stopAlarm() {
        isRunning = false;
        isSnoozed = false;
        if (repeatRunnable != null) repeatHandler.removeCallbacks(repeatRunnable);
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); mediaPlayer.release(); } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        if (vibrator != null) { vibrator.cancel(); vibrator = null; }
        if (wakeLock != null && wakeLock.isHeld()) { try { wakeLock.release(); } catch (Exception ignored) {} }
        stopForeground(true);
        stopSelf();
        sendBroadcast(new Intent(AppConstants.ACTION_ALARM_STOPPED));
        sendBroadcast(new Intent(AppConstants.ACTION_UI_REFRESH));
    }

    // ── NOTIFICATION ─────────────────────────────────────────────────────────

    private Notification buildNotification(String title, String text) {
        Intent openApp = new Intent(this, MainActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent piOpen = PendingIntent.getActivity(this, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent piStop = PendingIntent.getBroadcast(this, 1,
                new Intent(AppConstants.ACTION_STOP_ALARM),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent piSnooze = PendingIntent.getService(this, 2,
                new Intent(this, AlarmService.class).setAction(AppConstants.ACTION_SNOOZE_ALARM),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("🔔 " + (lastSource != null ? lastSource : "App") + " Alert!")
                .setContentText(title != null && !title.isEmpty() ? title : text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText((text != null ? text : "") + "\n\nKeyword: \"" + lastKeyword + "\""))
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentIntent(piOpen)
                .addAction(android.R.drawable.ic_media_pause,  "💤 Snooze", piSnooze)
                .addAction(android.R.drawable.ic_delete,        "⏹ Stop",   piStop)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Alarm Alerts", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Active when alarm is running");
            ch.setSound(null, null);
            ch.enableVibration(false);
            ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private void registerStopReceiver() {
        stopReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context ctx, Intent intent) { stopAlarm(); }
        };
        registerReceiver(stopReceiver, new IntentFilter(AppConstants.ACTION_STOP_ALARM));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAlarm();
        try { unregisterReceiver(stopReceiver); } catch (Exception ignored) {}
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
