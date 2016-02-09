package com.adjust.sdk;

/**
 * Created by pfms on 09/02/16.
 */
public class EventResponseData extends ResponseData {
    public String eventToken;

    public EventResponseData(ActivityPackage activityPackage) {
        eventToken = activityPackage.getParameters().get("event_token");
    }

    @Override
    public SuccessResponseData getSuccessResponseData() {
        SuccessResponseData successResponseData = super.getSuccessResponseData();

        successResponseData.eventToken = eventToken;

        return successResponseData;
    }

    @Override
    public FailureResponseData getFailureResponseData() {
        FailureResponseData failureResponseData = super.getFailureResponseData();

        failureResponseData.eventToken = eventToken;

        return failureResponseData;
    }
}