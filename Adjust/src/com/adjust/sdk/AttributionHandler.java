package com.adjust.sdk;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by pfms on 07/11/14.
 */
class AttributionHandler {
    private static final String url = Constants.BASE_URL + "/attribution";
    private ScheduledExecutorService scheduler;
    private ScheduledExecutorService maxTimeScheduler;
    private ActivityHandler activityHandler;
    private Logger logger;

    AttributionHandler(ActivityHandler activityHandler, Integer maxTimeMilliseconds) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        this.activityHandler = activityHandler;
        logger = AdjustFactory.getLogger();

        if (maxTimeMilliseconds != null) {
            maxTimeScheduler = Executors.newSingleThreadScheduledExecutor();
            maxTimeScheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    launchAttributionDelegate();
                }
            }, maxTimeMilliseconds, TimeUnit.MILLISECONDS);
        }
    }

    private void launchAttributionDelegate() {
        this.activityHandler.launchAttributionDelegate();
    }

    void getAttribution(int delayInMilliseconds) {
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                getAttributionInternal();
            }
        }, delayInMilliseconds, TimeUnit.MILLISECONDS);
    }

    void getAttribution() {
        getAttribution(0);
    }

    void checkAttribution(final JSONObject jsonResponse, int delayInMilliseconds) {
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                checkAttributionInternal(jsonResponse);
            }
        }, delayInMilliseconds, TimeUnit.MILLISECONDS);
    }

    void checkAttribution(final JSONObject jsonResponse) {
        checkAttribution(jsonResponse, 0);
    }


    private void checkAttributionInternal(JSONObject jsonResponse) {
        if (jsonResponse == null) return;

        JSONObject attributionJson = jsonResponse.optJSONObject("attribution");
        Attribution attribution = Attribution.fromJson(attributionJson);

        Integer timerMilliseconds = null;
        try {
            timerMilliseconds = jsonResponse.getInt("ask_in");
        } catch (JSONException e) { }

        if (attribution != null && timerMilliseconds == null) {
            attribution.finalAttribution = true;
        }

        activityHandler.updateAttribution(attribution);

        if (timerMilliseconds == null) {
            activityHandler.launchAttributionDelegate();
            return;
        }

        getAttribution(timerMilliseconds);
    }

    private void getAttributionInternal() {
        HttpResponse httpResponse = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            httpResponse = client.execute(request);
        } catch (Exception e) {
            logger.error("Failed to get attribution (%s)", e.getMessage());
            return;
        }

        String response = Util.parseResponse(httpResponse, logger);
        JSONObject jsonResponse = Util.buildJsonObject(response);

        if (jsonResponse == null) {
            logger.error("Failed to parse json attribution response: %s", response);
            return;
        }
        checkAttributionInternal(jsonResponse);
    }
}
