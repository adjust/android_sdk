package com.adjust.sdk.xiaomi;

public interface OnXiaomiInstallReferrerReadListener {

    void onXiaomiInstallReferrerRead(XiaomiInstallReferrerDetails referrerDetails);
    void onFail(String message);
}
