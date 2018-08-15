package com.adjust.sdk;

import org.json.JSONObject;

/**
 * Created by pfms on 09/02/16.
 */
public class SessionResponseData extends ResponseData {
    private String sdkPlatform;

    public SessionResponseData(final ActivityPackage activityPackage) {
        this.sdkPlatform = Util.getSdkPrefixPlatform(activityPackage.getClientSdk());
    }

    public AdjustSessionSuccess getSuccessResponseData() {
        if (!success) {
            return null;
        }

        AdjustSessionSuccess successResponseData = new AdjustSessionSuccess();
        if (this.sdkPlatform == null) {
            // Native Android platform.
            successResponseData.message = message;
            successResponseData.timestamp = timestamp;
            successResponseData.adid = adid;
            successResponseData.jsonResponse = jsonResponse;
        } else if (this.sdkPlatform.equals("unity")) {
            // Unity platform.
            successResponseData.message = message != null ? message : "";
            successResponseData.timestamp = timestamp != null ? timestamp : "";
            successResponseData.adid = adid != null ? adid : "";
            successResponseData.jsonResponse = jsonResponse != null ? jsonResponse : new JSONObject();
        } else {
            // Rest of non native platforms.
            successResponseData.message = message;
            successResponseData.timestamp = timestamp;
            successResponseData.adid = adid;
            successResponseData.jsonResponse = jsonResponse;
        }

        return successResponseData;
    }

    public AdjustSessionFailure getFailureResponseData() {
        if (success) {
            return null;
        }

        AdjustSessionFailure failureResponseData = new AdjustSessionFailure();
        if (this.sdkPlatform == null) {
            // Native Android platform.
            failureResponseData.message = message;
            failureResponseData.timestamp = timestamp;
            failureResponseData.adid = adid;
            failureResponseData.willRetry = willRetry;
            failureResponseData.jsonResponse = jsonResponse;
        }  else if (this.sdkPlatform.equals("unity")) {
            // Unity platform.
            failureResponseData.message = message != null ? message : "";
            failureResponseData.timestamp = timestamp != null ? timestamp : "";
            failureResponseData.adid = adid != null ? adid : "";
            failureResponseData.willRetry = willRetry;
            failureResponseData.jsonResponse = jsonResponse != null ? jsonResponse : new JSONObject();
        } else {
            // Rest of non native platforms.
            failureResponseData.message = message;
            failureResponseData.timestamp = timestamp;
            failureResponseData.adid = adid;
            failureResponseData.willRetry = willRetry;
            failureResponseData.jsonResponse = jsonResponse;
        }

        return failureResponseData;
    }
}
