package com.adjust.sdk.vivo;

import android.content.Context;

public class AdjustVivoReferrer {

   static boolean shouldReadVivoReferrer = true;

   public static void readVivoReferrer(Context context) {
      shouldReadVivoReferrer = true;
   }

   public static void doNotReadVivoReferrer() {
      shouldReadVivoReferrer = false;
   }
}
