package com.adjust.sdk;

import org.json.JSONObject;

import java.util.Locale;

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

    @Override
    public String toString() {
        return String.format(Locale.US, "%s msg:%s time:%s adid:%s event:%s retry:%b json:%s",
                activityKindString, message, timestamp, adid, eventToken, willRetry, jsonResponse);
    }
}
