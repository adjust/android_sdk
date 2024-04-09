package com.adjust.sdk.huawei;

import android.content.Context;

public class AdjustHuaweiReferrer {

   static boolean shouldReadHuaweiReferrer = true;
   static OnHuaweiInstallReferrerReadListener onHuaweiInstallReferrerReadListener;

   public static void setOnHuaweiInstallReferrerReadListener(OnHuaweiInstallReferrerReadListener onHuaweiInstallReferrerReadListener){
      AdjustHuaweiReferrer.onHuaweiInstallReferrerReadListener = onHuaweiInstallReferrerReadListener;
   }

   public static void readHuaweiReferrer(Context context) {
      shouldReadHuaweiReferrer = true;
   }

   public static void doNotReadHuaweiReferrer() {
      shouldReadHuaweiReferrer = false;
   }
}
