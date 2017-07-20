package com.adjust.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pfms on 04/12/14.
 */
public class AdjustInstance {
    private ActivityHandler activityHandler;
    private List<IRunActivityHandler> preLaunchActionsArray;
    private String pushToken;
    private Boolean startEnabled = null;
    private boolean startOffline = false;

    private static ILogger getLogger() {
        return AdjustFactory.getLogger();
    }

    public void onCreate(AdjustConfig adjustConfig) {
        if (activityHandler != null) {
            getLogger().error("Adjust already initialized");
            return;
        }

        adjustConfig.preLaunchActionsArray = preLaunchActionsArray;
        adjustConfig.pushToken = pushToken;
        adjustConfig.startEnabled = startEnabled;
        adjustConfig.startOffline = startOffline;

        activityHandler = ActivityHandler.getInstance(adjustConfig);
    }

    public void trackEvent(AdjustEvent event) {
        if (!checkActivityHandler()) { return; }
        activityHandler.trackEvent(event);
    }

    public void onResume() {
        if (!checkActivityHandler()) { return; }
        activityHandler.onResume();
    }

    public void onPause() {
        if (!checkActivityHandler()) { return; }
        activityHandler.onPause();
    }

    public void setEnabled(boolean enabled) {
        if (!checkActivityHandler(enabled, "enabled mode", "disabled mode")){
            this.startEnabled = enabled;
        } else {
            activityHandler.setEnabled(enabled);
        }
    }

    public boolean isEnabled() {
        if (!checkActivityHandler()) { return false; }
        return activityHandler.isEnabled();
    }

    public void appWillOpenUrl(Uri url) {
        if (!checkActivityHandler()) { return; }
        long clickTime = System.currentTimeMillis();
        activityHandler.readOpenUrl(url, clickTime);
    }

    public void sendReferrer(String referrer, Context context) {
        long clickTime = System.currentTimeMillis();

        // Check for referrer validity. If invalid, return.
        if (referrer == null || referrer.length() == 0) {
            return;
        }

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        sharedPreferencesManager.saveReferrerToSharedPreferences(referrer, clickTime);

        if (checkActivityHandler("referrer")) {
            activityHandler.sendReferrer(referrer, clickTime);
        }
    }

    public void setOfflineMode(boolean enabled) {
        if (!checkActivityHandler(enabled, "offline mode", "online mode")) {
            this.startOffline = enabled;
        } else {
            activityHandler.setOfflineMode(enabled);
        }
    }

    public void sendFirstPackages() {
        if (!checkActivityHandler()) { return; }
        activityHandler.sendFirstPackages();
    }

    public void addSessionCallbackParameter(final String key, final String value) {
        if (checkActivityHandler("adding session callback parameter")) {
            activityHandler.addSessionCallbackParameter(key, value);
            return;
        }

        if (preLaunchActionsArray == null) {
            preLaunchActionsArray = new ArrayList<IRunActivityHandler>();
        }

        preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(ActivityHandler activityHandler) {
                activityHandler.addSessionCallbackParameterI(key, value);
            }
        });
    }

    public void addSessionPartnerParameter(final String key, final String value) {
        if (checkActivityHandler("adding session partner parameter")) {
            activityHandler.addSessionPartnerParameter(key, value);
            return;
        }

        if (preLaunchActionsArray == null) {
            preLaunchActionsArray = new ArrayList<IRunActivityHandler>();
        }

        preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(ActivityHandler activityHandler) {
                activityHandler.addSessionPartnerParameterI(key, value);
            }
        });
    }

    public void removeSessionCallbackParameter(final String key) {
        if (checkActivityHandler("removing session callback parameter")) {
            activityHandler.removeSessionCallbackParameter(key);
            return;
        }

        if (preLaunchActionsArray == null) {
            preLaunchActionsArray = new ArrayList<IRunActivityHandler>();
        }

        preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(ActivityHandler activityHandler) {
                activityHandler.removeSessionCallbackParameterI(key);
            }
        });
    }

    public void removeSessionPartnerParameter(final String key) {
        if (checkActivityHandler("removing session partner parameter")) {
            activityHandler.removeSessionPartnerParameter(key);
            return;
        }

        if (preLaunchActionsArray == null) {
            preLaunchActionsArray = new ArrayList<IRunActivityHandler>();
        }

        preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(ActivityHandler activityHandler) {
                activityHandler.removeSessionPartnerParameterI(key);
            }
        });
    }

    public void resetSessionCallbackParameters() {
        if (checkActivityHandler("resetting session callback parameters")) {
            activityHandler.resetSessionCallbackParameters();
            return;
        }

        if (preLaunchActionsArray == null) {
            preLaunchActionsArray = new ArrayList<IRunActivityHandler>();
        }

        preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(ActivityHandler activityHandler) {
                activityHandler.resetSessionCallbackParametersI();
            }
        });
    }

    public void resetSessionPartnerParameters() {
        if (checkActivityHandler("resetting session partner parameters")) {
            activityHandler.resetSessionPartnerParameters();
            return;
        }

        if (preLaunchActionsArray == null) {
            preLaunchActionsArray = new ArrayList<IRunActivityHandler>();
        }

        preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(ActivityHandler activityHandler) {
                activityHandler.resetSessionPartnerParametersI();
            }
        });
    }

    public void teardown(boolean deleteState) {
        if (!checkActivityHandler()) { return; }
        activityHandler.teardown(deleteState);
        activityHandler = null;
    }

    public void setPushToken(String token) {
        if (!checkActivityHandler("push token")) {
            this.pushToken = token;
        } else {
            activityHandler.setPushToken(token);
        }
    }

    public String getAdid() {
        if (!checkActivityHandler()) { return null; }
        return activityHandler.getAdid();
    }

    public AdjustAttribution getAttribution() {
        if (!checkActivityHandler()) { return null; }
        return activityHandler.getAttribution();
    }

    private boolean checkActivityHandler() {
        return checkActivityHandler(null);
    }

    private boolean checkActivityHandler(boolean status, String trueMessage, String falseMessage) {
        if (status) {
            return checkActivityHandler(trueMessage);
        } else {
            return checkActivityHandler(falseMessage);
        }
    }

    private boolean checkActivityHandler(String savedForLaunchWarningSuffixMessage) {
        if (activityHandler == null) {
            if (savedForLaunchWarningSuffixMessage != null) {
                getLogger().warn("Adjust not initialized, but %s saved for launch", savedForLaunchWarningSuffixMessage);
            } else {
                getLogger().error("Adjust not initialized correctly");
            }
            return false;
        } else {
            return true;
        }
    }
}
