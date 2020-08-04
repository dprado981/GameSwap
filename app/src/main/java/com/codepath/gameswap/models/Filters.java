package com.codepath.gameswap.models;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

public class Filters {

    private boolean games;
    private boolean puzzles;
    private int lowerLimit;
    private int upperLimit;

    public Filters() {
        games = false;
        puzzles = false;
        lowerLimit = 0;
        upperLimit = 50;
    }

    public boolean getGames() {
        return games;
    }

    public Filters setGames(boolean games) {
        this.games = games;
        return this;
    }

    public boolean getPuzzles() {
        return puzzles;
    }

    public Filters setPuzzles(boolean puzzles) {
        this.puzzles = puzzles;
        return this;
    }

    public int getLowerLimit() {
        return lowerLimit;
    }

    public Filters setLowerLimit(int lowerLimit) {
        this.lowerLimit = lowerLimit;
        return this;
    }

    public int getUpperLimit() {
        return upperLimit;
    }

    public Filters setUpperLimit(int upperLimit) {
        this.upperLimit = upperLimit;
        return this;
    }

    @NotNull
    @Override
    public String toString() {
        return "Filters{" +
                "games=" + games +
                ", puzzles=" + puzzles +
                ", lowerLimit=" + lowerLimit +
                ", upperLimit=" + upperLimit +
                '}';
    }

    public boolean areDefault() {
        return games && puzzles && lowerLimit == 0 && upperLimit == 50;
    }
}
