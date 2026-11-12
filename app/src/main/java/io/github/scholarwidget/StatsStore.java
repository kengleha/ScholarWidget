package io.github.scholarwidget;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Keeps the last good snapshot (and the last error) so the widget can always draw. */
public final class StatsStore {
    private StatsStore() {}

    private static final String PREFS = "scholar_stats";
    private static final String KEY_STATS = "stats";
    private static final String KEY_ERROR = "error";
    private static final String KEY_REFRESHING = "refreshing";

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void save(Context context, ScholarStats s) {
        try {
            JSONObject o = new JSONObject();
            o.put("name", s.name);
            o.put("citationsAll", s.citationsAll);
            o.put("citationsSince", s.citationsSince);
            o.put("hAll", s.hAll);
            o.put("hSince", s.hSince);
            o.put("i10All", s.i10All);
            o.put("i10Since", s.i10Since);
            o.put("sinceYear", s.sinceYear);
            o.put("fetchedAt", s.fetchedAt);
            JSONArray years = new JSONArray();
            JSONArray counts = new JSONArray();
            for (int i = 0; i < s.years.length; i++) {
                years.put(s.years[i]);
                counts.put(s.counts[i]);
            }
            o.put("years", years);
            o.put("counts", counts);
            prefs(context).edit()
                    .putString(KEY_STATS, o.toString())
                    .remove(KEY_ERROR)
                    .putBoolean(KEY_REFRESHING, false)
                    .apply();
        } catch (JSONException e) {
            setError(context, "Could not save stats");
        }
    }

    /** Returns the last good snapshot, or null if nothing has been fetched yet. */
    public static ScholarStats load(Context context) {
        String json = prefs(context).getString(KEY_STATS, null);
        if (json == null) {
            return null;
        }
        try {
            JSONObject o = new JSONObject(json);
            ScholarStats s = new ScholarStats();
            s.name = o.optString("name", "");
            s.citationsAll = o.getInt("citationsAll");
            s.citationsSince = o.getInt("citationsSince");
            s.hAll = o.getInt("hAll");
            s.hSince = o.getInt("hSince");
            s.i10All = o.getInt("i10All");
            s.i10Since = o.getInt("i10Since");
            s.sinceYear = o.optInt("sinceYear", 0);
            s.fetchedAt = o.optLong("fetchedAt", 0L);
            JSONArray years = o.optJSONArray("years");
            JSONArray counts = o.optJSONArray("counts");
            if (years != null && counts != null && years.length() == counts.length()) {
                int n = years.length();
                s.years = new int[n];
                s.counts = new int[n];
                for (int i = 0; i < n; i++) {
                    s.years[i] = years.getInt(i);
                    s.counts[i] = counts.getInt(i);
                }
            }
            return s;
        } catch (JSONException e) {
            return null;
        }
    }

    public static void setError(Context context, String message) {
        prefs(context).edit()
                .putString(KEY_ERROR, message)
                .putBoolean(KEY_REFRESHING, false)
                .apply();
    }

    public static String getError(Context context) {
        return prefs(context).getString(KEY_ERROR, null);
    }

    public static void setRefreshing(Context context, boolean refreshing) {
        prefs(context).edit().putBoolean(KEY_REFRESHING, refreshing).apply();
    }

    public static boolean isRefreshing(Context context) {
        return prefs(context).getBoolean(KEY_REFRESHING, false);
    }
}
