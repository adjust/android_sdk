package com.adjust.sdk;

import android.net.Uri;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by pfms on 07/11/14.
 */
public class AttributionHandler implements IAttributionHandler {
    private ScheduledExecutorService scheduler;
    private IActivityHandler activityHandler;
    private Logger logger;
    private ActivityPackage attributionPackage;
    private ScheduledFuture waitingTask;

    public AttributionHandler(IActivityHandler activityHandler, ActivityPackage attributionPackage) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        this.activityHandler = activityHandler;
        logger = AdjustFactory.getLogger();
        this.attributionPackage = attributionPackage;
    }

    public void getAttribution() {
        if (waitingTask != null) {
            waitingTask.cancel(false);
        }
        getAttribution(0);
    }

    public void checkAttribution(final JSONObject jsonResponse) {
        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                checkAttributionInternal(jsonResponse);
            }
        });
    }

    private void getAttribution(int delayInMilliseconds) {
        waitingTask = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                getAttributionInternal();
            }
        }, delayInMilliseconds, TimeUnit.MILLISECONDS);
    }

    private void checkAttributionInternal(JSONObject jsonResponse) {
        if (jsonResponse == null) return;

        JSONObject attributionJson = jsonResponse.optJSONObject("attribution");
        Attribution attribution = Attribution.fromJson(attributionJson);

        int timerMilliseconds = jsonResponse.optInt("ask_in", -1);

        if (timerMilliseconds < 0) {
            boolean updated = activityHandler.updateAttribution(attribution);

            if (updated) {
                activityHandler.launchAttributionDelegate();
            }

            activityHandler.setAskingAttribution(false);

            return;
        }

        activityHandler.setAskingAttribution(true);

        if (waitingTask != null) {
            waitingTask.cancel(false);
        }

        logger.debug("Waiting to query attribution in %d milliseconds", timerMilliseconds);

        getAttribution(timerMilliseconds);
    }

    private void getAttributionInternal() {
        logger.verbose("%s", attributionPackage.getExtendedString());
        HttpResponse httpResponse = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = getRequest(attributionPackage);
            httpResponse = client.execute(request);
        } catch (Exception e) {
            logger.error("Failed to get attribution (%s)", e.getMessage());
            return;
        }

        JSONObject jsonResponse = Util.parseJsonResponse(httpResponse, logger);
        if (jsonResponse == null) return;

        String message = jsonResponse.optString("message");

        if (message == null) {
            message = "No message found";
        }

        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            logger.debug(message);
        } else {
            logger.error(message);
        }

        checkAttributionInternal(jsonResponse);
    }

    private Uri buildUri(ActivityPackage attributionPackage) {
        Uri.Builder uriBuilder = new Uri.Builder();

        uriBuilder.scheme(Constants.SCHEME);
        uriBuilder.authority(Constants.AUTHORITY);
        uriBuilder.appendPath(attributionPackage.getPath());

        for (Map.Entry<String, String> entity : attributionPackage.getParameters().entrySet()) {
            uriBuilder.appendQueryParameter(entity.getKey(), entity.getValue());
        }

        return uriBuilder.build();
    }

    private HttpGet getRequest(ActivityPackage attributionPackage) throws URISyntaxException {
        HttpGet request = new HttpGet();
        Uri uri = buildUri(attributionPackage);
        request.setURI(new URI(uri.toString()));

        request.addHeader("Client-SDK", attributionPackage.getClientSdk());

        return request;
    }
}
