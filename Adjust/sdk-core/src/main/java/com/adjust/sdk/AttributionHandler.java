//
//  AttributionHandler.java
//  Adjust SDK
//
//  Created by Pedro Silva (@nonelse) on 7th November 2014.
//  Copyright (c) 2014-2018 Adjust GmbH. All rights reserved.
//

package com.adjust.sdk;

import android.net.Uri;

import com.adjust.sdk.scheduler.SingleThreadCachedScheduler;
import com.adjust.sdk.scheduler.ThreadScheduler;
import com.adjust.sdk.scheduler.TimerOnce;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class AttributionHandler implements IAttributionHandler {
    private static final String ATTRIBUTION_TIMER_NAME = "Attribution timer";
    private boolean paused;
    private String basePath;
    private String clientSdk;
    private String lastInitiatedBy;

    private ILogger logger;
    private TimerOnce timer;
    private ThreadScheduler scheduler;
    private WeakReference<IActivityHandler> activityHandlerWeakRef;

    @Override
    public void teardown() {
        logger.verbose("AttributionHandler teardown");

        if (timer != null) {
            timer.teardown();
        }
        if (scheduler != null) {
            scheduler.teardown();
        }
        if (activityHandlerWeakRef != null) {
            activityHandlerWeakRef.clear();
        }

        timer = null;
        logger = null;
        scheduler = null;
        activityHandlerWeakRef = null;
    }

    public AttributionHandler(IActivityHandler activityHandler, boolean startsSending) {
        logger = AdjustFactory.getLogger();
        scheduler = new SingleThreadCachedScheduler("AttributionHandler");
        timer = new TimerOnce(new Runnable() {
            @Override
            public void run() {
                sendAttributionRequest();
            }
        }, ATTRIBUTION_TIMER_NAME);
        basePath = activityHandler.getBasePath();
        clientSdk = activityHandler.getDeviceInfo().clientSdk;
        init(activityHandler, startsSending);
    }

    @Override
    public void init(IActivityHandler activityHandler, boolean startsSending) {
        this.activityHandlerWeakRef = new WeakReference<IActivityHandler>(activityHandler);
        this.paused = !startsSending;
    }

    @Override
    public void getAttribution() {
        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                lastInitiatedBy = "sdk";
                getAttributionI(0);
            }
        });
    }

    @Override
    public void checkSessionResponse(final SessionResponseData sessionResponseData) {
        scheduler.submit(new Runnable() {
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
        scheduler.submit(new Runnable() {
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
        scheduler.submit(new Runnable() {
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

    private void sendAttributionRequest() {
        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                sendAttributionRequestI();
            }
        });
    }

    private void getAttributionI(long delayInMilliseconds) {
        // Don't reset if new time is shorter than last one.
        if (timer.getFireIn() > delayInMilliseconds) {
            return;
        }

        if (delayInMilliseconds != 0) {
            double waitTimeSeconds = delayInMilliseconds / 1000.0;
            String secondsString = Util.SecondsDisplayFormat.format(waitTimeSeconds);
            logger.debug("Waiting to query attribution in %s seconds", secondsString);
        }

        // Set the new time the timer will fire in.
        timer.startIn(delayInMilliseconds);
    }

    private void checkAttributionI(IActivityHandler activityHandler, ResponseData responseData) {
        if (responseData.jsonResponse == null) {
            return;
        }

        long timerMilliseconds = responseData.jsonResponse.optLong("ask_in", -1);
        if (timerMilliseconds >= 0) {
            activityHandler.setAskingAttribution(true);
            lastInitiatedBy = "backend";
            getAttributionI(timerMilliseconds);
            return;
        }

        activityHandler.setAskingAttribution(false);
        JSONObject attributionJson = responseData.jsonResponse.optJSONObject("attribution");
        responseData.attribution = AdjustAttribution.fromJson(
                attributionJson,
                responseData.adid,
                Util.getSdkPrefixPlatform(clientSdk));
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
        if (activityHandlerWeakRef.get().getActivityState().isGdprForgotten) {
            return;
        }
        if (paused) {
            logger.debug("Attribution handler is paused");
            return;
        }

        // Create attribution package before sending attribution request.
        ActivityPackage attributionPackage = buildAndGetAttributionPackage();
        logger.verbose("%s", attributionPackage.getExtendedString());

        try {
            ResponseData responseData = UtilNetworking.createGETHttpsURLConnection(attributionPackage, basePath);
            if (!(responseData instanceof AttributionResponseData)) {
                return;
            }
            if (responseData.trackingState == TrackingState.OPTED_OUT) {
                activityHandlerWeakRef.get().gotOptOutResponse();
                return;
            }
            checkAttributionResponse((AttributionResponseData)responseData);
        } catch (Exception e) {
            logger.error("Failed to get attribution (%s)", e.getMessage());
        }
    }

    private ActivityPackage buildAndGetAttributionPackage() {
        long now = System.currentTimeMillis();
        IActivityHandler activityHandler = activityHandlerWeakRef.get();
        PackageBuilder packageBuilder = new PackageBuilder(
                activityHandler.getAdjustConfig(),
                activityHandler.getDeviceInfo(),
                activityHandler.getActivityState(),
                activityHandler.getSessionParameters(),
                now);
        ActivityPackage activityPackage = packageBuilder.buildAttributionPackage(lastInitiatedBy);
        lastInitiatedBy = null;
        return activityPackage;
    }
}