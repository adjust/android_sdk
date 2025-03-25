package com.adjust.sdk;

import java.util.ArrayList;
import java.util.List;

class FirstSessionDelayManager {
    private final ActivityHandler activityHandler;
    private List<Runnable> apiActions;
    private String delayStatus;
    private Runnable initBlock;

    public FirstSessionDelayManager(
      final ActivityHandler activityHandler)
    {
        this.activityHandler = activityHandler;

        apiActions = new ArrayList<>();

        boolean delayFirstSession = activityHandler.getInternalState().isFirstLaunch()
          && activityHandler.getAdjustConfig().isFirstSessionDelayEnabled;

        if (delayFirstSession) {
            delayStatus = "notStarted";
        } else {
            delayStatus = "notSet";
        }
    }

    public void stopFirstSessionDelay() {
        if (!"started".equals(delayStatus)) {
            return;
        }
        delayStatus = "stopped";

        activityHandler.executor.submit(() -> {
            initBlock.run();
            for (final Runnable apiAction : apiActions) {
                apiAction.run();
            }
        });
    }

    public void delayOrInit(final Runnable initBlock) {
        if ("notStarted".equals(delayStatus)) {
            this.initBlock = initBlock;
            delayStatus = "started";
            return;
        }

        if ("notSet".equals(delayStatus)) {
            activityHandler.executor.submit(() -> {
                initBlock.run();
                for (final Runnable apiAction : apiActions) {
                    apiAction.run();
                }
            });
            return;
        }
    }

    public void setCoppaComplianceInDelay(final boolean isCoppaComplianceEnabled) {
        if (!"started".equals(delayStatus)) {
            return;
        }

        activityHandler.getAdjustConfig().coppaComplianceEnabled = isCoppaComplianceEnabled;
    }

    public void setExternalDeviceIdInDelay(final String externalDeviceId) {
        if (!"started".equals(delayStatus)) {
            return;
        }

        activityHandler.getAdjustConfig().externalDeviceId = externalDeviceId;
    }

    public void apiAction(final Runnable runnable) {
        if ("started".equals(delayStatus)) {
            apiActions.add(runnable);
        } else {
            activityHandler.executor.submit(runnable);
        }
    }

    public void preLaunchAction(final IRunActivityHandler runnableAH) {
        if ("started".equals(delayStatus)) {
            activityHandler.getAdjustConfig().preLaunchActions.preLaunchActionsArray.add(runnableAH);
        } else {
            activityHandler.executor.submit(() -> {
                runnableAH.run(activityHandler);
            });
        }
    }
}
