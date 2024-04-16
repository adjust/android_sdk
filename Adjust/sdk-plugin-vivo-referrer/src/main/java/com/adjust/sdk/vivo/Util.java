package com.adjust.sdk.vivo;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.ReferrerDetails;

public class Util {
   public synchronized static ReferrerDetails getVivoInstallReferrerDetails(Context context, ILogger logger) {
      if (!AdjustVivoReferrer.shouldReadVivoReferrer) {
         return null;
      }

      VivoInstallReferrerResult vivoInstallReferrerResult = VivoReferrerClient.getReferrer(context, logger);
      if (vivoInstallReferrerResult.vivoInstallReferrerDetails == null) {
         return null;
      }

      VivoInstallReferrerDetails vivoInstallReferrerDetails = vivoInstallReferrerResult.vivoInstallReferrerDetails;

      return new ReferrerDetails(vivoInstallReferrerDetails.installReferrer,
              vivoInstallReferrerDetails.referrerClickTimestampSeconds,
              vivoInstallReferrerDetails.installBeginTimestampSeconds,
              -1,
              -1,
              vivoInstallReferrerDetails.installVersion,
              null,
              null);
   }
}
