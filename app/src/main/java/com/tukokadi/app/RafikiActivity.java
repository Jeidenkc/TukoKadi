package com.tukokadi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RafikiActivity extends Activity {

    private EditText nameInput, codeInput;
    private RadioGroup sizeGroup;
    private View menuPanel, lobbyPanel, searchingPanel;
    private TextView roomCode, statusText, playersText, searchingText;
    private boolean handedOff = false;
    private boolean searching = false;

    private JSONArray friendList = new JSONArray();
    private JSONArray requestList = new JSONArray();
    private AlertDialog friendsDialog, inviteDialog;
    private LinearLayout friendsBody;
    private boolean friendsInviteMode = false;

    private final RafikiClient.Listener listener = new RafikiClient.Listener() {
        @Override public void onMessage(final JSONObject m) {
            runOnUiThread(() -> handle(m));
        }
        @Override public void onFailure() {
            runOnUiThread(() -> {
                toast("Cannot reach the server. Is it running?");
                showMenu();
            });
        }
    };

    // Quiet connection used while sitting in the menu, so friends can see you online.
    private final RafikiClient.Listener presenceListener = new RafikiClient.Listener() {
        @Override public void onMessage(final JSONObject m) {
            runOnUiThread(() -> handle(m));
        }
        @Override public void onFailure() { }
    };

    private final RafikiClient.Listener socialListener = new RafikiClient.Listener() {
        @Override public void onMessage(final JSONObject m) {
            runOnUiThread(() -> handleSocial(m));
        }
        @Override public void onFailure() { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rafiki);

        // Auth token identifies us to the server; balances are looked up server-side.
        RafikiClient.setToken(Api.token(this));

        nameInput = findViewById(R.id.nameInput);
        String sessName = getSharedPreferences("tuko_session", MODE_PRIVATE).getString("name", "");

        String __phone = getSharedPreferences("tuko_session", MODE_PRIVATE).getString("phone", null);
        String __savedName = !sessName.isEmpty() ? sessName : __phone != null
                ? getSharedPreferences("tuko_kadi_users", MODE_PRIVATE).getString(__phone + "_name", "")
                : "";
        if (!__savedName.isEmpty()) {
            nameInput.setText(__savedName);
            nameInput.setEnabled(false);
            nameInput.setFocusable(false);
        }
        codeInput = findViewById(R.id.codeInput);
        sizeGroup = findViewById(R.id.sizeGroup);
        menuPanel = findViewById(R.id.menuPanel);
        lobbyPanel = findViewById(R.id.lobbyPanel);
        searchingPanel = findViewById(R.id.searchingPanel);
        roomCode = findViewById(R.id.roomCode);
        statusText = findViewById(R.id.statusText);
        playersText = findViewById(R.id.playersText);
        searchingText = findViewById(R.id.searchingText);

        Button btnCreate = findViewById(R.id.btnCreate);
        Button btnJoin = findViewById(R.id.btnJoin);
        Button btnLeave = findViewById(R.id.btnLeave);
        Button btnAddBot = findViewById(R.id.btnAddBot);
        Button btnQuickMatch = findViewById(R.id.btnQuickMatch);
        Button btnCancelSearch = findViewById(R.id.btnCancelSearch);

        // Friends buttons are added here in code, so the layout file stays untouched.
        Button btnFriends = findViewById(R.id.btnFriends);
        btnFriends.setOnClickListener(v -> openFriends());

        Button btnInvite = makeGoldButton("INVITE FRIENDS");
        ViewGroup lobbyGroup = (ViewGroup) lobbyPanel;
        int leaveIdx = lobbyGroup.indexOfChild(btnLeave);
        lobbyGroup.addView(btnInvite, leaveIdx < 0 ? lobbyGroup.getChildCount() : leaveIdx);
        btnInvite.setOnClickListener(v -> openFriends());

        btnCreate.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) { toast("Enter your name"); return; }
            int size = 2;
            int id = sizeGroup.getCheckedRadioButtonId();
            if (id == R.id.size3) size = 3;
            else if (id == R.id.size4) size = 4;
            try {
                JSONObject msg = new JSONObject();
                msg.put("type", "create");
                msg.put("name", name);
                msg.put("max", size);
                RafikiClient.connect(msg, listener);
            } catch (Exception e) { toast("Something went wrong"); }
        });

        btnJoin.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String code = codeInput.getText().toString().trim().toUpperCase();
            if (name.isEmpty()) { toast("Enter your name"); return; }
            if (code.length() != 4) { toast("Enter the 4-letter room code"); return; }
            joinRoom(code);
        });

        btnLeave.setOnClickListener(v -> leaveRoom());

        btnAddBot.setOnClickListener(v -> {
            try {
                JSONObject msg = new JSONObject();
                msg.put("type", "addBot");
                RafikiClient.send(msg);
            } catch (Exception e) { toast("Something went wrong"); }
        });

        btnQuickMatch.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) { toast("Enter your name"); return; }
            try {
                JSONObject msg = new JSONObject();
                msg.put("type", "automatch");
                msg.put("name", name);
                msg.put("stake", Prefs.getPreferredStake(this));
                RafikiClient.connect(msg, listener);
                showSearching();
            } catch (Exception e) { toast("Something went wrong"); }
        });

        btnCancelSearch.setOnClickListener(v -> cancelSearch());

        RafikiClient.setSocialListener(socialListener);
        connectPresence();
    }

    // ---------- presence / friends ----------

    private void connectPresence() {
        String tk = Api.token(this);
        if (tk == null || tk.isEmpty()) return;
        try {
            JSONObject hello = new JSONObject();
            hello.put("type", "hello");
            RafikiClient.connect(hello, presenceListener);
        } catch (Exception ignored) { }
    }

    private void joinRoom(String code) {
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) { toast("Enter your name"); return; }
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", "join");
            msg.put("name", name);
            msg.put("code", code);
            RafikiClient.connect(msg, listener);
        } catch (Exception e) { toast("Something went wrong"); }
    }

    private void handleSocial(JSONObject m) {
        String type = m.optString("type");
        if ("friends".equals(type)) {
            applyFriends(m.optJSONArray("friends"), m.optJSONArray("requests"));
        } else if ("invite".equals(type)) {
            showInvite(m);
        } else if ("inviteReply".equals(type)) {
            toast(m.optString("name", "Your friend")
                    + (m.optBoolean("accepted") ? " accepted your invite" : " declined your invite"));
        } else if ("inviteSent".equals(type)) {
            toast("Invite sent to " + m.optString("name", "your friend"));
        }
    }

    private void showInvite(JSONObject m) {
        if (isFinishing() || searching) return;
        if (lobbyPanel.getVisibility() == View.VISIBLE) return;
        final String code = m.optString("code", "");
        if (code.isEmpty()) return;
        if (inviteDialog != null && inviteDialog.isShowing()) return;
        final String fromPhone = m.optString("fromPhone", "");
        final String fromName = m.optString("from", "A friend");
        final int ink = Color.parseColor("#F3EFE6");
        final int dim = Color.parseColor("#9DB8AB");

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setPadding(dp(24), dp(30), dp(24), dp(22));

        TextView who = new TextView(this);
        who.setText(fromName);
        who.setTextColor(ink);
        who.setTextSize(26);
        who.setTypeface(null, Typeface.BOLD);
        who.setGravity(Gravity.CENTER);
        box.addView(who);

        TextView sub = new TextView(this);
        sub.setText("is inviting you to play RAFIKI\n" + m.optInt("max", 0) + " players  \u2022  Entry "
                + m.optInt("entry", 0) + " NETCOINS each");
        sub.setTextColor(dim);
        sub.setTextSize(15);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, dp(10), 0, dp(18));
        box.addView(sub);

        Button acceptBtn = makeButton("Accept", R.drawable.g_btn_primary, "#052E1F", 54);
        Button declineBtn = makeButton("Decline", R.drawable.g_btn_danger, "#F0555A", 50);
        box.addView(acceptBtn);
        box.addView(declineBtn);

        inviteDialog = new AlertDialog.Builder(this).setView(box).setCancelable(false).create();
        inviteDialog.getWindow().setBackgroundDrawableResource(R.drawable.g_bg_dialog);
        inviteDialog.setOnDismissListener(d -> stopInviteRing());
        inviteDialog.show();
        startInviteRing();

        acceptBtn.setOnClickListener(v -> {
            inviteDialog.dismiss();
            replyInvite(fromPhone, true);
            joinRoom(code);
        });
        declineBtn.setOnClickListener(v -> {
            inviteDialog.dismiss();
            replyInvite(fromPhone, false);
        });
        // Missed call: auto-decline after 30 seconds.
        box.postDelayed(() -> {
            if (inviteDialog != null && inviteDialog.isShowing()) {
                inviteDialog.dismiss();
                replyInvite(fromPhone, false);
            }
        }, 30000);
    }

    private android.media.Ringtone inviteRing;

    private void startInviteRing() {
        try {
            android.net.Uri u = android.media.RingtoneManager.getDefaultUri(
                    android.media.RingtoneManager.TYPE_RINGTONE);
            inviteRing = android.media.RingtoneManager.getRingtone(this, u);
            if (inviteRing != null) inviteRing.play();
        } catch (Exception ignored) { }
        try {
            android.os.Vibrator vb = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vb != null) vb.vibrate(new long[]{0, 600, 400, 600, 400, 600}, -1);
        } catch (Exception ignored) { }
    }

    private void stopInviteRing() {
        try { if (inviteRing != null) inviteRing.stop(); } catch (Exception ignored) { }
        inviteRing = null;
        try {
            android.os.Vibrator vb = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vb != null) vb.cancel();
        } catch (Exception ignored) { }
    }

    private void replyInvite(String to, boolean accepted) {
        if (to == null || to.isEmpty()) return;
        try {
            JSONObject o = new JSONObject();
            o.put("type", "inviteReply");
            o.put("to", to);
            o.put("accepted", accepted);
            RafikiClient.send(o);
        } catch (Exception ignored) { }
    }

    private void applyFriends(JSONArray f, JSONArray r) {
        friendList = f == null ? new JSONArray() : f;
        requestList = r == null ? new JSONArray() : r;
        renderFriends();
    }

    private JSONArray withOnline(JSONArray f) {
        JSONArray out = new JSONArray();
        if (f == null) return out;
        for (int i = 0; i < f.length(); i++) {
            JSONObject o = f.optJSONObject(i);
            if (o == null) continue;
            boolean on = false;
            for (int k = 0; k < friendList.length(); k++) {
                JSONObject old = friendList.optJSONObject(k);
                if (old != null && old.optString("phone").equals(o.optString("phone"))) {
                    on = old.optBoolean("online", false);
                    break;
                }
            }
            try { o.put("online", on); } catch (Exception ignored) { }
            out.put(o);
        }
        return out;
    }

    private EditText addInput;

    private void openFriends() {
        friendsInviteMode = lobbyPanel.getVisibility() == View.VISIBLE;
        final int ink = Color.parseColor("#F3EFE6");
        final int dim = Color.parseColor("#9DB8AB");
        final int M = ViewGroup.LayoutParams.MATCH_PARENT;
        final int W = ViewGroup.LayoutParams.WRAP_CONTENT;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(16));

        TextView title = new TextView(this);
        title.setText(friendsInviteMode ? "Invite friends" : "Friends");
        title.setTextColor(ink);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title);

        // Input is created once so refreshes never close the keyboard.
        addInput = new EditText(this);
        addInput.setHint("Friend's phone or email");
        addInput.setHintTextColor(dim);
        addInput.setTextColor(ink);
        addInput.setTextSize(16);
        addInput.setSingleLine(true);
        addInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        addInput.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
        addInput.setBackgroundResource(R.drawable.g_bg_field);
        addInput.setPadding(dp(14), 0, dp(14), 0);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(M, dp(52));
        fp.topMargin = dp(14);
        root.addView(addInput, fp);

        final Runnable submit = () -> {
            String who = addInput.getText().toString().trim();
            if (who.isEmpty()) { toast("Enter your friend's phone or email"); return; }
            addByInput(who);
        };
        Button addBtn = makeButton("Add friend", R.drawable.g_btn_primary, "#052E1F", 50);
        addBtn.setOnClickListener(v -> submit.run());
        root.addView(addBtn);
        addInput.setOnEditorActionListener((tv, actionId, ev) -> { submit.run(); return true; });

        ScrollView sv = new ScrollView(this) {
            @Override protected void onMeasure(int w, int h) {
                int cap = (int) (getResources().getDisplayMetrics().heightPixels * 0.45f);
                super.onMeasure(w, android.view.View.MeasureSpec.makeMeasureSpec(cap,
                        android.view.View.MeasureSpec.AT_MOST));
            }
        };
        friendsBody = new LinearLayout(this);
        friendsBody.setOrientation(LinearLayout.VERTICAL);
        sv.addView(friendsBody);
        root.addView(sv, new LinearLayout.LayoutParams(M, W));

        Button close = makeButton("Close", R.drawable.g_btn_secondary, "#F3EFE6", 46);
        close.setOnClickListener(v -> { if (friendsDialog != null) friendsDialog.dismiss(); });
        root.addView(close);

        friendsDialog = new AlertDialog.Builder(this).setView(root).create();
        friendsDialog.getWindow().setBackgroundDrawableResource(R.drawable.g_bg_dialog);
        friendsDialog.getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                | android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        friendsDialog.show();
        // These flags are what blocked the keyboard inside the dialog.
        friendsDialog.getWindow().clearFlags(
                android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        addInput.postDelayed(() -> {
            addInput.requestFocus();
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(addInput,
                    android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }, 200);

        renderFriends();
        refreshFriends();
        refreshSent();
    }

    // Only redraws the lists. The input above is never touched.
    private void renderFriends() {
        if (friendsBody == null || friendsDialog == null || !friendsDialog.isShowing()) return;
        friendsBody.removeAllViews();

        if (requestList.length() > 0) {
            friendsBody.addView(label("Friend requests"));
            for (int i = 0; i < requestList.length(); i++) {
                JSONObject r = requestList.optJSONObject(i);
                if (r == null) continue;
                final String phone = r.optString("phone");
                LinearLayout row = row();
                TextView t = new TextView(this);
                t.setText(r.optString("name", "Player"));
                t.setTextSize(16);
                t.setTextColor(Color.parseColor("#F3EFE6"));
                row.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                Button ok = makeSmall("Accept", R.drawable.g_btn_primary, "#052E1F");
                ok.setOnClickListener(v -> friendAction("/friends/accept", phone));
                row.addView(ok);
                Button no = makeSmall("Decline", R.drawable.g_btn_danger, "#F0555A");
                no.setOnClickListener(v -> friendAction("/friends/decline", phone));
                row.addView(no);
                friendsBody.addView(row);
            }
        }

        addSuggestionSections();
        friendsBody.addView(label("Your friends"));
        friendsBody.addView(smallLine(friendList.length() == 0
                ? "No friends yet. Add one above."
                : "Hold a name to remove a friend."));
        java.util.List<JSONObject> sortedFriends = new java.util.ArrayList<>();
        for (int s = 0; s < friendList.length(); s++) {
            JSONObject o = friendList.optJSONObject(s);
            if (o != null) sortedFriends.add(o);
        }
        java.util.Collections.sort(sortedFriends,
                (x, y) -> Boolean.compare(y.optBoolean("online", false), x.optBoolean("online", false)));
        for (int i = 0; i < sortedFriends.size(); i++) {
            JSONObject f = sortedFriends.get(i);
            final String phone = f.optString("phone");
            final String nm = f.optString("name", "Player");
            final boolean on = f.optBoolean("online", false);
            LinearLayout row = row();
            TextView dot = new TextView(this);
            dot.setText("\u25CF ");
            dot.setTextColor(Color.parseColor(on ? "#34D399" : "#5E7A6D"));
            row.addView(dot);
            TextView t = new TextView(this);
            t.setText(nm);
            t.setTextSize(16);
            t.setTextColor(Color.parseColor("#F3EFE6"));
            row.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            TextView st = new TextView(this);
            st.setText(on ? "Online" : "Offline");
            st.setTextSize(13);
            st.setTextColor(Color.parseColor(on ? "#34D399" : "#5E7A6D"));
            st.setPadding(dp(8), 0, dp(8), 0);
            row.addView(st);
            if (friendsInviteMode && on) {
                Button inv = makeSmall("Invite", R.drawable.g_btn_primary, "#052E1F");
                inv.setOnClickListener(v -> sendInvite(phone));
                row.addView(inv);
            }
            row.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Remove friend?")
                        .setMessage(nm)
                        .setPositiveButton("Remove", (d, w) -> friendAction("/friends/remove", phone))
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
            friendsBody.addView(row);
        }
    }

    // ---------- contacts suggestions ----------

    private JSONArray suggestList = new JSONArray();
    private JSONArray sentList = new JSONArray();
    private boolean suggestLoaded = false;

    private static String canonKey(String raw) {
        String v = raw == null ? "" : raw.trim();
        if (v.contains("@")) return v.toLowerCase().replaceAll("\\s+", "");
        String d = v.replaceAll("\\D", "");
        return d.length() >= 9 ? d.substring(d.length() - 9) : "";
    }

    private static String keyHash(String k) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(("tukokadi:" + k).getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte x : h) sb.append(String.format("%02x", x));
            return sb.substring(0, 24);
        } catch (Exception e) { return ""; }
    }

    // Looks the player up on the server first, so 0712... and +254712... both work.
    private void addByInput(final String who) {
        final String tk = Api.token(this);
        if (tk == null || tk.isEmpty()) { toast("Please log in again"); return; }
        new Thread(() -> {
            try {
                JSONObject b = new JSONObject();
                b.put("query", who);
                JSONObject r = new JSONObject(httpJson("POST", "/friends/find", tk, b.toString()));
                if (r.has("error")) {
                    final String em = r.optString("error", "Player not found");
                    runOnUiThread(() -> toast(em));
                    return;
                }
                final String stored = r.optString("phone");
                runOnUiThread(() -> { if (addInput != null) addInput.setText(""); });
                friendAction("/friends/add", stored);
            } catch (Exception e) {
                runOnUiThread(() -> toast("Could not reach the server. Try again"));
            }
        }).start();
    }

    private void readContacts(android.net.Uri uri, String col, java.util.Set<String> out) {
        android.database.Cursor c = null;
        try {
            c = getContentResolver().query(uri, new String[]{col}, null, null, null);
            if (c != null) {
                while (c.moveToNext()) {
                    String k = canonKey(c.getString(0));
                    if (!k.isEmpty()) out.add(keyHash(k));
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (c != null) c.close();
        }
    }

    private void loadSuggestions(final boolean quiet) {
        final String tk = Api.token(this);
        if (tk == null || tk.isEmpty()) { toast("Please log in again"); return; }
        if (!quiet) toast("Checking your contacts\u2026");
        new Thread(() -> {
            try {
                java.util.LinkedHashSet<String> hs = new java.util.LinkedHashSet<>();
                readContacts(android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER, hs);
                readContacts(android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        android.provider.ContactsContract.CommonDataKinds.Email.ADDRESS, hs);
                JSONArray arr = new JSONArray();
                int n = 0;
                for (String h : hs) { arr.put(h); if (++n >= 3000) break; }
                JSONObject body = new JSONObject();
                body.put("hashes", arr);
                JSONObject r = new JSONObject(httpJson("POST", "/friends/suggest", tk, body.toString()));
                if (r.has("error")) {
                    final String em = r.optString("error");
                    runOnUiThread(() -> toast(em));
                    return;
                }
                final JSONArray sg = r.optJSONArray("suggestions");
                final JSONArray sn = r.optJSONArray("sent");
                runOnUiThread(() -> {
                    suggestList = sg == null ? new JSONArray() : sg;
                    sentList = sn == null ? new JSONArray() : sn;
                    suggestLoaded = true;
                    renderFriends();
                });
            } catch (Exception e) {
                if (!quiet) runOnUiThread(() -> toast("Could not read contacts. Try again"));
            }
        }).start();
    }

    // Only fetches requests you already sent (no contacts involved).
    private void refreshSent() {
        final String tk = Api.token(this);
        if (tk == null || tk.isEmpty()) return;
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("hashes", new JSONArray());
                JSONObject r = new JSONObject(httpJson("POST", "/friends/suggest", tk, body.toString()));
                final JSONArray sn = r.optJSONArray("sent");
                runOnUiThread(() -> { sentList = sn == null ? new JSONArray() : sn; renderFriends(); });
            } catch (Exception ignored) { }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code != 77) return;
        if (res.length > 0 && res[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            loadSuggestions(false);
        } else {
            toast("Allow contacts to see friend suggestions");
        }
    }

    private void addSuggestionSections() {
        final int ink = Color.parseColor("#F3EFE6");
        final int dim = Color.parseColor("#9DB8AB");
        if (!suggestLoaded) {
            Button b = makeButton("Find friends in my contacts", R.drawable.g_btn_secondary, "#F3EFE6", 46);
            b.setOnClickListener(v -> {
                if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    loadSuggestions(false);
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, 77);
                }
            });
            friendsBody.addView(b);
            friendsBody.addView(smallLine("Only scrambled numbers are sent. Your contacts are not saved."));
        } else {
            friendsBody.addView(label("People you may know"));
            if (suggestList.length() == 0) {
                friendsBody.addView(smallLine("None of your contacts are on Tuko Kadi yet."));
            }
            for (int i = 0; i < suggestList.length(); i++) {
                JSONObject s = suggestList.optJSONObject(i);
                if (s == null) continue;
                final String phone = s.optString("phone");
                LinearLayout row = row();
                TextView t = new TextView(this);
                t.setText(s.optString("name", "Player"));
                t.setTextSize(16);
                t.setTextColor(ink);
                row.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                if (s.optBoolean("incoming")) {
                    Button ok = makeSmall("Accept", R.drawable.g_btn_primary, "#052E1F");
                    ok.setOnClickListener(v -> friendAction("/friends/accept", phone));
                    row.addView(ok);
                } else if (s.optBoolean("pending")) {
                    TextView p = new TextView(this);
                    p.setText("Pending");
                    p.setTextColor(dim);
                    row.addView(p);
                } else {
                    Button add = makeSmall("Add", R.drawable.g_btn_primary, "#052E1F");
                    add.setOnClickListener(v -> friendAction("/friends/add", phone));
                    row.addView(add);
                }
                friendsBody.addView(row);
            }
        }
        if (sentList.length() > 0) {
            friendsBody.addView(label("Requests you sent"));
            for (int i = 0; i < sentList.length(); i++) {
                JSONObject s = sentList.optJSONObject(i);
                if (s == null) continue;
                LinearLayout row = row();
                TextView t = new TextView(this);
                t.setText(s.optString("name", "Player"));
                t.setTextSize(16);
                t.setTextColor(ink);
                row.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                TextView p = new TextView(this);
                p.setText("Pending");
                p.setTextColor(dim);
                row.addView(p);
                friendsBody.addView(row);
            }
        }
    }

    private void sendInvite(String phone) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", "invite");
            msg.put("phone", phone);
            RafikiClient.send(msg);
        } catch (Exception e) { toast("Something went wrong"); }
    }

    // Asks the Rafiki server for the list (it adds online dots) and also reads it
    // straight from the login server in case the connection is not ready yet.
    private void refreshFriends() {
        try {
            JSONObject h = new JSONObject();
            h.put("type", "hello");
            RafikiClient.send(h);
        } catch (Exception ignored) { }
        final String tk = Api.token(this);
        if (tk == null || tk.isEmpty()) return;
        new Thread(() -> {
            try {
                JSONObject j = new JSONObject(httpJson("GET", "/friends", tk, null));
                final JSONArray f = j.optJSONArray("friends");
                final JSONArray r = j.optJSONArray("requests");
                runOnUiThread(() -> applyFriends(withOnline(f), r));
            } catch (Exception ignored) { }
        }).start();
    }

    private void friendAction(final String path, final String who) {
        final String tk = Api.token(this);
        if (tk == null || tk.isEmpty()) { toast("Please log in again"); return; }
        new Thread(() -> {
            String msg;
            try {
                JSONObject b = new JSONObject();
                b.put("phone", who);
                JSONObject j = new JSONObject(httpJson("POST", path, tk, b.toString()));
                if (j.has("error")) {
                    msg = j.optString("error", "Something went wrong");
                } else {
                    String st = j.optString("status");
                    String nm = j.optString("name", "");
                    if ("requested".equals(st)) msg = "Request sent to " + nm;
                    else if ("friends".equals(st)) msg = "You are now friends with " + nm;
                    else if ("declined".equals(st)) msg = "Request declined";
                    else if ("removed".equals(st)) msg = "Friend removed";
                    else msg = "Done";
                }
            } catch (Exception e) {
                msg = "Could not reach the server. Try again";
            }
            final String fm = msg;
            runOnUiThread(() -> {
                toast(fm);
                refreshFriends();
                refreshSent();
                if (suggestLoaded) loadSuggestions(true);
            });
        }).start();
    }

    private static String httpJson(String method, String path, String token, String body) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(Api.BASE + path).openConnection();
        try {
            c.setRequestMethod(method);
            c.setConnectTimeout(60000);
            c.setReadTimeout(60000);
            c.setRequestProperty("Authorization", "Bearer " + token);
            if (body != null) {
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json");
                OutputStream os = c.getOutputStream();
                os.write(body.getBytes("UTF-8"));
                os.close();
            }
            int code = c.getResponseCode();
            InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
            if (in == null) return "{}";
            BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            r.close();
            return sb.toString();
        } finally {
            c.disconnect();
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private Button makeButton(String text, int bgRes, String textColor, int heightDp) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setBackgroundResource(bgRes);
        b.setTextColor(Color.parseColor(textColor));
        b.setTextSize(15);
        b.setTypeface(null, Typeface.BOLD);
        b.setStateListAnimator(null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(heightDp));
        lp.topMargin = dp(12);
        b.setLayoutParams(lp);
        return b;
    }

    private Button makeSmall(String text, int bgRes, String textColor) {
        Button b = makeButton(text, bgRes, textColor, 40);
        b.setTextSize(13);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setPadding(dp(14), 0, dp(14), 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(40));
        lp.leftMargin = dp(8);
        b.setLayoutParams(lp);
        return b;
    }

    // Kept under the old name: the "Invite friends" button in the lobby.
    private Button makeGoldButton(String text) {
        return makeButton(text, R.drawable.g_btn_secondary, "#F3EFE6", 50);
    }

    private TextView label(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(13);
        t.setAllCaps(true);
        t.setLetterSpacing(0.08f);
        t.setTextColor(Color.parseColor("#9DB8AB"));
        t.setTypeface(null, Typeface.BOLD);
        t.setPadding(0, dp(18), 0, dp(4));
        return t;
    }

    private TextView smallLine(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(12);
        t.setTextColor(Color.parseColor("#9DB8AB"));
        t.setPadding(0, 0, 0, dp(6));
        return t;
    }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0, dp(6), 0, dp(6));
        return r;
    }

    // ---------- original screens ----------

    private void showSearching() {
        searching = true;
        searchingText.setText("Searching for a match\u2026");
        menuPanel.setVisibility(View.GONE);
        lobbyPanel.setVisibility(View.GONE);
        searchingPanel.setVisibility(View.VISIBLE);
    }

    // The server has paired us and asks both players to accept before any stake is
    // escrowed. Auto-match accepts straight away; otherwise we ask.
    private void showMatchFound(JSONObject m) {
        final String matchId = m.optString("matchId", "");
        int stake = m.optInt("stake", 0);
        int fee = m.optInt("fee", 0);
        int win = m.optInt("winAmount", 0);
        if (Prefs.isAutoMatch(this)) {
            searchingText.setText("Match found" + (stake > 0 ? " at " + stake + " NETCOINS" : "")
                    + "\u2026 accepting");
            sendMatch("acceptMatch", matchId);
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Match found")
                .setMessage("Stake: " + stake + " NETCOINS\n"
                        + "Entry fee: " + fee + " NETCOINS each\n"
                        + "Winner gets: " + win + " NETCOINS\n\nAccept this match?")
                .setCancelable(false)
                .setPositiveButton("Accept", (d, w) -> sendMatch("acceptMatch", matchId))
                .setNegativeButton("Decline", (d, w) -> {
                    sendMatch("declineMatch", matchId);
                    showMenu();
                })
                .show();
    }

    private void sendMatch(String type, String matchId) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", type);
            msg.put("matchId", matchId);
            RafikiClient.send(msg);
        } catch (Exception e) { toast("Something went wrong"); }
    }

    private void cancelSearch() {
        searching = false;
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", "cancelMatch");
            RafikiClient.send(msg);
        } catch (Exception ignored) { }
        showMenu();
    }

    private void handle(JSONObject m) {
        try {
            String type = m.getString("type");
            switch (type) {
                case "room":
                    showLobby(m);
                    break;
                case "matchQueued":
                    showSearching();
                    int waiting = m.optInt("waiting", 0);
                    int queuedStake = m.optInt("stake", 0);
                    String atStake = queuedStake > 0 ? " at " + queuedStake + " NETCOINS" : "";
                    searchingText.setText("Searching for a match" + atStake + "\u2026"
                            + (waiting > 0 ? "\n(" + waiting + " waiting)" : ""));
                    break;
                case "matchFound":
                    showMatchFound(m);
                    break;
                case "matchEnded":
                    if (m.optBoolean("requeued", false)) {
                        showSearching();
                        searchingText.setText("Searching for a match\u2026");
                    } else {
                        showMenu();
                        String reason = m.optString("reason", "");
                        if (!reason.isEmpty()) toast(reason);
                    }
                    break;
                case "matchTimeout":
                    showMenu();
                    new AlertDialog.Builder(this)
                            .setTitle("No match")
                            .setMessage("No match found, nothing was charged.")
                            .setPositiveButton("OK", null)
                            .show();
                    break;
                case "matchCancelled":
                    showMenu();
                    break;
                case "started":
                    handedOff = true;
                    startActivity(new Intent(this, RafikiGameActivity.class));
                    finish();
                    break;
                case "error": {
                    String em = m.optString("message", "");
                    // Background friends sign-in problems are not worth a popup.
                    if (em.startsWith("Login expired") || em.startsWith("Could not load")) break;
                    if (searching) showMenu();
                    toast(em);
                    break;
                }
                case "left":
                    showMenu();
                    break;
            }
        } catch (Exception e) {
            toast("Bad reply from server");
        }
    }

    private void showLobby(JSONObject m) throws Exception {
        JSONArray arr = m.getJSONArray("players");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            sb.append(i == 0 ? "\uD83D\uDC51 " : "\u2660 ").append(arr.getString(i));
            if (i < arr.length() - 1) sb.append("\n");
        }
        int waiting = m.getInt("waitingFor");
        int max = m.getInt("max");
        roomCode.setText(m.getString("code"));
        playersText.setText(sb.toString());
        statusText.setText(waiting > 0
                ? "Waiting for " + waiting + " more player" + (waiting == 1 ? "" : "s")
                    + "  (" + arr.length() + "/" + max + ")"
                : "Room full!");
        menuPanel.setVisibility(View.GONE);
        lobbyPanel.setVisibility(View.VISIBLE);
    }

    private void showMenu() {
        searching = false;
        lobbyPanel.setVisibility(View.GONE);
        searchingPanel.setVisibility(View.GONE);
        menuPanel.setVisibility(View.VISIBLE);
    }

    private void leaveRoom() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", "leave");
            RafikiClient.send(msg);
        } catch (Exception ignored) { }
        showMenu();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (searchingPanel.getVisibility() == View.VISIBLE) cancelSearch();
        else if (lobbyPanel.getVisibility() == View.VISIBLE) leaveRoom();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RafikiClient.setSocialListener(null);
        if (friendsDialog != null && friendsDialog.isShowing()) friendsDialog.dismiss();
        if (inviteDialog != null && inviteDialog.isShowing()) inviteDialog.dismiss();
        if (!handedOff) RafikiClient.close();
    }
}
