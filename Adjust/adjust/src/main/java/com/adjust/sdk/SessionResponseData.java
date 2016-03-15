package com.adjust.sdk;

/**
 * Created by pfms on 09/02/16.
 */
public class SessionResponseData extends ResponseData {
    public AdjustSessionSuccess getSuccessResponseData() {
        if (!success) {
            return null;
        }

        AdjustSessionSuccess successResponseData = new AdjustSessionSuccess();
        successResponseData.message = message;
        successResponseData.timestamp = timestamp;
        successResponseData.adid = adid;
        successResponseData.jsonResponse = jsonResponse;

        return successResponseData;
    }

    public AdjustSessionFailure getFailureResponseData() {
        if (success) {
            return null;
        }

        AdjustSessionFailure failureResponseData = new AdjustSessionFailure();
        failureResponseData.message = message;
        failureResponseData.timestamp = timestamp;
        failureResponseData.adid = adid;
        failureResponseData.willRetry = willRetry;
        failureResponseData.jsonResponse = jsonResponse;

        return failureResponseData;
    }
}
