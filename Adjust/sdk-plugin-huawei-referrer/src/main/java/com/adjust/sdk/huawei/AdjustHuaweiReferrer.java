package com.adjust.sdk.huawei;

import android.content.Context;

public class AdjustHuaweiReferrer {

   static boolean shouldReadHuaweiReferrer = true;

   public static void readHuaweiReferrer(Context context) {
      shouldReadHuaweiReferrer = true;
   }

   public static void doNotReadHuaweiReferrer() {
      shouldReadHuaweiReferrer = false;
   }
}
