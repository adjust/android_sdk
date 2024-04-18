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
      new AsyncTaskExecutor<Context, HuaweiInstallReferrerResult>() {
         @Override
         protected HuaweiInstallReferrerResult doInBackground(Context[] contexts) {
            try {
               return HuaweiReferrerClient.getHuaweiAppGalleryInstallReferrer(context, AdjustFactory.getLogger());
            } catch (Exception exception) {
               return new HuaweiInstallReferrerResult(exception.getMessage());
            }
         }

         @Override
         protected void onPostExecute(HuaweiInstallReferrerResult huaweiInstallReferrerResult) {
            if (huaweiInstallReferrerResult != null) {
               if (huaweiInstallReferrerResult.huaweiInstallReferrerDetails != null) {
                  onHuaweiInstallReferrerReadListener.onHuaweiAppGalleryInstallReferrerDetailsRead(huaweiInstallReferrerResult.huaweiInstallReferrerDetails);
               } else if (huaweiInstallReferrerResult.error != null) {
                  onHuaweiInstallReferrerReadListener.onFail(huaweiInstallReferrerResult.error);
               }else {
                  onHuaweiInstallReferrerReadListener.onFail("HuaweiReferrer getInstallReferrer: huaweiInstallReferrerDetails is null");
               }
            }else {
               onHuaweiInstallReferrerReadListener.onFail("HuaweiReferrer getInstallReferrer: huaweiInstallReferrerResult is null");
            }
         }
      }.execute(context);

      new AsyncTaskExecutor<Context, HuaweiInstallReferrerResult>() {
         @Override
         protected HuaweiInstallReferrerResult doInBackground(Context[] contexts) {
            try {
               return HuaweiReferrerClient.getHuaweiAdsInstallReferrer(context, AdjustFactory.getLogger());
            } catch (Exception exception) {
               return new HuaweiInstallReferrerResult(exception.getMessage());
            }
         }
         @Override
         protected void onPostExecute(HuaweiInstallReferrerResult huaweiInstallReferrerResult) {
            if (huaweiInstallReferrerResult != null) {
               if (huaweiInstallReferrerResult.huaweiInstallReferrerDetails != null) {
                  onHuaweiInstallReferrerReadListener.onHuaweiAdsInstallReferrerDetailsRead(huaweiInstallReferrerResult.huaweiInstallReferrerDetails);
               } else if (huaweiInstallReferrerResult.error != null) {
                  onHuaweiInstallReferrerReadListener.onFail(huaweiInstallReferrerResult.error);
               }else {
                  onHuaweiInstallReferrerReadListener.onFail("HuaweiReferrer getInstallReferrer: huaweiInstallReferrerDetails is null");
               }
            }else {
               onHuaweiInstallReferrerReadListener.onFail("HuaweiReferrer getInstallReferrer: huaweiInstallReferrerResult is null");
            }
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
