package com.adjust.sdk;

import android.net.Uri;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Map;

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
    private String basePath;

    @Override
    public void teardown() {
        logger.verbose("AttributionHandler teardown");
        if (timer != null) {
            timer.teardown();
        }
        if (scheduledExecutor != null) {
            try {
                scheduledExecutor.shutdownNow();
            } catch(SecurityException se) {}
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

    public AttributionHandler(IActivityHandler activityHandler,
                              ActivityPackage attributionPackage,
                              boolean startsSending) {
        scheduledExecutor = new CustomScheduledExecutor("AttributionHandler", false);
        logger = AdjustFactory.getLogger();

        timer = new TimerOnce(new Runnable() {
            @Override
            public void run() {
                sendAttributionRequest();
            }
        }, ATTRIBUTION_TIMER_NAME);

        basePath = activityHandler.getBasePath();
        init(activityHandler, attributionPackage, startsSending);
    }

    @Override
    public void init(IActivityHandler activityHandler,
                     ActivityPackage attributionPackage,
                     boolean startsSending) {
        this.activityHandlerWeakRef = new WeakReference<IActivityHandler>(activityHandler);
        this.attributionPackage = attributionPackage;
        this.paused = !startsSending;
    }

    @Override
    public void getAttribution() {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                getAttributionI(0);
            }
        });
    }

    @Override
    public void checkSessionResponse(final SessionResponseData sessionResponseData) {
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

    @Override
    public void checkSdkClickResponse(final SdkClickResponseData sdkClickResponseData) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                IActivityHandler activityHandler = activityHandlerWeakRef.get();
                if (activityHandler == null) {
                    return;
                }

                checkSdkClickResponseI(activityHandler, sdkClickResponseData);
            }
        });
    }

    public void checkAttributionResponse(final AttributionResponseData attributionResponseData) {
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
    public void pauseSending() {
        paused = true;
    }

    @Override
    public void resumeSending() {
        paused = false;
    }

    public void sendAttributionRequest() {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                sendAttributionRequestI();
            }
        });
    }

    private void getAttributionI(long delayInMilliseconds) {
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

    private void checkAttributionI(IActivityHandler activityHandler, ResponseData responseData) {
        if (responseData.jsonResponse == null) {
            return;
        }

        long timerMilliseconds = responseData.jsonResponse.optLong("ask_in", -1);

        if (timerMilliseconds >= 0) {
            activityHandler.setAskingAttribution(true);

            getAttributionI(timerMilliseconds);

            return;
        }
        activityHandler.setAskingAttribution(false);

        JSONObject attributionJson = responseData.jsonResponse.optJSONObject("attribution");
        responseData.attribution = AdjustAttribution.fromJson(attributionJson, responseData.adid);
    }

    private void checkSessionResponseI(IActivityHandler activityHandler, SessionResponseData sessionResponseData) {
        checkAttributionI(activityHandler, sessionResponseData);

        activityHandler.launchSessionResponseTasks(sessionResponseData);
    }

    private void checkSdkClickResponseI(IActivityHandler activityHandler, SdkClickResponseData sdkClickResponseData) {
        checkAttributionI(activityHandler, sdkClickResponseData);

        activityHandler.launchSdkClickResponseTasks(sdkClickResponseData);
    }

    private void checkAttributionResponseI(IActivityHandler activityHandler, AttributionResponseData attributionResponseData) {
        checkAttributionI(activityHandler, attributionResponseData);

        checkDeeplinkI(attributionResponseData);

        activityHandler.launchAttributionResponseTasks(attributionResponseData);
    }

    private void checkDeeplinkI(AttributionResponseData attributionResponseData) {
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

    private void sendAttributionRequestI() {
        if (paused) {
            logger.debug("Attribution handler is paused");
            return;
        }

        logger.verbose("%s", attributionPackage.getExtendedString());

        try {
            ResponseData responseData = UtilNetworking.createGETHttpsURLConnection(attributionPackage, basePath);

            if (!(responseData instanceof AttributionResponseData)) {
                return;
            }

            checkAttributionResponse((AttributionResponseData)responseData);
        } catch (Exception e) {
            logger.error("Failed to get attribution (%s)", e.getMessage());
            return;
        }
    }
}