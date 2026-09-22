package com.tukokadi.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class RafikiActivity extends Activity {

    private EditText nameInput, codeInput;
    private RadioGroup sizeGroup;
    private View menuPanel, lobbyPanel;
    private TextView roomCode, statusText, playersText;
    private boolean handedOff = false;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rafiki);

        nameInput = findViewById(R.id.nameInput);

        String __phone = getSharedPreferences("tuko_session", MODE_PRIVATE).getString("phone", null);
        String __savedName = __phone != null
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
        roomCode = findViewById(R.id.roomCode);
        statusText = findViewById(R.id.statusText);
        playersText = findViewById(R.id.playersText);

        Button btnCreate = findViewById(R.id.btnCreate);
        Button btnJoin = findViewById(R.id.btnJoin);
        Button btnLeave = findViewById(R.id.btnLeave);
        Button btnAddBot = findViewById(R.id.btnAddBot);

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
            try {
                JSONObject msg = new JSONObject();
                msg.put("type", "join");
                msg.put("name", name);
                msg.put("code", code);
                RafikiClient.connect(msg, listener);
            } catch (Exception e) { toast("Something went wrong"); }
        });

        btnLeave.setOnClickListener(v -> leaveRoom());

        btnAddBot.setOnClickListener(v -> {
            try {
                JSONObject msg = new JSONObject();
                msg.put("type", "addBot");
                RafikiClient.send(msg);
            } catch (Exception e) { toast("Something went wrong"); }
        });
    }

    private void handle(JSONObject m) {
        try {
            String type = m.getString("type");
            switch (type) {
                case "room":
                    showLobby(m);
                    break;
                case "started":
                    handedOff = true;
                    startActivity(new Intent(this, RafikiGameActivity.class));
                    finish();
                    break;
                case "error":
                    toast(m.getString("message"));
                    break;
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
        lobbyPanel.setVisibility(View.GONE);
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
        if (lobbyPanel.getVisibility() == View.VISIBLE) leaveRoom();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!handedOff) RafikiClient.close();
    }
}
