package com.adjust.sdk;

import org.json.JSONObject;

/**
 * Created by pfms on 03/12/15.
 */
public class ResponseData {
    public String activityKindString;
    public boolean wasSuccess;
    public boolean willRetry;
    public String message;
    public String timestamp;
    public JSONObject jsonResponse;
}
