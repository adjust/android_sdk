package com.adjust.sdk.samsung;

import com.adjust.sdk.ReferrerDetails;

public interface OnSamsungInstallReferrerReadListener {
    void onInstallReferrerRead(ReferrerDetails referrerDetails, String referrerApi);
    void onFail(String message);
}
