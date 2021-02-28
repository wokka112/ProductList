package com.floatingpanda.productlist;

import androidx.annotation.Nullable;

// Models the British monetary system of pounds and pence.
public class Price implements Comparable<Price> {
    private int pounds;
    private int pence;

    public Price(int pounds, int pence) {
        this.pounds = pounds;
        this.pence = pence;
    }

    public int getPounds() { return pounds; }
    public void setPounds(int pounds) { this.pounds = pounds; }
    public int getPence() { return pence; }
    public void setPence(int pence) { this.pence = pence; }

    @Override
    public int compareTo(Price other) {
        if (other == null) {
            return 0;
        }

        if (this.pounds > other.pounds) {
            return 1;
        } else if (this.pounds < other.pounds) {
            return -1;
        } else {
            if (this.pence > other.pence) {
                return 1;
            } else if (this.pence < other.pence) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Price other = (Price) obj;

        return this.pounds == other.pounds
                && this.pence == other.pence;
    }

    @Override
    public String toString() {
        String returnString = pounds + ".";

        if(pence < 10) {
            returnString += "0";
        }

        returnString += pence;

        return returnString;
    }
}
