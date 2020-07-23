package com.codepath.gameswap.models;

import android.os.Parcel;
import android.os.Parcelable;

public class BGGGame implements Parcelable {

    public static final String TAG = BGGGame.class.getSimpleName();

    private String id;
    private String title;
    private String imageUrl;
    private float difficulty;

    private String ageRating;

    public BGGGame(String id, String title, String imageUrl, float difficulty, String ageRating) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.difficulty = difficulty;
        this.ageRating = ageRating;
    }

    protected BGGGame(Parcel in) {
        id = in.readString();
        title = in.readString();
        imageUrl = in.readString();
        difficulty = in.readFloat();
        ageRating = in.readString();
    }

    public static final Creator<BGGGame> CREATOR = new Creator<BGGGame>() {
        @Override
        public BGGGame createFromParcel(Parcel in) {
            return new BGGGame(in);
        }

        @Override
        public BGGGame[] newArray(int size) {
            return new BGGGame[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(imageUrl);
        parcel.writeFloat(difficulty);
        parcel.writeString(ageRating);
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public float getDifficulty() { return difficulty; }

    public void setDifficulty(float difficulty) { this.difficulty = difficulty; }

    public String getAgeRating() { return ageRating; }

    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }
}
