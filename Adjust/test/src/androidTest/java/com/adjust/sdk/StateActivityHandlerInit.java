package com.adjust.sdk;

import com.adjust.sdk.ActivityHandler;

/**
 * Created by pfms on 09/08/2016.
 */
public class StateActivityHandlerInit {
    ActivityHandler.InternalState internalState;
    boolean startEnabled = true;
    boolean updatePackages = false;
    boolean startsSending = false;
    boolean sdkClickHandlerAlsoStartsPaused = true;
    String defaultTracker = null;
    boolean eventBufferingIsEnabled = false;
    boolean sendInBackgroundConfigured = false;
    boolean delayStartConfigured = false;
    boolean activityStateAlreadyCreated = false;
    String sendReferrer = null;
    String readActivityState = null;
    String readAttribution = null;
    String readSessionParameters = null;
    String readCallbackParameters = null;
    String readPartnerParameters = null;
    int foregroundTimerStart = 60;
    int foregroundTimerCycle = 60;

    StateActivityHandlerInit(ActivityHandler activityHandler) {
        internalState = activityHandler.getInternalState();
    }
}
