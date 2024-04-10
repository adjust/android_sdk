package com.adjust.sdk.huawei;

import static com.adjust.sdk.Constants.REFERRER_API_HUAWEI_ADS;
import static com.adjust.sdk.Constants.REFERRER_API_HUAWEI_APP_GALLERY;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.ReferrerDetails;

public class AdjustHuaweiReferrer {

   static boolean shouldReadHuaweiReferrer = true;

   public static void getHuaweiInstallReferrer(Context context, OnHuaweiInstallReferrerReadListener onHuaweiInstallReferrerReadListener){
      Handler handlerAds = new Handler(Looper.getMainLooper());
      Handler handlerAppGallery = new Handler(Looper.getMainLooper());
      handlerAds.post(new Runnable() {
         @Override
         public void run() {
            try {
               ReferrerDetails referrerDetails = Util.getHuaweiAdsInstallReferrerDetails(context, AdjustFactory.getLogger());
               onHuaweiInstallReferrerReadListener.onHuaweiAdsInstallReferrerDetailsRead(referrerDetails);
            }catch (Exception exception){
               onHuaweiInstallReferrerReadListener.onFailure(exception.getMessage());
            }
         }
      });
      handlerAppGallery.post(new Runnable() {
         @Override
         public void run() {
            try {
               ReferrerDetails referrerDetails = Util.getHuaweiAppGalleryInstallReferrerDetails(context, AdjustFactory.getLogger());
               onHuaweiInstallReferrerReadListener.onHuaweiAppGalleryInstallReferrerDetailsRead(referrerDetails);
            }catch (Exception exception){
               onHuaweiInstallReferrerReadListener.onFailure(exception.getMessage());
            }
         }
      });

   }

   public static void readHuaweiReferrer(Context context) {
      shouldReadHuaweiReferrer = true;
   }

   public static void doNotReadHuaweiReferrer() {
      shouldReadHuaweiReferrer = false;
   }
}
