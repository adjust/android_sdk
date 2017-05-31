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
    private IActivityHandler activityHandler;
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

        activityHandler = AdjustFactory.getActivityHandler(adjustConfig);
    }

    public void trackEvent(AdjustEvent event) {
        if (!checkActivityHandler()) return;
        activityHandler.trackEvent(event);
    }

    public void onResume() {
        if (!checkActivityHandler()) return;
        activityHandler.onResume();
    }

    public void onPause() {
        if (!checkActivityHandler()) return;
        activityHandler.onPause();
    }

    public void setEnabled(boolean enabled) {
        if (!checkActivityHandler()) return;
        activityHandler.setEnabled(enabled);
    }

    public boolean isEnabled() {
        if (!checkActivityHandler()) return false;
        return activityHandler.isEnabled();
    }

    public void appWillOpenUrl(Uri url) {
        if (!checkActivityHandler()) return;
        long clickTime = System.currentTimeMillis();
        activityHandler.readOpenUrl(url, clickTime);
    }

    public void sendReferrer(String referrer) {
        long clickTime = System.currentTimeMillis();
        // sendReferrer might be triggered before Adjust
        if (activityHandler == null) {
            // save it to inject in the config before launch
            this.referrer = referrer;
            this.referrerClickTime = clickTime;
        } else {
            activityHandler.sendReferrer(referrer, clickTime);
        }
    }

    public void setOfflineMode(boolean enabled) {
        if (!checkActivityHandler()) return;
        activityHandler.setOfflineMode(enabled);
    }


    public void sendFirstPackages() {
        if (!checkActivityHandler()) return;
        activityHandler.sendFirstPackages();
    }

    public void addSessionCallbackParameter(final String key, final String value) {
        if (activityHandler != null) {
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
        if (activityHandler != null) {
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
        if (activityHandler != null) {
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
        if (activityHandler != null) {
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
        if (activityHandler != null) {
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
        if (activityHandler != null) {
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
        if (activityHandler != null) {
            activityHandler.teardown(deleteState);
        }
        referrer = null;
        activityHandler = null;
        sessionParametersActionsArray = null;
        pushToken = null;
    }

    public void setPushToken(String token) {
        pushToken = token;

        if (activityHandler != null) {
            activityHandler.setPushToken(token);
            return;
        }
    }

    public String getAdid() {
        if (!checkActivityHandler()) return null;
        return activityHandler.getAdid();
    }

    public AdjustAttribution getAttribution() {
        if (!checkActivityHandler()) return null;
        return activityHandler.getAttribution();
    }

    private boolean checkActivityHandler() {
        if (activityHandler == null) {
            getLogger().error("Adjust not initialized correctly");
            return false;
        } else {
            return true;
        }
    }
}
