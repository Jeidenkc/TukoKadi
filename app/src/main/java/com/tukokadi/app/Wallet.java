package com.tukokadi.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

public class Wallet {

    private static final String PREFS = "tuko_wallet";
    private static final String KEY_BALANCE = "balance";
    private static final String KEY_LAST_CLAIM = "last_claim_time";
    private static final long CLAIM_COOLDOWN_MS = 24L * 60 * 60 * 1000; // 24 hours
    private static final long STARTING_BONUS = 1000;

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static long getBalance(Context c) {
        return prefs(c).getLong(KEY_BALANCE, 0);
    }

    public static void setBalance(Context c, long balance) {
        prefs(c).edit().putLong(KEY_BALANCE, balance).apply();
    }

    public static void addBalance(Context c, long amount) {
        setBalance(c, getBalance(c) + amount);
    }

    public static boolean deductBalance(Context c, long amount) {
        long current = getBalance(c);
        if (current < amount) return false;
        setBalance(c, current - amount);
        return true;
    }

    public static void grantStartingBonusIfNeeded(Context c) {
        SharedPreferences p = prefs(c);
        if (!p.getBoolean("starting_bonus_granted", false)) {
            addBalance(c, STARTING_BONUS);
            p.edit().putBoolean("starting_bonus_granted", true).apply();
        }
    }

    public static long getLastClaimTime(Context c) {
        return prefs(c).getLong(KEY_LAST_CLAIM, 0);
    }

    public static boolean canClaimDailyGift(Context c) {
        long last = getLastClaimTime(c);
        return System.currentTimeMillis() - last >= CLAIM_COOLDOWN_MS;
    }

    public static long millisUntilNextClaim(Context c) {
        long last = getLastClaimTime(c);
        long remaining = CLAIM_COOLDOWN_MS - (System.currentTimeMillis() - last);
        return Math.max(remaining, 0);
    }

    // Returns the amount rewarded, or -1 if not eligible yet.
    public static long claimDailyGift(Context c) {
        if (!canClaimDailyGift(c)) return -1;
        Random r = new Random();
        // Weighted toward smaller amounts, with a rare chance of a big reward.
        long reward;
        int roll = r.nextInt(100);
        if (roll < 70) {
            reward = 1000 + r.nextInt(4000);       // 1,000 - 5,000  (70% chance)
        } else if (roll < 95) {
            reward = 5000 + r.nextInt(15000);      // 5,000 - 20,000 (25% chance)
        } else {
            reward = 50000 + r.nextInt(50000);     // 50,000 - 100,000 (5% chance)
        }
        addBalance(c, reward);
        prefs(c).edit().putLong(KEY_LAST_CLAIM, System.currentTimeMillis()).apply();
        return reward;
    }
}
