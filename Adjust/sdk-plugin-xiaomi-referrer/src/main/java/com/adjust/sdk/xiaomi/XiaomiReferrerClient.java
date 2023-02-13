package com.adjust.sdk.xiaomi;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.miui.referrer.annotation.GetAppsReferrerResponse;
import com.miui.referrer.api.GetAppsReferrerClient;
import com.miui.referrer.api.GetAppsReferrerDetails;
import com.miui.referrer.api.GetAppsReferrerStateListener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class XiaomiReferrerClient {

    public static GetAppsReferrerDetails getReferrer(Context context, final ILogger logger, long maxWaitTimeInMilli) {
        final GetAppsReferrerClient referrerClient = new GetAppsReferrerClient.Builder(context).build();
        final BlockingQueue<GetAppsReferrerDetails> referrerDetailsHolder = new LinkedBlockingQueue<GetAppsReferrerDetails>(1);
        referrerClient.startConnection(new GetAppsReferrerStateListener() {
            @Override
            public void onGetAppsReferrerSetupFinished(int responseCode) {
                switch(responseCode) {
                    case GetAppsReferrerResponse.OK:
                        try {
                            GetAppsReferrerDetails getAppsReferrerDetails = referrerClient.getInstallReferrer();
                            referrerDetailsHolder.offer(getAppsReferrerDetails);
                        } catch (Exception e) {
                            logger.error("XiaomiReferrer getInstallReferrer: " + e.getMessage());
                        }
                        break;
                    case GetAppsReferrerResponse.FEATURE_NOT_SUPPORTED:
                        logger.info("XiaomiReferrer onGetAppsReferrerSetupFinished: FEATURE_NOT_SUPPORTED");
                        break;
                    case GetAppsReferrerResponse.SERVICE_UNAVAILABLE:
                        logger.info("XiaomiReferrer onGetAppsReferrerSetupFinished: SERVICE_UNAVAILABLE");
                        break;
                }
            }

            @Override
            public void onGetAppsServiceDisconnected() {
            }
        });

        try {
            return referrerDetailsHolder.poll(maxWaitTimeInMilli, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("Exception while waiting to get referrer: ", e.getMessage());
        }

        return null;
    }
}
