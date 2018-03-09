package com.adjust.sdk;

import org.json.JSONObject;

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
        if (jsonResponse != null) {
            successResponseData.jsonResponse = jsonResponse;
        } else {
            successResponseData.jsonResponse = new JSONObject();
        }

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
        if (jsonResponse != null) {
            failureResponseData.jsonResponse = jsonResponse;
        } else {
            failureResponseData.jsonResponse = new JSONObject();
        }

        return failureResponseData;
    }
}
