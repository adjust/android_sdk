package com.adjust.sdk;

import org.json.JSONObject;

/**
 * Created by pfms on 09/02/16.
 */
public class EventResponseData extends ResponseData {
    private String eventToken;
    private String sdkPlatform;

    public EventResponseData(final ActivityPackage activityPackage) {
        this.eventToken = activityPackage.getParameters().get("event_token");
        this.sdkPlatform = Util.getSdkPrefixPlatform(activityPackage.getClientSdk());
    }

    public AdjustEventSuccess getSuccessResponseData() {
        if (!success) {
            return null;
        }

        AdjustEventSuccess successResponseData = new AdjustEventSuccess();
        if (this.sdkPlatform.equals("unity")) {
            successResponseData.eventToken = this.eventToken != null ? this.eventToken : "";
            successResponseData.message = message != null ? message : "";
            successResponseData.timestamp = timestamp != null ? timestamp : "";
            successResponseData.adid = adid != null ? adid : "";
            successResponseData.jsonResponse = jsonResponse != null ? jsonResponse : new JSONObject();
        } else {
            successResponseData.eventToken = this.eventToken;
            successResponseData.message = message;
            successResponseData.timestamp = timestamp;
            successResponseData.adid = adid;
            successResponseData.jsonResponse = jsonResponse;
        }

        return successResponseData;
    }

    public AdjustEventFailure getFailureResponseData() {
        if (success) {
            return null;
        }

        AdjustEventFailure failureResponseData = new AdjustEventFailure();
        if (this.sdkPlatform.equals("unity")) {
            failureResponseData.eventToken = this.eventToken != null ? this.eventToken : "";
            failureResponseData.message = message != null ? message : "";
            failureResponseData.timestamp = timestamp != null ? timestamp : "";
            failureResponseData.adid = adid != null ? adid : "";
            failureResponseData.willRetry = willRetry;
            failureResponseData.jsonResponse = jsonResponse != null ? jsonResponse : new JSONObject();
        } else {
            failureResponseData.eventToken = this.eventToken;
            failureResponseData.message = message;
            failureResponseData.timestamp = timestamp;
            failureResponseData.adid = adid;
            failureResponseData.willRetry = willRetry;
            failureResponseData.jsonResponse = jsonResponse;
        }

        return failureResponseData;
    }
}