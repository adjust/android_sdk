package com.adjust.sdk;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.concurrent.TimeUnit;

public class ReferrerProvider {

    /**
     * Huawei install referrer provider content uri.
     */
    private static final String REFERRER_PROVIDER_URI = "content://com.huawei.appmarket.commondata/item/5";

    /**
     * Adjust logger instance.
     */
    private ILogger logger;

    /**
     * Application context.
     */
    private Context context;

    /**
     * Weak reference to ActivityHandler instance.
     */
    private final InstallReferrerReadListener referrerCallback;

    /**
     * Default constructor.
     *
     * @param context         Application context
     * @param referrerCallback Callback for referrer information
     */
    public ReferrerProvider(final Context context, final InstallReferrerReadListener referrerCallback) {
        this.logger = AdjustFactory.getLogger();
        this.context = context;
        this.referrerCallback = referrerCallback;

        readReferrer();
    }

    private void readReferrer() {
        Cursor cursor = null;
        Uri uri = Uri.parse(REFERRER_PROVIDER_URI);
        ContentResolver contentResolver = context.getContentResolver();

        String packageName[] = new String[] { context.getPackageName() };
        try {
            cursor = contentResolver.query(uri, null, null, packageName, null);
            if (cursor == null) {
                logger.debug("No such content provider for Uri [%s]", uri.toString());
                return;
            }

            if (cursor.moveToFirst()) {

                String installReferrer = cursor.getString(0);
                String clickTime = cursor.getString(1);
                String installTime = cursor.getString(2);

                logger.debug("ReferrerProvider reads referrer[%s] clickTime[%s] installTime[%s]", installReferrer, clickTime, installTime );

                long clickTimeInMilliseconds = Long.parseLong(clickTime);
                long installTimeInMilliseconds = Long.parseLong(installTime);

                long referrerClickTimestampSeconds = TimeUnit.MILLISECONDS.toSeconds(clickTimeInMilliseconds);
                long installBeginTimestampSeconds = TimeUnit.MILLISECONDS.toSeconds(installTimeInMilliseconds);

                referrerCallback.onInstallReferrerRead(installReferrer, referrerClickTimestampSeconds, installBeginTimestampSeconds);

            } else {
                logger.debug("No install referrer info available for package [%s] content uri[%s]", context.getPackageName(), uri.toString());
            }
        } catch (Exception e) {
                logger.debug("Referrer provider error [%s]", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
