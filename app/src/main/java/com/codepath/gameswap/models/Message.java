package com.codepath.gameswap.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Message")
public class Message extends ParseObject {

    public static final String KEY_FROM = "from";
    public static final String KEY_TO = "to";
    public static final String KEY_TEXT = "text";
    public static final String KEY_CONVERSATION = "conversation";

    public ParseUser getFrom() { return getParseUser(KEY_FROM); }

    public void setFrom(ParseUser parseUser) { put(KEY_FROM, parseUser); }

    public ParseUser getTo() { return getParseUser(KEY_TO); }

    public void setTo(ParseUser parseUser) { put(KEY_TO, parseUser); }

    public String getText() { return getString(KEY_TEXT); }

    public void setText(String text) { put(KEY_TEXT, text); }

    public Conversation getConversation() { return (Conversation) getParseObject(KEY_CONVERSATION); }

    public void setConversation(Conversation conversation) { put(KEY_CONVERSATION, conversation); }

}
