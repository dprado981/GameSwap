package com.codepath.gameswap.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.Locale;

public class Puzzle extends Thing implements Parcelable {

    public static final String TAG = Puzzle.class.getSimpleName();
    public static final int TYPE = 2;

    private int numPieces;
    private float width;
    private float height;

    public Puzzle(String id, String title, String imageUrl, float difficulty, String ageRating,
                  int numPieces, float width, float height) {
        super(id, title, imageUrl, difficulty, ageRating);
        this.numPieces = numPieces;
        this.width = width;
        this.height = height;
    }

    protected Puzzle(Parcel in) {
        super(in);
        numPieces = in.readInt();
        width = in.readFloat();
        height = in.readFloat();
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(numPieces);
        parcel.writeFloat(width);
        parcel.writeFloat(height);
    }

    public int getNumPieces() { return numPieces; }

    public void setNumPieces(int numPieces) { this.numPieces = numPieces; }

    public String getDimensions() {
        return String.format(Locale.getDefault(),"%s x %s in",
                new BigDecimal(width).stripTrailingZeros(), new BigDecimal(height).stripTrailingZeros());
    }

    public void setDimensions(float width, float height, String units) {
        this.width = width;
        this.height = height;
    }

}
