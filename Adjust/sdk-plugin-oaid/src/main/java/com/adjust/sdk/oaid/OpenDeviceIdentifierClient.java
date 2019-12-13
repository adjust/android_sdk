package com.adjust.sdk.oaid;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.adjust.sdk.ILogger;

import java.util.concurrent.TimeUnit;

public class OpenDeviceIdentifierClient {
    private static final String OAID_INTENT_ACTION = "com.uodis.opendevice.OPENIDS_SERVICE";
    private static final String HUAWEI_PACKAGE_NAME = "com.huawei.hwid";

    private Context context;
    private long maxWaitTime;
    private ILogger logger;

    public static Info getOaidInfo(Context context, ILogger logger, long maxWaitTimeInMilli) {
        Info oaidInfo = null;
        try {
            OpenDeviceIdentifierClient openDeviceIdentifierClient =
                    new OpenDeviceIdentifierClient(context, logger, maxWaitTimeInMilli);
            oaidInfo = openDeviceIdentifierClient.getOaidInfo();
        } catch (Throwable e) {
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

    private OpenDeviceIdentifierClient(Context context, ILogger logger, long maxWaitTime) {
        this.context = context;
        this.logger = logger;
        this.maxWaitTime = maxWaitTime;
    }

    private synchronized Info getOaidInfo() throws RemoteException {
        OpenDeviceIdentifierConnector serviceConnector = getServiceConnector(this.context);
        if (serviceConnector == null) {
            return null;
        }

        OpenDeviceIdentifierService service =
                serviceConnector.getOpenDeviceIdentifierService(maxWaitTime, TimeUnit.MILLISECONDS);
        if (service == null) {
            // since service bind fails due to any reason (even timeout), its reasonable to
            // unbind it rather than keeping it open
            serviceConnector.unbindAndReset();
            return null;
        }

        return new Info(service.getOaid(), service.isOaidTrackLimited());
    }

    private OpenDeviceIdentifierConnector getServiceConnector(Context context) {
        OpenDeviceIdentifierConnector connector =
                OpenDeviceIdentifierConnector.getInstance(context, logger);

        // see if we still have a connected service, and return it
        if (connector.isServiceConnected()) {
            return connector;
        }

        // try to bind to the service and return it
        Intent intentForOaidService = new Intent(OAID_INTENT_ACTION);
        intentForOaidService.setPackage(HUAWEI_PACKAGE_NAME);
        boolean couldBind = false;

        try {
            // letting the connector know that it should unbind in all possible failure cases
            // also it should attempt to unbind only once after each bind attempt
            connector.shouldUnbind();

            couldBind = context.bindService(intentForOaidService, connector, Context.BIND_AUTO_CREATE);

            if (couldBind) {
                return connector;
            }
        } catch(Exception e) {
            logger.error("Fail to bind service %s", e.getMessage());
        } finally {
            if (!couldBind) {
                connector.unbindAndReset();
            }
        }

        logger.warn("OpenDeviceIdentifierService is not available to bind");
        return null;
    }
}
