package com.adjust.sdk.samsung;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.ReferrerDetails;


public class AdjustSamsungReferrer {

   static boolean shouldReadSamsungReferrer = true;

   public static void getSamsungInstallReferrer(Context context ,OnSamsungInstallReferrerReadListener onSamsungInstallReferrerReadListener){
      Handler handler = new Handler(Looper.getMainLooper());
      handler.post(() -> {
         try {
            ReferrerDetails referrerDetails = Util.getSamsungInstallReferrerDetails(context, AdjustFactory.getLogger());
            onSamsungInstallReferrerReadListener.onInstallReferrerRead(referrerDetails);
         }catch (Exception exception){
            onSamsungInstallReferrerReadListener.onFailure(exception.getMessage());
         }
      });
   }

   public static void readSamsungReferrer(Context context) {
      shouldReadSamsungReferrer = true;
   }

   public static void doNotReadSamsungReferrer() {
      shouldReadSamsungReferrer = false;
   }
}
