package com.adjust.sdk.samsung.clouddev;

import android.content.Context;

public class AdjustSamsungCloudDev {

   static boolean shouldUseSamsungCloudDevSdk = true;

   public static void useSamsungCloudDevSdk(Context context) {
      shouldUseSamsungCloudDevSdk = true;
   }

   public static void doNotUseSamsungCloudDevSdk() {
      shouldUseSamsungCloudDevSdk = false;
   }
}
