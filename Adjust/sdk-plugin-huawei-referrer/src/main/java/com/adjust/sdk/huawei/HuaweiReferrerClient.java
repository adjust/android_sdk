package com.adjust.sdk.huawei;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.ReferrerDetails;
import com.adjust.sdk.Util;

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

    public static HuaweiInstallReferrerResult getHuaweiAdsInstallReferrer(Context context, final ILogger logger) {

        String errorMessage = null;
        if (!shouldTryToReadHuaweiAdsReferrer.get()) {
            errorMessage = "Should not try to read HuaweiAdsInstallReferrer";
            logger.info(errorMessage);
            return new HuaweiInstallReferrerResult(errorMessage);
        }

        if (!resolveContentProvider(context, REFERRER_PROVIDER_AUTHORITY)) {
            errorMessage = "HuaweiAdsInstallReferrer fail to resolve content provider";
            return new HuaweiInstallReferrerResult(errorMessage);
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
                    HuaweiInstallReferrerDetails huaweiInstallReferrerDetails = new HuaweiInstallReferrerDetails(referrerHuaweiAds,
                            referrerClickTimestampSeconds,
                            installBeginTimestampSeconds);
                    return new HuaweiInstallReferrerResult(huaweiInstallReferrerDetails);
                }

            } else {
                errorMessage = Util.formatString("HuaweiAdsInstallReferrer fail to read referrer for " +
                                "package [%s] and content uri [%s]",
                        context.getPackageName(), uri.toString());
                logger.debug(errorMessage);
            }

            shouldTryToReadHuaweiAdsReferrer.set(false);

        } catch (Exception e) {
            errorMessage = "HuaweiAdsInstallReferrer error [" + e.getMessage() + "]";
            logger.debug(errorMessage);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return new HuaweiInstallReferrerResult(errorMessage);
    }

    public static HuaweiInstallReferrerResult getHuaweiAppGalleryInstallReferrer(Context context, final ILogger logger) {
        String errorMessage = null;
        if (!shouldTryToReadHuaweiAppGalleryReferrer.get()) {
            errorMessage = "Should not try to read HuaweiAppGalleryInstallReferrer";
            logger.debug(errorMessage);
            return new HuaweiInstallReferrerResult(errorMessage);
        }

        if (!resolveContentProvider(context, REFERRER_PROVIDER_AUTHORITY)) {
            errorMessage = "HuaweiAppGalleryInstallReferrer fail to resolve content provider";
            return new HuaweiInstallReferrerResult(errorMessage);
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
                    HuaweiInstallReferrerDetails huaweiInstallReferrerDetails = new HuaweiInstallReferrerDetails(referrerHuaweiAppGallery,
                            referrerClickTimestampSeconds,
                            installBeginTimestampSeconds);
                    return new HuaweiInstallReferrerResult(huaweiInstallReferrerDetails);
                }

            } else {
                errorMessage = Util.formatString(
                        "HuaweiAppGalleryInstallReferrer fail to read referrer for " +
                                "package [%s] and content uri [%s]",
                        context.getPackageName(), uri.toString());
                logger.debug(errorMessage);
            }

            shouldTryToReadHuaweiAppGalleryReferrer.set(false);

        } catch (Exception e) {
            errorMessage = "HuaweiAppGalleryInstallReferrer error [" + e.getMessage() + "]";
            logger.debug(errorMessage);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return new HuaweiInstallReferrerResult(errorMessage);
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
