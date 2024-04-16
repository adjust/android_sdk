package com.adjust.sdk.samsung;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.samsung.android.sdk.sinstallreferrer.api.InstallReferrerClient;
import com.samsung.android.sdk.sinstallreferrer.api.InstallReferrerStateListener;
import com.samsung.android.sdk.sinstallreferrer.api.ReferrerDetails;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SamsungReferrerClient {

    public static SamsungInstallReferrerResult getReferrer(Context context, final ILogger logger, long maxWaitTimeInMilli) {
        try {
            final InstallReferrerClient referrerClient = InstallReferrerClient.newBuilder(context).build();
            final BlockingQueue<SamsungInstallReferrerResult> referrerDetailsHolder = new LinkedBlockingQueue<SamsungInstallReferrerResult>(1);
            referrerClient.startConnection(new InstallReferrerStateListener() {
                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {
                    try {
                        switch (responseCode) {
                            case InstallReferrerClient.InstallReferrerResponse.OK:
                                try {
                                    SamsungInstallReferrerDetails samsungInstallReferrerDetails = getSamsungInstallReferrerDetails(referrerClient);
                                    referrerDetailsHolder.offer(new SamsungInstallReferrerResult(samsungInstallReferrerDetails));
                                } catch (Exception e) {
                                    logger.error("SamsungReferrer getInstallReferrer: " + e.getMessage());
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
                    } catch (Exception e) {
                        logger.error("SamsungReferrer onInstallReferrerSetupFinished: " + e.getMessage());
                    }
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {
                    referrerClient.endConnection();
                }
            });

            return referrerDetailsHolder.poll(maxWaitTimeInMilli, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            String error = "SamsungReferrer read error: " + e.getMessage();
            logger.info(error);
            return new SamsungInstallReferrerResult(error);
        }
    }

    private static SamsungInstallReferrerDetails getSamsungInstallReferrerDetails(InstallReferrerClient referrerClient) {
        ReferrerDetails details = referrerClient.getInstallReferrer();
        return new SamsungInstallReferrerDetails(
                        details.getInstallReferrer(),
                        details.getReferrerClickTimestampSeconds(),
                        details.getInstallBeginTimestampSeconds());
    }
}
