package com.adjust.sdk.oaid;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.adjust.sdk.ILogger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OpenDeviceIdentifierClient {

    private static final String OAID_INTENT_ACTION = "com.uodis.opendevice.OPENIDS_SERVICE";
    private static final String HUAWEI_PACKAGE_NAME = "com.huawei.hwid";

    private Context context;
    private long maxWaitTime;

    public static Info getOaidInfo(Context context, ILogger logger, long maxWaitTimeInMilli) {
        Info oaidInfo = null;
        try {
            OpenDeviceIdentifierClient openDeviceIdentifierClient = new OpenDeviceIdentifierClient(context, maxWaitTimeInMilli);
            oaidInfo = openDeviceIdentifierClient.getOaidInfo();
        } catch (IOException e) {
            logger.error("Fail to read oaid, %s", e.getMessage());
        }
        return oaidInfo;
    }

    public static final class Info {
        private final String oaid;
        private final boolean isOaidTrackLimited;

        public Info(String oaid, boolean isOaidTrackLimited) {
            this.oaid = oaid;
            this.isOaidTrackLimited = isOaidTrackLimited;
        }

        public String getOaid() {
            return oaid;
        }

        public boolean isOaidTrackLimited() {
            return isOaidTrackLimited;
        }

        @Override
        public String toString() {
            return "Info{" +
                    "oaid='" + oaid + '\'' +
                    ", isOaidTrackLimited=" + isOaidTrackLimited +
                    '}';
        }
    }

    private OpenDeviceIdentifierClient(Context context, long maxWaitTime) {
        this.context = context;
        this.maxWaitTime = maxWaitTime;
    }

    private Info getOaidInfo() throws IOException {
        Info oaidInfo;
        try {
            OpenDeviceIdentifierService service = getOpenDeviceIdentifierService();
            oaidInfo = new Info(service.getOaid(), service.isOaidTrackLimited());
        } catch (RemoteException e) {
            throw new IOException("OpenDeviceIdentifierService remote exception " + e.getMessage());
        }
        return oaidInfo;
    }

    private OpenDeviceIdentifierService getOpenDeviceIdentifierService() throws IOException {
        synchronized (this) {
            OpenDeviceIdentifierConnector serviceConnector = getServiceConnector(this.context);
            return getOpenDeviceIdentifierService(serviceConnector);
        }
    }

    private OpenDeviceIdentifierConnector getServiceConnector(Context context) throws IOException {

        OpenDeviceIdentifierConnector connector = OpenDeviceIdentifierConnector.getInstance(context);

        if (connector.isServiceConnected()) {
            return connector;
        }

        try {
            if (context.bindService(getIntentForOaidService(), connector, Context.BIND_AUTO_CREATE)) {
                return connector;
            }
        } catch (Throwable localThrowable) {
            connector.unbindAndReset();
        }

        throw new IOException("OpenDeviceIdentifierService is not available to bind");
    }

    private Intent getIntentForOaidService() {
        Intent intent = new Intent(OAID_INTENT_ACTION);
        intent.setPackage(HUAWEI_PACKAGE_NAME);
        return intent;
    }

    private OpenDeviceIdentifierService getOpenDeviceIdentifierService(OpenDeviceIdentifierConnector serviceConnector) throws IOException {
        try {
            return serviceConnector.getOpenDeviceIdentifierService(maxWaitTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            throw new IOException("Interrupted exception");
        } catch (Throwable throwable) {
            throw new IOException(throwable);
        }
    }

}
