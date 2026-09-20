package com.tukokadi.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

public class KibWallet {
    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences("kib_wallet", Context.MODE_PRIVATE);
    }

    public static long getBalance(Context c) {
        if (!p(c).contains("balance")) {
            p(c).edit().putLong("balance", 1000).apply();
            log(c, "Welcome bonus", 1000, 1000);
        }
        return p(c).getLong("balance", 0);
    }

    public static void add(Context c, long amount, String reason) {
        long nb = getBalance(c) + amount;
        p(c).edit().putLong("balance", nb).apply();
        log(c, reason, amount, nb);
    }

    public static boolean spend(Context c, long amount, String reason) {
        long b = getBalance(c);
        if (b < amount) return false;
        long nb = b - amount;
        p(c).edit().putLong("balance", nb).apply();
        log(c, reason, -amount, nb);
        return true;
    }

    private static void log(Context c, String reason, long amount, long after) {
        String old = p(c).getString("history", "");
        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis()).append("|")
          .append(reason.replace("|", "/")).append("|")
          .append(amount).append("|").append(after);
        if (!old.isEmpty()) {
            String[] parts = old.split("\n");
            for (int i = 0; i < parts.length && i < 99; i++) {
                sb.append("\n").append(parts[i]);
            }
        }
        p(c).edit().putString("history", sb.toString()).apply();
    }

    public static List<String[]> getHistory(Context c) {
        List<String[]> out = new ArrayList<>();
        String all = p(c).getString("history", "");
        if (all.isEmpty()) return out;
        for (String line : all.split("\n")) {
            String[] t = line.split("\\|");
            if (t.length == 4) out.add(t);
        }
        return out;
    }
}
