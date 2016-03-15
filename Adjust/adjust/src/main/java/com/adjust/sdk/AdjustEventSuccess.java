package com.adjust.sdk;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by pfms on 04/01/16.
 */
public class AdjustEventSuccess {
    public String message;
    public String timestamp;
    public String adid;
    public String eventToken;
    public JSONObject jsonResponse;

    @Override
    public String toString() {
        return String.format(Locale.US, "Event Success msg:%s time:%s adid:%s event:%s json:%s",
            message, timestamp, adid, eventToken, jsonResponse);
    }
}
