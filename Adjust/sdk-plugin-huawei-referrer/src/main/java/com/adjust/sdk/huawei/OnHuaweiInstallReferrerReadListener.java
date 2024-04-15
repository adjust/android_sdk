package com.adjust.sdk.huawei;

import com.adjust.sdk.ReferrerDetails;

public interface OnHuaweiInstallReferrerReadListener {
    void onHuaweiAdsInstallReferrerDetailsRead(HuaweiInstallReferrerDetails referrerDetails);
    void onHuaweiAppGalleryInstallReferrerDetailsRead(HuaweiInstallReferrerDetails referrerDetails);
    void onFail(String message);
}
