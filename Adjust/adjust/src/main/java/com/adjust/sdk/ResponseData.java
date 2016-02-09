package com.adjust.sdk;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by pfms on 03/12/15.
 */
public abstract class ResponseData {
    public ActivityKind activityKind;
    public String message;
    public String timestamp;
    public String adid;
    public boolean success;
    public boolean willRetry;
    public JSONObject jsonResponse;
    public AdjustAttribution attribution;

    public static ResponseData buildResponseData(ActivityPackage activityPackage) {
        ActivityKind activityKind = activityPackage.getActivityKind();
        ResponseData responseData;
        switch (activityKind) {
            case SESSION:
                responseData = new SessionResponseData();
                break;
            case ATTRIBUTION:
                responseData = new AttributionResponseData();
                break;
            case EVENT:
                responseData = new EventResponseData(activityPackage);
                break;
            case CLICK:
                responseData = new ClickResponseData();
                break;
            default:
                responseData = new UnknownResponseData();
                break;
        }

        responseData.activityKind = activityKind;

        return responseData;
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
        failureResponseData.jsonResponse = jsonResponse;

        return failureResponseData;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "message:%s timestamp:%s json:%s",
                message, timestamp, jsonResponse);
    }
}