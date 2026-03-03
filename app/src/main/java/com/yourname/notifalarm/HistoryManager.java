package com.yourname.notifalarm;

import android.content.SharedPreferences;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;

public class HistoryManager {
    private static final int MAX_HISTORY = 100;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public HistoryManager(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public void addEvent(AlarmEvent event) {
        List<AlarmEvent> list = getHistory();
        list.add(0, event); // newest first
        if (list.size() > MAX_HISTORY) list = list.subList(0, MAX_HISTORY);
        prefs.edit().putString(AppConstants.KEY_HISTORY, gson.toJson(list)).apply();
    }

    public List<AlarmEvent> getHistory() {
        String json = prefs.getString(AppConstants.KEY_HISTORY, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<AlarmEvent>>(){}.getType();
        try {
            List<AlarmEvent> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void clearHistory() {
        prefs.edit().remove(AppConstants.KEY_HISTORY).apply();
    }
}
