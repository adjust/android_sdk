package com.adjust.sdk;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

@Retention(RetentionPolicy.SOURCE)
@IntDef({
  DelayState.BEFORE_FILE_READ,
  DelayState.NOT_STARTED,
  DelayState.NOT_SET,
  DelayState.STARTED,
  DelayState.ENDED
})
@interface DelayState {
    int BEFORE_FILE_READ = 0;
    int NOT_STARTED = 1;
    int NOT_SET = 2;
    int STARTED = 3;
    int ENDED = 4;
}

class FirstSessionDelayManager {
    private final ActivityHandler activityHandler;
    private final List<Runnable> apiActions;
    private @DelayState int delayState;

    public FirstSessionDelayManager(final ActivityHandler activityHandler) {
        this.activityHandler = activityHandler;

        apiActions = new ArrayList<>();

        delayState = DelayState.BEFORE_FILE_READ;
    }

    public void endFirstSessionDelayI() {
        if (delayState != DelayState.STARTED) {
            return;
        }
        delayState = DelayState.ENDED;

        runInitActions();
    }

    public void activityStateFileReadI() {
        if (activityHandler.getActivityState() == null
          && activityHandler.getAdjustConfig().isFirstSessionDelayEnabled)
        {
            delayState = DelayState.STARTED;
        } else {
            delayState = DelayState.NOT_SET;

            runInitActions();
        }
    }

    private void runInitActions() {
        activityHandler.initI();

        for (final Runnable apiAction : apiActions) {
            apiAction.run();
        }
    }

    public void setCoppaComplianceInDelayI(final boolean isCoppaComplianceEnabled) {
        if (delayState != DelayState.STARTED) {
            return;
        }

        activityHandler.getAdjustConfig().coppaComplianceEnabled = isCoppaComplianceEnabled;
    }

    public void setPlayStoreKidsComplianceInDelayI(final boolean isPlayStoreKidsComplianceEnabled) {
        if (delayState != DelayState.STARTED) {
            return;
        }

        activityHandler.getAdjustConfig().playStoreKidsComplianceEnabled = isPlayStoreKidsComplianceEnabled;
    }

    public void setExternalDeviceIdInDelayI(final String externalDeviceId) {
        if (delayState != DelayState.STARTED) {
            return;
        }

        activityHandler.getAdjustConfig().externalDeviceId = externalDeviceId;
    }

    public void apiActionI(final String message, final Runnable runnable) {
        if (delayState == DelayState.STARTED) {
            activityHandler.getAdjustConfig().getLogger().debug(
                    "Enqueuing \"" + message + "\" action to be executed after first session delay ends");
            apiActions.add(runnable);
        } else {
            runnable.run();
        }
    }

    public void preLaunchActionI(final String message, final IRunActivityHandler runnableAH) {
        if (delayState == DelayState.STARTED) {
            activityHandler.getAdjustConfig().getLogger().debug(
                    "Enqueuing \"" + message + "\" action to be executed after first session delay ends");
            activityHandler.getAdjustConfig().preLaunchActions.preLaunchActionsArray.add(runnableAH);
        } else {
            runnableAH.run(activityHandler);
        }
    }

    public boolean wasSet() {
        return delayState != DelayState.NOT_SET && delayState != DelayState.BEFORE_FILE_READ;
    }
}
