package com.codepath.gameswap.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Conversation")
public class Conversation extends ParseObject {

    public static final String KEY_USERONE = "userOne";
    public static final String KEY_USERTWO = "userTwo";

    public ParseUser getUserOne() { return getParseUser(KEY_USERONE); };

    public void setUserOne(ParseUser parseUser) { put(KEY_USERONE, parseUser); }

    public ParseUser getUserTwo() { return getParseUser(KEY_USERTWO); };

    public void setUserTwo(ParseUser parseUser) { put(KEY_USERTWO, parseUser); }

}
