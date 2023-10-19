package com.adjust.sdk;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class InstallReferrerMeta {
    /**
     * Facebook install referrer provider content authority.
     */
    private static final String FACEBOOK_REFERRER_PROVIDER_AUTHORITY = "com.facebook.katana.provider.InstallReferrerProvider";

    /**
     * Instagram install referrer provider content authority.
     */
    private static final String INSTAGRAM_REFERRER_PROVIDER_AUTHORITY = "com.instagram.contentprovider.InstallReferrerProvider";

    /**
     * Meta referrer content provider install referrer column name.
     */
    private static final String COLUMN_INSTALL_REFERRER = "install_referrer";

    /**
     * Meta referrer content provider actual timestamp column name.
     */
    private static final String COLUMN_ACTUAL_TIMESTAMP = "actual_timestamp";

    /**
     * Meta referrer content provider is click or impression column name.
     */
    private static final String COLUMN_IS_CT = "is_ct";

    /**
     * Adjust logger instance.
     */
    private ILogger logger;

    /**
     * Application context.
     */
    private Context context;

    /**
     * FB app ID.
     */
    private String fbAppId;

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
    public InstallReferrerMeta(final Context context,
                               final String fbAppId,
                               final InstallReferrerReadListener referrerCallback)
    {
        this.logger = AdjustFactory.getLogger();
        this.context = context;
        this.fbAppId = fbAppId;
        this.referrerCallback = referrerCallback;
        this.shouldTryToRead = new AtomicBoolean(true);
    }

    public void readReferrer() {
        if (!shouldTryToRead.get()) {
            logger.debug("Should not retry to read Install referrer Meta");
            return;
        }

        if (TextUtils.isEmpty(fbAppId)) {
            logger.debug("Can't read Install referrer Meta with null or empty FB app ID");
            return;
        }

        Cursor cursor = null;

        try {
            Uri providerUri = null;

            if (Util.resolveContentProvider(context, FACEBOOK_REFERRER_PROVIDER_AUTHORITY)) {
                providerUri = Uri.parse("content://" + FACEBOOK_REFERRER_PROVIDER_AUTHORITY + "/" + fbAppId);
            } else if (Util.resolveContentProvider(context, INSTAGRAM_REFERRER_PROVIDER_AUTHORITY)) {
                providerUri = Uri.parse("content://" + INSTAGRAM_REFERRER_PROVIDER_AUTHORITY + "/" + fbAppId);
            } else {
                return;
            }

            ContentResolver contentResolver = context.getContentResolver();

            String[] projection = {COLUMN_INSTALL_REFERRER, COLUMN_IS_CT, COLUMN_ACTUAL_TIMESTAMP};

            cursor = contentResolver.query(providerUri, projection, null, null, null);

            if (cursor == null || !cursor.moveToFirst()) {
                return;
            }

            int installReferrerIndex = cursor.getColumnIndex(COLUMN_INSTALL_REFERRER);
            int timestampIndex = cursor.getColumnIndex(COLUMN_ACTUAL_TIMESTAMP);
            int isCTIndex = cursor.getColumnIndex(COLUMN_IS_CT);
            String installReferrer = cursor.getString(installReferrerIndex);
            long actualTimestampInSec = cursor.getLong(timestampIndex); // in seconds
            int ctValue = cursor.getInt(isCTIndex); // 0 = VT, 1 = CT
            boolean isClick = ctValue == 1;

            logger.debug("InstallReferrerMeta reads " +
                            "installReferrer[%s] actualTimestampInSec[%d] isClick[%b]",
                    installReferrer, actualTimestampInSec, ctValue);

            if (isValidReferrer(installReferrer)) {
                ReferrerDetails referrerDetails =
                        new ReferrerDetails(installReferrer,
                                actualTimestampInSec,
                                isClick);

                referrerCallback.onInstallReferrerRead(referrerDetails,
                        Constants.REFERRER_API_META);
            } else {
                logger.debug("InstallReferrerMeta invalid installReferrer");
            }

        } catch (Exception e) {
            logger.debug("InstallReferrerMeta error [%s]", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        shouldTryToRead.set(false);
    }

    private boolean isValidReferrer(String installReferrer) {
        if (installReferrer == null) {
            return false;
        }

        if (installReferrer.isEmpty()) {
            return false;
        }

        return true;
    }

}