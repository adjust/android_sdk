package com.adjust.sdk;

/**
 * Created by pfms on 09/02/16.
 */
public class EventResponseData extends ResponseData {
    public String eventToken;

    public EventResponseData(ActivityPackage activityPackage) {
        eventToken = activityPackage.getParameters().get("event_token");
    }

    public EventSuccessResponseData getSuccessResponseData() {
        if (!success) {
            return null;
        }

        EventSuccessResponseData successResponseData = new EventSuccessResponseData();
        successResponseData.message = message;
        successResponseData.timestamp = timestamp;
        successResponseData.adid = adid;
        successResponseData.jsonResponse = jsonResponse;
        successResponseData.eventToken = eventToken;

        return successResponseData;
    }

    public EventFailureResponseData getFailureResponseData() {
        if (success) {
            return null;
        }

        EventFailureResponseData failureResponseData = new EventFailureResponseData();
        failureResponseData.message = message;
        failureResponseData.timestamp = timestamp;
        failureResponseData.adid = adid;
        failureResponseData.willRetry = willRetry;
        failureResponseData.jsonResponse = jsonResponse;
        failureResponseData.eventToken = eventToken;

        return failureResponseData;
    }
}