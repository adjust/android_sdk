package com.adjust.sdk.huawei;

import com.adjust.sdk.ReferrerDetails;

public interface OnHuaweiInstallReferrerReadListener {
    void onInstallReferrerDetailsRead(HuaweiInstallReferrerDetails referrerDetails);
    void onFail(String message);
}
