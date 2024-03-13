package com.adjust.sdk.samsung.clouddev;

import android.content.Context;

import com.adjust.sdk.ILogger;
import com.samsung.android.game.cloudgame.dev.sdk.CloudDevCallback;
import com.samsung.android.game.cloudgame.dev.sdk.CloudDevSdk;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SamsungCloudDevClient {

    public static String getGoogleAdId(Context context, final ILogger logger, long maxWaitTimeInMilli) {

        try {
            final BlockingQueue<String> gaidHolder = new LinkedBlockingQueue<String>(1);

            CloudDevSdk.INSTANCE.request(context, Arrays.asList("gaid"), new CloudDevCallback() {
                @Override
                public void onSuccess(Map<String, String> kinds) { // This is called in IO thread
                    String gaid = kinds.getOrDefault("gaid", "");
                    logger.info("Google Advertising ID: " + gaid);
                    gaidHolder.offer(gaid);
                }
                @Override
                public void onError(String reason) { // This is called in IO thread
                    logger.error("Error: " + reason);
                } });

            return gaidHolder.poll(maxWaitTimeInMilli, TimeUnit.MILLISECONDS);

        } catch (Throwable t) {
            logger.error("Error while getting gps adid from samsung clould sdk: ", t.getMessage());
        }

        return null;
    }

    public static boolean isAppRunningInCloudEnvironment(Context context, final ILogger logger) {
        try {
            return CloudDevSdk.INSTANCE.isCloudEnvironment(context);
        } catch (Throwable t) {
            logger.error("Error while calling CloudDevSdk isCloudEnvironment: ", t.getMessage());
        }
        return false;
    }
}
