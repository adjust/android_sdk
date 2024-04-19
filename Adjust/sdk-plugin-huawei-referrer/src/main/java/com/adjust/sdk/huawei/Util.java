package com.adjust.sdk.huawei;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.ReferrerDetails;

public class Util {
    public synchronized static ReferrerDetails getHuaweiAdsInstallReferrerDetails(Context context, ILogger logger) {
        if (!AdjustHuaweiReferrer.shouldReadHuaweiReferrer) {
            return null;
        }
        HuaweiInstallReferrerResult huaweiInstallReferrerResult = HuaweiReferrerClient.getHuaweiAdsInstallReferrer(context, logger);
        if (huaweiInstallReferrerResult == null) {
            return null;
        }
        if (huaweiInstallReferrerResult.huaweiInstallReferrerDetails == null) {
            return null;
        }
        HuaweiInstallReferrerDetails huaweiInstallReferrerDetails = huaweiInstallReferrerResult.huaweiInstallReferrerDetails;
        return new ReferrerDetails(
                huaweiInstallReferrerDetails.installReferrer,
                huaweiInstallReferrerDetails.referrerClickTimestampSeconds,
                huaweiInstallReferrerDetails.installBeginTimestampSeconds);
    }

    public synchronized static ReferrerDetails getHuaweiAppGalleryInstallReferrerDetails(Context context, ILogger logger) {
        if (!AdjustHuaweiReferrer.shouldReadHuaweiReferrer) {
            return null;
        }

        HuaweiInstallReferrerResult huaweiInstallReferrerResult = HuaweiReferrerClient.getHuaweiAppGalleryInstallReferrer(context, logger);
        if (huaweiInstallReferrerResult == null) {
            return null;
        }
        if (huaweiInstallReferrerResult.huaweiInstallReferrerDetails == null) {
            return null;
        }

        HuaweiInstallReferrerDetails huaweiInstallReferrerDetails = huaweiInstallReferrerResult.huaweiInstallReferrerDetails;
        return new ReferrerDetails(
                huaweiInstallReferrerDetails.installReferrer,
                huaweiInstallReferrerDetails.referrerClickTimestampSeconds,
                huaweiInstallReferrerDetails.installBeginTimestampSeconds
        );
    }
}
