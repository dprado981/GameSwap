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
    public static final String GAME = "game";
    public static final String PUZZLE = "puzzle";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CONDITION = "condition";
    public static final String KEY_NOTES = "notes";
    public static final String KEY_USER = "user";
    public static final String KEY_COORDINATES = "coordinates";
    public static final String KEY_DIFFICULTY = "difficulty";
    public static final String KEY_AGE_RATING = "ageRating";
    public static final String KEY_MIN_PLAYERS = "minPlayers";
    public static final String KEY_MAX_PLAYERS = "maxPlayers";
    public static final String KEY_MIN_PLAYTIME = "minPlaytime";
    public static final String KEY_MAX_PLAYTIME = "maxPlaytime";
    public static final String KEY_PIECES = "pieces";
    public static final String KEY_WIDTH = "width";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_IMAGE_BASE = "image";
    public static final String KEY_IMAGE_ONE = "image1";
    public static final String KEY_IMAGE_TWO = "image2";
    public static final String KEY_IMAGE_THREE = "image3";
    public static final String KEY_IMAGE_FOUR = "image4";
    public static final String KEY_TYPE = "type";
    public static final String KEY_REPORTED_BY = "reportedBy";
    public static final String KEY_TO_BE_DELETED = "toBeDeleted";
    public static final String KEY_FAVORITED_BY = "favoritedBy";

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

    public int getAgeRating() { return getInt(KEY_AGE_RATING); }

    public void setAgeRating(int ageRating) { put(KEY_AGE_RATING, ageRating); }

    public void setMinPlayers(int minPlayers) { put(KEY_MIN_PLAYERS, minPlayers); }

    public int getMinPlayers() { return getInt(KEY_MIN_PLAYERS); }

    public void setMaxPlayers(int maxPlayers) { put(KEY_MAX_PLAYERS, maxPlayers); }

    public int getMaxPlayers() { return getInt(KEY_MAX_PLAYERS); }

    public void setMinPlaytime(int minPlaytime) { put(KEY_MIN_PLAYTIME, minPlaytime); }

    public int getMinPlaytime() { return getInt(KEY_MIN_PLAYTIME); }

    public void setMaxPlaytime(int maxPlaytime) { put(KEY_MAX_PLAYTIME, maxPlaytime); }

    public int getMaxPlaytime() { return getInt(KEY_MAX_PLAYTIME); }

    public void setPieces(int pieces) { put(KEY_PIECES, pieces); }

    public int getPieces() { return getInt(KEY_PIECES); }

    public void setWidth(float width) { put(KEY_WIDTH, (int) width*100); }

    public float getWidth() { return (getInt(KEY_WIDTH)/100.0f); }

    public void setHeight(float height) { put(KEY_HEIGHT, (int) height*100); }

    public float getHeight() { return (getInt(KEY_HEIGHT)/100.0f); }

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

    public String getType() { return getString(KEY_TYPE); }

    public void setType(String type) { put(KEY_TYPE, type); }

    public void addReportBy(ParseUser currentUser) {
        if (!getReports().contains(currentUser.getUsername())) {
            add(KEY_REPORTED_BY, currentUser.getUsername());
        }
    }

    public void setReports(List<String> reports) { put(KEY_REPORTED_BY, reports); }

    @SuppressWarnings("unchecked")
    public List<String> getReports() { return (List<String>) get(KEY_REPORTED_BY); }

    public boolean containedIn(List<Post> posts) {
        boolean containedIn = false;
        for (Post post : posts) {
            if (post.getObjectId().equals(this.getObjectId())) {
                containedIn = true;
            }
        }
        return containedIn;
    }

    public static Post copy(Post oldPost) {
        Post newPost = new Post();
        newPost.setType(oldPost.getType());
        newPost.setUser(oldPost.getUser());
        newPost.setTitle(oldPost.getTitle());
        newPost.setCondition(oldPost.getCondition());
        newPost.setNotes(oldPost.getNotes());
        newPost.setCoordinates(oldPost.getCoordinates());
        newPost.setDifficulty(oldPost.getDifficulty());
        newPost.setAgeRating(oldPost.getAgeRating());
        newPost.setImages(oldPost.getImages());
        newPost.setMinPlayers(oldPost.getMinPlayers());
        newPost.setMaxPlayers(oldPost.getMaxPlayers());
        newPost.setMinPlaytime(oldPost.getMinPlaytime());
        newPost.setMaxPlaytime(oldPost.getMaxPlaytime());
        newPost.setPieces(oldPost.getPieces());
        newPost.setWidth(oldPost.getWidth());
        newPost.setHeight(oldPost.getHeight());
        newPost.setReports(oldPost.getReports());
        return newPost;
    }

    public void setToBeDeleted(boolean toBeDeleted) { put(KEY_TO_BE_DELETED, toBeDeleted); }

    public boolean isToBeDeleted() { return getBoolean(KEY_TO_BE_DELETED); }

}
