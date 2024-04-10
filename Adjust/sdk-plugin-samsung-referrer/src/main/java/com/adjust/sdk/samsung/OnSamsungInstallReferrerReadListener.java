package com.adjust.sdk.samsung;

import com.adjust.sdk.ReferrerDetails;

public interface OnSamsungInstallReferrerReadListener {
    void onInstallReferrerRead(ReferrerDetails referrerDetails);
    void onFailure(String message);
}
