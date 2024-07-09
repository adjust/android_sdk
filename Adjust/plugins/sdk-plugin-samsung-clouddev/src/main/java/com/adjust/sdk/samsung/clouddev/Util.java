package com.adjust.sdk.samsung.clouddev;

import android.content.Context;

import com.adjust.sdk.ILogger;

public class Util {
   public synchronized static String getGoogleAdIdInCloudEnvironment(Context context, ILogger logger) {
      if (!AdjustSamsungCloudDev.shouldUseSamsungCloudDevSdk) {
         return null;
      }

      logger.info("getGoogleAdIdInCloudEnvironment invoked");

      return SamsungCloudDevClient.getGoogleAdId(context, logger, 4000);
   }

   public synchronized static boolean isAppRunningInCloudEnvironment(Context context, ILogger logger) {
      if (!AdjustSamsungCloudDev.shouldUseSamsungCloudDevSdk) {
         return false;
      }

      logger.info("isAppRunningInCloudEnvironment invoked");

      return SamsungCloudDevClient.isAppRunningInCloudEnvironment(context, logger);
   }
}
