package com.adjust.sdk.samsung;

import android.content.Context;

public class AdjustSamsungReferrer {

   static boolean shouldReadSamsungReferrer = true;

   public static void readSamsungReferrer(Context context) {
      shouldReadSamsungReferrer = true;
   }

   public static void doNotReadSamsungReferrer() {
      shouldReadSamsungReferrer = false;
   }
}
