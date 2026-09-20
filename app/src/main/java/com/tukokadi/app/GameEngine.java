package com.tukokadi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameEngine {

    public final List<Player> players = new ArrayList<>();
    public final List<Card> discardPile = new ArrayList<>();

    public int currentPlayerIndex = 0;
    public int direction = 1;

    public Card.Suit declaredSuit = null;
    public boolean gameOver = false;
    public String winnerName = null;
    public String winner = null;

    public int pendingPenalty = 0;
    public Card.Rank pendingPenaltyRank = null;

    public Card lastDrawnCard = null;

    public StringBuilder log = new StringBuilder();

    private Deck deck;

    public GameEngine(List<String> playerNames) {
        deck = new Deck();

        for (String name : playerNames) {
            players.add(new Player(name));
        }

        for (Player p : players) {
            p.hand.clear();

            for (int i = 0; i < 4; i++) {
                Card card = deck.draw();

                if (card != null) {
                    p.hand.add(card);
                }
            }
        }

        Card first = deck.draw();

        while (first != null &&
                (first.rank == Card.Rank.TWO ||
                 first.rank == Card.Rank.THREE ||
                 first.rank == Card.Rank.JACK ||
                 first.rank == Card.Rank.ACE)) {

            deck.recycle(first);
            first = deck.draw();
        }

        if (first == null) {
            first = new Card(Card.Suit.HEARTS, Card.Rank.FOUR);
        }

        discardPile.add(first);
    }

    public void start() {
        gameOver = false;
        winnerName = null;
        currentPlayerIndex = 0;
        direction = 1;

        declaredSuit = null;
        pendingPenalty = 0;
        pendingPenaltyRank = null;
        lastDrawnCard = null;

        log.setLength(0);

        log.append("Game started.\n");
        log.append(players.get(currentPlayerIndex).name)
                .append("'s turn.\n");
    }

    public Card topCard() {
        if (discardPile.isEmpty()) return null;
        return discardPile.get(discardPile.size() - 1);
    }

    public Player currentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public boolean canPlay(Card card) {
        if (card == null || gameOver) return false;

        Player p = currentPlayer();

        if (!p.hand.contains(card)) {
            return false;
        }

        if (pendingPenalty > 0) {
            return card.rank == pendingPenaltyRank ||
                    card.rank == Card.Rank.ACE;
        }

        if (card.rank == Card.Rank.ACE) {
            return true;
        }

        if (declaredSuit != null) {
            return card.suit == declaredSuit;
        }

        return card.suit == topCard().suit;
    }

    public boolean playCard(Card card) {
        return playCard(card, null);
    }

    public boolean playCard(Card card, Card.Suit declaredSuitIfAce) {

        if (gameOver) return false;

        Player p = currentPlayer();

        if (!canPlay(card)) {
            return false;
        }

        if (!p.hand.contains(card)) {
            return false;
        }

        Card previousTop = topCard();

        p.removeCard(card);
        discardPile.add(card);

        lastDrawnCard = null;

        log.append(p.name)
                .append(" played ")
                .append(card)
                .append("\n");

        checkWin(p, card.rank);

        if (gameOver) {
            return true;
        }

        switch (card.rank) {

            case ACE:

                if (pendingPenalty > 0) {
                    Card.Suit autoSuit = previousTop != null
                            ? previousTop.suit
                            : card.suit;

                    pendingPenalty = 0;
                    pendingPenaltyRank = null;

                    this.declaredSuit = autoSuit;

                    log.append(p.name)
                            .append(" cancels the pending penalty with an ACE.\n");
                } else {
                    this.declaredSuit = declaredSuitIfAce != null
                            ? declaredSuitIfAce
                            : card.suit;

                    log.append("Suit changed to ")
                            .append(this.declaredSuit)
                            .append(".\n");
                }

                advanceTurn();
                break;

            case KING:

                declaredSuit = null;
                direction *= -1;

                log.append("Direction reversed.\n");

                advanceTurn();
                break;

            case QUEEN:

                declaredSuit = null;

                log.append(p.name)
                        .append(" gets an extra turn.\n");

                break;

            case JACK:

                declaredSuit = null;
                resolveJack();

                break;

            case TWO:

                declaredSuit = null;
                stackPenalty(Card.Rank.TWO, 2);
                advanceTurn();
                break;

            case THREE:

                declaredSuit = null;
                stackPenalty(Card.Rank.THREE, 3);
                advanceTurn();
                break;

            case EIGHT:

                declaredSuit = null;

                log.append(p.name)
                        .append(" gets an extra turn.\n");

                break;

            default:

                declaredSuit = null;
                advanceTurn();
                break;
        }

        return true;
    }

    public boolean playCardGroup(List<Card> cards) {

        if (gameOver) return false;

        if (cards == null || cards.isEmpty()) {
            return false;
        }

        Player p = currentPlayer();

        for (Card card : cards) {

            if (card == null ||
                    !p.hand.contains(card)) {
                return false;
            }
        }

        Card.Rank rank = cards.get(0).rank;

        if (rank == Card.Rank.ACE ||
                rank == Card.Rank.JACK ||
                rank == Card.Rank.KING) {

            return false;
        }

        for (Card card : cards) {

            if (card.rank != rank) {
                return false;
            }
        }

        Card top = topCard();

        Card.Suit simDeclaredSuit = declaredSuit;
        int simPending = pendingPenalty;
        Card.Rank simPendingRank = pendingPenaltyRank;

        Card simulatedTop = top;

        for (Card card : cards) {

            if (!canSimPlay(
                    card,
                    simulatedTop,
                    simDeclaredSuit,
                    simPending,
                    simPendingRank)) {

                return false;
            }

            simulatedTop = card;

            if (card.rank == Card.Rank.TWO) {

                if (simPending == 0 ||
                        simPendingRank == Card.Rank.TWO) {

                    simPending += 2;
                    simPendingRank = Card.Rank.TWO;

                } else {
                    return false;
                }

            } else if (card.rank == Card.Rank.THREE) {

                if (simPending == 0 ||
                        simPendingRank == Card.Rank.THREE) {

                    simPending += 3;
                    simPendingRank = Card.Rank.THREE;

                } else {
                    return false;
                }

            } else {
                simPending = 0;
                simPendingRank = null;
            }

            simDeclaredSuit = null;
        }

        for (Card card : cards) {
            p.removeCard(card);
            discardPile.add(card);

            log.append(p.name)
                    .append(" played ")
                    .append(card)
                    .append("\n");
        }

        checkWin(p, rank);

        if (gameOver) {
            return true;
        }

        if (rank == Card.Rank.TWO) {
            stackPenalty(Card.Rank.TWO, 2 * cards.size());
        } else if (rank == Card.Rank.THREE) {
            stackPenalty(Card.Rank.THREE, 3 * cards.size());
        } else {
            pendingPenalty = 0;
            pendingPenaltyRank = null;
        }

        if (rank == Card.Rank.EIGHT ||
                rank == Card.Rank.QUEEN) {

            log.append(p.name)
                    .append(" gets an extra turn.\n");

        } else {
            advanceTurn();
        }

        return true;
    }

    private boolean canSimPlay(
            Card card,
            Card top,
            Card.Suit simDeclaredSuit,
            int simPending,
            Card.Rank simPendingRank) {

        if (simPending > 0) {

            return card.rank == simPendingRank ||
                    card.rank == Card.Rank.ACE;
        }

        if (card.rank == Card.Rank.ACE) {
            return true;
        }

        if (simDeclaredSuit != null) {
            return card.suit == simDeclaredSuit;
        }

        return card.suit == top.suit;
    }

    private boolean isWinningRank(Card.Rank r) {
        return r == Card.Rank.FOUR || r == Card.Rank.FIVE || r == Card.Rank.SIX
                || r == Card.Rank.SEVEN || r == Card.Rank.NINE || r == Card.Rank.TEN;
    }

    private void checkWin(Player p, Card.Rank lastPlayedRank) {

        if (p.hand.isEmpty()) {

            if (isWinningRank(lastPlayedRank)) {

                log.append(p.name)
                        .append(" WINS!\n");

                gameOver = true;
                winnerName = p.name;
                winner = winnerName;

            } else {

                log.append(p.name)
                        .append(" emptied their hand on a ")
                        .append(lastPlayedRank)
                        .append(" - not a finishing card. They stay cardless until their turn comes back around.\n");
            }
        }
    }

    public void advanceTurn() {

        if (gameOver) return;

        currentPlayerIndex =
                (currentPlayerIndex + direction + players.size())
                        % players.size();

        log.append(currentPlayer().name)
                .append("'s turn.\n");
    }

    public Card drawCard() {
        if (gameOver) return null;

        Player p = currentPlayer();

        if (pendingPenalty > 0) {
            int amount = pendingPenalty;
            Card lastCard = null;
            int actuallyDrawn = 0;
            for (int i = 0; i < amount; i++) {
                Card c = drawOneCard();
                if (c == null) break;
                p.hand.add(c);
                lastCard = c;
                actuallyDrawn++;
            }
            lastDrawnCard = lastCard;
            log.append(p.name)
                    .append(" draws ")
                    .append(actuallyDrawn)
                    .append(" cards as a penalty.\n");
            pendingPenalty = 0;
            pendingPenaltyRank = null;
            advanceTurn();
            return lastDrawnCard;
        }

        Card card = drawOneCard();

        if (card == null) {
            log.append("No cards available.\n");
            return null;
        }

        p.hand.add(card);

        lastDrawnCard = card;

        log.append(p.name)
                .append(" drew ")
                .append(card)
                .append(".\n");

        advanceTurn();

        return card;
    }

    private Card drawOneCard() {

        Card card = deck.draw();

        if (card != null) {
            return card;
        }

        if (discardPile.size() <= 1) {
            return null;
        }

        Card top = discardPile.remove(discardPile.size() - 1);

        List<Card> recycleCards =
                new ArrayList<>(discardPile);

        discardPile.clear();
        discardPile.add(top);

        Collections.shuffle(recycleCards);

        for (Card c : recycleCards) {
            deck.add(c);
        }

        log.append("Deck recycled from discard pile.\n");

        return deck.draw();
    }

    public void setNextSuit(Card.Suit suit) {

        if (suit == null) return;

        declaredSuit = suit;

        log.append("Suit changed to ")
                .append(suit)
                .append(".\n");

        advanceTurn();
    }

    private void stackPenalty(Card.Rank rank, int amount) {

        if (pendingPenalty > 0 &&
                pendingPenaltyRank == rank) {

            pendingPenalty += amount;

        } else {

            pendingPenalty = amount;
            pendingPenaltyRank = rank;
        }

        log.append("Pending penalty is now ")
                .append(pendingPenalty)
                .append(" (next player must defend with ")
                .append(rank)
                .append(", cancel with ACE, or draw).\n");
    }

    private void resolveJack() {

        advanceTurn();

        while (!gameOver) {

            Player candidate = currentPlayer();

            Card escapeJack =
                    findCard(candidate, Card.Rank.JACK);

            if (escapeJack != null) {

                candidate.removeCard(escapeJack);
                discardPile.add(escapeJack);

                log.append(candidate.name)
                        .append(" escapes the jump by playing ")
                        .append(escapeJack)
                        .append("\n");

                checkWin(candidate, Card.Rank.JACK);

                if (gameOver) return;

                advanceTurn();

            } else {

                log.append(candidate.name)
                        .append(" is jumped.\n");

                advanceTurn();
                return;
            }
        }
    }

    private Card findCard(Player player, Card.Rank rank) {

        for (Card card : player.hand) {

            if (card.rank == rank) {
                return card;
            }
        }

        return null;
    }

    public String getLog() {
        return log.toString();
    }
}
