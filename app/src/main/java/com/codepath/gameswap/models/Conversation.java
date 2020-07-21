package com.codepath.gameswap.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Conversation")
public class Conversation extends ParseObject {

    public static final String TAG = Conversation.class.getSimpleName();
    public static final String KEY_USER_ONE = "userOne";
    public static final String KEY_USER_TWO = "userTwo";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_DELETED_BY_ONE = "deletedByOne";
    public static final String KEY_DELETED_BY_TWO = "deletedByTwo";
    public static final String KEY_FROM_POST = "fromPost";

    public ParseUser getUserOne() { return getParseUser(KEY_USER_ONE); }

    public void setUserOne(ParseUser parseUser) { put(KEY_USER_ONE, parseUser); }

    public ParseUser getUserTwo() { return getParseUser(KEY_USER_TWO); }

    public void setUserTwo(ParseUser parseUser) { put(KEY_USER_TWO, parseUser); }

    public Message getLastMessage() { return (Message) getParseObject(KEY_LAST_MESSAGE); }

    public void setLastMessage(Message lastMessage) { put(KEY_LAST_MESSAGE, lastMessage); }

    public boolean getDeletedByOne() { return (boolean) get(KEY_DELETED_BY_ONE); }

    public void setDeletedByOne(boolean deletedByOne) { put(KEY_DELETED_BY_ONE, deletedByOne); }

    public boolean getDeletedByTwo() { return (boolean) get(KEY_DELETED_BY_TWO); }

    public void setDeletedByTwo(boolean deletedByTwo) { put(KEY_DELETED_BY_TWO, deletedByTwo); }

    public Post getFromPost() { return (Post) get(KEY_FROM_POST); }

    public void setFromPost(Post fromPost) { put(KEY_FROM_POST, fromPost); }

}
