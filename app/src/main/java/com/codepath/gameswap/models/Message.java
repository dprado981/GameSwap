package com.codepath.gameswap.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Message")
public class Message extends ParseObject {

    public static final String KEY_FROM = "from";
    public static final String KEY_TO = "to";
    public static final String KEY_TEXT = "text";

    public String getFrom() { return getString(KEY_FROM); };

    public void setFrom(ParseUser parseUser) { put(KEY_FROM, parseUser); }

    public String getTo() { return getString(KEY_TO); };

    public void setTo(ParseUser parseUser) { put(KEY_TO, parseUser); }

    public int getText() { return getInt(KEY_TEXT); };

    public void setText(String text) { put(KEY_TEXT, text); }

}
