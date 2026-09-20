package com.tukokadi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RafikiGameActivity extends Activity {

    private static final int GOLD = 0xFFFFD54A;
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    private LinearLayout pileRow, handRow;
    private TextView turnInfo, statusText, timerText;
    private AvatarView youAvatar;
    private LinearLayout topLeftSlot, topRightSlot, bottomRightSlot;
    private final java.util.List<String> handOrder = new java.util.ArrayList<>();
    private String myName;
    private boolean myTurn = false;
    private boolean gameOverShown = false;
    private CountDownTimer myTurnTimer;
    private int cardW, cardH;
    private int pileCardW, pileCardH;

    private int pendingPenalty = 0;
    private String pendingPenaltyRank = null;
    private String declaredSuit = null;
    private String currentTop = null;

    private ToneGenerator toneGen;
    private Handler soundHandler = new Handler(Looper.getMainLooper());
    private Vibrator vibrator;
    private SoundPool soundPool;
    private int soundWin, soundTimer, soundPlaying, soundWrong;
    private boolean soundsLoaded = false;

    private final RafikiClient.Listener listener = new RafikiClient.Listener() {
        @Override public void onMessage(final JSONObject m) {
            runOnUiThread(() -> handle(m));
        }
        @Override public void onFailure() {
            runOnUiThread(() -> {
                toast("Connection lost");
                finish();
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.screen_bg);
        root.setLayoutParams(new LinearLayout.LayoutParams(MATCH, MATCH));

        View header = getLayoutInflater().inflate(R.layout.game_header, root, false);
        root.addView(header);
        turnInfo = header.findViewById(R.id.turnInfo);
        header.findViewById(R.id.btnSettings).setVisibility(View.GONE);

        // top row: two corner seats (opponent 1 top-left, opponent 2 top-right)
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setPadding(dp(8), dp(8), dp(8), dp(4));

        topLeftSlot = new LinearLayout(this);
        topLeftSlot.setOrientation(LinearLayout.VERTICAL);
        topLeftSlot.setGravity(Gravity.CENTER);

        topRightSlot = new LinearLayout(this);
        topRightSlot.setOrientation(LinearLayout.VERTICAL);
        topRightSlot.setGravity(Gravity.CENTER);

        topRow.addView(topLeftSlot, new LinearLayout.LayoutParams(0, WRAP, 1f));
        topRow.addView(topRightSlot, new LinearLayout.LayoutParams(0, WRAP, 1f));
        root.addView(topRow, new LinearLayout.LayoutParams(MATCH, WRAP));

        LinearLayout middle = new LinearLayout(this);
        middle.setOrientation(LinearLayout.VERTICAL);
        middle.setGravity(Gravity.CENTER);
        root.addView(middle, new LinearLayout.LayoutParams(MATCH, 0, 1f));

        pileRow = new LinearLayout(this);
        pileRow.setOrientation(LinearLayout.HORIZONTAL);
        pileRow.setGravity(Gravity.CENTER);
        middle.addView(pileRow, new LinearLayout.LayoutParams(WRAP, WRAP));

        statusText = new TextView(this);
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(15);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(dp(16), dp(8), dp(16), 0);
        middle.addView(statusText, new LinearLayout.LayoutParams(WRAP, WRAP));

        timerText = new TextView(this);
        timerText.setTextSize(20);
        timerText.setTypeface(null, Typeface.BOLD);
        timerText.setTextColor(Color.parseColor("#C62828"));
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, dp(4), 0, 0);
        middle.addView(timerText, new LinearLayout.LayoutParams(WRAP, WRAP));

        // bottom row: you (bottom-left) + opponent 3 (bottom-right)
        LinearLayout youColumn = new LinearLayout(this);
        youColumn.setOrientation(LinearLayout.VERTICAL);
        youColumn.setGravity(Gravity.CENTER_HORIZONTAL);

        youAvatar = new AvatarView(this);
        youAvatar.setPlayer("You", null);
        LinearLayout.LayoutParams youLp = new LinearLayout.LayoutParams(dp(56), dp(56));
        youLp.topMargin = dp(4);
        youColumn.addView(youAvatar, youLp);

        TextView youLabel = new TextView(this);
        youLabel.setText("YOUR CARDS");
        youLabel.setTextColor(GOLD);
        youLabel.setTypeface(null, Typeface.BOLD);
        youLabel.setGravity(Gravity.CENTER);
        youLabel.setPadding(0, dp(2), 0, 0);
        youColumn.addView(youLabel, new LinearLayout.LayoutParams(WRAP, WRAP));

        bottomRightSlot = new LinearLayout(this);
        bottomRightSlot.setOrientation(LinearLayout.VERTICAL);
        bottomRightSlot.setGravity(Gravity.CENTER);

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);
        bottomRow.setPadding(0, dp(2), 0, dp(2));
        bottomRow.addView(youColumn, new LinearLayout.LayoutParams(0, WRAP, 1f));
        bottomRow.addView(bottomRightSlot, new LinearLayout.LayoutParams(0, WRAP, 1f));
        root.addView(bottomRow, new LinearLayout.LayoutParams(MATCH, WRAP));

        HorizontalScrollView scrollHand = new HorizontalScrollView(this);
        scrollHand.setHorizontalScrollBarEnabled(false);
        handRow = new LinearLayout(this);
        handRow.setOrientation(LinearLayout.HORIZONTAL);
        handRow.setPadding(dp(8), dp(4), dp(8), dp(4));
        scrollHand.addView(handRow);
        root.addView(scrollHand, new LinearLayout.LayoutParams(MATCH, WRAP));

        Button exit = new Button(this);
        exit.setText("EXIT GAME");
        exit.setBackgroundResource(R.drawable.btn_gold);
        exit.setTextColor(0xFF0B3D1E);
        exit.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(MATCH, WRAP);
        elp.setMargins(dp(16), dp(6), dp(16), dp(10));
        root.addView(exit, elp);
        exit.setOnClickListener(v -> confirmExit());

        ScrollView outerScroll = new ScrollView(this);
        outerScroll.setFillViewport(true);
        outerScroll.addView(root);
        setContentView(outerScroll);

        RafikiClient.setListener(listener);
        JSONObject last = RafikiClient.getLastGame();
        if (last != null) handle(last);
    }

    private void updateCardSizesForPlayerCount(int totalPlayers) {
        float baseHandW = 92, baseHandH = 132;
        float basePileW = 84, basePileH = 120;
        int extra = Math.max(0, totalPlayers - 2);
        float shrink = 1f - (extra * 0.08f);
        shrink = Math.max(shrink, 0.80f);

        cardW = (int) (dp((int) baseHandW) * shrink);
        cardH = (int) (dp((int) baseHandH) * shrink);
        pileCardW = (int) (dp((int) basePileW) * shrink);
        pileCardH = (int) (dp((int) basePileH) * shrink);
    }

    private void vibrate(long millis) {
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(millis);
        }
    }

    private void shakeView(View v) {
        if (v == null) return;
        TranslateAnimation shake = new TranslateAnimation(0, 25, 0, 0);
        shake.setDuration(60);
        shake.setRepeatCount(5);
        shake.setRepeatMode(Animation.REVERSE);
        v.startAnimation(shake);
    }

    private void ensureSounds() {
        if (soundsLoaded) return;
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attrs)
                .build();
        soundWin = soundPool.load(this, R.raw.win, 1);
        soundTimer = soundPool.load(this, R.raw.timer, 1);
        soundPlaying = soundPool.load(this, R.raw.playing, 1);
        soundWrong = soundPool.load(this, R.raw.wrong, 1);
        soundsLoaded = true;
    }

    private void playWrongSound(View sourceView) {
        ensureSounds();
        soundPool.play(soundWrong, 1f, 1f, 1, 0, 1f);
        vibrate(350);
        shakeView(sourceView);
    }

    private void playCorrectSound() {
        ensureSounds();
        soundPool.play(soundPlaying, 1f, 1f, 1, 0, 1f);
    }

    private void playDrawSound() {
        ensureSounds();
        soundPool.play(soundPlaying, 1f, 1f, 1, 0, 1f);
    }

    private void playTickSound(long secondsLeft) {
        ensureSounds();
        if (secondsLeft == 4) {
            soundPool.play(soundTimer, 1f, 1f, 1, 0, 1f);
        }
        vibrate(60);
    }

    private void playTimeUpSound() {
        if (toneGen != null) toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 400);
        vibrate(250);
    }

    private void playWinFanfare() {
        ensureSounds();
        soundPool.play(soundWin, 1f, 1f, 1, 0, 1f);
        if (toneGen == null) return;
        int[] tones = {ToneGenerator.TONE_DTMF_1, ToneGenerator.TONE_DTMF_3, ToneGenerator.TONE_DTMF_5, ToneGenerator.TONE_DTMF_8};
        for (int i = 0; i < tones.length; i++) {
            final int t = tones[i];
            soundHandler.postDelayed(() -> { if (toneGen != null) toneGen.startTone(t, 180); }, i * 180L);
        }
        vibrate(150);
    }

    private void handle(JSONObject m) {
        try {
            String type = m.getString("type");
            if (type.equals("game")) render(m);
            else if (type.equals("error")) toast(m.getString("message"));
        } catch (Exception e) {
            toast("Bad reply from server");
        }
    }

    private void cancelMyTurnTimer() {
        if (myTurnTimer != null) {
            myTurnTimer.cancel();
            myTurnTimer = null;
        }
        youAvatar.stopCountdown();
        timerText.setText("");
    }

    private void startMyTurnTimer() {
        cancelMyTurnTimer();
        youAvatar.startCountdown(10000);
        myTurnTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsLeft = millisUntilFinished / 1000 + 1;
                timerText.setText("\u23F1 " + secondsLeft + "s");
                if (secondsLeft <= 4 && secondsLeft >= 1) {
                    playTickSound(secondsLeft);
                }
            }

            @Override
            public void onFinish() {
                timerText.setText("");
                if (myTurn) {
                    playTimeUpSound();
                    Toast.makeText(RafikiGameActivity.this, "Time's up! Card drawn automatically.", Toast.LENGTH_SHORT).show();
                    sendMsg("draw", null, null);
                }
            }
        }.start();
    }

    private boolean canPlayNow(String card) {
        if (pendingPenalty > 0) {
            return rank(card).equals(pendingPenaltyRank) || rank(card).equals("A");
        }
        if (rank(card).equals("A")) return true;
        if (declaredSuit != null) {
            return suit(card).equals(declaredSuit) || rank(card).equals(rank(currentTop));
        }
        return canPlay(card, currentTop);
    }

    private LinearLayout buildOpponentBox(JSONObject o, boolean theirTurn, int totalOpponents) throws org.json.JSONException {
        String name = o.getString("name");
        int count = o.getInt("count");

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);

        AvatarView oppAvatar = new AvatarView(this);
        oppAvatar.setPlayer(name, null);
        oppAvatar.setActive(theirTurn);
        if (theirTurn) oppAvatar.startCountdown(10000);
        box.addView(oppAvatar, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout fan = new LinearLayout(this);
        fan.setOrientation(LinearLayout.HORIZONTAL);
        fan.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams fanContainerLp = new LinearLayout.LayoutParams(WRAP, cardH);
        fanContainerLp.topMargin = dp(4);

        int shown = Math.max(1, count);
        int oppAvail = getResources().getDisplayMetrics().widthPixels / Math.max(1, totalOpponents) - dp(24);
        int oppStep = shown > 1 ? Math.max(dp(4), Math.min((int) (cardW * 0.20f), (oppAvail - cardW) / (shown - 1))) : 0;
        for (int c = 0; c < shown; c++) {
            CardView back = new CardView(this);
            back.setFaceDown(true);
            LinearLayout.LayoutParams backLp = new LinearLayout.LayoutParams(cardW, cardH);
            backLp.leftMargin = (c == 0) ? 0 : -(cardW - oppStep);
            fan.addView(back, backLp);
        }
        box.addView(fan, fanContainerLp);

        TextView label = new TextView(this);
        label.setText(name + "\n" + count + (count == 1 ? " card" : " cards"));
        label.setTextColor(theirTurn ? GOLD : Color.WHITE);
        label.setTypeface(null, Typeface.BOLD);
        label.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(WRAP, WRAP);
        labelLp.topMargin = dp(2);
        box.addView(label, labelLp);

        return box;
    }

    private void render(JSONObject m) throws Exception {
        final String you = m.getString("you");
        myName = you;
        String turn = m.getString("turn");
        final String top = m.getString("top");
        currentTop = top;
        int deckCount = m.getInt("deckCount");
        int dir = m.getInt("direction");
        String winner = m.isNull("winner") ? null : m.getString("winner");
        JSONArray hand = m.getJSONArray("hand");
        JSONArray others = m.getJSONArray("others");

        pendingPenalty = m.optInt("pendingPenalty", 0);
        pendingPenaltyRank = m.isNull("pendingPenaltyRank") ? null : m.optString("pendingPenaltyRank", null);
        declaredSuit = m.isNull("declaredSuit") ? null : m.optString("declaredSuit", null);

        updateCardSizesForPlayerCount(others.length() + 1);

        boolean wasMyTurn = myTurn;
        myTurn = winner == null && turn.equals(you);

        turnInfo.setText("Turn: " + (turn.equals(you) ? "You" : turn) + "  "
                + (dir == 1 ? "\u279C" : "\u2B05"));

        youAvatar.setActive(myTurn);
        if (myTurn && !wasMyTurn) {
            startMyTurnTimer();
        } else if (!myTurn) {
            cancelMyTurnTimer();
        }

        topLeftSlot.removeAllViews();
        topRightSlot.removeAllViews();
        bottomRightSlot.removeAllViews();
        int n = others.length();

        if (n >= 1) {
            JSONObject o0 = others.getJSONObject(0);
            boolean t0 = winner == null && o0.getString("name").equals(turn);
            topLeftSlot.addView(buildOpponentBox(o0, t0, n));
        }
        if (n >= 2) {
            JSONObject o1 = others.getJSONObject(1);
            boolean t1 = winner == null && o1.getString("name").equals(turn);
            topRightSlot.addView(buildOpponentBox(o1, t1, n));
        }
        if (n >= 3) {
            JSONObject o2 = others.getJSONObject(2);
            boolean t2 = winner == null && o2.getString("name").equals(turn);
            bottomRightSlot.addView(buildOpponentBox(o2, t2, n));
        }

        pileRow.removeAllViews();

        LinearLayout drawColumn = new LinearLayout(this);
        drawColumn.setOrientation(LinearLayout.VERTICAL);
        drawColumn.setGravity(Gravity.CENTER);

        TextView drawLabel = new TextView(this);
        if (pendingPenalty > 0) {
            drawLabel.setText("\u26A0 PENALTY " + pendingPenalty);
            drawLabel.setTextColor(Color.parseColor("#FF5252"));
        } else {
            drawLabel.setText("\u2660 DRAW CARDS \u2660");
            drawLabel.setTextColor(Color.WHITE);
        }
        drawLabel.setTypeface(null, Typeface.BOLD);
        drawLabel.setGravity(Gravity.CENTER);
        drawLabel.setPadding(0, 0, 0, dp(4));
        drawColumn.addView(drawLabel, new LinearLayout.LayoutParams(WRAP, WRAP));

        CardView drawPileCardView = new CardView(this);
        drawPileCardView.setFaceDown(true);
        drawPileCardView.setClickable(true);

        int deckLayers = 4;
        int deckStep = dp(3);
        FrameLayout deckStack = new FrameLayout(this);
        for (int di = 0; di < deckLayers - 1; di++) {
            CardView under = new CardView(this);
            under.setFaceDown(true);
            FrameLayout.LayoutParams ulp = new FrameLayout.LayoutParams(pileCardW, pileCardH);
            ulp.leftMargin = di * deckStep;
            deckStack.addView(under, ulp);
        }
        FrameLayout.LayoutParams topLp = new FrameLayout.LayoutParams(pileCardW, pileCardH);
        topLp.leftMargin = (deckLayers - 1) * deckStep;
        deckStack.addView(drawPileCardView, topLp);
        deckStack.setOnClickListener(dv -> drawPileCardView.performClick());

        drawPileCardView.setOnClickListener(v -> {
            if (!myTurn) {
                playWrongSound(v);
                toast("Not your turn");
                return;
            }
            DrawFly.fly(RafikiGameActivity.this, drawPileCardView, handRow);
            playDrawSound();
            sendMsg("draw", null, null);
        });

        LinearLayout.LayoutParams deckLp = new LinearLayout.LayoutParams(
                pileCardW + (deckLayers - 1) * deckStep, pileCardH);
        deckLp.setMargins(dp(12), 0, dp(12), 0);
        drawColumn.addView(deckStack, deckLp);

        pileRow.addView(drawColumn);

        CardView topCardView = new CardView(this);
        topCardView.setCard(rank(top), symbol(suit(top)), isRed(top));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(pileCardW, pileCardH);
        tlp.setMargins(dp(8), 0, dp(8), 0);
        pileRow.addView(topCardView, tlp);

        if (winner != null) {
            statusText.setText("Game over");
        } else if (myTurn) {
            if (pendingPenalty > 0) {
                statusText.setText("Defend with " + pendingPenaltyRank + " or ACE, or draw " + pendingPenalty);
            } else if (declaredSuit != null) {
                statusText.setText("Your turn: suit is " + symbol(declaredSuit) + " " + declaredSuit);
            } else {
                statusText.setText("Your turn: play a matching card or tap DRAW");
            }
        } else {
            statusText.setText("Waiting for " + turn + "...");
        }

        handRow.removeAllViews();
        List<String> handCards = new ArrayList<>();
        for (int i = 0; i < hand.length(); i++) handCards.add(hand.getString(i));
        handOrder.retainAll(handCards);
        for (String s : handCards) if (!handOrder.contains(s)) handOrder.add(s);
        handCards = new ArrayList<>(handOrder);

        int screenW = getResources().getDisplayMetrics().widthPixels;
        int handAvail = screenW - dp(24);
        int minSliver = (int) (cardW * 0.30f);
        int handCount = handCards.size();
        int handStep = handCount > 1
                ? Math.max(minSliver, Math.min(cardW, (handAvail - cardW) / (handCount - 1)))
                : 0;

        for (int i = 0; i < handCards.size(); i++) {
            final String c = handCards.get(i);
            boolean playable = myTurn && canPlayNow(c);

            CardView cardView = new CardView(this);
            cardView.setCard(rank(c), symbol(suit(c)), isRed(c));
            cardView.setAlpha(1f);
            cardView.setClickable(true);
            cardView.setOnClickListener(v -> onCardTapped(c, v, handOrder));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(cardW, cardH);
            lp.leftMargin = (i == 0) ? dp(4) : -(cardW - handStep);
            handRow.addView(cardView, lp);
        }

        if (winner != null && !gameOverShown) {
            gameOverShown = true;
            cancelMyTurnTimer();
            boolean iWon = winner.equals(you);
            playWinFanfare();
            soundHandler.postDelayed(() -> {
                WinScreenManager.show(this, iWon ? "You" : winner, this::exitGame);
            }, 300);
        }
    }

    private void onCardTapped(String card, View sourceView, List<String> handCards) {
        if (!myTurn) {
            playWrongSound(sourceView);
            toast("Not your turn");
            return;
        }
        if (!canPlayNow(card)) {
            playWrongSound(sourceView);
            toast("That card doesn't match \u2014 pick another or draw");
            return;
        }

        String r = rank(card);

        if (r.equals("A") && pendingPenalty == 0) {
            promptForSuit(card);
            return;
        }

        if (r.equals("A") || r.equals("J") || r.equals("K")) {
            playCorrectSound();
            sendMsg("play", card, null);
            return;
        }

        List<String> sameRankOthers = new ArrayList<>();
        for (String c : handCards) {
            if (!c.equals(card) && rank(c).equals(r)) sameRankOthers.add(c);
        }

        if (sameRankOthers.isEmpty()) {
            playCorrectSound();
            sendMsg("play", card, null);
        } else {
            promptForGroupPlay(card, sameRankOthers);
        }
    }

    private void promptForGroupPlay(String firstCard, List<String> others) {
        cancelMyTurnTimer();
        String[] labels = new String[others.size()];
        boolean[] checked = new boolean[others.size()];
        for (int i = 0; i < others.size(); i++) {
            labels[i] = rank(others.get(i)) + symbol(suit(others.get(i)));
        }

        new AlertDialog.Builder(this)
                .setTitle("You have more " + rank(firstCard) + "s \u2014 play together?")
                .setMultiChoiceItems(labels, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("Play Selected", (dialog, which) -> {
                    List<String> group = new ArrayList<>();
                    group.add(firstCard);
                    for (int i = 0; i < others.size(); i++) {
                        if (checked[i]) group.add(others.get(i));
                    }
                    playCorrectSound();
                    if (group.size() > 1) {
                        sendMsgGroup(group);
                    } else {
                        sendMsg("play", firstCard, null);
                    }
                })
                .setNegativeButton("Just This One", (dialog, which) -> {
                    playCorrectSound();
                    sendMsg("play", firstCard, null);
                })
                .setCancelable(false)
                .show();
    }

    private void promptForSuit(String aceCard) {
        cancelMyTurnTimer();
        String[] suitCodes = {"H", "D", "C", "S"};
        String[] suitLabels = {"\u2665 HEARTS", "\u2666 DIAMONDS", "\u2663 CLUBS", "\u2660 SPADES"};
        new AlertDialog.Builder(this)
                .setTitle("Choose next suit")
                .setItems(suitLabels, (dialog, which) -> {
                    playCorrectSound();
                    sendMsg("play", aceCard, suitCodes[which]);
                })
                .setCancelable(false)
                .show();
    }

    // ---------- card helpers ----------
    private static String suit(String c) { return c.substring(c.length() - 1); }
    private static String rank(String c) { return c.substring(0, c.length() - 1); }

    private static boolean canPlay(String c, String top) {
        return suit(c).equals(suit(top)) || rank(c).equals(rank(top));
    }

    private static boolean isRed(String c) {
        String s = suit(c);
        return s.equals("H") || s.equals("D");
    }

    private static String symbol(String s) {
        switch (s) {
            case "S": return "\u2660";
            case "H": return "\u2665";
            case "D": return "\u2666";
            default:  return "\u2663";
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    // ---------- actions ----------
    private void sendMsg(String type, String card, String suit) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", type);
            if (card != null) msg.put("card", card);
            if (suit != null) msg.put("suit", suit);
            RafikiClient.send(msg);
        } catch (Exception ignored) { }
    }

    private void sendMsgGroup(List<String> cards) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", "playGroup");
            JSONArray arr = new JSONArray();
            for (String c : cards) arr.put(c);
            msg.put("cards", arr);
            RafikiClient.send(msg);
        } catch (Exception ignored) { }
    }

    private void confirmExit() {
        if (gameOverShown) { exitGame(); return; }
        new AlertDialog.Builder(this)
                .setTitle("Leave the game?")
                .setPositiveButton("Leave", (d, w) -> exitGame())
                .setNegativeButton("Stay", null)
                .show();
    }

    private void exitGame() {
        cancelMyTurnTimer();
        sendMsg("leave", null, null);
        RafikiClient.close();
        finish();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        confirmExit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelMyTurnTimer();
        soundHandler.removeCallbacksAndMessages(null);
        if (toneGen != null) {
            toneGen.release();
            toneGen = null;
        }
        RafikiClient.setListener(null);
    }
}
