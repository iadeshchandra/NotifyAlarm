package com.yourname.notifalarm;

import android.content.SharedPreferences;
import android.os.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch swSound, swVibrate, swActiveHours;
    private SeekBar seekRepeat, seekSnooze;
    private TextView tvRepeatLabel, tvSnoozeLabel;
    private NumberPicker npStartHour, npEndHour;
    private Button btnReset;
    private SharedPreferences prefs;

    private static final int[] INTERVALS_MS  = {1000,2000,3000,5000,10000};
    private static final String[] INT_LABELS = {"1 sec","2 sec","3 sec","5 sec","10 sec"};
    private static final int[] SNOOZE_MINS   = {2,5,10,15,30};
    private static final String[] SNO_LABELS = {"2 min","5 min","10 min","15 min","30 min"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("⚙️ Settings");

        prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        bindViews();
        loadSettings();
        setupListeners();
    }

    private void bindViews() {
        swSound       = findViewById(R.id.swSound);
        swVibrate     = findViewById(R.id.swVibrate);
        swActiveHours = findViewById(R.id.swActiveHours);
        seekRepeat    = findViewById(R.id.seekRepeat);
        seekSnooze    = findViewById(R.id.seekSnooze);
        tvRepeatLabel = findViewById(R.id.tvRepeatLabel);
        tvSnoozeLabel = findViewById(R.id.tvSnoozeLabel);
        npStartHour   = findViewById(R.id.npStartHour);
        npEndHour     = findViewById(R.id.npEndHour);
        btnReset      = findViewById(R.id.btnReset);

        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) hours[i] = String.format("%02d:00", i);
        npStartHour.setMinValue(0); npStartHour.setMaxValue(23); npStartHour.setDisplayedValues(hours);
        npEndHour.setMinValue(0);   npEndHour.setMaxValue(23);   npEndHour.setDisplayedValues(hours);
    }

    private void loadSettings() {
        swSound.setChecked(prefs.getBoolean(AppConstants.KEY_SOUND_ENABLED, true));
        swVibrate.setChecked(prefs.getBoolean(AppConstants.KEY_VIBRATE, true));
        swActiveHours.setChecked(prefs.getBoolean(AppConstants.KEY_ACTIVE_HOURS_ON, false));
        npStartHour.setValue(prefs.getInt(AppConstants.KEY_HOUR_START, 8));
        npEndHour.setValue(prefs.getInt(AppConstants.KEY_HOUR_END, 22));

        int rPos = msToPos(prefs.getInt(AppConstants.KEY_REPEAT_MS, 2000), INTERVALS_MS);
        seekRepeat.setMax(INTERVALS_MS.length - 1); seekRepeat.setProgress(rPos);
        tvRepeatLabel.setText("Alarm repeat: " + INT_LABELS[rPos]);

        int sPos = minToPos(prefs.getInt(AppConstants.KEY_SNOOZE_MIN, 5), SNOOZE_MINS);
        seekSnooze.setMax(SNOOZE_MINS.length - 1); seekSnooze.setProgress(sPos);
        tvSnoozeLabel.setText("Snooze duration: " + SNO_LABELS[sPos]);
    }

    private void setupListeners() {
        swSound.setOnCheckedChangeListener((b,v) -> prefs.edit().putBoolean(AppConstants.KEY_SOUND_ENABLED, v).apply());
        swVibrate.setOnCheckedChangeListener((b,v) -> prefs.edit().putBoolean(AppConstants.KEY_VIBRATE, v).apply());
        swActiveHours.setOnCheckedChangeListener((b,v) -> prefs.edit().putBoolean(AppConstants.KEY_ACTIVE_HOURS_ON, v).apply());
        npStartHour.setOnValueChangedListener((p,o,n) -> prefs.edit().putInt(AppConstants.KEY_HOUR_START, n).apply());
        npEndHour.setOnValueChangedListener((p,o,n) -> prefs.edit().putInt(AppConstants.KEY_HOUR_END, n).apply());

        seekRepeat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean u) {
                tvRepeatLabel.setText("Alarm repeat: " + INT_LABELS[p]);
                prefs.edit().putInt(AppConstants.KEY_REPEAT_MS, INTERVALS_MS[p]).apply();
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });

        seekSnooze.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean u) {
                tvSnoozeLabel.setText("Snooze duration: " + SNO_LABELS[p]);
                prefs.edit().putInt(AppConstants.KEY_SNOOZE_MIN, SNOOZE_MINS[p]).apply();
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });

        btnReset.setOnClickListener(v -> {
            prefs.edit()
                .putInt(AppConstants.KEY_ALARM_COUNT, 0)
                .remove(AppConstants.KEY_HISTORY)
                .apply();
            Toast.makeText(this, "✅ Stats reset", Toast.LENGTH_SHORT).show();
        });
    }

    private int msToPos(int ms, int[] arr) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == ms) return i;
        return 1;
    }
    private int minToPos(int min, int[] arr) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == min) return i;
        return 1;
    }
}
