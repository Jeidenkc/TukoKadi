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
        wallet.setText(String.format("%,d", balance) + " Coins");
    }

    public static void setup(final Activity a) {
        Button wallet = (Button) a.findViewById(R.id.walletButton);
        Button menu = (Button) a.findViewById(R.id.menuButton);
        refreshBalance(a);

        wallet.setOnClickListener(v ->
            a.startActivity(new Intent(a, WalletActivity.class)));

        menu.setOnClickListener(v -> {
            PopupMenu m = new PopupMenu(a, v);
            m.getMenu().add(0, 1, 0, "Help");
            m.getMenu().add(0, 2, 1, "Contact Us");
            m.getMenu().add(0, 3, 2, "Logout");
            m.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    a.findViewById(R.id.faqsButton).performClick();
                } else if (item.getItemId() == 2) {
                    Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:youremail@gmail.com"));
                    i.putExtra(Intent.EXTRA_SUBJECT, "TUKO KADI support");
                    a.startActivity(i);
                } else if (item.getItemId() == 3) {
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
