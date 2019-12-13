package com.adjust.sdk.oaid;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.IIdentifierListener;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.supplier.IdSupplier;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MsaSdkClient {
    public static String getOaid(Context context, final ILogger logger, long maxWaitTimeInMilli) {
        final BlockingQueue<String> oaidHolder = new LinkedBlockingQueue<String>(1);

        try {
            boolean msaInternalLogging = false;
            int result = MdidSdkHelper.InitSdk(context, msaInternalLogging, new IIdentifierListener() {
                @Override
                public void OnSupport(boolean b, IdSupplier idSupplier) {
                    try {
                        if (idSupplier == null || idSupplier.getOAID() == null) {
                            // so to avoid waiting for timeout
                            oaidHolder.offer("");
                        } else {
                            oaidHolder.offer(idSupplier.getOAID());
                        }
                    } catch (Exception e) {
                        logger.error("Fail to add %s", e.getMessage());
                    }
                }
            });

            if (!isError(result, logger)) {
                return oaidHolder.poll(maxWaitTimeInMilli, TimeUnit.MILLISECONDS);
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
            case ErrorCode.INIT_ERROR_BEGIN:
                logger.error("msa sdk error - INIT_ERROR_BEGIN");
                return true;
            case ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT:
                logger.error("msa sdk error - INIT_ERROR_DEVICE_NOSUPPORT");
                return true;
            case ErrorCode.INIT_ERROR_LOAD_CONFIGFILE:
                logger.error("msa sdk error - INIT_ERROR_LOAD_CONFIGFILE");
                return true;
            case ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT:
                logger.error("msa sdk error - INIT_ERROR_MANUFACTURER_NOSUPPORT");
                return true;
            case ErrorCode.INIT_HELPER_CALL_ERROR:
                logger.error("msa sdk error - INIT_HELPER_CALL_ERROR");
                return true;
            default:
                return false;
        }
    }
}
