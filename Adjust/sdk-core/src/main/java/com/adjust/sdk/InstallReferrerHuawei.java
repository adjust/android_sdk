package com.adjust.sdk;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.concurrent.atomic.AtomicBoolean;

public class InstallReferrerHuawei {

    /**
     * Huawei install referrer provider content authority.
     */
    private static final String REFERRER_PROVIDER_AUTHORITY = "com.huawei.appmarket.commondata";

    /**
     * Huawei install referrer provider content uri.
     */
    private static final String REFERRER_PROVIDER_URI = "content://" + REFERRER_PROVIDER_AUTHORITY + "/item/5";

    /**
     * Huawei install referrer provider column index referrer.
     */
    private static final int COLUMN_INDEX_REFERRER = 0;

    /**
     * Huawei install referrer provider column index click time.
     */
    private static final int COLUMN_INDEX_CLICK_TIME = 1;

    /**
     * Huawei install referrer provider column index install time.
     */
    private static final int COLUMN_INDEX_INSTALL_TIME = 2;

    /**
     * Huawei install referrer provider column index track ID.
     */
    private static final int COLUMN_INDEX_TRACK_ID = 4;

    /**
     * Adjust logger instance.
     */
    private ILogger logger;

    /**
     * Application context.
     */
    private Context context;

    /**
     * Huawei Referrer callback.
     */
    private final InstallReferrerReadListener referrerCallback;

    /**
     * Boolean indicating whether service should be tried to read.
     * Either because it has not yet tried,
     *  or it did and it was successful
     *  or it did, was not successful, but it should not retry
     */
    private final AtomicBoolean shouldTryToRead;

    /**
     * Default constructor.
     *
     * @param context         Application context
     * @param referrerCallback Callback for referrer information
     */
    public InstallReferrerHuawei(final Context context, final InstallReferrerReadListener referrerCallback) {
        this.logger = AdjustFactory.getLogger();
        this.context = context;
        this.referrerCallback = referrerCallback;
        this.shouldTryToRead = new AtomicBoolean(true);
    }

    public void readReferrer() {
        if (!shouldTryToRead.get()) {
            logger.debug("Should not try to read Install referrer Huawei");
            return;
        }

        if (!Util.resolveContentProvider(context, REFERRER_PROVIDER_AUTHORITY)) {
            return;
        }

        Cursor cursor = null;
        Uri uri = Uri.parse(REFERRER_PROVIDER_URI);
        ContentResolver contentResolver = context.getContentResolver();

        String packageName[] = new String[] { context.getPackageName() };
        try {
            cursor = contentResolver.query(uri, null, null, packageName, null);

            if (cursor != null && cursor.moveToFirst()) {

                String installReferrer = cursor.getString(COLUMN_INDEX_REFERRER);
                String clickTime = cursor.getString(COLUMN_INDEX_CLICK_TIME);
                String installTime = cursor.getString(COLUMN_INDEX_INSTALL_TIME);
                String referrerApi = Constants.REFERRER_API_HUAWEI;

                if (installReferrer == null || installReferrer.isEmpty()) {
                    installReferrer = cursor.getString(COLUMN_INDEX_TRACK_ID);
                    referrerApi = Constants.REFERRER_API_HUAWEI_ADS;
                }

                logger.debug("InstallReferrerHuawei reads referrer[%s] clickTime[%s] installTime[%s]", installReferrer, clickTime, installTime );

                long referrerClickTimestampSeconds = Long.parseLong(clickTime);
                long installBeginTimestampSeconds = Long.parseLong(installTime);

                ReferrerDetails referrerDetails = new ReferrerDetails(installReferrer,
                        referrerClickTimestampSeconds, installBeginTimestampSeconds);

                referrerCallback.onInstallReferrerRead(referrerDetails, referrerApi);

            } else {
                logger.debug("InstallReferrerHuawei fail to read referrer for package [%s] and content uri [%s]", context.getPackageName(), uri.toString());
            }
        } catch (Exception e) {
                logger.debug("InstallReferrerHuawei error [%s]", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        shouldTryToRead.set(false);
    }

}
