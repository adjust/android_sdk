package com.adjust.sdk.samsung;

import android.content.Context;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.ReferrerDetails;
import com.adjust.sdk.scheduler.AsyncTaskExecutor;


public class AdjustSamsungReferrer {

   static boolean shouldReadSamsungReferrer = true;

   /**
    * Called to get Samsung Install Referrer.
    *
    * @param context Application context
    * @param onSamsungInstallReferrerReadListener Callback to obtain install referrer.
    */
   public static void getSamsungInstallReferrer(final Context context, final OnSamsungInstallReferrerReadListener onSamsungInstallReferrerReadListener) {

       if (onSamsungInstallReferrerReadListener == null){
           AdjustFactory.getLogger().error("onSamsungInstallReferrerReadListener can not be null");
           return;
       }

       new AsyncTaskExecutor<Context, SamsungInstallReferrerResult>() {
           @Override
           protected SamsungInstallReferrerResult doInBackground(Context[] contexts) {
               try {
                   return SamsungReferrerClient.getReferrer(context, AdjustFactory.getLogger(),2000);
               } catch (Exception exception) {
                   return new SamsungInstallReferrerResult(exception.getMessage());
               }
           }

           @Override
           protected void onPostExecute(SamsungInstallReferrerResult samsungInstallReferrerResult) {
               if (samsungInstallReferrerResult != null) {
                   if (samsungInstallReferrerResult.samsungInstallReferrerDetails != null) {
                       onSamsungInstallReferrerReadListener.onSamsungInstallReferrerRead(samsungInstallReferrerResult.samsungInstallReferrerDetails);
                   } else if (samsungInstallReferrerResult.error != null) {
                       onSamsungInstallReferrerReadListener.onFail(samsungInstallReferrerResult.error);
                   }
               }else {
                   onSamsungInstallReferrerReadListener.onFail("SamsungReferrer getInstallReferrer: samsungInstallReferrerResult is null");
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
