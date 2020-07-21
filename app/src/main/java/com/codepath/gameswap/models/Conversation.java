package com.codepath.gameswap.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

@ParseClassName("Conversation")
public class Conversation extends ParseObject {

    public static final String TAG = Conversation.class.getSimpleName();
    public static final String KEY_USER_ONE = "userOne";
    public static final String KEY_USER_TWO = "userTwo";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_MESSAGES = "messages";

    public ParseUser getUserOne() { return getParseUser(KEY_USER_ONE); }

    public void setUserOne(ParseUser parseUser) { put(KEY_USER_ONE, parseUser); }

    public ParseUser getUserTwo() { return getParseUser(KEY_USER_TWO); }

    public void setUserTwo(ParseUser parseUser) { put(KEY_USER_TWO, parseUser); }

    public Message getLastMessage() { return (Message) getParseObject(KEY_LAST_MESSAGE); }

    public void setLastMessage(Message lastMessage) { put(KEY_LAST_MESSAGE, lastMessage); }

    public ParseRelation<Message> getMessagesRelation() { return getRelation(KEY_MESSAGES); }

}
