package com.adjust.sdk;

import org.json.JSONObject;

/**
 * Adjust SDK
 * Created by Pedro Silva (@nonelse) on 3rd December 2015.
 * Copyright \u00a9 2015-2018 Adjust GmbH. All rights reserved.
 */
public class ResponseData {
    public boolean success;
    public boolean willRetry;
    public String adid;
    public String message;
    public String timestamp;
    public JSONObject jsonResponse;
    public ActivityKind activityKind;
    public TrackingState trackingState;
    public AdjustAttribution attribution;

    protected ResponseData() {}

    public static ResponseData buildResponseData(ActivityPackage activityPackage) {
        ResponseData responseData;
        ActivityKind activityKind = activityPackage.getActivityKind();
        switch (activityKind) {
            case SESSION:
                responseData = new SessionResponseData(activityPackage);
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
        return Util.formatString("message:%s timestamp:%s json:%s", message, timestamp, jsonResponse);
    }
}