package com.adjust.sdk.samsung;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.ReferrerDetails;
import com.adjust.sdk.scheduler.AsyncTaskExecutor;


public class AdjustSamsungReferrer {

   static boolean shouldReadSamsungReferrer = true;

   public static void getSamsungInstallReferrer(Context context ,OnSamsungInstallReferrerReadListener onSamsungInstallReferrerReadListener){

      new AsyncTaskExecutor<Context, ReferrerDetails>() {
         @Override
         protected ReferrerDetails doInBackground(Context[] contexts) {
            ReferrerDetails referrerDetails=null;
            try {
                referrerDetails = Util.getSamsungInstallReferrerDetails(context, AdjustFactory.getLogger());
            }catch (Exception exception){
               if (onSamsungInstallReferrerReadListener != null) {
                  onSamsungInstallReferrerReadListener.onFailure(exception.getMessage());
               }
            }
            return referrerDetails;
         }

         @Override
         protected void onPostExecute(ReferrerDetails referrerDetails) {
            if (onSamsungInstallReferrerReadListener != null){
               onSamsungInstallReferrerReadListener.onInstallReferrerRead(referrerDetails);
            }
         }
      }.execute(context);

   }

   public static void readSamsungReferrer(Context context) {
      shouldReadSamsungReferrer = true;
   }

   public static void doNotReadSamsungReferrer() {
      shouldReadSamsungReferrer = false;
   }
}
