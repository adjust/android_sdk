package com.adjust.sdk.meta;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.Util;

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

    public static MetaInstallReferrerResult getMetaInstallReferrer(
            final Context context, final String fbAppId, final ILogger logger) {
        String errorMessage = null;

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
            } else {
                return new MetaInstallReferrerResult("Failed to find Meta Install Referrer content provider");
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
