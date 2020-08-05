package com.codepath.gameswap.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Block")
public class Block extends ParseObject {

    public static final String TAG = Block.class.getSimpleName();
    public static final String KEY_USER = "user";
    public static final String KEY_BLOCKED_BY = "blockedBy";

    public ParseUser getUser() { return getParseUser(KEY_USER); }

    public void setUser(ParseUser parseUser) { put(KEY_USER, parseUser); }

    public ParseUser getBlockedBy() { return getParseUser(KEY_BLOCKED_BY); }

    public void setBlockedBy(ParseUser parseUser) { put(KEY_BLOCKED_BY, parseUser); }
}
