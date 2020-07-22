package com.codepath.gameswap.models;

import android.os.Parcel;
import android.os.Parcelable;

public class BGGGame implements Parcelable {

    public static final String TAG = BGGGame.class.getSimpleName();

    private String id;
    private String title;
    private String imageUrl;

    public BGGGame(String id, String title, String imageUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    protected BGGGame(Parcel in) {
        id = in.readString();
        title = in.readString();
        imageUrl = in.readString();
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

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(imageUrl);
    }
}
