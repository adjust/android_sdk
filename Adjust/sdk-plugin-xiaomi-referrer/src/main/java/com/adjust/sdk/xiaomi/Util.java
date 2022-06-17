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

      GetAppsReferrerDetails getAppsReferrerDetails = XiaomiReferrerClient.getReferrer(context, logger, 3000);
      if (getAppsReferrerDetails == null) {
         return null;
      }

      return new ReferrerDetails(getAppsReferrerDetails.getInstallReferrer(),
                                 getAppsReferrerDetails.getReferrerClickTimestampSeconds(),
                                 getAppsReferrerDetails.getInstallBeginTimestampSeconds(),
                                 getAppsReferrerDetails.getReferrerClickTimestampServerSeconds(),
                                 getAppsReferrerDetails.getInstallBeginTimestampServerSeconds(),
                                 getAppsReferrerDetails.getInstallVersion(), null);
   }
}
