package com.adjust.sdk.xiaomi;

import android.content.Context;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.scheduler.AsyncTaskExecutor;

public class AdjustXiaomiReferrer {

   static boolean shouldReadXiaomiReferrer = true;

   public static void readXiaomiReferrer(Context context) {
      shouldReadXiaomiReferrer = true;
   }

   public static void doNotReadXiaomiReferrer() {
      shouldReadXiaomiReferrer = false;
   }

   /**
    * Read Xiaomi Install Referrer
    * @param context Application context
    * @param onXiaomiInstallReferrerReadListener Callback to obtain install referrer.
    */
   public static void getXiaomiInstallReferrer(final Context context, final OnXiaomiInstallReferrerReadListener onXiaomiInstallReferrerReadListener) {

      if (onXiaomiInstallReferrerReadListener == null){
         AdjustFactory.getLogger().error("onXiaomiInstallReferrerReadListener can not be null");
         return;
      }

      new AsyncTaskExecutor<Context, XiaomiInstallReferrerResult>() {
         @Override
         protected XiaomiInstallReferrerResult doInBackground(Context[] contexts) {
            try {
               return XiaomiReferrerClient.getReferrer(context, AdjustFactory.getLogger(),2000);
            } catch (Exception exception) {
               return new XiaomiInstallReferrerResult(exception.getMessage());
            }
         }

         @Override
         protected void onPostExecute(XiaomiInstallReferrerResult xiaomiInstallReferrerResult) {
            if (xiaomiInstallReferrerResult != null) {
               if (xiaomiInstallReferrerResult.xiaomiInstallReferrerDetails != null) {
                  onXiaomiInstallReferrerReadListener.onXiaomiInstallReferrerRead(xiaomiInstallReferrerResult.xiaomiInstallReferrerDetails);
               } else if (xiaomiInstallReferrerResult.error != null) {
                  onXiaomiInstallReferrerReadListener.onFail(xiaomiInstallReferrerResult.error);
               }else {
                  onXiaomiInstallReferrerReadListener.onFail("XiaomiReferrer getInstallReferrer: xiaomiInstallReferrerDetails is null");
               }
            }else {
               onXiaomiInstallReferrerReadListener.onFail("XiaomiReferrer getInstallReferrer: xiaomiInstallReferrerResult is null");
            }
         }
      }.execute(context);
   }

}
