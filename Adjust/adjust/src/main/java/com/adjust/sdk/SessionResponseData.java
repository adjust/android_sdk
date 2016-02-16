package com.adjust.sdk;

/**
 * Created by pfms on 09/02/16.
 */
public class SessionResponseData extends ResponseData {
    public SessionSuccessResponseData getSuccessResponseData() {
        if (!success) {
            return null;
        }

        SessionSuccessResponseData successResponseData = new SessionSuccessResponseData();
        successResponseData.message = message;
        successResponseData.timestamp = timestamp;
        successResponseData.adid = adid;
        successResponseData.jsonResponse = jsonResponse;

        return successResponseData;
    }

    public SessionFailureResponseData getFailureResponseData() {
        if (success) {
            return null;
        }

        SessionFailureResponseData failureResponseData = new SessionFailureResponseData();
        failureResponseData.message = message;
        failureResponseData.timestamp = timestamp;
        failureResponseData.adid = adid;
        failureResponseData.willRetry = willRetry;
        failureResponseData.jsonResponse = jsonResponse;

        return failureResponseData;
    }
}
