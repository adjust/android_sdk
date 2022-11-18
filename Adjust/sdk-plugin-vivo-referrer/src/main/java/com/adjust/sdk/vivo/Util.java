package com.adjust.sdk.vivo;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.ReferrerDetails;

public class Util {
   public synchronized static ReferrerDetails getVivoInstallReferrerDetails(Context context, ILogger logger) {
      if (!AdjustVivoReferrer.shouldReadVivoReferrer) {
         return null;
      }

      return VivoReferrerClient.getReferrer(context, logger);
   }
}
