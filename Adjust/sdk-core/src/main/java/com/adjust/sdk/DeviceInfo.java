package com.adjust.sdk;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static android.content.res.Configuration.UI_MODE_TYPE_MASK;
import static android.content.res.Configuration.UI_MODE_TYPE_TELEVISION;
import static com.adjust.sdk.Constants.HIGH;
import static com.adjust.sdk.Constants.LARGE;
import static com.adjust.sdk.Constants.LONG;
import static com.adjust.sdk.Constants.LOW;
import static com.adjust.sdk.Constants.MEDIUM;
import static com.adjust.sdk.Constants.NORMAL;
import static com.adjust.sdk.Constants.SMALL;
import static com.adjust.sdk.Constants.XLARGE;

/**
 * Created by pfms on 06/11/14.
 */
class DeviceInfo {

    private static final String OFFICIAL_FACEBOOK_SIGNATURE =
            "30820268308201d102044a9c4610300d06092a864886f70d0101040500307a310b3009060355040613" +
                    "025553310b3009060355040813024341311230100603550407130950616c6f20416c746f31" +
                    "183016060355040a130f46616365626f6f6b204d6f62696c653111300f060355040b130846" +
                    "616365626f6f6b311d301b0603550403131446616365626f6f6b20436f72706f726174696f" +
                    "6e3020170d3039303833313231353231365a180f32303530303932353231353231365a307a" +
                    "310b3009060355040613025553310b30090603550408130243413112301006035504071309" +
                    "50616c6f20416c746f31183016060355040a130f46616365626f6f6b204d6f62696c653111" +
                    "300f060355040b130846616365626f6f6b311d301b0603550403131446616365626f6f6b20" +
                    "436f72706f726174696f6e30819f300d06092a864886f70d010101050003818d0030818902" +
                    "818100c207d51df8eb8c97d93ba0c8c1002c928fab00dc1b42fca5e66e99cc3023ed2d214d" +
                    "822bc59e8e35ddcf5f44c7ae8ade50d7e0c434f500e6c131f4a2834f987fc46406115de201" +
                    "8ebbb0d5a3c261bd97581ccfef76afc7135a6d59e8855ecd7eacc8f8737e794c60a761c536" +
                    "b72b11fac8e603f5da1a2d54aa103b8a13c0dbc10203010001300d06092a864886f70d0101" +
                    "040500038181005ee9be8bcbb250648d3b741290a82a1c9dc2e76a0af2f2228f1d9f9c4007" +
                    "529c446a70175c5a900d5141812866db46be6559e2141616483998211f4a673149fb2232a1" +
                    "0d247663b26a9031e15f84bc1c74d141ff98a02d76f85b2c8ab2571b6469b232d8e768a7f7" +
                    "ca04f7abe4a775615916c07940656b58717457b42bd928a2";

    String playAdId;
    String playAdIdSource;
    int playAdIdAttempt = -1;
    Boolean isTrackingEnabled;
    private boolean nonGoogleIdsReadOnce = false;
    private boolean playIdsReadOnce = false;
    private boolean otherDeviceIdsParamsReadOnce = false;
    String androidId;
    String fbAttributionId;
    String clientSdk;
    String packageName;
    String appVersion;
    String deviceType;
    String deviceName;
    String deviceManufacturer;
    String osName;
    String osVersion;
    String apiLevel;
    String language;
    String country;
    String screenSize;
    String screenFormat;
    String screenDensity;
    String displayWidth;
    String displayHeight;
    String hardwareName;
    String abi;
    String buildName;
    String appInstallTime;
    String appUpdateTime;
    int uiMode;
    String appSetId;
    boolean isGooglePlayGamesForPC;

    Map<String, String> imeiParameters;
    Map<String, String> oaidParameters;
    String fireAdId;
    Boolean fireTrackingEnabled;
    int connectivityType;
    String mcc;
    String mnc;

    DeviceInfo(AdjustConfig adjustConfig) {
        Context context = adjustConfig.context;
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        Locale locale = Util.getLocale(configuration);
        PackageInfo packageInfo = getPackageInfo(context);
        int screenLayout = configuration.screenLayout;
        isGooglePlayGamesForPC = Util.isGooglePlayGamesForPC(context);

        packageName = getPackageName(context);
        appVersion = getAppVersion(packageInfo);
        deviceType = getDeviceType(configuration);
        deviceName = getDeviceName();
        deviceManufacturer = getDeviceManufacturer();
        osName = getOsName();
        osVersion = getOsVersion();
        apiLevel = getApiLevel();
        language = getLanguage(locale);
        country = getCountry(locale);
        screenSize = getScreenSize(screenLayout);
        screenFormat = getScreenFormat(screenLayout);
        screenDensity = getScreenDensity(displayMetrics);
        displayWidth = getDisplayWidth(displayMetrics);
        displayHeight = getDisplayHeight(displayMetrics);
        clientSdk = getClientSdk(adjustConfig.sdkPrefix);
        fbAttributionId = getFacebookAttributionId(context);
        hardwareName = getHardwareName();
        abi = getABI();
        buildName = getBuildName();
        appInstallTime = getAppInstallTime(packageInfo);
        appUpdateTime = getAppUpdateTime(packageInfo);
        uiMode = getDeviceUiMode(configuration);
        if (Util.canReadPlayIds(adjustConfig)) {
            appSetId = Reflection.getAppSetId(context);
        }
    }

    void reloadPlayIds(final AdjustConfig adjustConfig) {
        if (playIdsReadOnce && adjustConfig.isDeviceIdsReadingOnceEnabled) {
            if (!Util.canReadPlayIds(adjustConfig)) {
                playAdId = null;
                isTrackingEnabled = null;
                playAdIdSource = null;
                playAdIdAttempt = -1;
            }
            return;
        }

        playAdId = null;
        isTrackingEnabled = null;
        playAdIdSource = null;
        playAdIdAttempt = -1;

        if (!Util.canReadPlayIds(adjustConfig)) {
            return;
        }

        Context context = adjustConfig.context;

        if (Reflection.isAppRunningInSamsungCloudEnvironment(context, adjustConfig.logger)) {
            playAdId = Reflection.getSamsungCloudDevGoogleAdId(context, adjustConfig.logger);
            playAdIdSource = "samsung_cloud_sdk";
            playIdsReadOnce = true;
        }

        String previousPlayAdId = playAdId;
        Boolean previousIsTrackingEnabled = isTrackingEnabled;

        // attempt connecting to Google Play Service by own
        for (int serviceAttempt = 1; serviceAttempt <= 3; serviceAttempt += 1) {
            try {
                // timeout is a multiplier of the attempt number with 3 seconds
                // so first 3 seconds, second 6 seconds and third and last 9 seconds
                long timeoutServiceMilli = Constants.ONE_SECOND * 3 * serviceAttempt;
                GooglePlayServicesClient.GooglePlayServicesInfo gpsInfo =
                        GooglePlayServicesClient.getGooglePlayServicesInfo(context,
                                timeoutServiceMilli);
                if (playAdId == null) {
                    playAdId = gpsInfo.getGpsAdid();
                    playIdsReadOnce = true;
                }
                if (isTrackingEnabled == null) {
                    isTrackingEnabled = gpsInfo.isTrackingEnabled();
                }

                if (playAdId != null && isTrackingEnabled != null) {
                    playAdIdSource = "service";
                    playAdIdAttempt = serviceAttempt;
                    return;
                }
            } catch (Exception e) {}
        }

        // as fallback attempt connecting to Google Play Service using library
        for (int libAttempt = 1; libAttempt <= 3; libAttempt += 1) {
            // timeout inside library is 10 seconds, so 10 + 1 seconds are given
            Object advertisingInfoObject = Util.getAdvertisingInfoObject(
                    context, Constants.ONE_SECOND * 11);

            if (advertisingInfoObject == null) {
                continue;
            }

            if (playAdId == null) {
                // just needs a short timeout since it should be just accessing a POJO
                playAdId = Util.getPlayAdId(
                        context, advertisingInfoObject, Constants.ONE_SECOND);
                playIdsReadOnce = true;
            }
            if (isTrackingEnabled == null) {
                // just needs a short timeout since it should be just accessing a POJO
                isTrackingEnabled = Util.isPlayTrackingEnabled(
                        context, advertisingInfoObject, Constants.ONE_SECOND);
            }

            if (playAdId != null && isTrackingEnabled != null) {
                playAdIdSource = "library";
                playAdIdAttempt = libAttempt;
                return;
            }
        }

        // if both weren't found, use previous values
        if (playAdId == null) {
            playAdId = previousPlayAdId;
            playIdsReadOnce = true;
        }
        if (isTrackingEnabled == null) {
            isTrackingEnabled = previousIsTrackingEnabled;
        }
    }

    void reloadNonPlayIds(final AdjustConfig adjustConfig) {
        if (!Util.canReadNonPlayIds(adjustConfig)) {
            return;
        }

        if (nonGoogleIdsReadOnce) {
            return;
        }

        androidId = Util.getAndroidId(adjustConfig.context);
        nonGoogleIdsReadOnce = true;
    }

    void reloadOtherDeviceInfoParams(final AdjustConfig adjustConfig,
                                     final ILogger logger) {
        if (adjustConfig.isDeviceIdsReadingOnceEnabled && otherDeviceIdsParamsReadOnce) {
            return;
        }

        imeiParameters = UtilDeviceIds.getImeiParameters(adjustConfig, logger);
        oaidParameters = UtilDeviceIds.getOaidParameters(adjustConfig, logger);
        fireAdId = UtilDeviceIds.getFireAdvertisingId(adjustConfig);
        fireTrackingEnabled = UtilDeviceIds.getFireTrackingEnabled(adjustConfig);
        connectivityType = UtilDeviceIds.getConnectivityType(adjustConfig.context, logger);
        mcc = UtilDeviceIds.getMcc(adjustConfig.context, logger);
        mnc = UtilDeviceIds.getMnc(adjustConfig.context, logger);

        otherDeviceIdsParamsReadOnce = true;
    }
    public static void getFireAdvertisingIdBypassConditions(ContentResolver contentResolver, OnAmazonAdIdReadListener onAmazonAdIdReadListener) {
        UtilDeviceIds.getFireAdvertisingIdAsync(contentResolver, onAmazonAdIdReadListener);
    }

    private String getPackageName(Context context) {
        return context.getPackageName();
    }

    private PackageInfo getPackageInfo(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String name = context.getPackageName();
            return packageManager.getPackageInfo(name, PackageManager.GET_PERMISSIONS);
        } catch (Exception e) {
            return null;
        }
    }

    private String getAppVersion(PackageInfo packageInfo) {
        try {
            return packageInfo.versionName;
        } catch (Exception e) {
            return null;
        }
    }

    private String getDeviceType(Configuration configuration) {
        if (isGooglePlayGamesForPC) {
            return "pc";
        }

        int uiMode = configuration.uiMode & UI_MODE_TYPE_MASK;
        if (uiMode == UI_MODE_TYPE_TELEVISION) {
            return "tv";
        }

        int screenSize = configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "phone";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
            case 4:
                return "tablet";
            default:
                return null;
        }
    }

    private int getDeviceUiMode(Configuration configuration) {
        return configuration.uiMode & UI_MODE_TYPE_MASK;
    }

    private String getDeviceName() {
        if (isGooglePlayGamesForPC) {
            return null;
        }
        return Build.MODEL;
    }

    private String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    private String getOsName() {
        if (isGooglePlayGamesForPC) {
            return "windows";
        }
        return "android";
    }

    private String getOsVersion() {
        if (isGooglePlayGamesForPC) {
            return null;
        }
        return Build.VERSION.RELEASE;
    }

    private String getApiLevel() {
        return "" + Build.VERSION.SDK_INT;
    }

    private String getLanguage(Locale locale) {
        return locale.getLanguage();
    }

    private String getCountry(Locale locale) {
        return locale.getCountry();
    }

    private String getBuildName() {
        return Build.ID;
    }

    private String getHardwareName() {
        return Build.DISPLAY;
    }

    private String getScreenSize(int screenLayout) {
        int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return SMALL;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return NORMAL;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return LARGE;
            case 4:
                return XLARGE;
            default:
                return null;
        }
    }

    private String getScreenFormat(int screenLayout) {
        int screenFormat = screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;

        switch (screenFormat) {
            case Configuration.SCREENLAYOUT_LONG_YES:
                return LONG;
            case Configuration.SCREENLAYOUT_LONG_NO:
                return NORMAL;
            default:
                return null;
        }
    }

    private String getScreenDensity(DisplayMetrics displayMetrics) {
        int density = displayMetrics.densityDpi;
        int low = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_LOW) / 2;
        int high = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_HIGH) / 2;

        if (density == 0) {
            return null;
        } else if (density < low) {
            return LOW;
        } else if (density > high) {
            return HIGH;
        }
        return MEDIUM;
    }

    private String getDisplayWidth(DisplayMetrics displayMetrics) {
        return String.valueOf(displayMetrics.widthPixels);
    }

    private String getDisplayHeight(DisplayMetrics displayMetrics) {
        return String.valueOf(displayMetrics.heightPixels);
    }

    private String getClientSdk(String sdkPrefix) {
        if (sdkPrefix == null) {
            return Constants.CLIENT_SDK;
        } else {
            return Util.formatString("%s@%s", sdkPrefix, Constants.CLIENT_SDK);
        }
    }

    @SuppressWarnings("deprecation")
    private String getFacebookAttributionId(final Context context) {
        try {
            @SuppressLint("PackageManagerGetSignatures")
            Signature[] signatures = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                SigningInfo signingInfo = context.getPackageManager().getPackageInfo(
                        "com.facebook.katana",
                        PackageManager.GET_SIGNING_CERTIFICATES).signingInfo;
                if (signingInfo != null) {
                    signatures = signingInfo.getApkContentsSigners();
                }
            } else {
                signatures = context.getPackageManager().getPackageInfo(
                "com.facebook.katana",
                PackageManager.GET_SIGNATURES).signatures;
            }

            if (signatures == null || signatures.length != 1) {
                // Unable to find the correct signatures for this APK
                return null;
            }
            Signature facebookApkSignature = signatures[0];
            if (!OFFICIAL_FACEBOOK_SIGNATURE.equals(facebookApkSignature.toCharsString())) {
                // not the official Facebook application
                return null;
            }

            final ContentResolver contentResolver = context.getContentResolver();
            final Uri uri = Uri.parse("content://com.facebook.katana.provider.AttributionIdProvider");
            final String columnName = "aid";
            final String[] projection = {columnName};
            final Cursor cursor = contentResolver.query(uri, projection, null, null, null);

            if (cursor == null) {
                return null;
            }
            if (!cursor.moveToFirst()) {
                cursor.close();
                return null;
            }

            final String attributionId = cursor.getString(cursor.getColumnIndex(columnName));
            cursor.close();
            return attributionId;
        } catch (Exception e) {
            return null;
        }
    }

    private String getABI() {
        String[] SupportedABIS = Util.getSupportedAbis();

        // SUPPORTED_ABIS is only supported in API level 21
        // get CPU_ABI instead
        if (SupportedABIS == null || SupportedABIS.length == 0) {
            return Util.getCpuAbi();
        }

        return SupportedABIS[0];
    }

    private String getAppInstallTime(PackageInfo packageInfo) {
        try {
            return Util.dateFormatter.format(new Date(packageInfo.firstInstallTime));
        } catch (Exception ex) {
            return null;
        }
    }

    private String getAppUpdateTime(PackageInfo packageInfo) {
        try {
            return Util.dateFormatter.format(new Date(packageInfo.lastUpdateTime));
        } catch (Exception ex) {
            return null;
        }
    }

    private static class UtilDeviceIds {
        private static Map<String, String> getImeiParameters(final AdjustConfig adjustConfig,
                                                             final ILogger logger)
        {
            if (adjustConfig.coppaComplianceEnabled || adjustConfig.playStoreKidsComplianceEnabled) {
                return null;
            }

            return Reflection.getImeiParameters(adjustConfig.context, logger);
        }
        private static Map<String, String> getOaidParameters(final AdjustConfig adjustConfig,
                                                             final ILogger logger)
        {
            if (adjustConfig.coppaComplianceEnabled || adjustConfig.playStoreKidsComplianceEnabled) {
                return null;
            }

            return Reflection.getOaidParameters(adjustConfig.context, logger);
        }
        private static String getFireAdvertisingId(final AdjustConfig adjustConfig)
        {
            if (adjustConfig.coppaComplianceEnabled || adjustConfig.playStoreKidsComplianceEnabled) {
                return null;
            }

            return getFireAdvertisingId(adjustConfig.context.getContentResolver());
        }
        private static String getFireAdvertisingId(final ContentResolver contentResolver) {
            if (contentResolver == null) {
                return null;
            }
            try {
                // get advertising
                return Settings.Secure.getString(contentResolver, "advertising_id");
            } catch (Exception ex) {
                // not supported
            }
            return null;
        }

        private static void getFireAdvertisingIdAsync(final ContentResolver contentResolver,final OnAmazonAdIdReadListener onAmazonAdIdReadListener) {
            if (contentResolver == null) {
                AdjustFactory.getLogger().error("contentResolver could not be retrieved");
                return;
            }
            try {
                // get advertising
                String amazonAdId = Settings.Secure.getString(contentResolver, "advertising_id");
                onAmazonAdIdReadListener.onAmazonAdIdRead(amazonAdId);
            } catch (Exception ex) {
                AdjustFactory.getLogger().error(ex.getMessage());
            }
        }

        private static Boolean getFireTrackingEnabled(final AdjustConfig adjustConfig) {
            if (adjustConfig.coppaComplianceEnabled || adjustConfig.playStoreKidsComplianceEnabled) {
                return null;
            }

            return getFireTrackingEnabled(adjustConfig.context.getContentResolver());
        }

        private static Boolean getFireTrackingEnabled(final ContentResolver contentResolver) {
            try {
                // get user's tracking preference
                return Settings.Secure.getInt(contentResolver, "limit_ad_tracking") == 0;
            } catch (Exception ex) {
                // not supported
            }
            return null;
        }
        @SuppressWarnings("deprecation")
        private static int getConnectivityType(final Context context, final ILogger logger) {
            try {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                if (cm == null) {
                    return -1;
                }

                // for api 22 or lower, still need to get raw type
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    return activeNetwork.getType();
                }

                // .getActiveNetwork() is only available from api 23
                Network activeNetwork = cm.getActiveNetwork();
                if (activeNetwork == null) {
                    return -1;
                }

                NetworkCapabilities activeNetworkCapabilities = cm.getNetworkCapabilities(activeNetwork);
                if (activeNetworkCapabilities == null) {
                    return -1;
                }

                // check each network capability available from api 23
                if (activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return NetworkCapabilities.TRANSPORT_WIFI;
                }
                if (activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return NetworkCapabilities.TRANSPORT_CELLULAR;
                }
                if (activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return NetworkCapabilities.TRANSPORT_ETHERNET;
                }
                if (activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    return NetworkCapabilities.TRANSPORT_VPN;
                }
                if (activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
                    return NetworkCapabilities.TRANSPORT_BLUETOOTH;
                }

                // only after api 26, that more transport capabilities were added
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    return -1;
                }

                if (activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
                    return NetworkCapabilities.TRANSPORT_WIFI_AWARE;
                }

                // and then after api 27, that more transport capabilities were added
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                    return -1;
                }

                if (activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN)) {
                    return NetworkCapabilities.TRANSPORT_LOWPAN;
                }
            } catch (Exception e) {
                logger.warn("Couldn't read connectivity type (%s)", e.getMessage());
            }

            return -1;
        }
        public static String getMcc(final Context context, final ILogger logger) {
            try {
                TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String networkOperator = tel.getNetworkOperator();

                if (TextUtils.isEmpty(networkOperator)) {
                    AdjustFactory.getLogger().warn("Couldn't receive networkOperator string to read MCC");
                    return null;
                }
                return networkOperator.substring(0, 3);
            } catch (Exception ex) {
                AdjustFactory.getLogger().warn("Couldn't return mcc");
                return null;
            }
        }

        private static String getMnc(final Context context, final ILogger logger) {
            try {
                TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String networkOperator = tel.getNetworkOperator();

                if (TextUtils.isEmpty(networkOperator)) {
                    AdjustFactory.getLogger().warn("Couldn't receive networkOperator string to read MNC");
                    return null;
                }
                return networkOperator.substring(3);
            } catch (Exception ex) {
                logger.warn("Couldn't return mnc");
                return null;
            }
        }
    }
}
