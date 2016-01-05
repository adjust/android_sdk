package com.adjust.sdk;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by pfms on 03/12/15.
 */
public class ResponseData {
    public ActivityKind activityKind;
    public String message;
    public String timestamp;
    public String adid;
    public String eventToken;
    public boolean success;
    public boolean willRetry;
    public JSONObject jsonResponse;
    public AdjustAttribution attribution;

    public ResponseData(ActivityPackage activityPackage) {
        activityKind = activityPackage.getActivityKind();
        eventToken = activityPackage.getParameters().get("event_token");
    }

    public SuccessResponseData getSuccessResponseData() {
        if (!success) {
            return null;
        }

        SuccessResponseData successResponseData = new SuccessResponseData();
        successResponseData.activityKindString = activityKind.toString();
        successResponseData.message = message;
        successResponseData.timestamp = timestamp;
        successResponseData.adid = adid;
        successResponseData.eventToken = eventToken;
        successResponseData.jsonResponse = jsonResponse;

        return successResponseData;
    }

    public FailureResponseData getFailureResponseData() {
        if (success) {
            return null;
        }

        FailureResponseData failureResponseData = new FailureResponseData();
        failureResponseData.activityKindString = activityKind.toString();
        failureResponseData.message = message;
        failureResponseData.timestamp = timestamp;
        failureResponseData.adid = adid;
        failureResponseData.willRetry = willRetry;
        failureResponseData.eventToken = eventToken;
        failureResponseData.jsonResponse = jsonResponse;

        return failureResponseData;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "message:%s timestamp:%s json:%s",
                message, timestamp, jsonResponse);
    }
}
