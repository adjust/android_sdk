package com.adjust.sdk.huawei;

import com.adjust.sdk.ReferrerDetails;

public interface OnHuaweiInstallReferrerReadListener {
    void onHuaweiAdsInstallReferrerDetailsRead(ReferrerDetails referrerDetails, String referrerApi);
    void onHuaweiAppGalleryInstallReferrerDetailsRead(ReferrerDetails referrerDetails, String referrerApi);
    void onFail(String message);
}
