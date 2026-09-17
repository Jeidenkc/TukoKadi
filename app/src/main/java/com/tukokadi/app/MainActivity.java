package com.tukokadi.app;

import android.app.Activity;
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
    private TextView logText;
    private TextView timerText;
    private LinearLayout bobContainer;
    private LinearLayout carolContainer;
    private LinearLayout handLayout;
    private LinearLayout rootLayout;
    private Button newGameButton;
    private CountDownTimer turnTimer;
    private ToneGenerator toneGen;
    private Handler soundHandler = new Handler(Looper.getMainLooper());
    private Vibrator vibrator;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean winAnnounced = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        rootLayout.setBackgroundColor(CREAM);

        TextView title = new TextView(this);
        title.setText("TUKO KADI");
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(GOLD);
        title.setBackgroundColor(GREEN_DARK);
        title.setGravity(Gravity.CENTER);
        title.setPadding(24, 36, 24, 36);
        rootLayout.addView(title);

        LinearLayout tableLayout = new LinearLayout(this);
        tableLayout.setOrientation(LinearLayout.HORIZONTAL);
        tableLayout.setGravity(Gravity.CENTER);
        tableLayout.setPadding(0, 20, 0, 8);

        bobContainer = new LinearLayout(this);
        bobContainer.setOrientation(LinearLayout.VERTICAL);
        bobContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bobParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        bobContainer.setLayoutParams(bobParams);
        tableLayout.addView(bobContainer);

        Button drawPileView = new Button(this);
        drawPileView.setText("DRAW\nPILE");
        drawPileView.setTextSize(13);
        drawPileView.setTypeface(null, Typeface.BOLD);
        drawPileView.setTextColor(Color.WHITE);
        GradientDrawable pileBg = new GradientDrawable();
        pileBg.setColor(GREEN_DARK);
        pileBg.setCornerRadius(16);
        pileBg.setStroke(3, GOLD);
        drawPileView.setBackground(pileBg);
        LinearLayout.LayoutParams pileParams = new LinearLayout.LayoutParams(180, 140);
        pileParams.setMargins(16, 0, 16, 0);
        drawPileView.setLayoutParams(pileParams);
        drawPileView.setOnClickListener(v -> {
            if (game.currentPlayerIndex == 0 && !game.gameOver) {
                playDrawSound();
                game.drawCard();
                refresh();
                maybeRunBotTurns();
            }
        });
        tableLayout.addView(drawPileView);

        carolContainer = new LinearLayout(this);
        carolContainer.setOrientation(LinearLayout.VERTICAL);
        carolContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams carolParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        carolContainer.setLayoutParams(carolParams);
        tableLayout.addView(carolContainer);

        rootLayout.addView(tableLayout);

        topCardText = new TextView(this);
        topCardText.setTextSize(20);
        topCardText.setTypeface(null, Typeface.BOLD);
        topCardText.setTextColor(GREEN_DARK);
        topCardText.setPadding(0, 24, 0, 4);
        rootLayout.addView(topCardText);

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

        TextView handLabel = new TextView(this);
        handLabel.setText("Your hand:");
        handLabel.setTextSize(16);
        handLabel.setTypeface(null, Typeface.BOLD);
        handLabel.setTextColor(GREEN_DARK);
        rootLayout.addView(handLabel);

        handLayout = new LinearLayout(this);
        handLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.addView(handLayout);

        newGameButton = new Button(this);
        newGameButton.setText("New Game");
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
        newGameButton.setOnClickListener(v -> startNewGame());
        rootLayout.addView(newGameButton);

        TextView logLabel = new TextView(this);
        logLabel.setText("Recent moves:");
        logLabel.setTextSize(16);
        logLabel.setTypeface(null, Typeface.BOLD);
        logLabel.setTextColor(GREEN_DARK);
        logLabel.setPadding(0, 24, 0, 8);
        rootLayout.addView(logLabel);

        logText = new TextView(this);
        logText.setTextSize(13);
        logText.setTextColor(Color.DKGRAY);
        rootLayout.addView(logText);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(rootLayout);
        setContentView(scroll);

        startNewGame();
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

    private void playWrongSound(View sourceView) {
        if (toneGen != null) toneGen.startTone(ToneGenerator.TONE_SUP_ERROR, 400);
        vibrate(350);
        shakeView(sourceView);
    }

    private void playCorrectSound() {
        if (toneGen == null) return;
        toneGen.startTone(ToneGenerator.TONE_DTMF_1, 80);
        soundHandler.postDelayed(() -> { if (toneGen != null) toneGen.startTone(ToneGenerator.TONE_DTMF_3, 80); }, 90);
        soundHandler.postDelayed(() -> { if (toneGen != null) toneGen.startTone(ToneGenerator.TONE_DTMF_5, 120); }, 180);
    }

    private void playDrawSound() {
        if (toneGen != null) toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 60);
    }

    private void playGroupSound() {
        if (toneGen == null) return;
        toneGen.startTone(ToneGenerator.TONE_DTMF_5, 120);
        soundHandler.postDelayed(() -> {
            if (toneGen != null) toneGen.startTone(ToneGenerator.TONE_DTMF_8, 150);
        }, 140);
    }

    private void playTickSound(long secondsLeft) {
        if (toneGen == null) return;
        int tone;
        if (secondsLeft == 4) tone = ToneGenerator.TONE_DTMF_4;
        else if (secondsLeft == 3) tone = ToneGenerator.TONE_DTMF_3;
        else if (secondsLeft == 2) tone = ToneGenerator.TONE_DTMF_2;
        else tone = ToneGenerator.TONE_DTMF_1;
        toneGen.startTone(tone, 150);
        vibrate(60);
    }

    private void playTimeUpSound() {
        if (toneGen != null) toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 400);
        vibrate(250);
    }

    private void playWinFanfare() {
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
    }

    private void startNewGame() {
        game = new GameEngine(Arrays.asList("You", "Bob", "Carol"));
        winAnnounced = false;
        refresh();
        maybeRunBotTurns();
    }

    private void renderPlayerStack(LinearLayout container, String name, int count) {
        container.removeAllViews();

        int cardW = 70;
        int cardH = 100;
        int shown = Math.max(1, Math.min(count, 3));
        int overlap = 16;

        FrameLayout stack = new FrameLayout(this);
        LinearLayout.LayoutParams stackParams = new LinearLayout.LayoutParams(
                cardW + (shown - 1) * overlap, cardH);
        stack.setLayoutParams(stackParams);

        if (count > 0) {
            for (int i = 0; i < shown; i++) {
                CardView back = new CardView(this);
                back.setFaceDown(true);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(cardW, cardH);
                lp.leftMargin = i * overlap;
                back.setLayoutParams(lp);
                stack.addView(back);
            }
        }
        container.addView(stack);

        TextView label = new TextView(this);
        label.setText(name + "\n" + count + " cards");
        label.setTextSize(13);
        label.setTypeface(null, Typeface.BOLD);
        label.setTextColor(GREEN_DARK);
        label.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = 6;
        label.setLayoutParams(labelParams);
        container.addView(label);
    }

    private void refresh() {
        topCardText.setText("Top card: " + displayName(game.topCard()));
        renderPlayerStack(bobContainer, "Bob", game.players.get(1).hand.size());
        renderPlayerStack(carolContainer, "Carol", game.players.get(2).hand.size());

        if (game.gameOver) {
            statusText.setText((game.winnerName.equals("You") ? "You win the game!" : game.winnerName + " wins the game!") + " Tap New Game to play again.");
            if (!winAnnounced) {
                winAnnounced = true;
                announceWinner(game.winnerName);
            }
        } else {
            statusText.setText("Turn: " + game.currentPlayer().name);
        }

        handLayout.removeAllViews();
        Player you = game.players.get(0);
        List<Card> sortedHand = new ArrayList<>(you.hand);
        sortedHand.sort(Comparator
                .comparing((Card c) -> c.suit.ordinal())
                .thenComparing(c -> c.rank.ordinal()));

        for (Card card : sortedHand) {
            Button cardButton = new Button(this);
            cardButton.setText(displayName(card));
            cardButton.setTextSize(18);
            cardButton.setTypeface(null, Typeface.BOLD);
            cardButton.setTextColor(isRed(card) ? Color.RED : Color.BLACK);

            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setColor(Color.WHITE);
            cardBg.setCornerRadius(20);
            cardBg.setStroke(3, GREEN_DARK);
            cardButton.setBackground(cardBg);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 6, 0, 6);
            cardButton.setLayoutParams(cardParams);

            cardButton.setOnClickListener(v -> onCardTapped(card, v));
            handLayout.addView(cardButton);
        }

        String fullLog = game.log.toString();
        String[] lines = fullLog.split("\n");
        int start = Math.max(0, lines.length - 8);
        StringBuilder recent = new StringBuilder();
        for (int i = start; i < lines.length; i++) {
            recent.append(lines[i]).append("\n");
        }
        logText.setText(recent.toString());

        cancelTurnTimer();
        if (!game.gameOver && game.currentPlayerIndex == 0) {
            startTurnTimer();
        } else {
            timerText.setText("");
        }
    }

    private void startTurnTimer() {
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
                    maybeRunBotTurns();
                }
            }
        }.start();
    }

    private void cancelTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
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
            default: return String.valueOf(r.ordinal());
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

        if (card.rank == Card.Rank.ACE || card.rank == Card.Rank.JACK || card.rank == Card.Rank.KING) {
            game.playCard(card, null);
            playCorrectSound();
            refresh();
            maybeRunBotTurns();
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
            maybeRunBotTurns();
        } else {
            promptForGroupPlay(card, sameRankOthers);
        }
    }

    private void promptForGroupPlay(Card firstCard, List<Card> others) {
        cancelTurnTimer();
        String[] labels = new String[others.size()];
        boolean[] checked = new boolean[others.size()];
        for (int i = 0; i < others.size(); i++) {
            labels[i] = displayName(others.get(i));
        }

        new AlertDialog.Builder(this)
                .setTitle("You have more " + rankLabel(firstCard.rank) + "s — play together?")
                .setMultiChoiceItems(labels, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("Play Selected", (dialog, which) -> {
                    List<Card> group = new ArrayList<>();
                    group.add(firstCard);
                    for (int i = 0; i < others.size(); i++) {
                        if (checked[i]) group.add(others.get(i));
                    }
                    boolean ok = group.size() > 1 && game.playCardGroup(group);
                    if (!ok) {
                        Toast.makeText(this, "Couldn't play that combination — playing just one card", Toast.LENGTH_SHORT).show();
                        game.playCard(firstCard, null);
                        playCorrectSound();
                    } else {
                        playGroupSound();
                    }
                    refresh();
                    maybeRunBotTurns();
                })
                .setNegativeButton("Just This One", (dialog, which) -> {
                    game.playCard(firstCard, null);
                    playCorrectSound();
                    refresh();
                    maybeRunBotTurns();
                })
                .setCancelable(false)
                .show();
    }

    private void promptForSuit(Card aceCard) {
        cancelTurnTimer();
        String[] suitNames = {"HEARTS", "DIAMONDS", "CLUBS", "SPADES"};
        new AlertDialog.Builder(this)
                .setTitle("Choose next suit")
                .setItems(suitNames, (dialog, which) -> {
                    Card.Suit chosen = Card.Suit.values()[which];
                    game.playCard(aceCard, chosen);
                    playCorrectSound();
                    refresh();
                    maybeRunBotTurns();
                })
                .setCancelable(false)
                .show();
    }

    private void maybeRunBotTurns() {
        while (!game.gameOver && game.currentPlayerIndex != 0) {
            Player bot = game.currentPlayer();
            Card playable = null;
            for (Card c : bot.hand) {
                if (game.canPlay(c)) {
                    playable = c;
                    break;
                }
            }
            if (playable != null) {
                if (playable.rank == Card.Rank.ACE || playable.rank == Card.Rank.JACK || playable.rank == Card.Rank.KING) {
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
        refresh();
    }
}
