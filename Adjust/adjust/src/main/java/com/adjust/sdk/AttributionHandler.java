package com.adjust.sdk;

import android.net.Uri;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by pfms on 07/11/14.
 */
public class AttributionHandler implements IAttributionHandler {
    private ScheduledExecutorService scheduler;
    private IActivityHandler activityHandler;
    private ILogger logger;
    private ActivityPackage attributionPackage;
    private TimerOnce timer;

    private boolean paused;
    private boolean hasListener;

    public AttributionHandler(IActivityHandler activityHandler,
                              ActivityPackage attributionPackage,
                              boolean startPaused,
                              boolean hasListener) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        logger = AdjustFactory.getLogger();

        if (this.scheduler != null) {
            timer = new TimerOnce(scheduler, new Runnable() {
                @Override
                public void run() {
                    getAttributionInternal();
                }
            });
        } else {
            this.logger.error("Timer not initialized, attribution handler is disabled");
        }

        init(activityHandler, attributionPackage, startPaused, hasListener);
    }

    @Override
    public void init(IActivityHandler activityHandler,
                     ActivityPackage attributionPackage,
                     boolean startPaused,
                     boolean hasListener) {
        this.activityHandler = activityHandler;
        this.attributionPackage = attributionPackage;
        this.paused = startPaused;
        this.hasListener = hasListener;
    }

    @Override
    public void getAttribution() {
        getAttribution(0);
    }

    @Override
    public void checkResponse(final ResponseData responseData) {
        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                checkResponseInternal(responseData);
            }
        });
    }

    @Override
    public void pauseSending() {
        paused = true;
    }

    @Override
    public void resumeSending() {
        paused = false;
    }

    private void getAttribution(long delayInMilliseconds) {
        // don't reset if new time is shorter than last one
        if (timer.getFireIn() > delayInMilliseconds) {
            return;
        }

        if (delayInMilliseconds != 0) {
            logger.debug("Waiting to query attribution in %d milliseconds", delayInMilliseconds);
        }

        // set the new time the timer will fire in
        timer.startIn(delayInMilliseconds);
    }

    private void checkAttributionInternal(ResponseData responseData) {
        if (responseData.jsonResponse == null) {
            return;
        }

        long timerMilliseconds = responseData.jsonResponse.optLong("ask_in", -1);

        if (timerMilliseconds >= 0) {
            activityHandler.setAskingAttribution(true);

            getAttribution(timerMilliseconds);

            return;
        }
        activityHandler.setAskingAttribution(false);

        JSONObject attributionJson = responseData.jsonResponse.optJSONObject("attribution");
        responseData.attribution = AdjustAttribution.fromJson(attributionJson);
    }

    private void checkResponseInternal(ResponseData responseData) {
        checkAttributionInternal(responseData);

        activityHandler.launchResponseTasks(responseData);
    }

    private void getAttributionInternal() {
        if (!hasListener) {
            return;
        }

        if (paused) {
            logger.debug("Attribution handler is paused");
            return;
        }

        logger.verbose("%s", attributionPackage.getExtendedString());

        try {
            HttpsURLConnection connection = Util.createGETHttpsURLConnection(
                    buildUri(attributionPackage.getPath(), attributionPackage.getParameters()).toString(),
                    attributionPackage.getClientSdk());

            ResponseData responseData = Util.readHttpResponse(connection, attributionPackage);

            checkResponseInternal(responseData);
        } catch (Exception e) {
            logger.error("Failed to get attribution (%s)", e.getMessage());
            return;
        }
    }

    private Uri buildUri(String path, Map<String, String> parameters) {
        Uri.Builder uriBuilder = new Uri.Builder();

        uriBuilder.scheme(Constants.SCHEME);
        uriBuilder.authority(Constants.AUTHORITY);
        uriBuilder.appendPath(path);

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        return uriBuilder.build();
    }
}