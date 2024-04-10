package com.adjust.sdk.huawei;

import com.adjust.sdk.ReferrerDetails;

public interface OnHuaweiInstallReferrerReadListener {
    void onHuaweiAdsInstallReferrerDetailsRead(ReferrerDetails referrerDetails);
    void onHuaweiAppGalleryInstallReferrerDetailsRead(ReferrerDetails referrerDetails);
    void onFailure(String message);
}
