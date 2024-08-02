package com.adjust.sdk.oaid;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.bun.miitmdid.core.InfoCode;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IIdentifierListener;
import com.bun.miitmdid.interfaces.IdSupplier;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MsaSdkClient {
    public static OaidInfo getOaidInfo(Context context, final ILogger logger, long maxWaitTimeInMilli) {
        final BlockingQueue<OaidInfo> oaidInfoHolder = new LinkedBlockingQueue<OaidInfo>(1);

        try {
            boolean msaInternalLogging = false;
            int result = MdidSdkHelper.InitSdk(context, msaInternalLogging, new IIdentifierListener() {
                @Override
                public void onSupport(IdSupplier idSupplier) {
                    try {
                        if (idSupplier == null || idSupplier.getOAID() == null) {
                            // so to avoid waiting for timeout
                            oaidInfoHolder.offer(new OaidInfo(null, false));
                        } else {
                            oaidInfoHolder.offer(new OaidInfo(idSupplier.getOAID(), !idSupplier.isLimited()));
                        }
                    } catch (Exception e) {
                        logger.error("Fail to add %s", e.getMessage());
                    }
                }
            });

            if (!isError(result, logger)) {
                return oaidInfoHolder.poll(maxWaitTimeInMilli, TimeUnit.MILLISECONDS);
            }
        } catch (NoClassDefFoundError ex) {
          logger.error("Couldn't find msa sdk " + ex.getMessage());
        } catch (InterruptedException e) {
            logger.error("Waiting to read oaid from callback interrupted: %s",
                    e.getMessage());
        } catch (Throwable t) {
            logger.error("Oaid reading process failed %s", t.getMessage());
        }

        return null;
    }

    private static boolean isError(int result, ILogger logger) {
        switch(result) {
            case InfoCode.INIT_ERROR_CERT_ERROR:
                logger.error("msa sdk error - INIT_ERROR_CERT_ERROR");
                return true;
            case InfoCode.INIT_ERROR_DEVICE_NOSUPPORT:
                logger.error("msa sdk error - INIT_ERROR_DEVICE_NOSUPPORT");
                return true;
            case InfoCode.INIT_ERROR_LOAD_CONFIGFILE:
                logger.error("msa sdk error - INIT_ERROR_LOAD_CONFIGFILE");
                return true;
            case InfoCode.INIT_ERROR_MANUFACTURER_NOSUPPORT:
                logger.error("msa sdk error - INIT_ERROR_MANUFACTURER_NOSUPPORT");
                return true;
            case InfoCode.INIT_ERROR_SDK_CALL_ERROR:
                logger.error("msa sdk error - INIT_ERROR_SDK_CALL_ERROR");
                return true;
            default:
                return false;
        }
    }
}
