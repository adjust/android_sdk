package com.adjust.sdk;

import com.adjust.sdk.scheduler.TimerOnce;

public class AdjustTimeoutCallback {
    private OnAdidReadListener onAdidReadListener;
    private OnAttributionReadListener onAttributionReadListener;
    private OnThirdPartySharingSettingsReadListener onThirdPartySharingSettingsReadListener;
    private TimerOnce timeoutTimer;

    public AdjustTimeoutCallback(OnAdidReadListener onAdidReadListener) {
        this.onAdidReadListener = onAdidReadListener;
    }

    public AdjustTimeoutCallback(OnAttributionReadListener onAttributionReadListener) {
        this.onAttributionReadListener = onAttributionReadListener;
    }

    public AdjustTimeoutCallback(OnThirdPartySharingSettingsReadListener onThirdPartySharingSettingsReadListener) {
        this.onThirdPartySharingSettingsReadListener = onThirdPartySharingSettingsReadListener;
    }

    public void setOnAdidReadListener(OnAdidReadListener onAdidReadListener) {
        this.onAdidReadListener = onAdidReadListener;
    }

    public void setOnAttributionReadListener(OnAttributionReadListener onAttributionReadListener) {
        this.onAttributionReadListener = onAttributionReadListener;
    }

    public void setOnThirdPartySharingSettingsReadListener(OnThirdPartySharingSettingsReadListener onThirdPartySharingSettingsReadListener) {
        this.onThirdPartySharingSettingsReadListener = onThirdPartySharingSettingsReadListener;
    }

    public void setTimer(TimerOnce timer) {
        this.timeoutTimer = timer;
    }

    public OnAdidReadListener getOnAdidReadListener() {
        return onAdidReadListener;
    }

    public OnAttributionReadListener getOnAttributionReadListener() {
        return onAttributionReadListener;
    }

    public OnThirdPartySharingSettingsReadListener getOnThirdPartySharingSettingsReadListener() {
        return onThirdPartySharingSettingsReadListener;
    }

    public TimerOnce getTimeoutTimer() {
        return timeoutTimer;
    }
}
