package com.codepath.gameswap.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("PostReport")
public class PostReport extends ParseObject {

    public static final String TAG = PostReport.class.getSimpleName();
    public static final String KEY_POST = "post";
    public static final String KEY_REPORTED_BY = "reportedBy";
    public static final String KEY_REASON = "reason";

    public Post getPost() { return (Post) get(KEY_POST); }

    public void setPost(Post post) { put(KEY_POST, post); }

    public ParseUser getReportedBy() { return getParseUser(KEY_REPORTED_BY); }

    public void setReportedBy(ParseUser parseUser) { put(KEY_REPORTED_BY, parseUser); }

    public String getReason() { return getString(KEY_REASON); }

    public void setReason(String reason) { put(KEY_REASON, reason); }

}
