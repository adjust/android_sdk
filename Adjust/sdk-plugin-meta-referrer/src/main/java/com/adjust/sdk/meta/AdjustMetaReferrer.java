package com.adjust.sdk.meta;

import android.content.Context;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.scheduler.AsyncTaskExecutor;

public class AdjustMetaReferrer {

   static boolean shouldReadMetaReferrer = true;

   /**
    * Called to get Meta Install Referrer.
    *
    * @param context Application context
    * @param fbAppId FB App Id
    * @param onMetaInstallReferrerReadListener Callback to obtain install referrer.
    */
   public static void getMetaInstallReferrer(final Context context, final String fbAppId,
                                             final OnMetaInstallReferrerReadListener onMetaInstallReferrerReadListener) {
      if (onMetaInstallReferrerReadListener == null){
         AdjustFactory.getLogger().error("onMetaInstallReferrerReadListener can not be null");
         return;
      }
      new AsyncTaskExecutor<Context, MetaInstallReferrerResult>() {
         @Override
         protected MetaInstallReferrerResult doInBackground(Context[] contexts) {
            try {
               return MetaReferrerClient.getMetaInstallReferrer(context, fbAppId, AdjustFactory.getLogger(), false);
            } catch (Exception exception) {
               return new MetaInstallReferrerResult(exception.getMessage());
            }
         }

         @Override
         protected void onPostExecute(MetaInstallReferrerResult metaInstallReferrerResult) {
            if (metaInstallReferrerResult != null) {
               if (metaInstallReferrerResult.metaInstallReferrerDetails != null) {
                  onMetaInstallReferrerReadListener.onInstallReferrerDetailsRead(metaInstallReferrerResult.metaInstallReferrerDetails);
               } else if (metaInstallReferrerResult.error != null) {
                  onMetaInstallReferrerReadListener.onFail(metaInstallReferrerResult.error);
               } else {
                  onMetaInstallReferrerReadListener.onFail("Meta Install Referrer details null");
               }
            } else {
               onMetaInstallReferrerReadListener.onFail("Meta Install Referrer details null");
            }
         }
      }.execute(context);
   }

   public static void readMetaReferrer(Context context) {
      shouldReadMetaReferrer = true;
   }

   public static void doNotReadMetaReferrer() {
      shouldReadMetaReferrer = false;
   }
}
