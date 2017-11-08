package com.adjust.sdk.plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.adjust.sdk.Reflection;

/**
 * Created by nonelse on 06.11.17.
 */

public class MobileEquipmentIdentityUtil {
    public static String getMobileEquipmentIdentity(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        String mobileEquipmentIdentity = null;

        mobileEquipmentIdentity = Reflection.getLinkIMEI(telephonyManager);
        if (mobileEquipmentIdentity != null) {
            return mobileEquipmentIdentity;
        }

        mobileEquipmentIdentity = Reflection.getLinkMEID(telephonyManager);
        if (mobileEquipmentIdentity != null) {
            return mobileEquipmentIdentity;
        }

        mobileEquipmentIdentity = Reflection.getTelephonyId(telephonyManager);

        return mobileEquipmentIdentity;
    }
}
