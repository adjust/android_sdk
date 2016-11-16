package com.adjust.sdk;

import android.net.Uri;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.*;
import java.util.Map;

import static android.R.string.no;

/**
 * Created by pfms on 07/11/14.
 */
public class AttributionHandler implements IAttributionHandler {
    private CustomScheduledExecutor scheduledExecutor;
    private WeakReference<IActivityHandler> activityHandlerWeakRef;
    private ILogger logger;
    private ActivityPackage attributionPackage;
    private TimerOnce timer;
    private static final String ATTRIBUTION_TIMER_NAME = "Attribution timer";

    private boolean paused;
    private boolean hasListener;

    private URL lastUrlUsed;

    @Override
    public final void teardown() {
        logger.verbose("AttributionHandler teardown");
        if (timer != null) {
            timer.teardown();
        }
        if (scheduledExecutor != null) {
            try {
                scheduledExecutor.shutdownNow();
            } catch (SecurityException ignored) {
            }
        }
        if (activityHandlerWeakRef != null) {
            activityHandlerWeakRef.clear();
        }
        scheduledExecutor = null;
        activityHandlerWeakRef = null;
        logger = null;
        attributionPackage = null;
        timer = null;
    }

    public AttributionHandler(final IActivityHandler activityHandler,
                              final ActivityPackage attributionPackage,
                              final boolean startsSending,
                              final boolean hasListener) {
        scheduledExecutor = new CustomScheduledExecutor("AttributionHandler");
        logger = AdjustFactory.getLogger();

        timer = new TimerOnce(scheduledExecutor, new Runnable() {
            @Override
            public void run() {
                getAttributionI();
            }
        }, ATTRIBUTION_TIMER_NAME);

        init(activityHandler, attributionPackage, startsSending, hasListener);
    }

    @Override
    public final void init(final IActivityHandler activityHandler,
                     final ActivityPackage attributionPackage,
                     final boolean startsSending,
                     final boolean hasListener) {
        this.activityHandlerWeakRef = new WeakReference<IActivityHandler>(activityHandler);
        this.attributionPackage = attributionPackage;
        this.paused = !startsSending;
        this.hasListener = hasListener;
    }

    @Override
    public final void getAttribution() {
        getAttribution(0);
    }

    @Override
    public final void checkSessionResponse(final SessionResponseData sessionResponseData) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                IActivityHandler activityHandler = activityHandlerWeakRef.get();
                if (activityHandler == null) {
                    return;
                }
                checkSessionResponseI(activityHandler, sessionResponseData);
            }
        });
    }

    public final void checkAttributionResponse(final AttributionResponseData attributionResponseData) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                IActivityHandler activityHandler = activityHandlerWeakRef.get();
                if (activityHandler == null) {
                    return;
                }

                checkAttributionResponseI(activityHandler, attributionResponseData);
            }
        });
    }

    @Override
    public final void pauseSending() {
        paused = true;
    }

    @Override
    public final void resumeSending() {
        paused = false;
    }

    private void getAttribution(final long delayInMilliseconds) {
        // don't reset if new time is shorter than last one
        if (timer.getFireIn() > delayInMilliseconds) {
            return;
        }

        if (delayInMilliseconds != 0) {
            double waitTimeSeconds = delayInMilliseconds / 1000.0;
            String secondsString = Util.SecondsDisplayFormat.format(waitTimeSeconds);

            logger.debug("Waiting to query attribution in %s seconds", secondsString);
        }

        // set the new time the timer will fire in
        timer.startIn(delayInMilliseconds);
    }

    private void checkAttributionI(final IActivityHandler activityHandler,
                                         final ResponseData responseData) {
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

    private void checkSessionResponseI(final IActivityHandler activityHandler,
                                       final SessionResponseData sessionResponseData) {
        checkAttributionI(activityHandler, sessionResponseData);

        activityHandler.launchSessionResponseTasks(sessionResponseData);
    }

    private void checkAttributionResponseI(final IActivityHandler activityHandler,
                                           final AttributionResponseData attributionResponseData) {
        checkAttributionI(activityHandler, attributionResponseData);

        checkDeeplinkI(attributionResponseData);

        activityHandler.launchAttributionResponseTasks(attributionResponseData);
    }

    private void checkDeeplinkI(final AttributionResponseData attributionResponseData) {
        if (attributionResponseData.jsonResponse == null) {
            return;
        }

        JSONObject attributionJson = attributionResponseData.jsonResponse.optJSONObject("attribution");
        if (attributionJson == null) {
            return;
        }

        String deeplinkString = attributionJson.optString("deeplink", null);
        if (deeplinkString == null) {
            return;
        }

        attributionResponseData.deeplink = Uri.parse(deeplinkString);
    }

    private void getAttributionI() {
        if (!hasListener) {
            return;
        }

        if (paused) {
            logger.debug("Attribution handler is paused");
            return;
        }

        logger.verbose("%s", attributionPackage.getExtendedString());

        try {
            AdjustFactory.URLGetConnection urlGetConnection = Util.createGETHttpsURLConnection(
                    buildUriI(attributionPackage.getPath(), attributionPackage.getParameters()).toString(),
                    attributionPackage.getClientSdk());

            ResponseData responseData = Util.readHttpResponse(urlGetConnection.httpsURLConnection, attributionPackage);
            lastUrlUsed = urlGetConnection.url;

            if (!(responseData instanceof AttributionResponseData)) {
                return;
            }

            checkAttributionResponse((AttributionResponseData) responseData);
        } catch (Exception e) {
            logger.error("Failed to get attribution (%s)", e.getMessage());
        }
    }

    private Uri buildUriI(final String path,
                          final Map<String, String> parameters) {
        Uri.Builder uriBuilder = new Uri.Builder();

        uriBuilder.scheme(Constants.SCHEME);
        uriBuilder.authority(Constants.AUTHORITY);
        uriBuilder.appendPath(path);

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        long now = System.currentTimeMillis();

        final DateFormat dateFormat = new SimpleDateFormat(Util.DATE_FORMAT);
        String dateString = dateFormat.format(now);

        uriBuilder.appendQueryParameter("sent_at", dateString);

        return uriBuilder.build();
    }

    public URL getLastUrlUsed() {
        return lastUrlUsed;
    }
}