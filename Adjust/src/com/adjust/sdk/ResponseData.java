package com.adjust.sdk;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

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
        try {
            ResponseData data = new ResponseData();
            JSONObject jsonObject = new JSONObject(jsonString);

            data.error = jsonObject.optString("error", null);
            data.trackerToken = jsonObject.optString("tracker_token", null);
            data.trackerName = jsonObject.optString("tracker_name", null);

            return data;
        } catch (JSONException e) {
            String error = String.format("Failed to parse json response: %s", jsonString.trim());
            return ResponseData.fromError(error);
        }
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
                Util.quote(error),
                trackerToken,
                Util.quote(trackerName));
    }

    public void setActivityKind(ActivityKind activityKind) {
        this.activityKind = activityKind;
    }

    public void setWasSuccess(boolean success) {
        this.success = success;
    }

    public void setWillRetry(boolean willRetry) {
        this.willRetry = willRetry;
    }

    public Map<String, String> toDic() {
        Map<String, String> responseDataDic = new HashMap<String, String>();

        responseDataDic.put("activityKind", activityKind.toString());
        responseDataDic.put("success", success ? "true" : "false");
        responseDataDic.put("willRetry", willRetry ? "true" : "false");

        if (!TextUtils.isEmpty(error)) {
            responseDataDic.put("error", error);
        }

        if (!TextUtils.isEmpty(trackerToken)) {
            responseDataDic.put("trackerToken", trackerToken);
        }

        if (!TextUtils.isEmpty(trackerName)) {
            responseDataDic.put("trackerName", trackerName);
        }

        return responseDataDic;
    }
}
