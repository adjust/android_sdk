package com.adjust.sdk;

import org.json.JSONObject;

/**
 * Created by pfms on 04/01/16.
 */
public class FailureResponseData {
    public String activityKindString;
    public String message;
    public String timestamp;
    public String adid;
    public String eventToken;
    public boolean willRetry;
    public JSONObject jsonResponse;
}
