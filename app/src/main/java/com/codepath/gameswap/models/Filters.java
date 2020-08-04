package com.codepath.gameswap.models;

import org.jetbrains.annotations.NotNull;

public class Filters {

    private boolean games;
    private boolean puzzles;
    private int lowerConditionLimit;
    private int upperConditionLimit;
    private int lowerDifficultyLimit;
    private int upperDifficultyLimit;
    private int lowerAgeRatingLimit;
    private int upperAgeRatingLimit;

    public Filters() {
        games = false;
        puzzles = false;
        lowerConditionLimit = 0;
        upperConditionLimit = 50;
        lowerConditionLimit = 0;
        lowerDifficultyLimit = 50;
        lowerConditionLimit = 0;
        upperDifficultyLimit = 50;
        lowerAgeRatingLimit = 2;
        upperAgeRatingLimit = 21;
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

    public int getLowerConditionLimit() {
        return lowerConditionLimit;
    }

    public Filters setLowerConditionLimit(int lowerConditionLimit) {
        this.lowerConditionLimit = lowerConditionLimit;
        return this;
    }

    public int getUpperConditionLimit() {
        return upperConditionLimit;
    }

    public Filters setUpperConditionLimit(int upperConditionLimit) {
        this.upperConditionLimit = upperConditionLimit;
        return this;
    }

    public int getLowerDifficultyLimit() {
        return lowerDifficultyLimit;
    }

    public Filters setLowerDifficultyLimit(int lowerDifficultyLimit) {
        this.lowerDifficultyLimit = lowerDifficultyLimit;
        return this;
    }

    public int getUpperDifficultyLimit() {
        return upperDifficultyLimit;
    }

    public Filters setUpperDifficultyLimit(int upperDifficultyLimit) {
        this.upperDifficultyLimit = upperDifficultyLimit;
        return this;
    }

    public int getLowerAgeRatingLimit() {
        return lowerAgeRatingLimit;
    }

    public Filters setLowerAgeRatingLimit(int lowerAgeRatingLimit) {
        this.lowerAgeRatingLimit = lowerAgeRatingLimit;
        return this;
    }

    public int getUpperAgeRatingLimit() {
        return upperAgeRatingLimit;
    }

    public Filters setUpperAgeRatingLimit(int upperAgeRatingLimit) {
        this.upperAgeRatingLimit = upperAgeRatingLimit;
        return this;
    }


    @NotNull
    @Override
    public String toString() {
        return "Filters{" +
                "games=" + games +
                ", puzzles=" + puzzles +
                ", lowerConditionLimit=" + lowerConditionLimit +
                ", upperConditionLimit=" + upperConditionLimit +
                ", lowerDifficultyLimit=" + lowerDifficultyLimit +
                ", upperDifficultyLimit=" + upperDifficultyLimit +
                ", lowerAgeRatingLimit=" + lowerAgeRatingLimit +
                ", upperAgeRatingLimit=" + upperAgeRatingLimit +
                '}';
    }

    public boolean areDefault() {
        return games && puzzles
                && lowerConditionLimit == 0 && upperConditionLimit == 50
                && lowerDifficultyLimit == 0 && upperDifficultyLimit == 50
                && lowerAgeRatingLimit == 2 && upperAgeRatingLimit == 21;
    }
}
