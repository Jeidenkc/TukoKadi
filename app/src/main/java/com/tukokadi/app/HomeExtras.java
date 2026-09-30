package com.tukokadi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

public class HomeExtras {

    public static void refreshBalance(Activity a) {
        Button wallet = (Button) a.findViewById(R.id.walletButton);
        Wallet.grantStartingBonusIfNeeded(a);
        long balance = Wallet.getBalance(a);
        wallet.setText("\u2630  " + String.format("%,d", balance) + " Coins");
    }

    public static void setup(final Activity a) {
        Button wallet = (Button) a.findViewById(R.id.walletButton);
        refreshBalance(a);

        wallet.setOnClickListener(v -> {
            PopupMenu m = new PopupMenu(a, v);
            m.getMenu().add(0, 1, 0, "\uD83D\uDCB0 Wallet");
            m.getMenu().add(0, 2, 1, "\uD83C\uDF81 Daily Gift");
            m.getMenu().add(0, 3, 2, "\uD83D\uDCE4 Share");
            m.getMenu().add(0, 4, 3, "Help");
            m.getMenu().add(0, 5, 4, "Contact Us");
            m.getMenu().add(0, 6, 5, "Logout");
            m.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    a.startActivity(new Intent(a, WalletActivity.class));
                } else if (item.getItemId() == 2) {
                    a.startActivity(new Intent(a, DailyGiftActivity.class));
                } else if (item.getItemId() == 3) {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, "Play TukoKadi, the Kadi card game! Download it here: https://github.com/Jeidenkc/TukoKadi/releases/download/v1.3/TukoKadi-v1.3.apk");
                    a.startActivity(Intent.createChooser(share, "Share TukoKadi"));
                } else if (item.getItemId() == 4) {
                    a.findViewById(R.id.faqsButton).performClick();
                } else if (item.getItemId() == 5) {
                    Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:youremail@gmail.com"));
                    i.putExtra(Intent.EXTRA_SUBJECT, "TUKO KADI support");
                    a.startActivity(i);
                } else if (item.getItemId() == 6) {
                    new AlertDialog.Builder(a)
                        .setTitle("Logout")
                        .setMessage("Do you want to log out?")
                        .setPositiveButton("Yes", (d, w) -> {
                            Api.clearSession(a); Intent i = new Intent(a, AuthActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            a.startActivity(i);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                }
                return true;
            });
            m.show();
        });
    }
}
