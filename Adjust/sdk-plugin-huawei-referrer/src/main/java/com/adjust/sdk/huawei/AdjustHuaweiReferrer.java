package com.adjust.sdk.huawei;

import android.content.Context;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.ReferrerDetails;
import com.adjust.sdk.scheduler.AsyncTaskExecutor;

public class AdjustHuaweiReferrer {

   static boolean shouldReadHuaweiReferrer = true;

   /**
    * Called to get Huawei Install Referrer.
    *
    * @param context Application context
    * @param onHuaweiInstallReferrerReadListener Callback to obtain install referrer.
    */
   public static void getHuaweiInstallReferrer(final Context context, final OnHuaweiInstallReferrerReadListener onHuaweiInstallReferrerReadListener) {
      if (onHuaweiInstallReferrerReadListener == null){
         AdjustFactory.getLogger().error("onHuaweiInstallReferrerReadListener can not be null");
         return;
      }
      new AsyncTaskExecutor<Context, ReferrerDetails>() {
         @Override
         protected ReferrerDetails doInBackground(Context[] contexts) {
            ReferrerDetails referrerDetails = null;
            try {
               referrerDetails = Util.getHuaweiAppGalleryInstallReferrerDetails(context, AdjustFactory.getLogger());
            } catch (Exception exception) {
               if (onHuaweiInstallReferrerReadListener == null) {
                  AdjustFactory.getLogger().error("onHuaweiInstallReferrerReadListener can not be null");
                  return null;
               }
               onHuaweiInstallReferrerReadListener.onFail(exception.getMessage());
            }
            return referrerDetails;
         }

         @Override
         protected void onPostExecute(ReferrerDetails referrerDetails) {
            if (referrerDetails == null) {
               return;
            }
            onHuaweiInstallReferrerReadListener.onHuaweiAppGalleryInstallReferrerDetailsRead(new HuaweiInstallReferrerDetails(referrerDetails));
         }
      }.execute(context);
      new AsyncTaskExecutor<Context, ReferrerDetails>() {
         @Override
         protected ReferrerDetails doInBackground(Context[] contexts) {
            ReferrerDetails referrerDetails = null;
            try {
               referrerDetails = Util.getHuaweiAdsInstallReferrerDetails(context, AdjustFactory.getLogger());
            } catch (Exception exception) {
               if (onHuaweiInstallReferrerReadListener == null) {
                  AdjustFactory.getLogger().error("onHuaweiInstallReferrerReadListener can not be null");
                  return null;
               }
               onHuaweiInstallReferrerReadListener.onFail(exception.getMessage());
            }
            return referrerDetails;
         }

         @Override
         protected void onPostExecute(ReferrerDetails referrerDetails) {
            if (onHuaweiInstallReferrerReadListener == null) {
               AdjustFactory.getLogger().error("onHuaweiInstallReferrerReadListener can not be null");
               return;
            }
            onHuaweiInstallReferrerReadListener.onHuaweiAdsInstallReferrerDetailsRead(new HuaweiInstallReferrerDetails(referrerDetails));
         }
      }.execute(context);
   }

   public static void readHuaweiReferrer(Context context) {
      shouldReadHuaweiReferrer = true;
   }

   public static void doNotReadHuaweiReferrer() {
      shouldReadHuaweiReferrer = false;
   }
}
