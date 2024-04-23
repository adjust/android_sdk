package com.adjust.sdk;

public interface OnAmazonAdIdReadListener {

    void onAmazonAdIdRead(String amazonAdId);
    void onFail(String message);
}
