package com.tukokadi.app;

import android.content.Context;
import android.content.SharedPreferences;

public class GameHistory {
    private static final String PREFS = "tuko_kadi_history";
    private static final String BOT_WINS = "bot_wins";
    private static final String BOT_LOSSES = "bot_losses";
    private static final String RAFIKI_WINS = "rafiki_wins";
    private static final String RAFIKI_LOSSES = "rafiki_losses";

    public static void recordBotResult(Context ctx, boolean youWon) {
        record(ctx, youWon, BOT_WINS, BOT_LOSSES);
    }

    public static void recordRafikiResult(Context ctx, boolean youWon) {
        record(ctx, youWon, RAFIKI_WINS, RAFIKI_LOSSES);
    }

    private static void record(Context ctx, boolean youWon, String winsKey, String lossesKey) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String key = youWon ? winsKey : lossesKey;
        int current = prefs.getInt(key, 0);
        prefs.edit().putInt(key, current + 1).apply();
    }

    public static int getBotWins(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(BOT_WINS, 0);
    }

    public static int getBotLosses(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(BOT_LOSSES, 0);
    }

    public static int getRafikiWins(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(RAFIKI_WINS, 0);
    }

    public static int getRafikiLosses(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(RAFIKI_LOSSES, 0);
    }
}
