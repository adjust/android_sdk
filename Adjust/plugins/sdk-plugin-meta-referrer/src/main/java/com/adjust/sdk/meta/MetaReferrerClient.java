package com.adjust.sdk.meta;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.Util;

import java.util.concurrent.atomic.AtomicBoolean;

public class MetaReferrerClient {

    /**
     * Facebook install referrer provider content authority.
     */
    private static final String FACEBOOK_REFERRER_PROVIDER_AUTHORITY = "com.facebook.katana.provider.InstallReferrerProvider";

    /**
     * Instagram install referrer provider content authority.
     */
    private static final String INSTAGRAM_REFERRER_PROVIDER_AUTHORITY = "com.instagram.contentprovider.InstallReferrerProvider";

    /**
     * FBLite install referrer provider content authority.
     */
    private static final String FBLITE_REFERRER_PROVIDER_AUTHORITY = "com.facebook.lite.provider.InstallReferrerProvider";

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
     * Boolean indicating whether service should be tried to read.
     * Either because it has not yet tried,
     *  or it did and it was successful
     *  or it did, was not successful, but it should not retry
     */
    private static final AtomicBoolean shouldTryToRead = new AtomicBoolean(true);

    public static MetaInstallReferrerResult getMetaInstallReferrer(
            final Context context, final String fbAppId, final ILogger logger, final boolean shouldAvoidFrequentRead) {
        String errorMessage = null;

        if (shouldAvoidFrequentRead) {
            if (!shouldTryToRead.get()) {
                errorMessage = "Shouldn't try to read Meta Install referrer";
                logger.debug(errorMessage);
                return new MetaInstallReferrerResult(errorMessage);
            }
        }

        if (TextUtils.isEmpty(fbAppId)) {
            errorMessage = "Can't read Meta Install referrer with null or empty FBAppId";
            logger.debug(errorMessage);
            return new MetaInstallReferrerResult(errorMessage);
        }

        Cursor cursor = null;

        try {
            Uri providerUri = null;

            if (resolveContentProvider(context, FACEBOOK_REFERRER_PROVIDER_AUTHORITY)) {
                providerUri = Uri.parse("content://" + FACEBOOK_REFERRER_PROVIDER_AUTHORITY + "/" + fbAppId);
            } else if (resolveContentProvider(context, INSTAGRAM_REFERRER_PROVIDER_AUTHORITY)) {
                providerUri = Uri.parse("content://" + INSTAGRAM_REFERRER_PROVIDER_AUTHORITY + "/" + fbAppId);
            } else if (resolveContentProvider(context, FBLITE_REFERRER_PROVIDER_AUTHORITY)) {
                providerUri = Uri.parse("content://" + FBLITE_REFERRER_PROVIDER_AUTHORITY + "/" + fbAppId);
            } else {
                errorMessage = "Failed to find Meta Install Referrer content provider";
                logger.debug(errorMessage);
                return new MetaInstallReferrerResult(errorMessage);
            }

            ContentResolver contentResolver = context.getContentResolver();

            String[] projection = {COLUMN_INSTALL_REFERRER, COLUMN_IS_CT, COLUMN_ACTUAL_TIMESTAMP};

            cursor = contentResolver.query(providerUri, projection, null, null, null);

            if (cursor == null || !cursor.moveToFirst()) {
                errorMessage = Util.formatString("Fail to read Meta Install Referrer for FB AppId [%s]", fbAppId);
                logger.debug(errorMessage);
                return new MetaInstallReferrerResult(errorMessage);
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
                    installReferrer, actualTimestampInSec, isClick);

            if (isValidReferrer(installReferrer)) {
                shouldTryToRead.set(false);

                MetaInstallReferrerDetails metaInstallReferrerDetails =
                        new MetaInstallReferrerDetails(installReferrer, actualTimestampInSec, isClick);

                return new MetaInstallReferrerResult(metaInstallReferrerDetails);
            } else {
                errorMessage = "Invalid Meta Install Referrer";
                logger.debug(errorMessage);
            }

        } catch (Exception e) {
            errorMessage = "Meta Install Referrer error " +  e.getMessage();
            logger.debug(errorMessage);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return new MetaInstallReferrerResult(errorMessage);
    }

    private static boolean resolveContentProvider(final Context applicationContext,
                                                 final String authority) {
        try {
            return (applicationContext.getPackageManager()
                    .resolveContentProvider(authority, 0) != null);

        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isValidReferrer(String referrer) {
        if (referrer == null) {
            return false;
        }

        if (referrer.isEmpty()) {
            return false;
        }

        return true;
    }
}
