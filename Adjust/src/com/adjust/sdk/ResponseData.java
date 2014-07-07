package com.adjust.sdk;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    // network of the tracker
    public String getNetwork() {
        return network;
    }

    // campaign of the tracker
    public String getCampaign() {
        return campaign;
    }

    // adgroup of the tracker
    public String getAdgroup() {
        return adgroup;
    }

    // creative of the tracker
    public String getCreative() {
        return creative;
    }

    // internals

    private ActivityKind activityKind = ActivityKind.UNKNOWN;
    private boolean success;
    private boolean willRetry;
    private String error;
    private String trackerToken;
    private String trackerName;
    private String network;
    private String campaign;
    private String adgroup;
    private String creative;

    public static ResponseData fromJson(JSONObject jsonObject, String jsonString) {

        if (jsonObject == null) {
            String error = String.format("Failed to parse json response: %s", jsonString.trim());
            return ResponseData.fromError(error);
        }

        ResponseData data = new ResponseData();

        data.error = jsonObject.optString("error", null);
        data.trackerToken = jsonObject.optString("tracker_token", null);
        data.trackerName = jsonObject.optString("tracker_name", null);
        data.network = jsonObject.optString("network", null);
        data.campaign = jsonObject.optString("campaign", null);
        data.adgroup = jsonObject.optString("adgroup", null);
        data.creative = jsonObject.optString("creative", null);

        return data;
    }

    public static ResponseData fromError(String error) {
        ResponseData data = new ResponseData();
        data.error = error;
        return data;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "[kind:%s success:%b willRetry:%b "
                + "error:%s trackerToken:%s trackerName:%s "
                + "network:%s campaign:%s adgroup:%s creative:%s]",
                getActivityKindString(),
                success,
                willRetry,
                Util.quote(error),
                trackerToken,
                Util.quote(trackerName),
                Util.quote(network),
                Util.quote(campaign),
                Util.quote(adgroup),
                Util.quote(creative));
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

        if (!TextUtils.isEmpty(network)) {
            responseDataDic.put("network", network);
        }

        if (!TextUtils.isEmpty(campaign)) {
            responseDataDic.put("campaign", campaign);
        }

        if (!TextUtils.isEmpty(adgroup)) {
            responseDataDic.put("adgroup", adgroup);
        }

        if (!TextUtils.isEmpty(creative)) {
            responseDataDic.put("creative", creative);
        }

        return responseDataDic;
    }
}
