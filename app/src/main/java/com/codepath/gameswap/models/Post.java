package com.codepath.gameswap.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String TAG = Post.class.getSimpleName();
    public static final String KEY_TITLE = "title";
    public static final String KEY_CONDITION = "condition";
    public static final String KEY_NOTES = "notes";
    public static final String KEY_USER = "user";
    public static final String KEY_COORDINATES = "coordinates";
    public static final String KEY_DIFFICULTY = "difficulty";
    public static final String KEY_AGE_RATING = "ageRating";
    public static final String KEY_IMAGE_BASE = "image";
    public static final String KEY_IMAGE_ONE = "image1";
    public static final String KEY_IMAGE_TWO = "image2";
    public static final String KEY_IMAGE_THREE = "image3";
    public static final String KEY_IMAGE_FOUR = "image4";

    public String getTitle() { return getString(KEY_TITLE); }

    public void setTitle(String title) { put(KEY_TITLE, title); }

    public int getCondition() { return getInt(KEY_CONDITION); }

    public void setCondition(int condition) { put(KEY_CONDITION, condition); }

    public String getNotes() { return getString(KEY_NOTES); }

    public void setNotes(String notes) { put(KEY_NOTES, notes); }

    public ParseUser getUser() { return getParseUser(KEY_USER); }

    public void setUser(ParseUser parseUser) { put(KEY_USER, parseUser); }

    public ParseGeoPoint getCoordinates() { return getParseGeoPoint(KEY_COORDINATES); }

    public void setCoordinates(ParseGeoPoint parseGeoPoint) { put(KEY_COORDINATES, parseGeoPoint); }

    public int getDifficulty() { return getInt(KEY_DIFFICULTY); }

    public void setDifficulty(int difficulty) { put(KEY_DIFFICULTY, difficulty); }

    public String getAgeRating() { return getString(KEY_AGE_RATING); }

    public void setAgeRating(String ageRating) { put(KEY_AGE_RATING, ageRating); }

    public ParseFile getImageOne() { return getParseFile(KEY_IMAGE_ONE); }

    public void setImageOne(ParseFile parseFile) { put(KEY_IMAGE_ONE, parseFile); }

    public ParseFile getImageTwo() { return getParseFile(KEY_IMAGE_TWO); }

    public void setImageTwo(ParseFile parseFile) { put(KEY_IMAGE_TWO, parseFile); }

    public ParseFile getImageThree() { return getParseFile(KEY_IMAGE_THREE); }

    public void setImageThree(ParseFile parseFile) { put(KEY_IMAGE_THREE, parseFile); }

    public ParseFile getImageFour() { return getParseFile(KEY_IMAGE_FOUR); }

    public void setImageFour(ParseFile parseFile) { put(KEY_IMAGE_FOUR, parseFile); }

    public List<ParseFile> getImages() {
        List<ParseFile> parseFiles = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ParseFile file = getParseFile(KEY_IMAGE_BASE + (i+1));
            if (file != null) {
                parseFiles.add(file);
            }
        }
        return parseFiles;
    }

    public void setImages(List<ParseFile> parseFiles) {
        int limit = Math.min(parseFiles.size(), 4);
        for (int i = 0; i < limit; i++) {
            ParseFile file = parseFiles.get(i);
            put(KEY_IMAGE_BASE + (i+1), file);
        }
    }
}
