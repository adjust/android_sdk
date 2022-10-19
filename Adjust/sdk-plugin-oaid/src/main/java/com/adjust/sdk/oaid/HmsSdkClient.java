package com.adjust.sdk.oaid;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.huawei.hms.ads.identifier.AdvertisingIdClient;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class HmsSdkClient {

    public static OaidInfo getOaidInfo(Context context, final ILogger logger, long maxWaitTimeInMilli) {
        try {
            FutureTask<OaidInfo> task = new FutureTask<>(new Callable<OaidInfo>() {
                @Override
                public OaidInfo call() {
                    OaidInfo oaidInfo = null;
                    try {
                        AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(context);
                        if (info != null) {
                            oaidInfo = new OaidInfo(info.getId(), !info.isLimitAdTrackingEnabled());
                        }
                    } catch (Exception e) {
                        logger.error("Exception while reading oaid using hms %s", e.getMessage());
                    }
                    return oaidInfo;
                }
            });
            new Thread(task).start();
            return task.get(maxWaitTimeInMilli, TimeUnit.MILLISECONDS);
        } catch (Throwable t) {
            logger.error("Fail to read oaid info using hms, %s", t.getMessage());
            return null;
        }
    }


}
