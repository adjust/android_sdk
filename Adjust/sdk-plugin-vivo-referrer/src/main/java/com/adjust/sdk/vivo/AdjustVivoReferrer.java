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
      if (onVivoInstallReferrerReadListener == null){
         AdjustFactory.getLogger().error("onVivoInstallReferrerReadListener can not be null");
         return;
      }

      new AsyncTaskExecutor<Context, VivoInstallReferrerResult>() {
         @Override
         protected VivoInstallReferrerResult doInBackground(Context[] contexts) {
            try {
               return VivoReferrerClient.getReferrer(context, AdjustFactory.getLogger());
            } catch (Exception exception) {
               return new VivoInstallReferrerResult(exception.getMessage());
            }
         }

         @Override
         protected void onPostExecute(VivoInstallReferrerResult vivoInstallReferrerResult) {
            if (vivoInstallReferrerResult != null) {
               if (vivoInstallReferrerResult.vivoInstallReferrerDetails != null) {
                  onVivoInstallReferrerReadListener.onVivoInstallReferrerRead(vivoInstallReferrerResult.vivoInstallReferrerDetails);
               } else if (vivoInstallReferrerResult.error != null) {
                  onVivoInstallReferrerReadListener.onFail(vivoInstallReferrerResult.error);
               }
            }
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
