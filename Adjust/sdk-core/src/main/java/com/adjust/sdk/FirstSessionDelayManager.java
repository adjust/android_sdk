package com.adjust.sdk;

import java.util.ArrayList;
import java.util.List;

class FirstSessionDelayManager {
    private final ActivityHandler activityHandler;
    private List<Runnable> apiActions;
    private String delayStatus;

    public FirstSessionDelayManager(
      final ActivityHandler activityHandler)
    {
        this.activityHandler = activityHandler;

        apiActions = new ArrayList<>();
/*
        boolean delayFirstSession = activityHandler.getInternalState().isFirstLaunch()
          && activityHandler.getAdjustConfig().isFirstSessionDelayEnabled;

        if (delayFirstSession) {
            delayStatus = "notStarted";
        } else {
            delayStatus = "notSet";
        }

 */
        delayStatus = "beforeFileRead";
    }

    public void endFirstSessionDelayI() {
        if (!"started".equals(delayStatus)) {
            return;
        }
        delayStatus = "stopped";

        activityHandler.initI();

        for (final Runnable apiAction : apiActions) {
            apiAction.run();
        }
    }

    public void activityStateFileReadI() {
        boolean delayFirstSession = activityHandler.getActivityState() == null
          && activityHandler.getAdjustConfig().isFirstSessionDelayEnabled;

        if (delayFirstSession) {
            delayStatus = "started";
            return;
        }
        delayStatus = "notSet";

        activityHandler.initI();

        for (final Runnable apiAction : apiActions) {
            apiAction.run();
        }
    }

    public void setCoppaComplianceInDelayI(final boolean isCoppaComplianceEnabled) {
        if (!"started".equals(delayStatus)) {
            return;
        }

        activityHandler.getAdjustConfig().coppaComplianceEnabled = isCoppaComplianceEnabled;
    }

    public void setExternalDeviceIdInDelayI(final String externalDeviceId) {
        if (!"started".equals(delayStatus)) {
            return;
        }

        activityHandler.getAdjustConfig().externalDeviceId = externalDeviceId;
    }

    public void apiActionI(final String message, final Runnable runnable) {
        if ("started".equals(delayStatus)) {
            activityHandler.getAdjustConfig().getLogger().debug(
                    "Enqueuing \"" + message + "\" action to be executed after first session delay ends");
            apiActions.add(runnable);
        } else {
            runnable.run();
        }
    }

    public void preLaunchActionI(final String message, final IRunActivityHandler runnableAH) {
        if ("started".equals(delayStatus)) {
            activityHandler.getAdjustConfig().getLogger().debug(
                    "Enqueuing \"" + message + "\" action to be executed after first session delay ends");
            activityHandler.getAdjustConfig().preLaunchActions.preLaunchActionsArray.add(runnableAH);
        } else {
            runnableAH.run(activityHandler);
        }
    }

    public boolean wasSet() {
        return ! "notSet".equals(delayStatus);
    }
}
