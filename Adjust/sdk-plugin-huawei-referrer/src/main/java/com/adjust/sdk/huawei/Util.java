package com.adjust.sdk.huawei;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.ReferrerDetails;

public class Util {
   public synchronized static ReferrerDetails getHuaweiAdsInstallReferrerDetails(Context context, ILogger logger) {
      if (!AdjustHuaweiReferrer.shouldReadHuaweiReferrer) {
         return null;
      }

      return HuaweiReferrerClient.getHuaweiAdsInstallReferrer(context, logger);
   }

   public synchronized static ReferrerDetails getHuaweiAppGalleryInstallReferrerDetails(Context context, ILogger logger) {
      if (!AdjustHuaweiReferrer.shouldReadHuaweiReferrer) {
         return null;
      }

      return HuaweiReferrerClient.getHuaweiAppGalleryInstallReferrer(context, logger);
   }
}
