package com.tukokadi.app;

import android.content.Context;
import android.content.SharedPreferences;

// Player-facing settings, persisted in SharedPreferences. Read at the point of
// use (sound/vibration helpers, quick match) so a change takes effect immediately.
public class Prefs {

    private static final String PREFS = "tuko_settings";
    private static final String KEY_SOUND = "sound_enabled";
    private static final String KEY_VIBRATION = "vibration_enabled";
    private static final String KEY_STAKE = "preferred_stake";
    private static final String KEY_AUTO_MATCH = "auto_match";

    // Must match the server's STAKE_TIERS (server.js).
    public static final int[] STAKES = {100, 200, 500, 1000};
    public static final int DEFAULT_STAKE = 100;

    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isSoundEnabled(Context c) {
        return p(c).getBoolean(KEY_SOUND, true);
    }

    public static void setSoundEnabled(Context c, boolean enabled) {
        p(c).edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public static boolean isVibrationEnabled(Context c) {
        return p(c).getBoolean(KEY_VIBRATION, true);
    }

    public static void setVibrationEnabled(Context c, boolean enabled) {
        p(c).edit().putBoolean(KEY_VIBRATION, enabled).apply();
    }

    // Falls back to the default if a stored value is not a valid tier (e.g. the
    // tier list changed between versions).
    public static int getPreferredStake(Context c) {
        int stake = p(c).getInt(KEY_STAKE, DEFAULT_STAKE);
        for (int t : STAKES) {
            if (t == stake) return stake;
        }
        return DEFAULT_STAKE;
    }

    public static void setPreferredStake(Context c, int stake) {
        p(c).edit().putInt(KEY_STAKE, stake).apply();
    }

    public static boolean isAutoMatch(Context c) {
        return p(c).getBoolean(KEY_AUTO_MATCH, false);
    }

    public static void setAutoMatch(Context c, boolean enabled) {
        p(c).edit().putBoolean(KEY_AUTO_MATCH, enabled).apply();
    }
}
