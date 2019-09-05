package com.adjust.sdk.oaid;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.PackageBuilder;
import com.uodis.opendevice.aidl.OpenDeviceIdentifierService;

import java.util.Map;

public class OpenDeviceIdUtil {
    private static final String OAID_INTENT_ACTION = "com.uodis.opendevice.OPENIDS_SERVICE";
    private static final String HUAWEI_PACKAGE_NAME = "com.huawei.hwid";
    private static OpenDeviceIdentifierService oaidService = null;
    private static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            oaidService = OpenDeviceIdentifierService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            oaidService = null;
        }

        @Override
        public void onBindingDied(ComponentName name) {
            oaidService = null;
        }

        @Override
        public void onNullBinding(ComponentName name) {
            oaidService = null;
        }
    };

    public static boolean connect(Context context) {
        if (!AdjustOaid.isOaidToBeRead) {
            return false;
        }

        if (oaidService != null) {
            return true;
        }

        Intent bindIntent = new Intent(OAID_INTENT_ACTION);
        bindIntent.setPackage(HUAWEI_PACKAGE_NAME);

        try {
            return context.bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (SecurityException ex) {
            Log.d("OAID", "Fail to connect to OAID service: " + ex.getMessage());
        }

        return false;
    }

    public static void injectOaid(Map<String, String> parameters, Context context, ILogger logger) {
        if (!AdjustOaid.isOaidToBeRead) {
            return;
        }

        if (connect(context)) {
            PackageBuilder.addString(parameters, "oaid", getOaid(context, logger));
            PackageBuilder.addBoolean(parameters, "oaid_tracking_enabled", isOaidTrackingEnabled(context, logger));
        }
    }

    public static void disconnect(Context context) {
        context.unbindService(serviceConnection);
        oaidService = null;
    }

    private static String getOaid(Context context, ILogger logger) {

        if (oaidService != null) {
            try {
                String oaid = oaidService.getOaid();
                logger.info("Read OAID " + oaid);
                return oaid;
            } catch (RemoteException e) {
                logger.debug("Couldn't read OAID: %s", e.getMessage());
            }
        }

        return null;
    }

    private static Boolean isOaidTrackingEnabled(Context context, ILogger logger) {

        if (oaidService != null) {
            try {
                boolean trackingDisabled = oaidService.isOaidTrackLimited();
                logger.info("OAID tracking disabled status is " + trackingDisabled);
                return !trackingDisabled;
            } catch (RemoteException e) {
                logger.debug("Couldn't read OAID tracking: %s", e.getMessage());
            }
        }

        return null;
    }


}
