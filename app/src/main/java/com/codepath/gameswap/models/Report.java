package com.codepath.gameswap.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Report")
public class Report extends ParseObject {

    public static final String TAG = Report.class.getSimpleName();
    public static final String KEY_REPORTING = "reporting";
    public static final String KEY_REPORTED = "reported";
    public static final String KEY_REASON = "reason";

    public ParseUser getReporting() { return getParseUser(KEY_REPORTING); }

    public void setReporting(ParseUser parseUser) { put(KEY_REPORTING, parseUser); }

    public ParseUser getReported() { return getParseUser(KEY_REPORTED); }

    public void setReported(ParseUser parseUser) { put(KEY_REPORTED, parseUser); }

    public String getReason() { return getString(KEY_REASON); }

    public void setReason(String reason) { put(KEY_REASON, reason); }

}
