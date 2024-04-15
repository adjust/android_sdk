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
       new AsyncTaskExecutor<Context, ReferrerDetails>() {
           @Override
           protected ReferrerDetails doInBackground(Context[] contexts) {
               ReferrerDetails referrerDetails = null;
               try {
                   referrerDetails = Util.getSamsungInstallReferrerDetails(context, AdjustFactory.getLogger());
               } catch (Exception exception) {
                   if (onSamsungInstallReferrerReadListener == null) {
                       AdjustFactory.getLogger().error("onSamsungInstallReferrerReadListener can not be null");
                       return null;
                   }
                   onSamsungInstallReferrerReadListener.onFail(exception.getMessage());
               }
               return referrerDetails;
           }

           @Override
           protected void onPostExecute(ReferrerDetails referrerDetails) {
               if (onSamsungInstallReferrerReadListener == null) {
                   AdjustFactory.getLogger().error("onSamsungInstallReferrerReadListener can not be null");
                   return;
               }
               onSamsungInstallReferrerReadListener.onInstallReferrerRead(new SamsungInstallReferrerDetails(referrerDetails));
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
