package com.adjust.sdk.lvl;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.LicenseRequiredData;

public class Util {

    public synchronized static LicenseRequiredData getLicenseRequiredData(Context context, ILogger logger, long installTimestamp) {
        return AdjustLicenseVerification.fetchLicenseDate(context, logger, installTimeStamp);
    }
}
