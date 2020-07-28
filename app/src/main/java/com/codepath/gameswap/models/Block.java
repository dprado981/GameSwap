package com.codepath.gameswap.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Block")
public class Block extends ParseObject {

    public static final String TAG = Block.class.getSimpleName();
    public static final String KEY_BLOCKING = "blocking";
    public static final String KEY_BLOCKED = "blocked";

    public ParseUser getBlocking() { return getParseUser(KEY_BLOCKING); }

    public void setBlocking(ParseUser parseUser) { put(KEY_BLOCKING, parseUser); }

    public ParseUser getBlocked() { return getParseUser(KEY_BLOCKED); }

    public void setBlocked(ParseUser parseUser) { put(KEY_BLOCKED, parseUser); }
}
