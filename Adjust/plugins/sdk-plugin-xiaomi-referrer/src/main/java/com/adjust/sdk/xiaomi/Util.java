package com.adjust.sdk.xiaomi;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.ReferrerDetails;
import com.miui.referrer.api.GetAppsReferrerDetails;

public class Util {
   public synchronized static ReferrerDetails getXiaomiInstallReferrerDetails(Context context, ILogger logger) {
      if (!AdjustXiaomiReferrer.shouldReadXiaomiReferrer) {
         return null;
      }

      logger.info("getXiaomiInstallReferrerDetails invoked");

      XiaomiInstallReferrerDetails xiaomiInstallReferrerDetails = XiaomiReferrerClient.getReferrer(context, logger, 3000).xiaomiInstallReferrerDetails;
      if (xiaomiInstallReferrerDetails == null) {
         return null;
      }

      return new ReferrerDetails(xiaomiInstallReferrerDetails.installReferrer,
                                 xiaomiInstallReferrerDetails.referrerClickTimestampSeconds,
                                 xiaomiInstallReferrerDetails.installBeginTimestampSeconds,
                                 xiaomiInstallReferrerDetails.referrerClickTimestampServerSeconds,
                                 xiaomiInstallReferrerDetails.installBeginTimestampServerSeconds,
                                 xiaomiInstallReferrerDetails.installVersion, null, null);
   }
}
