package com.codepath.gameswap.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Report")
public class Report extends ParseObject {

    public static final String TAG = Report.class.getSimpleName();
    public static final String KEY_USER = "user";
    public static final String KEY_REPORTED_BY = "reportedBy";
    public static final String KEY_REASON = "reason";
    public static final String KEY_STATUS = "status";
    public static final String KEY_UNDER_REVIEW = "underReview";

    public ParseUser getUser() { return getParseUser(KEY_USER); }

    public void setUser(ParseUser parseUser) { put(KEY_USER, parseUser); }

    public ParseUser getReportedBy() { return getParseUser(KEY_REPORTED_BY); }

    public void setReportedBy(ParseUser parseUser) { put(KEY_REPORTED_BY, parseUser); }

    public String getReason() { return getString(KEY_REASON); }

    public void setReason(String reason) { put(KEY_REASON, reason); }

    public String getStatus() { return getString(KEY_STATUS); }

    public void setStatus(String status) { put(KEY_STATUS, status); }

    public boolean isUnderReview() { return getBoolean(KEY_UNDER_REVIEW); }

    public void setStatus(boolean isUnderReview) { put(KEY_UNDER_REVIEW, isUnderReview); }

}
