package com.adjust.sdk.imei;

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
    static void injectImei(Map<String, String> parameters, Context context, ILogger logger) {
        if (!AdjustImei.isImeiToBeRead) {
            return;
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        PackageBuilder.addString(parameters, "telephony_ids", getTelephonyIds(telephonyManager, logger));
        PackageBuilder.addString(parameters, "imeis", getImeis(telephonyManager, logger));
        PackageBuilder.addString(parameters, "meids", getMeids(telephonyManager, logger));
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

    private static String getImeis(TelephonyManager telephonyManager, ILogger logger) {
        List<String> imeiList = new ArrayList<String>();
        String imeiNoIdx = getDefaultImei(telephonyManager, logger);
        tryAddToStringList(imeiList, imeiNoIdx);

        for (int i = 0; i < 10; i++) {
            String imei = getImeiByIndex(telephonyManager, i, logger);
            if (!tryAddToStringList(imeiList, imei)) {
                break;
            }
        }

        return TextUtils.join(",", imeiList);
    }

    private static String getDefaultImei(TelephonyManager telephonyManager, ILogger logger) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return telephonyManager.getImei();
            }
        } catch (SecurityException e) {
            logger.debug("Couldn't read default IMEI: %s", e.getMessage());
        }
        return null;
    }

    private static String getImeiByIndex(TelephonyManager telephonyManager, int index, ILogger logger) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return telephonyManager.getImei(index);
            }
        } catch (SecurityException e) {
            logger.debug("Couldn't read IMEI in position %d: %s", index, e.getMessage());
        }
        return null;
    }

    public static String getMeids(TelephonyManager telephonyManager, ILogger logger) {
        List<String> meidList = new ArrayList<String>();

        String meidNoIdx = getDefaultMeid(telephonyManager, logger);
        tryAddToStringList(meidList, meidNoIdx);

        for (int i = 0; i < 10; i++) {
            String meid = getMeidByIndex(telephonyManager, i, logger);
            if (!tryAddToStringList(meidList, meid)) {
                break;
            }
        }

        return TextUtils.join(",", meidList);
    }

    private static String getDefaultMeid(TelephonyManager telephonyManager, ILogger logger) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return telephonyManager.getMeid();
            }
        } catch (SecurityException e) {
            logger.debug("Couldn't read default MEID: %s", e.getMessage());
        }
        return null;
    }

    private static String getMeidByIndex(TelephonyManager telephonyManager, int index, ILogger logger) {
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
