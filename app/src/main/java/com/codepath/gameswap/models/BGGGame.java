package com.codepath.gameswap.models;

import android.os.Parcel;
import android.os.Parcelable;

public class BGGGame extends Thing implements Parcelable {

    public static final String TAG = BGGGame.class.getSimpleName();
    public static final int TYPE = 1;
    private int minPlayers;
    private int maxPlayers;
    private int minPlaytime;
    private int maxPlaytime;

    public BGGGame(String id, String title, String imageUrl, float difficulty, String ageRating,
                  int minPlayers, int maxPlayers, int minPlaytime, int maxPlaytime) {
        super(id, title, imageUrl, difficulty, ageRating);
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.minPlaytime = minPlaytime;
        this.maxPlaytime = maxPlaytime;
    }

    protected BGGGame(Parcel in) {
        super(in);
        minPlayers = in.readInt();
        maxPlayers = in.readInt();
        minPlaytime = in.readInt();
        maxPlayers = in.readInt();
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(minPlayers);
        parcel.writeInt(maxPlayers);
        parcel.writeInt(minPlaytime);
        parcel.writeInt(maxPlaytime);
    }

    public int getMinPlayers() { return minPlayers; }

    public int getMaxPlayers() { return maxPlayers; }

    public int getMinPlaytime() { return minPlaytime; }

    public int getMaxPlaytime() { return maxPlaytime; }

}
