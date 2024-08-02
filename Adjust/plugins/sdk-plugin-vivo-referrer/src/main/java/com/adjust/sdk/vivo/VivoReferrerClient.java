package com.adjust.sdk.vivo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.ReferrerDetails;

public class VivoReferrerClient {
    public static VivoInstallReferrerResult getReferrer(Context context, final ILogger logger) {

        try {
            Uri url = Uri.parse("content://com.vivo.appstore.provider.referrer");
            Bundle resultBundle = context.getContentResolver().call(url, "read_referrer", null, null);
            if (resultBundle == null) {
                return new VivoInstallReferrerResult("VivoReferrer read error: resultBundle null");
            }

            String installReferrer = resultBundle.getString("install_referrer");
            if (TextUtils.isEmpty(installReferrer)) {
                return new VivoInstallReferrerResult("VivoReferrer read error: referrer string null");
            }

            long clickTime = resultBundle.getLong("referrer_click_timestamp_seconds");
            long installBeginTime = resultBundle.getLong("download_begin_timestamp_seconds");
            String installVersion = resultBundle.getString("install_version");

            VivoInstallReferrerDetails vivoInstallReferrerDetails = new VivoInstallReferrerDetails(
                    installReferrer, clickTime, installBeginTime, installVersion);
            return new VivoInstallReferrerResult(vivoInstallReferrerDetails);

        } catch (Exception e) {
            String error = "VivoReferrer read error: " + e.getMessage();
            logger.info(error);
            return new VivoInstallReferrerResult(error);
        }
    }
}
