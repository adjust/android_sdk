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

    public static XiaomiInstallReferrerResult getReferrer(Context context, final ILogger logger, long maxWaitTimeInMilli) {
        try {
            final GetAppsReferrerClient referrerClient = new GetAppsReferrerClient.Builder(context).build();
            final BlockingQueue<XiaomiInstallReferrerResult> referrerDetailsHolder = new LinkedBlockingQueue<XiaomiInstallReferrerResult>(1);
            referrerClient.startConnection(new GetAppsReferrerStateListener() {
                @Override
                public void onGetAppsReferrerSetupFinished(int responseCode) {
                    try {
                        switch (responseCode) {
                            case GetAppsReferrerResponse.OK:
                                try {
                                    XiaomiInstallReferrerDetails xiaomiInstallReferrerDetails = new XiaomiInstallReferrerDetails(referrerClient.getInstallReferrer());
                                    referrerDetailsHolder.offer(new XiaomiInstallReferrerResult(xiaomiInstallReferrerDetails));
                                } catch (Exception e) {
                                    String message = "XiaomiReferrer getInstallReferrer: " + e.getMessage();
                                    logger.error(message);
                                    referrerDetailsHolder.offer(new XiaomiInstallReferrerResult(message));
                                }
                                break;
                            case GetAppsReferrerResponse.FEATURE_NOT_SUPPORTED:
                                String message = "XiaomiReferrer onGetAppsReferrerSetupFinished: FEATURE_NOT_SUPPORTED";
                                logger.info(message);
                                referrerDetailsHolder.offer(new XiaomiInstallReferrerResult(message));
                                break;
                            case GetAppsReferrerResponse.SERVICE_UNAVAILABLE:
                                String message1 = "XiaomiReferrer onGetAppsReferrerSetupFinished: SERVICE_UNAVAILABLE";
                                logger.info(message1);
                                referrerDetailsHolder.offer(new XiaomiInstallReferrerResult(message1));
                                break;
                        }
                    } catch (Exception e) {
                        String message = "XiaomiReferrer onGetAppsReferrerSetupFinished: " + e.getMessage();
                        logger.error(message);
                        referrerDetailsHolder.offer(new XiaomiInstallReferrerResult(message));
                    }
                }

                @Override
                public void onGetAppsServiceDisconnected() {
                }
            });

            return referrerDetailsHolder.poll(maxWaitTimeInMilli, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            logger.error("Exception while getting referrer: ", e.getMessage());
            return new XiaomiInstallReferrerResult(e.getMessage());
        }
    }
}
