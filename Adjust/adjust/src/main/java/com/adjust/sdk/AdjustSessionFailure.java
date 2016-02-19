package com.adjust.sdk;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by pfms on 16/02/16.
 */
public class AdjustSessionFailure {
    public String message;
    public String timestamp;
    public String adid;
    public boolean willRetry;
    public JSONObject jsonResponse;

    @Override
    public String toString() {
        return String.format(Locale.US, "Session Failure msg:%s time:%s adid:%s retry:%b json:%s",
            message, timestamp, adid, willRetry, jsonResponse);
    }
}
