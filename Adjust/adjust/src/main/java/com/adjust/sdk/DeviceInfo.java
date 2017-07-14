package com.adjust.sdk;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

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
    String playAdId;
    Boolean isTrackingEnabled;
    private boolean nonGoogleIdsRead = false;
    String macSha1;
    String macShortMd5;
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
    String vmInstructionSet;
    String appInstallTime;
    String appUpdateTime;
    String mcc;
    String mnc;
    String networkType;
    Map<String, String> pluginKeys;

    DeviceInfo(Context context, String sdkPrefix) {
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        Locale locale = Util.getLocale(configuration);
        int screenLayout = configuration.screenLayout;
        ContentResolver contentResolver = context.getContentResolver();

        reloadDeviceIds(context);

        packageName = getPackageName(context);
        appVersion = getAppVersion(context);
        deviceType = getDeviceType(screenLayout);
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
        clientSdk = getClientSdk(sdkPrefix);
        fbAttributionId = getFacebookAttributionId(context);
        pluginKeys = Util.getPluginKeys(context);
        hardwareName = getHardwareName();
        abi = getABI();
        buildName = getBuildName();
        vmInstructionSet = getVmInstructionSet();
        appInstallTime = getAppInstallTime(context);
        appUpdateTime = getAppUpdateTime(context);
        mcc = getMcc(context);
        mnc = getMnc(context);
        networkType = NetworkUtil.getNetworkType(context);
    }

    void reloadDeviceIds(Context context) {
        isTrackingEnabled = Util.isPlayTrackingEnabled(context);
        playAdId = Util.getPlayAdId(context);

        if (playAdId == null && !nonGoogleIdsRead) {
            if (!Util.checkPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE)) {
                AdjustFactory.getLogger().warn("Missing permission: ACCESS_WIFI_STATE");
            }
            String macAddress = Util.getMacAddress(context);
            macSha1 = getMacSha1(macAddress);
            macShortMd5 = getMacShortMd5(macAddress);

            androidId = Util.getAndroidId(context);

            nonGoogleIdsRead = true;
        }
    }

    private String getMacAddress(Context context, boolean isGooglePlayServicesAvailable) {
        if (!isGooglePlayServicesAvailable) {
            if (!Util.checkPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE)) {
                AdjustFactory.getLogger().warn("Missing permission: ACCESS_WIFI_STATE");
            }
            return Util.getMacAddress(context);
        } else {
            return null;
        }
    }

    private String getPackageName(Context context) {
        return context.getPackageName();
    }

    private String getAppVersion(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String name = context.getPackageName();
            PackageInfo info = packageManager.getPackageInfo(name, 0);
            return info.versionName;
        } catch (Exception e) {
            return null;
        }
    }

    private String getDeviceType(int screenLayout) {
        int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

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

    private String getDeviceName() {
        return Build.MODEL;
    }

    private String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    private String getOsName() {
        return "android";
    }

    private String getOsVersion() {
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
            return String.format(Locale.US, "%s@%s", sdkPrefix, Constants.CLIENT_SDK);
        }
    }

    private String getMacSha1(String macAddress) {
        if (macAddress == null) {
            return null;
        }
        String macSha1 = Util.sha1(macAddress);

        return macSha1;
    }

    private String getMacShortMd5(String macAddress) {
        if (macAddress == null) {
            return null;
        }
        String macShort = macAddress.replaceAll(":", "");
        String macShortMd5 = Util.md5(macShort);

        return macShortMd5;
    }

    private String getFacebookAttributionId(final Context context) {
        try {
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

    private String getVmInstructionSet() {
        String instructionSet = Util.getVmInstructionSet();
        return instructionSet;
    }

    private String getAppInstallTime(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);

            String appInstallTime = Util.dateFormatter.format(new Date(packageInfo.firstInstallTime));

            return appInstallTime;
        } catch (Exception ex) {
            return null;
        }
    }

    private String getAppUpdateTime(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);

            String appInstallTime = Util.dateFormatter.format(new Date(packageInfo.lastUpdateTime));

            return appInstallTime;
        } catch (Exception ex) {
            return null;
        }
    }

    private String getMcc(Context context) {
        try {
            TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String networkOperator = tel.getNetworkOperator();

            if (TextUtils.isEmpty(networkOperator)) {
                AdjustFactory.getLogger().warn("Couldn't receive networkOperator string");
                return null;
            }
            return networkOperator.substring(0, 3);
        } catch (Exception ex) {
            AdjustFactory.getLogger().warn("Couldn't return mcc");
            return null;
        }
    }

    private String getMnc(Context context) {
        try {
            TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String networkOperator = tel.getNetworkOperator();

            if (TextUtils.isEmpty(networkOperator)) {
                AdjustFactory.getLogger().warn("Couldn't receive networkOperator string");
                return null;
            }
            return networkOperator.substring(3);
        } catch (Exception ex) {
            AdjustFactory.getLogger().warn("Couldn't return mnc");
            return null;
        }
    }

    private static class NetworkUtil {
        private final static String NETWORKTYPE_2G = "2g";
        private final static String NETWORKTYPE_3G = "3g";
        private final static String NETWORKTYPE_4G = "4g";
        private final static String NETWORKTYPE_WIFI = "wifi";
        private final static String NETWORKTYPE_UNKNOWN = "unknown";
        private final static String NETWORKTYPE_NOT_CONNECTED = "not_connected";

        private NetworkUtil() {
        }

        // Returns the network type based as one of the NETWORKTYPE_XX const values.
        // Priority goes to 'wifi' even if mobile data is enabled
        static String getNetworkType(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    return NETWORKTYPE_WIFI;
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    // connected to the mobile provider's data plan
                    return getMobileNetworkType(context);
                }
            }

            // not connected to the internet
            return NETWORKTYPE_NOT_CONNECTED;
        }

        private static String getMobileNetworkType(Context context) {
            TelephonyManager teleMan =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int networkType = teleMan.getNetworkType();

            switch (networkType) {
                //- Most network types were determined using the table at the end of this page:
                // https://en.wikipedia.org/wiki/List_of_mobile_phone_generations

                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return NETWORKTYPE_2G;
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return NETWORKTYPE_2G;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return NETWORKTYPE_2G;

                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return NETWORKTYPE_3G;
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return NETWORKTYPE_3G;
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return NETWORKTYPE_3G;
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    return NETWORKTYPE_3G;
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return NETWORKTYPE_3G;
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return NETWORKTYPE_3G;
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    return NETWORKTYPE_3G;
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return NETWORKTYPE_3G;
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return NETWORKTYPE_3G;
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return NETWORKTYPE_3G;

                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return NETWORKTYPE_4G;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return NETWORKTYPE_4G;
            }

            return NETWORKTYPE_UNKNOWN;
        }
    }
}
