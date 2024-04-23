package com.adjust.sdk.meta;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.ReferrerDetails;

public class Util {
   public synchronized static ReferrerDetails getMetaInstallReferrerDetails(Context context, String fbAppId, ILogger logger) {
      if (!AdjustMetaReferrer.shouldReadMetaReferrer) {
         return null;
      }

      MetaInstallReferrerResult metaInstallReferrerResult = MetaReferrerClient.getMetaInstallReferrer(context, fbAppId, logger);
      if (metaInstallReferrerResult == null) {
         return null;
      }
      if (metaInstallReferrerResult.metaInstallReferrerDetails == null) {
         return null;
      }
      MetaInstallReferrerDetails metaInstallReferrerDetails = metaInstallReferrerResult.metaInstallReferrerDetails;
      return new ReferrerDetails(
              metaInstallReferrerDetails.installReferrer,
              metaInstallReferrerDetails.actualTimestampInSec,
              metaInstallReferrerDetails.isClick);
   }
}
