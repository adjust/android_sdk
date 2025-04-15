//
//  AttributionHandler.java
//  Adjust SDK
//
//  Created by Pedro Silva (@nonelse) on 7th November 2014.
//  Copyright (c) 2014-2018 Adjust GmbH. All rights reserved.
//

package com.adjust.sdk;

import android.net.Uri;

import com.adjust.sdk.network.IActivityPackageSender;
import com.adjust.sdk.scheduler.SingleThreadCachedScheduler;
import com.adjust.sdk.scheduler.ThreadScheduler;
import com.adjust.sdk.scheduler.TimerOnce;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class AttributionHandler implements IAttributionHandler,
        IActivityPackageSender.ResponseDataCallbackSubscriber
{
    private static final String ATTRIBUTION_TIMER_NAME = "Attribution timer";
    private boolean paused;
    private String lastInitiatedBy;
    private IActivityPackageSender activityPackageSender;

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

    public AttributionHandler(IActivityHandler activityHandler,
                              boolean startsSending,
                              IActivityPackageSender attributionHandlerActivityPackageSender)
    {
        logger = AdjustFactory.getLogger();
        scheduler = new SingleThreadCachedScheduler("AttributionHandler");
        timer = new TimerOnce(new Runnable() {
            @Override
            public void run() {
                sendAttributionRequest();
            }
        }, ATTRIBUTION_TIMER_NAME);

        init(activityHandler, startsSending, attributionHandlerActivityPackageSender);
    }

    @Override
    public void init(IActivityHandler activityHandler,
                     boolean startsSending,
                     IActivityPackageSender attributionHandlerActivityPackageSender)
    {
        this.activityHandlerWeakRef = new WeakReference<IActivityHandler>(activityHandler);
        this.paused = !startsSending;
        this.activityPackageSender = attributionHandlerActivityPackageSender;
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

        Long timerMilliseconds = responseData.askIn; // responseData.jsonResponse.optLong("ask_in", -1);
        if (timerMilliseconds != null && timerMilliseconds >= 0) {
            activityHandler.setAskingAttribution(true);
            lastInitiatedBy = "backend";
            getAttributionI(timerMilliseconds);
            return;
        }

        activityHandler.setAskingAttribution(false);
    }

    private void checkSessionResponseI(IActivityHandler activityHandler,
                                       SessionResponseData sessionResponseData)
    {
        checkAttributionI(activityHandler, sessionResponseData);
        activityHandler.launchSessionResponseTasks(sessionResponseData);
    }

    private void checkSdkClickResponseI(IActivityHandler activityHandler,
                                        SdkClickResponseData sdkClickResponseData)
    {
        checkAttributionI(activityHandler, sdkClickResponseData);
        activityHandler.launchSdkClickResponseTasks(sdkClickResponseData);
    }

    private void checkAttributionResponseI(IActivityHandler activityHandler,
                                           AttributionResponseData attributionResponseData)
    {
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
        ActivityPackage attributionPackage = buildAndGetAttributionPackage(activityHandlerWeakRef.get().getInternalState());
        logger.verbose("%s", attributionPackage.getExtendedString());

        activityPackageSender.sendActivityPackage(
                attributionPackage,
                null,
                this);
    }

    private ActivityPackage buildAndGetAttributionPackage(ActivityHandler.InternalState internalState) {
        long now = System.currentTimeMillis();
        IActivityHandler activityHandler = activityHandlerWeakRef.get();
        PackageBuilder packageBuilder = new PackageBuilder(
                activityHandler.getAdjustConfig(),
                activityHandler.getDeviceInfo(),
                activityHandler.getActivityState(),
                activityHandler.getGlobalParameters(),
                activityHandler.getFirstSessionDelayManager(),
                now);
        packageBuilder.internalState = internalState;

        ActivityPackage activityPackage = packageBuilder.buildAttributionPackage(lastInitiatedBy);
        lastInitiatedBy = null;
        return activityPackage;
    }

    @Override
    public void onResponseDataCallback(final ResponseData responseData) {
        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                IActivityHandler activityHandler = activityHandlerWeakRef.get();
                if (activityHandler == null) {
                    return;
                }

                if (responseData.trackingState == TrackingState.OPTED_OUT) {
                    activityHandler.gotOptOutResponse();
                    return;
                }

                if (!(responseData instanceof AttributionResponseData)) {
                    return;
                }

                checkAttributionResponseI(activityHandler, (AttributionResponseData)responseData);
            }
        });
    }
}