package com.adjust.sdk.vivo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.ReferrerDetails;

public class VivoReferrerClient {
    public static ReferrerDetails getReferrer(Context context, final ILogger logger) {
        try {
            Uri url = Uri.parse("content://com.vivo.appstore.provider.referrer");
            Bundle resultBundle = context.getContentResolver().call(url, "read_referrer", null, null);
            if (resultBundle == null) {
                return null;
            }

            String installReferrer = resultBundle.getString("install_referrer");
            if (TextUtils.isEmpty(installReferrer)) {
                return null;
            }

            long clickTime = resultBundle.getLong("referrer_click_timestamp_seconds");
            long installBeginTime = resultBundle.getLong("download_begin_timestamp_seconds");
            String installVersion = resultBundle.getString("install_version");

            return new ReferrerDetails(installReferrer, clickTime, installBeginTime, -1, -1, installVersion, null, null);
        } catch (Exception e) {
            logger.info("VivoReferrer read error" + e.getMessage());
        }
        return null;
    }
}
