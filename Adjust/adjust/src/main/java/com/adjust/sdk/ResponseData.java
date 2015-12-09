package com.adjust.sdk;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by pfms on 03/12/15.
 */
public class ResponseData {
    public String message;
    public String timestamp;
    public JSONObject jsonResponse;

    @Override
    public String toString() {
        return String.format(Locale.US, "message:%s timestamp:%s json:%s",
                message, timestamp, jsonResponse);
    }
}
