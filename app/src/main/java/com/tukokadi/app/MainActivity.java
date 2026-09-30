package com.tukokadi.app;
import android.widget.HorizontalScrollView;
import android.media.SoundPool;
import android.media.AudioAttributes;

import android.app.Activity;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipDescription;
import android.view.DragEvent;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int GREEN_DARK = Color.parseColor("#1B5E20");
    private static final int GREEN_LIGHT = Color.parseColor("#2E7D32");
    private static final int GOLD = Color.parseColor("#FFD700");
    private static final int CREAM = Color.parseColor("#F5F5F0");

    private GameEngine game;
    private TextView statusText;
    private TextView topCardText;
    private TextView declaredSuitText;
    private TextView timerText;
    private TextView bobTimerText;
    private TextView carolTimerText;
    private LinearLayout bobContainer;
    private LinearLayout carolContainer;
    private LinearLayout handLayout;
    private LinearLayout rootLayout;
    private CardView discardCardView;
    private CardView drawPileCardView;
    private String lastTopCardKey = "";
    private Button newGameButton;
    private CountDownTimer turnTimer;
    private CountDownTimer botTimer;
    private AvatarView youAvatar, bobAvatar, carolAvatar;
    private ToneGenerator toneGen;
    private Handler soundHandler = new Handler(Looper.getMainLooper());
    private Vibrator vibrator;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean winAnnounced = false;
    private int cardW;
    private int cardH;
    private int screenW;
    private int overlapMargin;
    private List<Card> handOrder = new ArrayList<>();
    private List<View> highlightedGroupViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                java.io.StringWriter sw = new java.io.StringWriter();
                e.printStackTrace(new java.io.PrintWriter(sw));
                java.io.FileWriter fw = new java.io.FileWriter(new java.io.File(getFilesDir(), "crash.txt"));
                fw.write(sw.toString());
                fw.close();
            } catch (Exception ignored) {}
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        });

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenH = dm.heightPixels;
    screenW = dm.widthPixels;
        cardH = (int) (screenH * 0.22f);
        cardW = (int) (cardH * 0.7f);
        overlapMargin = -(int) (cardW * 0.80f);
        int botGapPx = (int) (0.3937f * dm.densityDpi);

        toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                ttsReady = true;
            }
        });

        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(24, 0, 24, 24);
        android.graphics.drawable.GradientDrawable feltBg = new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#2E7D32"), Color.parseColor("#1B5E20"), Color.parseColor("#0D3D12")});
        rootLayout.setBackground(feltBg);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        TextView title = new TextView(this);
        title.setText("TUKO KADI");
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(GOLD);
        title.setBackgroundColor(GREEN_DARK);
        title.setGravity(Gravity.CENTER);
        title.setPadding(24, 36, 24, 36);
        rootLayout.addView(title);

        LinearLayout botsRow = new LinearLayout(this);
        botsRow.setOrientation(LinearLayout.HORIZONTAL);
        botsRow.setGravity(Gravity.CENTER_VERTICAL);
        botsRow.setPadding(0, 12, 0, 4);

        LinearLayout bobColumn = new LinearLayout(this);
        bobColumn.setOrientation(LinearLayout.VERTICAL);
        bobColumn.setGravity(Gravity.CENTER);
        bobColumn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        bobTimerText = new TextView(this);
        bobTimerText.setTextSize(16);
        bobTimerText.setTypeface(null, Typeface.BOLD);
        bobTimerText.setTextColor(Color.parseColor("#FF5252"));
        bobTimerText.setGravity(Gravity.CENTER);
        bobAvatar = new AvatarView(this);
        bobAvatar.setPlayer("Bob", null);
        LinearLayout.LayoutParams bobAvLp = new LinearLayout.LayoutParams((int) (56 * getResources().getDisplayMetrics().density), (int) (56 * getResources().getDisplayMetrics().density));
        bobAvLp.gravity = Gravity.CENTER_HORIZONTAL;
        bobColumn.addView(bobAvatar, bobAvLp);
        TextView bobNameLabel = new TextView(this);
        bobNameLabel.setText("Bob");
        bobNameLabel.setTextColor(Color.parseColor("#F4D160"));
        bobNameLabel.setTextSize(14);
        bobNameLabel.setGravity(Gravity.CENTER);
        bobNameLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams bobNameLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bobNameLp.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
        bobColumn.addView(bobNameLabel, bobNameLp);
        bobColumn.addView(bobTimerText);

        bobContainer = new LinearLayout(this);
        bobContainer.setOrientation(LinearLayout.VERTICAL);
        bobContainer.setGravity(Gravity.CENTER);
        bobColumn.addView(bobContainer);

        botsRow.addView(bobColumn, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        View botGap = new View(this);
        LinearLayout.LayoutParams gapParams = new LinearLayout.LayoutParams(0, 1, 1f);
        botGap.setMinimumWidth(botGapPx);
        botGap.setLayoutParams(gapParams);
        

        LinearLayout carolColumn = new LinearLayout(this);
        carolColumn.setOrientation(LinearLayout.VERTICAL);
        carolColumn.setGravity(Gravity.CENTER);
        carolColumn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        carolTimerText = new TextView(this);
        carolTimerText.setTextSize(16);
        carolTimerText.setTypeface(null, Typeface.BOLD);
        carolTimerText.setTextColor(Color.parseColor("#FF5252"));
        carolTimerText.setGravity(Gravity.CENTER);
        carolAvatar = new AvatarView(this);
        carolAvatar.setPlayer("Carol", null);
        LinearLayout.LayoutParams carolAvLp = new LinearLayout.LayoutParams((int) (56 * getResources().getDisplayMetrics().density), (int) (56 * getResources().getDisplayMetrics().density));
        carolAvLp.gravity = Gravity.CENTER_HORIZONTAL;
        carolColumn.addView(carolAvatar, carolAvLp);
        TextView carolNameLabel = new TextView(this);
        carolNameLabel.setText("Carol");
        carolNameLabel.setTextColor(Color.parseColor("#F4D160"));
        carolNameLabel.setTextSize(14);
        carolNameLabel.setGravity(Gravity.CENTER);
        carolNameLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams carolNameLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        carolNameLp.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
        carolColumn.addView(carolNameLabel, carolNameLp);
        carolColumn.addView(carolTimerText);

        carolContainer = new LinearLayout(this);
        carolContainer.setOrientation(LinearLayout.VERTICAL);
        carolContainer.setGravity(Gravity.CENTER);
        carolColumn.addView(carolContainer);

        botsRow.addView(carolColumn, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        rootLayout.addView(botsRow);

        LinearLayout tableLayout = new LinearLayout(this);
        tableLayout.setOrientation(LinearLayout.HORIZONTAL);
        tableLayout.setGravity(Gravity.CENTER);
        tableLayout.setPadding(0, 20, 0, 8);

        LinearLayout drawPileColumn = new LinearLayout(this);
        drawPileColumn.setOrientation(LinearLayout.VERTICAL);
        drawPileColumn.setGravity(Gravity.CENTER);

        TextView drawLabel = new TextView(this);
        drawLabel.setText("\u2660 DRAW CARDS \u2660");
        drawLabel.setTextSize(17);
        drawLabel.setTypeface(null, Typeface.BOLD);
        drawLabel.setTextColor(Color.parseColor("#FFD700"));
        drawLabel.setGravity(Gravity.CENTER);
        drawLabel.setPadding(0, 0, 0, 6);

        drawPileCardView = new CardView(this);
        drawPileCardView.setFaceDown(true);
        drawPileCardView.setClickable(true);
        LinearLayout.LayoutParams pileParams = new LinearLayout.LayoutParams(cardW, cardH);
        pileParams.setMargins(16, 0, 16, 0);
        drawPileCardView.setLayoutParams(pileParams);
        drawPileCardView.setOnClickListener(v -> {
            if (game.currentPlayerIndex == 0 && !game.gameOver) DrawFly.fly(MainActivity.this, drawPileCardView, handLayout);
            if (game.currentPlayerIndex == 0 && !game.gameOver) {
                playDrawSound();
                game.drawCard();
                refresh();
                runNextBotTurnIfNeeded();
            }
        });
        int deckLayers = 6;
        int deckStep = (int) (3 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout deckStack = new android.widget.FrameLayout(this);
        for (int di = 0; di < deckLayers - 1; di++) {
            CardView under = new CardView(this);
            under.setFaceDown(true);
            android.widget.FrameLayout.LayoutParams ulp = new android.widget.FrameLayout.LayoutParams(cardW, cardH);
            ulp.leftMargin = di * deckStep;
            deckStack.addView(under, ulp);
        }
        android.widget.FrameLayout.LayoutParams topLp = new android.widget.FrameLayout.LayoutParams(cardW, cardH);
        topLp.leftMargin = (deckLayers - 1) * deckStep;
        deckStack.addView(drawPileCardView, topLp);
        deckStack.setOnClickListener(dv -> drawPileCardView.performClick());
        LinearLayout.LayoutParams deckLp = new LinearLayout.LayoutParams(cardW + (deckLayers - 1) * deckStep, cardH);
        deckLp.setMargins(16, 0, 16, 0);
        drawPileColumn.addView(deckStack, deckLp);
        drawPileColumn.addView(drawLabel);

        tableLayout.addView(drawPileColumn);
        drawPileColumn.setTranslationX(-20f * getResources().getDisplayMetrics().density);

        LinearLayout discardColumn = new LinearLayout(this);
        discardColumn.setOrientation(LinearLayout.VERTICAL);
        discardColumn.setGravity(Gravity.CENTER);

        discardCardView = new CardView(this);
        LinearLayout.LayoutParams discardParams = new LinearLayout.LayoutParams(cardW, cardH);
        discardParams.setMargins(16, 0, 16, 0);
        discardCardView.setLayoutParams(discardParams);
        discardColumn.addView(discardCardView);

        topCardText = new TextView(this);
        topCardText.setTextSize(40);
        topCardText.setTypeface(null, Typeface.BOLD);
        topCardText.setPadding(0, 8, 0, 4);
        topCardText.setGravity(Gravity.END);

        tableLayout.addView(discardColumn);
        tableLayout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);

        rootLayout.addView(tableLayout);

        
        declaredSuitText = new TextView(this);
        declaredSuitText.setTextSize(18);
        declaredSuitText.setTypeface(null, Typeface.BOLD);
        declaredSuitText.setTextColor(Color.parseColor("#B8860B"));
        declaredSuitText.setPadding(0, 0, 0, 4);
        rootLayout.addView(declaredSuitText);

        statusText = new TextView(this);
        statusText.setTextSize(16);
        statusText.setTextColor(Color.DKGRAY);
        statusText.setPadding(0, 4, 0, 4);
        rootLayout.addView(statusText);

        timerText = new TextView(this);
        timerText.setTextSize(22);
        timerText.setTypeface(null, Typeface.BOLD);
        timerText.setTextColor(Color.parseColor("#C62828"));
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, 0, 0, 16);
        rootLayout.addView(timerText);

        View spacer = new View(this);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        spacer.setLayoutParams(spacerParams);
        rootLayout.addView(spacer);

        TextView handLabel = new TextView(this);
        handLabel.setText("You:");
        handLabel.setTextSize(16);
        handLabel.setTypeface(null, Typeface.BOLD);
        handLabel.setTextColor(GREEN_DARK);
        youAvatar = new AvatarView(this);
        youAvatar.setPlayer("You", null);
        rootLayout.addView(youAvatar, new LinearLayout.LayoutParams((int) (56 * getResources().getDisplayMetrics().density), (int) (56 * getResources().getDisplayMetrics().density)));
        rootLayout.addView(handLabel);

        handLayout = new HandRow(this);
        handLayout.setOrientation(LinearLayout.HORIZONTAL);
        handLayout.setGravity(Gravity.CENTER_VERTICAL);
        handLayout.setPadding(8, 8, 8, 8);
        LinearLayout handScroll = new LinearLayout(this);
        handScroll.setClipChildren(false);
        handScroll.setClipToPadding(false);
        handLayout.setClipChildren(false);
        handLayout.setClipToPadding(false);
        handScroll.addView(handLayout);
        rootLayout.addView(handScroll);

        newGameButton = new Button(this);
        newGameButton.setText("Exit Game");
        newGameButton.setTextColor(GOLD);
        newGameButton.setTypeface(null, Typeface.BOLD);
        GradientDrawable newGameBg = new GradientDrawable();
        newGameBg.setColor(GREEN_LIGHT);
        newGameBg.setCornerRadius(24);
        newGameButton.setBackground(newGameBg);
        LinearLayout.LayoutParams newGameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        newGameParams.setMargins(0, 20, 0, 12);
        newGameButton.setLayoutParams(newGameParams);
        newGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        rootLayout.addView(newGameButton);

        FitLayout scroll = new FitLayout(this);
        
        scroll.addView(rootLayout);
        setContentView(scroll);

        startNewGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        soundHandler.removeCallbacksAndMessages(null);
        cancelTurnTimer();
        cancelBotTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundHandler.removeCallbacksAndMessages(null);
        if (toneGen != null) {
            toneGen.release();
            toneGen = null;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
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

    private SoundPool soundPool;
    private int soundWin, soundTimer, soundPlaying, soundWrong;
    private boolean soundsLoaded = false;

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

    private void playGroupSound() {
        ensureSounds();
        soundPool.play(soundPlaying, 1f, 1f, 1, 0, 1f);
    }

    private void playGroupAvailableSound() {
        ensureSounds();
        soundPool.play(soundTimer, 1f, 1f, 1, 0, 1.35f);
    }

    private void clearGroupHighlights() {
        for (View v : highlightedGroupViews) {
            v.setForeground(null);
            v.animate().translationY(0f).scaleX(1f).scaleY(1f).setDuration(150).start();
            v.setElevation(0f);
        }
        highlightedGroupViews.clear();
    }

    private void highlightGroupCards(Card firstCard, List<Card> others) {
        clearGroupHighlights();
        // Highlighting disabled intentionally - player chooses freely without visual hints.
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

    private void announceWinner(String name) {
        playWinFanfare();
        soundHandler.postDelayed(() -> {
            if (tts != null && ttsReady) {
                String msg = "Congratulations " + name + "! You win the game!";
                tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "winMsg");
            }
        }, 950);

    WinScreenManager.show(this, name, this::startNewGame);
}
    private void startNewGame() {
        cancelTurnTimer();
        cancelBotTimer();
        game = new GameEngine(Arrays.asList("You", "Bob", "Carol"));
        winAnnounced = false;
        handOrder.clear();
        refresh();
        runNextBotTurnIfNeeded();
    }

    private void renderPlayerStack(LinearLayout container, String name, int count) {
        container.removeAllViews();

        int shown = count;
        int localGapPx = (int) (0.3937f * getResources().getDisplayMetrics().densityDpi);
        int half = (screenW - localGapPx) / 2 - 16;

        android.widget.FrameLayout fan = new android.widget.FrameLayout(this);
        LinearLayout.LayoutParams fanParams = new LinearLayout.LayoutParams(half, cardH);
        fan.setLayoutParams(fanParams);

        if (shown > 0) {
            int step;
            if (shown <= 1) {
                step = 0;
            } else {
                int usableWidth = half - cardW;
                step = usableWidth / (shown - 1);
                if (step > cardW) step = cardW;
                if (step < 10) step = 10;
            }

            int totalWidth = cardW + (step * (shown - 1));
            int startX = (half - totalWidth) / 2;
            if (startX < 0) startX = 0;

            for (int i = 0; i < shown; i++) {
                CardView back = new CardView(this);
                back.setFaceDown(true);

                android.widget.FrameLayout.LayoutParams lp =
                        new android.widget.FrameLayout.LayoutParams(cardW, cardH);
                lp.leftMargin = startX + (i * step);
                lp.topMargin = 0;
                back.setLayoutParams(lp);

                fan.addView(back);
            }
        }

        container.addView(fan);

        TextView label = new TextView(this);
        label.setText(name + "\n" + count + " cards");
        label.setTextSize(13);
        label.setTypeface(null, Typeface.BOLD);
        label.setTextColor(Color.parseColor("#FFD700"));
        label.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = 6;
        label.setLayoutParams(labelParams);
        container.addView(label);
    }

    private void updateDeclaredChip() {
        Card.Suit s = game.declaredSuit;
        if (s == null) {
            declaredSuitText.setText("");
            declaredSuitText.setBackgroundColor(Color.TRANSPARENT);
            return;
        }
        boolean red = (s == Card.Suit.HEARTS || s == Card.Suit.DIAMONDS);
        declaredSuitText.setText("  Next: " + suitSymbol(s) + "  ");
        declaredSuitText.setTextSize(24);
        declaredSuitText.setTextColor(Color.parseColor(red ? "#E53935" : "#212121"));
        declaredSuitText.setBackgroundColor(Color.WHITE);
    }

    private void flipDiscard(Card card) {
        discardCardView.animate().scaleX(0f).setDuration(120).withEndAction(() -> {
            discardCardView.setCard(rankLabel(card.rank), suitSymbol(card.suit), isRed(card));
            discardCardView.animate().scaleX(1f).setDuration(120).start();
        }).start();
    }

    private void refresh() {
        topCardText.setTextColor(isRed(game.topCard()) ? Color.parseColor("#E53935") : Color.parseColor("#212121"));
        topCardText.setText(suitSymbol(game.topCard().suit));

        if (game.declaredSuit != null) {
            updateDeclaredChip();
        } else {
            updateDeclaredChip();
        }

        Card topNow = game.topCard();
        String topKey = displayName(topNow);
        if (!topKey.equals(lastTopCardKey)) {
            lastTopCardKey = topKey;
            flipDiscard(topNow);
        }
        renderPlayerStack(bobContainer, "Bob", game.players.get(1).hand.size());
        renderPlayerStack(carolContainer, "Carol", game.players.get(2).hand.size());

        if (game.gameOver) {
            statusText.setText((game.winnerName.equals("You") ? "You win the game!" : game.winnerName + " wins the game!") + " Tap New Game to play again.");
            if (!winAnnounced) {
                winAnnounced = true;
                GameHistory.recordBotResult(this, game.winnerName.equals("You"));
                announceWinner(game.winnerName);
            }
        } else {
            statusText.setText("");
        }

        handLayout.removeAllViews();
        Player you = game.players.get(0);

        if (handOrder.isEmpty() && !you.hand.isEmpty()) {
            handOrder.addAll(you.hand);
            handOrder.sort(Comparator
                    .comparing((Card c) -> c.suit.ordinal())
                    .thenComparing(c -> c.rank.value));
        } else {
            handOrder.retainAll(you.hand);
            for (Card c : you.hand) {
                if (!handOrder.contains(c)) {
                    handOrder.add(c);
                }
            }
        }

        for (int i = 0; i < handOrder.size(); i++) {
            final int index = i;
            Card card = handOrder.get(i);
            CardView cardView = new CardView(this);
            cardView.setCard(rankLabel(card.rank), suitSymbol(card.suit), isRed(card));
            cardView.setClickable(true);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(cardW, cardH);
            cardParams.topMargin = 4;
            cardParams.bottomMargin = 4;
            cardParams.leftMargin = (i == 0) ? 6 : overlapMargin;
            cardParams.rightMargin = 6;
            cardView.setLayoutParams(cardParams);
            cardView.setOnClickListener(v -> onCardTapped(card, v));
            cardView.setOnLongClickListener(v -> {
                ClipData.Item item = new ClipData.Item(String.valueOf(index));
                ClipData dragData = new ClipData(
                        "card_reorder",
                        new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                        item);
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDragAndDrop(dragData, shadowBuilder, v, 0);
                v.setAlpha(0.4f);
                return true;
            });
            cardView.setOnDragListener((v, event) -> {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.setScaleY(1.08f);
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        v.setScaleY(1f);
                        return true;
                    case DragEvent.ACTION_DROP: {
                        v.setScaleY(1f);
                        try {
                            int fromIndex = Integer.parseInt(
                                    event.getClipData().getItemAt(0).getText().toString());
                            int toIndex = index;
                            if (fromIndex != toIndex && fromIndex >= 0 && fromIndex < handOrder.size()) {
                                Card moved = handOrder.remove(fromIndex);
                                if (toIndex > handOrder.size()) toIndex = handOrder.size();
                                handOrder.add(toIndex, moved);
                                refresh();
                            }
                        } catch (Exception ignored) {
                        }
                        return true;
                    }
                    case DragEvent.ACTION_DRAG_ENDED: {
                        v.setScaleY(1f);
                        View draggedView = (View) event.getLocalState();
                        if (draggedView != null) draggedView.setAlpha(1f);
                        return true;
                    }
                    default:
                        return true;
                }
            });
            handLayout.addView(cardView);
        }

        cancelTurnTimer();
        if (!game.gameOver && game.currentPlayerIndex == 0) {
            startTurnTimer();
        } else {
            timerText.setText("");
        }
    }

    private void startTurnTimer() {
        bobAvatar.stopCountdown();
        carolAvatar.stopCountdown();
        youAvatar.startCountdown(10000);
        turnTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsLeft = millisUntilFinished / 1000 + 1;
                timerText.setText("⏱ " + secondsLeft + "s");
                if (secondsLeft <= 4 && secondsLeft >= 1) {
                    playTickSound(secondsLeft);
                }
            }

            @Override
            public void onFinish() {
                timerText.setText("");
                if (!game.gameOver && game.currentPlayerIndex == 0) {
                    playTimeUpSound();
                    Toast.makeText(MainActivity.this, "Time's up! Card drawn automatically.", Toast.LENGTH_SHORT).show();
                    game.drawCard();
                    refresh();
                    runNextBotTurnIfNeeded();
                }
            }
        }.start();
    }

    private void cancelTurnTimer() {
        if (youAvatar != null) youAvatar.stopCountdown();
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
    }

    private void cancelBotTimer() {
        if (bobAvatar != null) bobAvatar.stopCountdown();
        if (carolAvatar != null) carolAvatar.stopCountdown();
        if (botTimer != null) {
            botTimer.cancel();
            botTimer = null;
        }
    }

    private void runNextBotTurnIfNeeded() {
        cancelBotTimer();

        if (game.gameOver || game.currentPlayerIndex == 0) {
            if (bobTimerText != null) bobTimerText.setText("");
            if (carolTimerText != null) carolTimerText.setText("");
            return;
        }

        int idx = game.currentPlayerIndex;
        TextView activeTimerText = (idx == 1) ? bobTimerText : carolTimerText;
        TextView otherTimerText = (idx == 1) ? carolTimerText : bobTimerText;
        if (otherTimerText != null) otherTimerText.setText("");

        youAvatar.stopCountdown();
        (idx == 1 ? carolAvatar : bobAvatar).stopCountdown();
        (idx == 1 ? bobAvatar : carolAvatar).startCountdown(10000);
        botTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsLeft = millisUntilFinished / 1000 + 1;
                if (activeTimerText != null) {
                    activeTimerText.setText("\u23F1 " + secondsLeft + "s");
                }
            }

            @Override
            public void onFinish() {
                if (activeTimerText != null) {
                    activeTimerText.setText("");
                }
                performBotMove();
                refresh();
                runNextBotTurnIfNeeded();
            }
        }.start();

        long randomDelay = 1000 + (long) (Math.random() * 3000);
        soundHandler.postDelayed(() -> {
            if (botTimer != null) {
                botTimer.cancel();
                botTimer = null;
                if (activeTimerText != null) {
                    activeTimerText.setText("");
                }
                performBotMove();
                refresh();
                runNextBotTurnIfNeeded();
            }
        }, randomDelay);
    }

    private void performBotMove() {
        if (game.gameOver || game.currentPlayerIndex == 0) return;
        Player bot = game.currentPlayer();
        Card playable = null;
        for (Card c : bot.hand) {
            if (game.canPlay(c)) {
                playable = c;
                break;
            }
        }
        if (playable != null) {
            if (playable.rank == Card.Rank.ACE || playable.rank == Card.Rank.JACK) {
                Card.Suit declared = (playable.rank == Card.Rank.ACE) ? Card.Suit.HEARTS : null;
                game.playCard(playable, declared);
            } else {
                List<Card> group = new ArrayList<>();
                group.add(playable);
                for (Card c : bot.hand) {
                    if (c.rank == playable.rank && c != playable) {
                        group.add(c);
                    }
                }
                if (group.size() > 1) {
                    boolean ok = game.playCardGroup(group);
                    if (!ok) {
                        game.playCard(playable, null);
                    }
                } else {
                    game.playCard(playable, null);
                }
            }
        } else {
            game.drawCard();
        }
    }

    private boolean isRed(Card c) {
        return c.suit == Card.Suit.HEARTS || c.suit == Card.Suit.DIAMONDS;
    }

    private String suitSymbol(Card.Suit s) {
        switch (s) {
            case HEARTS: return "\u2665";
            case DIAMONDS: return "\u2666";
            case CLUBS: return "\u2663";
            case SPADES: return "\u2660";
        }
        return "";
    }

    private String rankLabel(Card.Rank r) {
        switch (r) {
            case ACE: return "A";
            case KING: return "K";
            case QUEEN: return "Q";
            case JACK: return "J";
            default: return String.valueOf(r.value);
        }
    }

    private String displayName(Card c) {
        return rankLabel(c.rank) + suitSymbol(c.suit);
    }

    private void onCardTapped(Card card, View sourceView) {
        if (game.gameOver || game.currentPlayerIndex != 0) return;

        if (!game.canPlay(card)) {
            playWrongSound(sourceView);
            Toast.makeText(this, "That card doesn't match — pick another or draw", Toast.LENGTH_SHORT).show();
            return;
        }

        if (card.rank == Card.Rank.ACE && game.pendingPenalty == 0) {
            promptForSuit(card);
            return;
        }

        if (card.rank == Card.Rank.ACE || card.rank == Card.Rank.JACK) {
            game.playCard(card, null);
            playCorrectSound();
            refresh();
            runNextBotTurnIfNeeded();
            return;
        }

        Player you = game.players.get(0);
        List<Card> sameRankOthers = new ArrayList<>();
        for (Card c : you.hand) {
            if (c.rank == card.rank && c != card) {
                sameRankOthers.add(c);
            }
        }

        if (sameRankOthers.isEmpty()) {
            game.playCard(card, null);
            playCorrectSound();
            refresh();
            runNextBotTurnIfNeeded();
        } else {
            promptForGroupPlay(card, sameRankOthers);
        }
    }

    private void promptForGroupPlay(Card firstCard, List<Card> others) {
        cancelTurnTimer();

        highlightGroupCards(firstCard, others);
        playGroupAvailableSound();

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(48, 36, 48, 36);
        android.graphics.drawable.GradientDrawable boxBg =
                new android.graphics.drawable.GradientDrawable();
        boxBg.setColor(Color.parseColor("#CC0B3D20"));
        boxBg.setCornerRadius(32);
        box.setBackground(boxBg);

        TextView msg = new TextView(this);
        msg.setText("You have " + (others.size() + 1) + " cards of rank "
                + rankLabel(firstCard.rank) + " \u2014 the glowing cards can be played together.");
        msg.setTextColor(Color.WHITE);
        msg.setTextSize(15);
        msg.setGravity(Gravity.CENTER);
        msg.setPadding(0, 0, 0, 28);
        box.addView(msg);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(Gravity.CENTER);

        Button ignoreBtn = new Button(this);
        ignoreBtn.setText("Ignore");
        ignoreBtn.setTextColor(Color.WHITE);
        android.graphics.drawable.GradientDrawable ignoreBg =
                new android.graphics.drawable.GradientDrawable();
        ignoreBg.setColor(Color.parseColor("#33FFFFFF"));
        ignoreBg.setCornerRadius(100);
        ignoreBg.setStroke(2, Color.WHITE);
        ignoreBtn.setBackground(ignoreBg);
        LinearLayout.LayoutParams ignoreLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ignoreLp.rightMargin = 24;
        ignoreBtn.setLayoutParams(ignoreLp);
        ignoreBtn.setPadding(48, 20, 48, 20);

        Button playBtn = new Button(this);
        playBtn.setText("Play");
        playBtn.setTextColor(Color.parseColor("#0B3D20"));
        playBtn.setTypeface(null, Typeface.BOLD);
        android.graphics.drawable.GradientDrawable playBg =
                new android.graphics.drawable.GradientDrawable();
        playBg.setColor(Color.parseColor("#FFD700"));
        playBg.setCornerRadius(100);
        playBtn.setBackground(playBg);
        playBtn.setPadding(48, 20, 48, 20);

        buttonRow.addView(ignoreBtn);
        buttonRow.addView(playBtn);
        box.addView(buttonRow);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(box)
                .setCancelable(false)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0.25f);
        }

        ignoreBtn.setOnClickListener(v -> {
            clearGroupHighlights();
            game.playCard(firstCard, null);
            playCorrectSound();
            refresh();
            runNextBotTurnIfNeeded();
            dialog.dismiss();
        });

        playBtn.setOnClickListener(v -> {
            clearGroupHighlights();
            List<Card> group = new ArrayList<>();
            group.add(firstCard);
            group.addAll(others);
            boolean ok = game.playCardGroup(group);
            if (!ok) {
                Toast.makeText(this, "Couldn't play that combination — playing just one card", Toast.LENGTH_SHORT).show();
                game.playCard(firstCard, null);
                playCorrectSound();
            } else {
                playGroupSound();
            }
            refresh();
            runNextBotTurnIfNeeded();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void promptForSuit(Card aceCard) {
        cancelTurnTimer();
        float density = getResources().getDisplayMetrics().density;

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(24, 32, 24, 32);
        android.graphics.drawable.GradientDrawable panelBg =
                new android.graphics.drawable.GradientDrawable();
        panelBg.setColor(Color.parseColor("#330B3D20"));
        panelBg.setCornerRadius(32);
        panel.setBackground(panelBg);

        TextView title = new TextView(this);
        title.setText("Choose next suit");
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        panel.addView(title);

        android.widget.GridLayout grid = new android.widget.GridLayout(this);
        grid.setColumnCount(2);
        grid.setRowCount(2);

        String[] symbols = {"\u2665", "\u2666", "\u2663", "\u2660"};
        Card.Suit[] suits = {Card.Suit.HEARTS, Card.Suit.DIAMONDS, Card.Suit.CLUBS, Card.Suit.SPADES};

        AlertDialog[] dialogHolder = new AlertDialog[1];

        for (int i = 0; i < 4; i++) {
            final Card.Suit chosen = suits[i];
            Button b = new Button(this);
            b.setText(symbols[i]);
            b.setTextSize(56);
            boolean red = (chosen == Card.Suit.HEARTS || chosen == Card.Suit.DIAMONDS);
            b.setTextColor(red ? Color.parseColor("#FF5252") : Color.WHITE);
            android.graphics.drawable.GradientDrawable cellBg =
                    new android.graphics.drawable.GradientDrawable();
            cellBg.setColor(Color.parseColor("#33FFFFFF"));
            cellBg.setCornerRadius(24);
            cellBg.setStroke((int) (1 * density), Color.parseColor("#55FFD700"));
            b.setBackground(cellBg);

            android.widget.GridLayout.LayoutParams lp = new android.widget.GridLayout.LayoutParams();
            lp.width = (int) (110 * density);
            lp.height = (int) (110 * density);
            lp.setMargins(12, 12, 12, 12);
            lp.rowSpec = android.widget.GridLayout.spec(i / 2);
            lp.columnSpec = android.widget.GridLayout.spec(i % 2);
            b.setLayoutParams(lp);

            b.setOnClickListener(v -> {
                game.playCard(aceCard, chosen);
                playCorrectSound();
                refresh();
                runNextBotTurnIfNeeded();
                if (dialogHolder[0] != null) dialogHolder[0].dismiss();
            });

            grid.addView(b);
        }
        panel.addView(grid);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(panel)
                .setCancelable(false)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0.35f);
        }
        dialogHolder[0] = dialog;
        dialog.show();
    }
}
