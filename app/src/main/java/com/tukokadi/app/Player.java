package com.tukokadi.app;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public final String name;
    public List<Card> hand = new ArrayList<>();

    private static final Card.Rank[] WINNING_NUMBERS = {
        Card.Rank.FOUR, Card.Rank.FIVE, Card.Rank.SIX,
        Card.Rank.SEVEN, Card.Rank.NINE, Card.Rank.TEN
    };

    public Player(String name) {
        this.name = name;
    }

    public boolean hasCard(Card.Rank rank) {
        for (Card c : hand) {
            if (c.rank == rank) return true;
        }
        return false;
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    private boolean isWinningNumber(Card.Rank rank) {
        for (Card.Rank r : WINNING_NUMBERS) {
            if (r == rank) return true;
        }
        return false;
    }

    // A rank that is allowed to be the very last card played to empty a hand.
    // ACE, TWO, THREE, JACK and a lone KING can never finish a hand.
    private boolean isValidFinisher(Card.Rank rank) {
        return isWinningNumber(rank);
    }

    // Call after any normal single-card play (not a King-pair combo).
    // Wins ONLY if the hand is truly empty AND the last card played qualifies.
    public boolean hasWon(Card.Rank lastPlayedRank) {
        return hand.isEmpty() && isValidFinisher(lastPlayedRank);
    }

    // Call right after the second King of a King-pair combo is played
    // (and after any suited follow-up card, if one was played).
    // Wins ONLY if the hand is truly empty.
    public boolean hasWonWithKingPair() {
        return hand.isEmpty();
    }
}
