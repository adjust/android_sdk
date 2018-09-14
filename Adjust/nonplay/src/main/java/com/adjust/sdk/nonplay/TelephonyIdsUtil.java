package com.adjust.sdk.nonplay;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.PackageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class TelephonyIdsUtil {
    static void injectIMEI(Map<String, String> parameters, Context context, ILogger logger) {
        if (!AdjustNonPlay.isReadIMEIset) {
            return;
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        PackageBuilder.addString(parameters, "telephony_ids", getTelephonyIds(telephonyManager, logger));
        PackageBuilder.addString(parameters, "imeis", getIMEIs(telephonyManager, logger));
        PackageBuilder.addString(parameters, "meids", getMEIDs(telephonyManager, logger));
    }

    private static String getTelephonyIds(TelephonyManager telephonyManager, ILogger logger) {
        List<String> telephonyIdList = new ArrayList<String>();

        String telephonyNoIdx = getDefaultTelephonyId(telephonyManager, logger);
        tryAddToStringList(telephonyIdList, telephonyNoIdx);

        for (int i = 0; i < 10; i++) {
            String telephonyId = getTelephonyIdByIndex(telephonyManager, i, logger);
            if (!tryAddToStringList(telephonyIdList, telephonyId)) {
                break;
            }
        }

        return TextUtils.join(",", telephonyIdList);
    }

    // XXX test difference mentioned here https://stackoverflow.com/a/35343531
    private static String getDefaultTelephonyId(TelephonyManager telephonyManager, ILogger logger) {
        try {
            return telephonyManager.getDeviceId();
        } catch (SecurityException e) {
            logger.debug("Couldn't read default Telephony Id: %s", e.getMessage());
        }
        return null;
    }

    private static String getTelephonyIdByIndex(TelephonyManager telephonyManager, int index, ILogger logger) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return telephonyManager.getDeviceId(index);
            }
        } catch (SecurityException e) {
            logger.debug("Couldn't read Telephony Id in position %d: %s", index, e.getMessage());
        }
        return null;
    }

    private static String getIMEIs(TelephonyManager telephonyManager, ILogger logger) {
        List<String> imeiList = new ArrayList<String>();

        String imeiNoIdx = getDefaultIMEI(telephonyManager, logger);
        tryAddToStringList(imeiList, imeiNoIdx);

        for (int i = 0; i < 10; i++) {
            String imei = getIMEIbyIndex(telephonyManager, i, logger);
            if (!tryAddToStringList(imeiList, imei)) {
                break;
            }
        }

        return TextUtils.join(",", imeiList);
    }

    private static String getDefaultIMEI(TelephonyManager telephonyManager, ILogger logger) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return telephonyManager.getImei();
            }
        } catch (SecurityException e) {
            logger.debug("Couldn't read default IMEI: %s", e.getMessage());
        }
        return null;
    }

    private static String getIMEIbyIndex(TelephonyManager telephonyManager, int index, ILogger logger) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return telephonyManager.getImei(index);
            }
        } catch (SecurityException e) {
            logger.debug("Couldn't read IMEI in position %d: %s", index, e.getMessage());
        }
        return null;
    }

    public static String getMEIDs(TelephonyManager telephonyManager, ILogger logger) {
        List<String> meidList = new ArrayList<String>();

        String meidNoIdx = getDefaultMEID(telephonyManager, logger);
        tryAddToStringList(meidList, meidNoIdx);

        for (int i = 0; i < 10; i++) {
            String meid = getMEIDbyIndex(telephonyManager, i, logger);
            if (!tryAddToStringList(meidList, meid)) {
                break;
            }
        }

        return TextUtils.join(",", meidList);
    }

    private static String getDefaultMEID(TelephonyManager telephonyManager, ILogger logger) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return telephonyManager.getMeid();
            }
        } catch (SecurityException e) {
            logger.debug("Couldn't read default MEID: %s", e.getMessage());
        }
        return null;
    }

    private static String getMEIDbyIndex(TelephonyManager telephonyManager, int index, ILogger logger) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return telephonyManager.getMeid(index);
            }
        } catch (SecurityException e) {
            logger.debug("Couldn't read MEID in position %d: %s", index, e.getMessage());
        }
        return null;
    }

    private static boolean tryAddToStringList(List<String> list, String value) {
        if (value == null) {
            return false;
        }
        if (list.contains(value)) {
            return false;
        }

        return list.add(value);
    }
}
