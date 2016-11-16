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

    private static ILogger getLogger() {
        return AdjustFactory.getLogger();
    }

    public final void onCreate(final AdjustConfig adjustConfig) {
        if (activityHandler != null) {
            getLogger().error("Adjust already initialized");
            return;
        }

        adjustConfig.referrer = this.referrer;
        adjustConfig.referrerClickTime = this.referrerClickTime;
        adjustConfig.sessionParametersActionsArray = sessionParametersActionsArray;

        activityHandler = ActivityHandler.getInstance(adjustConfig);
    }

    public final void trackEvent(final AdjustEvent event) {
        if (!checkActivityHandler()) return;
        activityHandler.trackEvent(event);
    }

    public final void onResume() {
        if (!checkActivityHandler()) return;
        activityHandler.onResume();
    }

    public final void onPause() {
        if (!checkActivityHandler()) return;
        activityHandler.onPause();
    }

    public final void setEnabled(final boolean enabled) {
        if (!checkActivityHandler()) return;
        activityHandler.setEnabled(enabled);
    }

    public final boolean isEnabled() {
        if (!checkActivityHandler()) return false;
        return activityHandler.isEnabled();
    }

    public final void appWillOpenUrl(final Uri url) {
        if (!checkActivityHandler()) return;
        long clickTime = System.currentTimeMillis();
        activityHandler.readOpenUrl(url, clickTime);
    }

    public final void sendReferrer(final String referrer) {
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

    public final void setOfflineMode(final boolean enabled) {
        if (!checkActivityHandler()) return;
        activityHandler.setOfflineMode(enabled);
    }


    public final void sendFirstPackages() {
        if (!checkActivityHandler()) return;
        activityHandler.sendFirstPackages();
    }

    public final void addSessionCallbackParameter(final String key, final String value) {
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

    public final void addSessionPartnerParameter(final String key, final String value) {
        if (activityHandler != null) {
            activityHandler.addSessionPartnerParameter(key, value);
            return;
        }

        if (sessionParametersActionsArray == null) {
            sessionParametersActionsArray = new ArrayList<>();
        }

        sessionParametersActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(ActivityHandler activityHandler) {
                activityHandler.addSessionPartnerParameterI(key, value);
            }
        });
    }

    public final void removeSessionCallbackParameter(final String key) {
        if (activityHandler != null) {
            activityHandler.removeSessionCallbackParameter(key);
            return;
        }

        if (sessionParametersActionsArray == null) {
            sessionParametersActionsArray = new ArrayList<>();
        }

        sessionParametersActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(ActivityHandler activityHandler) {
                activityHandler.removeSessionCallbackParameterI(key);
            }
        });
    }

    public final void removeSessionPartnerParameter(final String key) {
        if (activityHandler != null) {
            activityHandler.removeSessionPartnerParameter(key);
            return;
        }

        if (sessionParametersActionsArray == null) {
            sessionParametersActionsArray = new ArrayList<>();
        }

        sessionParametersActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(ActivityHandler activityHandler) {
                activityHandler.removeSessionPartnerParameterI(key);
            }
        });
    }

    public final void resetSessionCallbackParameters() {
        if (activityHandler != null) {
            activityHandler.resetSessionCallbackParameters();
            return;
        }

        if (sessionParametersActionsArray == null) {
            sessionParametersActionsArray = new ArrayList<>();
        }

        sessionParametersActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(ActivityHandler activityHandler) {
                activityHandler.resetSessionCallbackParametersI();
            }
        });
    }

    public final void resetSessionPartnerParameters() {
        if (activityHandler != null) {
            activityHandler.resetSessionPartnerParameters();
            return;
        }

        if (sessionParametersActionsArray == null) {
            sessionParametersActionsArray = new ArrayList<>();
        }

        sessionParametersActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(ActivityHandler activityHandler) {
                activityHandler.resetSessionPartnerParametersI();
            }
        });
    }

    public final void teardown(final boolean deleteState) {
        if (!checkActivityHandler()) return;
        activityHandler.teardown(deleteState);
        activityHandler = null;
    }

    public final void setPushToken(final String token) {
        if (!checkActivityHandler()) return;
        activityHandler.setPushToken(token);
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
