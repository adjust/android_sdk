package com.adjust.sdk;

/**
 * Created by pfms on 09/08/2016.
 */
public class StateSession {
    boolean toSend = true;
    int sessionCount = 1;
    int subsessionCount = 1;
    SessionType sessionType = null;
    int eventCount = 0;
    Boolean getAttributionIsCalled = null;
    boolean eventBufferingIsEnabled = false;
    boolean foregroundTimerStarts = true;
    boolean foregroundTimerAlreadyStarted = false;
    boolean sendInBackgroundConfigured = false;
    boolean sdkClickHandlerAlsoStartsPaused = true;
    boolean startSubsession = true;
    boolean disabled = false;
    String delayStart = null;
    boolean activityStateAlreadyCreated = false;

    public enum SessionType {
        NEW_SESSION,
        NEW_SUBSESSION,
        TIME_TRAVEL,
        NONSESSION,
        DISABLED,
    }

    StateSession(SessionType sessionType) {
        this.sessionType = sessionType;
    }
}
