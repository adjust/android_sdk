package com.adjust.sdk.samsung.clouddev;

import android.content.Context;

import com.adjust.sdk.ILogger;

public class Util {
   public synchronized static String getGoogleAdId(Context context, ILogger logger) {
      if (!AdjustSamsungCloudDev.shouldUseSamsungCloudDevSdk) {
         return null;
      }

      return SamsungCloudDevClient.getGoogleAdId(context, logger, 4000);
   }

   public synchronized static boolean isAppRunningInCloudEnvironment(Context context, ILogger logger) {
      if (!AdjustSamsungCloudDev.shouldUseSamsungCloudDevSdk) {
         return false;
      }

      return SamsungCloudDevClient.isAppRunningInCloudEnvironment(context, logger);
   }
}
