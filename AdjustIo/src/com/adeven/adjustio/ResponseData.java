package com.adeven.adjustio;

import java.util.Locale;

/*
 * Information about the result of a tracking attempt
 *
 * Will be passed to the delegate function TODO: update
 */
public class ResponseData {
    // set by SDK

    // the kind of activity (ActivityKind.SESSION etc.)
    // see the ActivityKind definition
    public ActivityKind getActivityKind() {
        return activityKind;
    }

    // returns human readable version of activityKind
    // (session, event, revenue), see above
    public String getActivityKindString() {
        return activityKind.toString();
    }

    // true when the activity was tracked successfully
    // might be true even if response could not be parsed
    public boolean wasSuccess() {
        return success;
    }

    // true if the server was not reachable and the request will be tried again
    public boolean willRetry() {
        return willRetry;
    }

    // set by SDK or server

    // null if activity was tracked successfully and response could be parsed
    // might be not null even when activity was tracked successfully
    public String getError() {
        return error;
    }

    // returned by server

    // tracker token of current device
    public String getTrackerToken() {
        return trackerToken;
    }

    // tracker name of current device
    public String getTrackerName() {
        return trackerName;
    }

    // internals

    private ActivityKind activityKind = ActivityKind.UNKNOWN;
    private boolean success;
    private boolean willRetry;
    private String error;
    private String trackerToken;
    private String trackerName;

    public static ResponseData fromJson(String jsonString) {
        ResponseData data = new ResponseData();
        // TODO: parse jsonString
        return data;
    }

    public static ResponseData fromError(String error) {
        ResponseData data = new ResponseData();
        data.error = error;
        return data;
    }

    public String toString() {
        return String.format(Locale.US,
                "[kind:%s success:%b willRetry:%b error:%s trackerToken:%s trackerName:%s]",
                getActivityKindString(),
                success,
                willRetry,
                error,
                trackerToken,
                trackerName);
    }
}
