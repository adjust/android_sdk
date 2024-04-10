package com.adjust.sdk.samsung;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.Constants;
import com.adjust.sdk.ReferrerDetails;


public class AdjustSamsungReferrer {

   static boolean shouldReadSamsungReferrer = true;

   public static void setOnSamsungInstallReferrerReadListener(Context context ,OnSamsungInstallReferrerReadListener onSamsungInstallReferrerReadListener){
      Handler handler = new Handler(Looper.getMainLooper());
      handler.post(() -> {
         try {
            ReferrerDetails referrerDetails = Util.getSamsungInstallReferrerDetails(context, AdjustFactory.getLogger());
            onSamsungInstallReferrerReadListener.onInstallReferrerRead(referrerDetails, Constants.REFERRER_API_SAMSUNG);
         }catch (Exception exception){
            onSamsungInstallReferrerReadListener.onFail(exception.getMessage());
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
