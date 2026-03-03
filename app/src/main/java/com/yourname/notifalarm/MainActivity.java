package com.yourname.notifalarm;

import android.content.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.*;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    // Home tab views
    private CardView cardStatus;
    private TextView tvStatusTitle, tvStatusDesc, tvAlarmCount, tvLastTrigger;
    private Switch masterSwitch;
    private com.google.android.material.button.MaterialButton btnStopAlarm, btnSnooze, btnTestAlarm;

    // Bottom nav tabs
    private LinearLayout tabHome, tabApps, tabKeywords, tabHistory, tabSettings;
    private View[] tabViews;
    private int currentTab = 0;

    // Content panels
    private View panelHome, panelApps, panelKeywords, panelHistory;

    // Apps tab
    private LinearLayout appListContainer;
    private Switch switchAllApps;

    // Keywords tab
    private EditText etKeyword;
    private LinearLayout keywordChips;
    private Button btnAddKeyword;
    private TextView tvKeywordCount;

    // History tab
    private LinearLayout historyList;
    private Button btnClearHistory;
    private TextView tvNoHistory;

    SharedPreferences prefs;
    private HistoryManager historyManager;
    private BroadcastReceiver uiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        historyManager = new HistoryManager(prefs);
        initViews();
        loadAllData();
        setupListeners();
        registerUiReceiver();
        checkPermission();
    }

    @Override protected void onResume() { super.onResume(); refreshHomeTab(); }
    @Override protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(uiReceiver); } catch (Exception ignored) {}
    }

    // ── INIT ─────────────────────────────────────────────────────────────────

    private void initViews() {
        // Home
        cardStatus    = findViewById(R.id.cardStatus);
        tvStatusTitle = findViewById(R.id.tvStatusTitle);
        tvStatusDesc  = findViewById(R.id.tvStatusDesc);
        tvAlarmCount  = findViewById(R.id.tvAlarmCount);
        tvLastTrigger = findViewById(R.id.tvLastTrigger);
        masterSwitch  = findViewById(R.id.masterSwitch);
        btnStopAlarm  = findViewById(R.id.btnStopAlarm);
        btnSnooze     = findViewById(R.id.btnSnooze);
        btnTestAlarm  = findViewById(R.id.btnTestAlarm);

        // Tabs
        tabHome     = findViewById(R.id.tabHome);
        tabApps     = findViewById(R.id.tabApps);
        tabKeywords = findViewById(R.id.tabKeywords);
        tabHistory  = findViewById(R.id.tabHistory);
        tabSettings = findViewById(R.id.tabSettings);

        // Panels
        panelHome     = findViewById(R.id.panelHome);
        panelApps     = findViewById(R.id.panelApps);
        panelKeywords = findViewById(R.id.panelKeywords);
        panelHistory  = findViewById(R.id.panelHistory);

        // Apps
        appListContainer = findViewById(R.id.appListContainer);
        switchAllApps    = findViewById(R.id.switchAllApps);

        // Keywords
        etKeyword      = findViewById(R.id.etKeyword);
        keywordChips   = findViewById(R.id.keywordChips);
        btnAddKeyword  = findViewById(R.id.btnAddKeyword);
        tvKeywordCount = findViewById(R.id.tvKeywordCount);

        // History
        historyList    = findViewById(R.id.historyList);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        tvNoHistory    = findViewById(R.id.tvNoHistory);
    }

    // ── TABS ─────────────────────────────────────────────────────────────────

    private void showTab(int idx) {
        currentTab = idx;
        View[] panels = {panelHome, panelApps, panelKeywords, panelHistory};
        for (int i = 0; i < panels.length; i++) {
            panels[i].setVisibility(i == idx ? View.VISIBLE : View.GONE);
        }
        LinearLayout[] tabs = {tabHome, tabApps, tabKeywords, tabHistory};
        int active   = ContextCompat.getColor(this, R.color.primaryGreen);
        int inactive = ContextCompat.getColor(this, R.color.textMuted);
        for (int i = 0; i < tabs.length; i++) {
            ((TextView) tabs[i].getChildAt(1)).setTextColor(i == idx ? active : inactive);
        }
        if (idx == 2) renderKeywordChips();
        if (idx == 3) renderHistory();
    }

    // ── SETUP LISTENERS ──────────────────────────────────────────────────────

    private void setupListeners() {
        tabHome.setOnClickListener(v     -> showTab(0));
        tabApps.setOnClickListener(v     -> showTab(1));
        tabKeywords.setOnClickListener(v -> showTab(2));
        tabHistory.setOnClickListener(v  -> showTab(3));
        tabSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        masterSwitch.setOnCheckedChangeListener((b, on) -> {
            prefs.edit().putBoolean(AppConstants.KEY_MASTER_ON, on).apply();
            refreshHomeTab();
            Toast.makeText(this, on ? "✅ Monitoring ON" : "⏸ Monitoring PAUSED",
                    Toast.LENGTH_SHORT).show();
        });

        btnStopAlarm.setOnClickListener(v ->
                sendBroadcast(new Intent(AppConstants.ACTION_STOP_ALARM)));

        btnSnooze.setOnClickListener(v -> {
            int min = prefs.getInt(AppConstants.KEY_SNOOZE_MIN, 5);
            Intent i = new Intent(this, AlarmService.class);
            i.setAction(AppConstants.ACTION_SNOOZE_ALARM);
            startService(i);
            Toast.makeText(this, "💤 Snoozed for " + min + " minutes", Toast.LENGTH_SHORT).show();
        });

        btnTestAlarm.setOnClickListener(v -> {
            Intent i = new Intent(this, AlarmService.class);
            i.setAction(AppConstants.ACTION_START_ALARM);
            i.putExtra("source",  "Test");
            i.putExtra("keyword", "test");
            i.putExtra("title",   "Test Alarm");
            i.putExtra("text",    "This is a test alarm!");
            startService(i);
        });

        btnAddKeyword.setOnClickListener(v -> addKeyword());
        etKeyword.setOnEditorActionListener((v, id, ev) -> { addKeyword(); return true; });

        // Apps tab
        switchAllApps.setOnCheckedChangeListener((b, on) -> {
            Set<String> apps = new HashSet<>(prefs.getStringSet(
                    AppConstants.KEY_ENABLED_APPS, AppConstants.defaultEnabledApps()));
            if (on) apps.add("ALL"); else apps.remove("ALL");
            prefs.edit().putStringSet(AppConstants.KEY_ENABLED_APPS, apps).apply();
            renderAppList();
        });

        btnClearHistory.setOnClickListener(v -> {
            historyManager.clearHistory();
            prefs.edit().putInt(AppConstants.KEY_ALARM_COUNT, 0).apply();
            renderHistory();
            refreshHomeTab();
            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
        });
    }

    // ── LOAD DATA ────────────────────────────────────────────────────────────

    private void loadAllData() {
        masterSwitch.setChecked(prefs.getBoolean(AppConstants.KEY_MASTER_ON, true));
        Set<String> apps = prefs.getStringSet(AppConstants.KEY_ENABLED_APPS, AppConstants.defaultEnabledApps());
        switchAllApps.setChecked(apps.contains("ALL"));
        renderAppList();
        renderKeywordChips();
        refreshHomeTab();
        showTab(0);
    }

    // ── HOME TAB ─────────────────────────────────────────────────────────────

    void refreshHomeTab() {
        boolean on      = prefs.getBoolean(AppConstants.KEY_MASTER_ON, true);
        boolean running = AlarmService.isRunning;
        boolean snoozed = AlarmService.isSnoozed;
        int count       = prefs.getInt(AppConstants.KEY_ALARM_COUNT, 0);

        tvAlarmCount.setText(count + " alarm" + (count != 1 ? "s" : "") + " triggered");

        List<AlarmEvent> history = historyManager.getHistory();
        if (!history.isEmpty()) {
            AlarmEvent last = history.get(0);
            tvLastTrigger.setText("Last: " + last.appName + " · " + last.getTimeAgo());
            tvLastTrigger.setVisibility(View.VISIBLE);
        } else {
            tvLastTrigger.setVisibility(View.GONE);
        }

        if (running && !snoozed) {
            cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.alarmRed));
            tvStatusTitle.setText("🔴  ALARM ACTIVE!");
            tvStatusTitle.setTextColor(0xFFFFFFFF);
            tvStatusDesc.setText("Keyword: \"" + AlarmService.lastKeyword + "\" detected from " + AlarmService.lastSource);
            tvStatusDesc.setTextColor(0xFFFFCCCC);
            btnStopAlarm.setVisibility(View.VISIBLE);
            btnSnooze.setVisibility(View.VISIBLE);
            btnTestAlarm.setEnabled(false);
        } else if (snoozed) {
            cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.snoozedOrange));
            tvStatusTitle.setText("💤  Snoozed");
            tvStatusTitle.setTextColor(0xFFFFFFFF);
            tvStatusDesc.setText("Alarm will resume after snooze period");
            tvStatusDesc.setTextColor(0xFFFFEECC);
            btnStopAlarm.setVisibility(View.VISIBLE);
            btnSnooze.setVisibility(View.GONE);
            btnTestAlarm.setEnabled(false);
        } else if (!on) {
            cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.pausedGray));
            tvStatusTitle.setText("⏸  Monitoring Paused");
            tvStatusTitle.setTextColor(0xFF333333);
            tvStatusDesc.setText("Toggle the switch to resume monitoring");
            tvStatusDesc.setTextColor(0xFF666666);
            btnStopAlarm.setVisibility(View.GONE);
            btnSnooze.setVisibility(View.GONE);
            btnTestAlarm.setEnabled(true);
        } else {
            cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.activeGreen));
            tvStatusTitle.setText("🟢  Monitoring Active");
            tvStatusTitle.setTextColor(0xFFFFFFFF);
            tvStatusDesc.setText("Watching for keywords in your selected apps");
            tvStatusDesc.setTextColor(0xFFCCFFCC);
            btnStopAlarm.setVisibility(View.GONE);
            btnSnooze.setVisibility(View.GONE);
            btnTestAlarm.setEnabled(true);
        }
    }

    // ── APPS TAB ─────────────────────────────────────────────────────────────

    private void renderAppList() {
        appListContainer.removeAllViews();
        Set<String> enabled = new HashSet<>(prefs.getStringSet(
                AppConstants.KEY_ENABLED_APPS, AppConstants.defaultEnabledApps()));
        boolean allOn = enabled.contains("ALL");

        for (String[] app : AppConstants.SUPPORTED_APPS) {
            String pkg  = app[0];
            String name = app[1];
            String icon = app[2];

            View row = getLayoutInflater().inflate(R.layout.item_app_row, appListContainer, false);
            ((TextView) row.findViewById(R.id.tvAppIcon)).setText(icon);
            ((TextView) row.findViewById(R.id.tvAppName)).setText(name);
            Switch sw = row.findViewById(R.id.swApp);
            sw.setChecked(allOn || enabled.contains(pkg));
            sw.setEnabled(!allOn);
            sw.setOnCheckedChangeListener((b, on) -> {
                Set<String> cur = new HashSet<>(prefs.getStringSet(
                        AppConstants.KEY_ENABLED_APPS, AppConstants.defaultEnabledApps()));
                if (on) cur.add(pkg); else cur.remove(pkg);
                prefs.edit().putStringSet(AppConstants.KEY_ENABLED_APPS, cur).apply();
            });

            // Divider
            View divider = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            divider.setLayoutParams(lp);
            divider.setBackgroundColor(0xFFEEEEEE);

            appListContainer.addView(row);
            appListContainer.addView(divider);
        }
    }

    // ── KEYWORDS TAB ─────────────────────────────────────────────────────────

    private void addKeyword() {
        String kw = etKeyword.getText().toString().trim().toLowerCase();
        if (kw.isEmpty()) return;
        Set<String> cur = new HashSet<>(prefs.getStringSet(
                AppConstants.KEY_KEYWORDS, AppConstants.defaultKeywords()));
        if (cur.contains(kw)) {
            Toast.makeText(this, "Already exists!", Toast.LENGTH_SHORT).show();
            return;
        }
        cur.add(kw);
        prefs.edit().putStringSet(AppConstants.KEY_KEYWORDS, cur).apply();
        etKeyword.setText("");
        renderKeywordChips();
        Toast.makeText(this, "✅ Added: " + kw, Toast.LENGTH_SHORT).show();
    }

    private void renderKeywordChips() {
        keywordChips.removeAllViews();
        Set<String> keywords = prefs.getStringSet(
                AppConstants.KEY_KEYWORDS, AppConstants.defaultKeywords());
        List<String> sorted = new ArrayList<>(keywords);
        Collections.sort(sorted);
        tvKeywordCount.setText(sorted.size() + " keywords active");

        for (String kw : sorted) {
            View chip = getLayoutInflater().inflate(R.layout.item_keyword_chip, keywordChips, false);
            ((TextView) chip.findViewById(R.id.tvChipText)).setText(kw);
            chip.findViewById(R.id.btnChipDelete).setOnClickListener(v -> {
                Set<String> cur = new HashSet<>(prefs.getStringSet(
                        AppConstants.KEY_KEYWORDS, AppConstants.defaultKeywords()));
                cur.remove(kw);
                prefs.edit().putStringSet(AppConstants.KEY_KEYWORDS, cur).apply();
                renderKeywordChips();
            });
            keywordChips.addView(chip);
        }
    }

    // ── HISTORY TAB ──────────────────────────────────────────────────────────

    private void renderHistory() {
        historyList.removeAllViews();
        List<AlarmEvent> history = historyManager.getHistory();
        if (history.isEmpty()) {
            tvNoHistory.setVisibility(View.VISIBLE);
            return;
        }
        tvNoHistory.setVisibility(View.GONE);
        for (AlarmEvent ev : history) {
            View item = getLayoutInflater().inflate(R.layout.item_history_row, historyList, false);
            ((TextView) item.findViewById(R.id.tvHistoryApp)).setText(ev.appName);
            ((TextView) item.findViewById(R.id.tvHistoryKeyword)).setText("\"" + ev.keyword + "\"");
            ((TextView) item.findViewById(R.id.tvHistoryText)).setText(ev.title);
            ((TextView) item.findViewById(R.id.tvHistoryTime)).setText(ev.getFormattedTime());
            historyList.addView(item);
        }
    }

    // ── RECEIVER ─────────────────────────────────────────────────────────────

    private void registerUiReceiver() {
        uiReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context ctx, Intent i) {
                refreshHomeTab();
                if (currentTab == 3) renderHistory();
            }
        };
        IntentFilter f = new IntentFilter();
        f.addAction(AppConstants.ACTION_ALARM_STARTED);
        f.addAction(AppConstants.ACTION_ALARM_STOPPED);
        f.addAction(AppConstants.ACTION_UI_REFRESH);
        registerReceiver(uiReceiver, f);
    }

    // ── PERMISSION ───────────────────────────────────────────────────────────

    private void checkPermission() {
        String l = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (l == null || !l.contains(getPackageName())) {
            new AlertDialog.Builder(this)
                    .setTitle("🔔 Permission Required")
                    .setMessage("Notif Alarm needs Notification Access to automatically detect messages.\n\nOn the next screen: find 'Notif Alarm' → turn it ON.")
                    .setPositiveButton("Open Settings", (d, w) ->
                            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")))
                    .setCancelable(false)
                    .show();
        }
    }
}
