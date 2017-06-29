package com.adjust.sdk;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pfms on 04/12/14.
 */
public class AdjustInstance {
    private String referrer;
    private long referrerClickTime;
    private ActivityHandler activityHandler;
    private List<IRunActivityHandler> sessionParametersActionsArray;
    private String pushToken;

    private static ILogger getLogger() {
        return AdjustFactory.getLogger();
    }

    public void onCreate(AdjustConfig adjustConfig) {
        if (activityHandler != null) {
            getLogger().error("Adjust already initialized");
            return;
        }

        adjustConfig.referrer = this.referrer;
        adjustConfig.referrerClickTime = this.referrerClickTime;
        adjustConfig.sessionParametersActionsArray = sessionParametersActionsArray;
        adjustConfig.pushToken = pushToken;

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
        if (!checkActivityHandler()) { return; }
        activityHandler.setEnabled(enabled);
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

    public void sendReferrer(String referrer) {
        long clickTime = System.currentTimeMillis();
        // sendReferrer might be triggered before Adjust
        if (!checkActivityHandler("referrer")) {
            // save it to inject in the config before launch
            this.referrer = referrer;
            this.referrerClickTime = clickTime;
        } else {
            activityHandler.sendReferrer(referrer, clickTime);
        }
    }

    public void setOfflineMode(boolean enabled) {
        if (!checkActivityHandler()) { return; }
        activityHandler.setOfflineMode(enabled);
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

        if (sessionParametersActionsArray == null) {
            sessionParametersActionsArray = new ArrayList<IRunActivityHandler>();
        }

        sessionParametersActionsArray.add(new IRunActivityHandler() {
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

        if (sessionParametersActionsArray == null) {
            sessionParametersActionsArray = new ArrayList<IRunActivityHandler>();
        }

        sessionParametersActionsArray.add(new IRunActivityHandler() {
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

        if (sessionParametersActionsArray == null) {
            sessionParametersActionsArray = new ArrayList<IRunActivityHandler>();
        }

        sessionParametersActionsArray.add(new IRunActivityHandler() {
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

        if (sessionParametersActionsArray == null) {
            sessionParametersActionsArray = new ArrayList<IRunActivityHandler>();
        }

        sessionParametersActionsArray.add(new IRunActivityHandler() {
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

        if (sessionParametersActionsArray == null) {
            sessionParametersActionsArray = new ArrayList<IRunActivityHandler>();
        }

        sessionParametersActionsArray.add(new IRunActivityHandler() {
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

        if (sessionParametersActionsArray == null) {
            sessionParametersActionsArray = new ArrayList<IRunActivityHandler>();
        }

        sessionParametersActionsArray.add(new IRunActivityHandler() {
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
