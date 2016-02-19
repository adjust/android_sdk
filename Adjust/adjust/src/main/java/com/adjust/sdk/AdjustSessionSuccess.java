package com.adjust.sdk;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by pfms on 16/02/16.
 */
public class AdjustSessionSuccess {
    public String message;
    public String timestamp;
    public String adid;
    public JSONObject jsonResponse;

    @Override
    public String toString() {
        return String.format(Locale.US, "Session Success msg:%s time:%s adid:%s json:%s",
                message, timestamp, adid, jsonResponse);
    }
}
