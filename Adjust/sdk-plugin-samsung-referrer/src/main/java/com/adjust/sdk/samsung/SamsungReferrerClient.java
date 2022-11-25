package com.adjust.sdk.samsung;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.sec.android.app.samsungapps.installreferrer.api.InstallReferrerClient;
import com.sec.android.app.samsungapps.installreferrer.api.InstallReferrerStateListener;
import com.sec.android.app.samsungapps.installreferrer.api.ReferrerDetails;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SamsungReferrerClient {

    public static ReferrerDetails getReferrer(Context context, final ILogger logger, long maxWaitTimeInMilli) {
        final InstallReferrerClient referrerClient = InstallReferrerClient.newBuilder(context).build();
        final BlockingQueue<ReferrerDetails> referrerDetailsHolder = new LinkedBlockingQueue<ReferrerDetails>(1);
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                switch (responseCode) {
                    case InstallReferrerClient.InstallReferrerResponse.OK:
                        try {
                            ReferrerDetails details = referrerClient.getInstallReferrer();
                            referrerDetailsHolder.offer(details);
                        } catch (Exception e) {
                            logger.error("SamsungReferrer getInstallReferrer: " + e.getMessage());
                            referrerDetailsHolder.offer(null);
                        } finally {
                            referrerClient.endConnection();
                        }
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        logger.info("SamsungReferrer onInstallReferrerSetupFinished: FEATURE_NOT_SUPPORTED");
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        logger.info("SamsungReferrer onInstallReferrerSetupFinished: SERVICE_UNAVAILABLE");
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                referrerClient.endConnection();
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
