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
    public boolean success;
    public boolean willRetry;
    public JSONObject jsonResponse;
    public AdjustAttribution attribution;

    protected ResponseData() {}

    public static ResponseData buildResponseData(ActivityPackage activityPackage) {
        ActivityKind activityKind = activityPackage.getActivityKind();
        ResponseData responseData;
        switch (activityKind) {
            case SESSION:
                responseData = new SessionResponseData();
                break;
            case CLICK:
                responseData = new SdkClickResponseData();
                break;
            case ATTRIBUTION:
                responseData = new AttributionResponseData();
                break;
            case EVENT:
                responseData = new EventResponseData(activityPackage);
                break;
            default:
                responseData = new ResponseData();
                break;
        }

        responseData.activityKind = activityKind;

        return responseData;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "message:%s timestamp:%s json:%s",
                message, timestamp, jsonResponse);
    }
}