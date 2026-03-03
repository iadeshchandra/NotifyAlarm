╔══════════════════════════════════════════════════════════════════════╗
║           🔔 NOTIF ALARM PRO v2.0 — SOURCE CODE                     ║
║      Modern UI · True Auto-Detection · Unlimited Alarms              ║
╚══════════════════════════════════════════════════════════════════════╝

★ NEW IN VERSION 2.0
═════════════════════
✅ Modern Material Design 3 UI
✅ Bottom navigation (Home / Apps / Keywords / History / Settings)
✅ Alarm History — logs every triggered alarm with timestamp
✅ Snooze Feature — snooze alarm for 2/5/10/15/30 minutes
✅ Active Hours — set time window (e.g. only alarm 8am–10pm)
✅ Snooze & Stop buttons in notification bar
✅ 12 supported apps (added Instagram, Twitter, Teams, etc.)
✅ Real-time UI updates when alarm fires
✅ Stats card on home screen
✅ Keyword chips with easy delete buttons


📁 FILE STRUCTURE
══════════════════
NotifAlarmV2/
├── .github/workflows/build.yml     ← GitHub auto-build (DO NOT DELETE)
├── build.gradle                    ← Root gradle config
├── settings.gradle                 ← Project settings
├── gradlew                         ← Build script (Linux/Mac)
├── gradlew.bat                     ← Build script (Windows)
├── gradle/wrapper/
│   └── gradle-wrapper.properties  ← Gradle version config
└── app/
    ├── build.gradle                ← App dependencies
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/yourname/notifalarm/
        │   ├── AppConstants.java         ← All constants & defaults
        │   ├── AlarmEvent.java           ← Alarm history model
        │   ├── HistoryManager.java       ← Save/load history (Gson)
        │   ├── MainActivity.java         ← Main screen (4 tabs)
        │   ├── SettingsActivity.java     ← Settings screen
        │   ├── NotifListenerService.java ← ★ AUTO-DETECTION CORE
        │   ├── AlarmService.java         ← Alarm + Snooze
        │   └── BootReceiver.java         ← Restart after reboot
        └── res/
            ├── layout/
            │   ├── activity_main.xml
            │   ├── activity_settings.xml
            │   ├── item_app_row.xml
            │   ├── item_keyword_chip.xml
            │   └── item_history_row.xml
            ├── values/
            │   ├── colors.xml
            │   ├── strings.xml
            │   └── themes.xml
            └── raw/
                └── alarm.mp3  ← ADD THIS (see below)


⚙️ HOW TO BUILD (GitHub Actions — FREE)
════════════════════════════════════════
1. Go to github.com → create free account
2. Create new repository → name it "NotifAlarm" → check "Add README"
3. Upload ALL files from this ZIP into the repository
4. GitHub automatically starts building (check the "Actions" tab)
5. Wait 5-8 minutes → green ✅ appears
6. Click the build → scroll to "Artifacts" → download "NotifAlarm-APK"
7. Extract ZIP → install app-debug.apk on your Android phone

⚠️ IMPORTANT: The .github folder must be uploaded!
   On Windows: Enable "Show hidden files" in File Explorer to see it.


🔊 ADD ALARM SOUND
═══════════════════
1. Download any .mp3 alarm sound from: https://freesound.org
2. Rename it to: alarm.mp3
3. Place it in: app/src/main/res/raw/alarm.mp3
4. If not added, app will use Android's default alarm sound


📱 FIRST LAUNCH
════════════════
1. Install app-debug.apk on your phone
2. Tap "Open" → app asks for Notification Access
3. Tap "Open Settings" → find "Notif Alarm Pro" → toggle ON
4. Return to app → status shows 🟢 Monitoring Active
5. Go to Apps tab → make sure Fiverr and/or Upwork are ON
6. Done! App now monitors automatically 24/7


✨ ALL FEATURES
════════════════
✅ TRUE auto-detection (NotificationListenerService API)
✅ No alarm limit — fires every single time keyword matches
✅ Works on locked/sleeping screen (WakeLock + ForegroundService)
✅ STOP alarm from notification bar (one tap)
✅ SNOOZE alarm from notification bar
✅ 4 tabs: Home, Apps, Keywords, History
✅ 12 supported apps (Fiverr, Upwork, Gmail, WhatsApp, Telegram, etc.)
✅ Monitor ALL apps with one toggle
✅ Unlimited custom keywords
✅ Alarm history with timestamps (last 100 alarms)
✅ Active hours schedule (e.g. 8am–10pm only)
✅ Adjustable alarm repeat: 1/2/3/5/10 seconds
✅ Snooze duration: 2/5/10/15/30 minutes
✅ Toggle sound on/off
✅ Toggle vibration on/off
✅ Stats on home screen
✅ Auto-restart after phone reboot


🆘 TROUBLESHOOTING
════════════════════
• Alarm not firing?
  → Check Notification Access is ON (Settings → Apps → Special permissions)
  → Check Master Switch is ON (toggle in top bar)
  → Check the app is enabled in Apps tab

• App stops working in background?
  → Go to Settings → Battery → find NotifAlarm → Unrestricted
  → This is very important on Samsung, Xiaomi, Huawei phones

• Build failed on GitHub?
  → Make sure .github/workflows/build.yml was uploaded
  → Check the error log and send screenshot for help


📞 RESOURCES
═════════════
GitHub:         https://github.com
Free sounds:    https://freesound.org
Android Studio: https://developer.android.com/studio
