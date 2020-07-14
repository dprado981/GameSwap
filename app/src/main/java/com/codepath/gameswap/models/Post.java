package com.codepath.gameswap.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_TITLE = "title";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_CONDITION = "condition";
    public static final String KEY_NOTES = "notes";
    public static final String KEY_USER = "user";
    public static final String KEY_COORDINATES = "coordinates";

    public String getTitle() { return getString(KEY_TITLE); }

    public void setTitle(String title) { put(KEY_TITLE, title); }

    public ParseFile getImage() { return getParseFile(KEY_IMAGE); }

    public void setImage(ParseFile parseFile) { put(KEY_IMAGE, parseFile); }

    public int getCondition() { return getInt(KEY_CONDITION); }

    public void setCondition(int condition) { put(KEY_CONDITION, condition); }

    public String getNotes() { return getString(KEY_NOTES); }

    public void setNotes(String notes) { put(KEY_NOTES, notes); }

    public ParseUser getUser() { return getParseUser(KEY_USER); }

    public void setUser(ParseUser parseUser) { put(KEY_USER, parseUser); }

    public ParseGeoPoint getCoordinates() { return getParseGeoPoint(KEY_COORDINATES); }

    public void setCoordinates(ParseGeoPoint parseGeoPoint) { put(KEY_COORDINATES, parseGeoPoint); }
}
