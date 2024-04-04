package com.adjust.sdk.huawei;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.ReferrerDetails;

import java.util.concurrent.atomic.AtomicBoolean;

public class HuaweiReferrerClient {

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
     * Boolean indicating whether to read HuaweiAdsReferrer.
     * Either because it has not yet tried,
     *  or it did and it was successful
     *  or it did, was not successful, but it should not retry
     */
    private static final AtomicBoolean shouldTryToReadHuaweiAdsReferrer = new AtomicBoolean(true);

    /**
     * Boolean indicating whether to read HuaweiAdsReferrer.
     * Either because it has not yet tried,
     *  or it did and it was successful
     *  or it did, was not successful, but it should not retry
     */
    private static final AtomicBoolean shouldTryToReadHuaweiAppGalleryReferrer = new AtomicBoolean(true);

    public static ReferrerDetails getHuaweiAdsInstallReferrer(Context context, final ILogger logger) {

        if (!shouldTryToReadHuaweiAdsReferrer.get()) {
            logger.debug("Should not try to read HuaweiAdsInstallReferrer");
            return null;
        }

        if (!resolveContentProvider(context, REFERRER_PROVIDER_AUTHORITY)) {
            return null;
        }

        Cursor cursor = null;

        try {
            Uri uri = Uri.parse(REFERRER_PROVIDER_URI);
            ContentResolver contentResolver = context.getContentResolver();
            String[] packageName = new String[] { context.getPackageName() };
            cursor = contentResolver.query(uri, null, null, packageName, null);

            if (cursor != null && cursor.moveToFirst()) {

                String referrerHuaweiAds = cursor.getString(COLUMN_INDEX_REFERRER);

                logger.debug("HuaweiAdsInstallReferrer index_referrer[%s]", referrerHuaweiAds);

                String clickTime = cursor.getString(COLUMN_INDEX_CLICK_TIME);
                String installTime = cursor.getString(COLUMN_INDEX_INSTALL_TIME);

                logger.debug("HuaweiAdsInstallReferrer " +
                        "clickTime[%s] installTime[%s]", clickTime, installTime );

                long referrerClickTimestampSeconds = Long.parseLong(clickTime);
                long installBeginTimestampSeconds = Long.parseLong(installTime);

                if (isValidHuaweiAdsInstallReferrer(referrerHuaweiAds)) {
                    return new ReferrerDetails(referrerHuaweiAds,
                                    referrerClickTimestampSeconds,
                                    installBeginTimestampSeconds);
                }

            } else {
                logger.debug("HuaweiAdsInstallReferrer fail to read referrer for " +
                                "package [%s] and content uri [%s]",
                        context.getPackageName(), uri.toString());
            }

            shouldTryToReadHuaweiAdsReferrer.set(false);

        } catch (Exception e) {
            logger.debug("HuaweiAdsInstallReferrer error [%s]", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    public static ReferrerDetails getHuaweiAppGalleryInstallReferrer(Context context, final ILogger logger) {
        if (!shouldTryToReadHuaweiAppGalleryReferrer.get()) {
            logger.debug("Should not try to read HuaweiAppGalleryInstallReferrer");
            return null;
        }

        if (!resolveContentProvider(context, REFERRER_PROVIDER_AUTHORITY)) {
            return null;
        }

        Cursor cursor = null;

        try {
            Uri uri = Uri.parse(REFERRER_PROVIDER_URI);
            ContentResolver contentResolver = context.getContentResolver();
            String[] packageName = new String[] { context.getPackageName() };
            cursor = contentResolver.query(uri, null, null, packageName, null);

            if (cursor != null && cursor.moveToFirst()) {

                String referrerHuaweiAppGallery = cursor.getString(COLUMN_INDEX_TRACK_ID);

                logger.debug("HuaweiAppGalleryInstallReferrer index_track_id[%s]", referrerHuaweiAppGallery);

                String clickTime = cursor.getString(COLUMN_INDEX_CLICK_TIME);
                String installTime = cursor.getString(COLUMN_INDEX_INSTALL_TIME);

                logger.debug("HuaweiAppGalleryInstallReferrer " +
                        "clickTime[%s] installTime[%s]", clickTime, installTime );

                long referrerClickTimestampSeconds = Long.parseLong(clickTime);
                long installBeginTimestampSeconds = Long.parseLong(installTime);

                if (isValidHuaweiAppGalleryInstallReferrer(referrerHuaweiAppGallery)) {
                    return new ReferrerDetails(referrerHuaweiAppGallery,
                                    referrerClickTimestampSeconds,
                                    installBeginTimestampSeconds);
                }

            } else {
                logger.debug("HuaweiAppGalleryInstallReferrer fail to read referrer for " +
                                "package [%s] and content uri [%s]",
                        context.getPackageName(), uri.toString());
            }

            shouldTryToReadHuaweiAppGalleryReferrer.set(false);

        } catch (Exception e) {
            logger.debug("HuaweiAppGalleryInstallReferrer error [%s]", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
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

    private static boolean isValidHuaweiAdsInstallReferrer(String referrerHuaweiAds) {
        if (referrerHuaweiAds == null) {
            return false;
        }

        if (referrerHuaweiAds.isEmpty()) {
            return false;
        }

        return true;
    }

    private static boolean isValidHuaweiAppGalleryInstallReferrer(String referrerHuaweiAppGallery) {
        if (referrerHuaweiAppGallery == null) {
            return false;
        }

        if (referrerHuaweiAppGallery.isEmpty()) {
            return false;
        }

        return true;
    }
}
