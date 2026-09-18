package com.tukokadi.app;

import java.util.Objects;

public class Card {

    public enum Suit {
        HEARTS, DIAMONDS, CLUBS, SPADES
    }

    public enum Rank {
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        TEN(10),
        JACK(11),
        QUEEN(12),
        KING(13),
        ACE(14);

        public final int value;

        Rank(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    public final Suit suit;
    public final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public boolean matches(Card other) {
        if (other == null) return false;
        return this.suit == other.suit || this.rank == other.rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;

        Card other = (Card) o;

        return this.suit == other.suit &&
               this.rank == other.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
