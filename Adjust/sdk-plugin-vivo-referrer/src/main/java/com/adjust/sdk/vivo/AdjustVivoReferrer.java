package com.adjust.sdk.vivo;

import android.content.Context;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.ReferrerDetails;
import com.adjust.sdk.scheduler.AsyncTaskExecutor;

public class AdjustVivoReferrer {

   static boolean shouldReadVivoReferrer = true;

   /**
    * Called to get Vivo Install Referrer.
    *
    * @param context Application context
    * @param onVivoInstallReferrerReadListener Callback to obtain install referrer.
    */
   public static void getVivoInstallReferrer(final Context context, final OnVivoInstallReferrerReadListener onVivoInstallReferrerReadListener) {
      new AsyncTaskExecutor<Context, ReferrerDetails>() {
         @Override
         protected ReferrerDetails doInBackground(Context[] contexts) {
            ReferrerDetails referrerDetails = null;
            try {
               referrerDetails = Util.getVivoInstallReferrerDetails(context, AdjustFactory.getLogger());
            } catch (Exception exception) {
               if (onVivoInstallReferrerReadListener == null) {
                  AdjustFactory.getLogger().error("onVivoInstallReferrerReadListener can not be null");
                  return null;
               }
               onVivoInstallReferrerReadListener.onFail(exception.getMessage());
            }
            return referrerDetails;
         }

         @Override
         protected void onPostExecute(ReferrerDetails referrerDetails) {
            if (onVivoInstallReferrerReadListener == null) {
               AdjustFactory.getLogger().error("onVivoInstallReferrerReadListener can not be null");
               return;
            }
            onVivoInstallReferrerReadListener.onVivoInstallReferrerRead(new VivoInstallReferrerDetails(referrerDetails));
         }
      }.execute(context);
   }


   public static void readVivoReferrer(Context context) {
      shouldReadVivoReferrer = true;
   }

   public static void doNotReadVivoReferrer() {
      shouldReadVivoReferrer = false;
   }
}
