var Adjust = {
    onCreate: function (adjustConfig) {
        if (adjustConfig && !adjustConfig.getSdkPrefix()) {
            adjustConfig.setSdkPrefix(this.getSdkPrefix());
        }
        this.adjustConfig = adjustConfig;
        if (AdjustBridge) {
            AdjustBridge.onCreate(JSON.stringify(adjustConfig));
        }
    },

    getConfig: function () {
        return this.adjustConfig;
    },

    trackEvent: function (adjustEvent) {
        if (AdjustBridge) {
            AdjustBridge.trackEvent(JSON.stringify(adjustEvent));
        }
    },

    trackAdRevenue: function(source, payload) {
        if (AdjustBridge) {
            AdjustBridge.trackAdRevenue(source, payload);
        }
    },

    onResume: function () {
        if (AdjustBridge) {
            AdjustBridge.onResume();
        }
    },

    onPause: function () {
        if (AdjustBridge) {
            AdjustBridge.onPause();
        }
    },

    setEnabled: function (enabled) {
        if (AdjustBridge) {
            AdjustBridge.setEnabled(enabled);
        }
    },

    isEnabled: function (callback) {
        if (!AdjustBridge) {
            return undefined;
        }
        // supports legacy return with callback
        if (arguments.length === 1) {
            // with manual string call
            if (typeof callback === 'string' || callback instanceof String) {
                this.isEnabledCallbackName = callback;
            } else {
                // or save callback and call later
                this.isEnabledCallbackName = 'Adjust.adjust_isEnabledCallback';
                this.isEnabledCallbackFunction = callback;
            }
            AdjustBridge.isEnabled(this.isEnabledCallbackName);
        } else {
            return AdjustBridge.isEnabled();
        }
    },

    adjust_isEnabledCallback: function (isEnabled) {
        if (AdjustBridge && this.isEnabledCallbackFunction) {
            this.isEnabledCallbackFunction(isEnabled);
        }
    },

    appWillOpenUrl: function (url) {
        if (AdjustBridge) {
            AdjustBridge.appWillOpenUrl(url);
        }
    },

    setReferrer: function (referrer) {
        if (AdjustBridge) {
            AdjustBridge.setReferrer(referrer);
        }
    },

    setOfflineMode: function(isOffline) {
        if (AdjustBridge) {
            AdjustBridge.setOfflineMode(isOffline);
        }
    },

    sendFirstPackages: function() {
        if (AdjustBridge) {
            AdjustBridge.sendFirstPackages();
        }
    },

    addSessionCallbackParameter: function(key, value) {
        if (AdjustBridge) {
            AdjustBridge.addSessionCallbackParameter(key, value);
        }
    },

    addSessionPartnerParameter: function(key, value) {
        if (AdjustBridge) {
            AdjustBridge.addSessionPartnerParameter(key, value);
        }
    },

    removeSessionCallbackParameter: function(key) {
        if (AdjustBridge) {
            AdjustBridge.removeSessionCallbackParameter(key);
        }
    },

    removeSessionPartnerParameter: function(key) {
        if (AdjustBridge) {
            AdjustBridge.removeSessionPartnerParameter(key);
        }
    },

    resetSessionCallbackParameters: function() {
        if (AdjustBridge) {
            AdjustBridge.resetSessionCallbackParameters();
        }
    },

    resetSessionPartnerParameters: function() {
        if (AdjustBridge) {
            AdjustBridge.resetSessionPartnerParameters();
        }
    },

    setPushToken: function(token) {
        if (AdjustBridge) {
            AdjustBridge.setPushToken(token);
        }
    },

    gdprForgetMe: function() {
        if (AdjustBridge) {
            AdjustBridge.gdprForgetMe();
        }
    },

    disableThirdPartySharing: function() {
        if (AdjustBridge) {
            AdjustBridge.disableThirdPartySharing();
        }
    },

    trackThirdPartySharing: function(adjustThirdPartySharing) {
        if (AdjustBridge) {
            AdjustBridge.trackThirdPartySharing(JSON.stringify(adjustThirdPartySharing));
        }
    },

    trackMeasurementConsent: function(consentMeasurement) {
        if (AdjustBridge) {
            AdjustBridge.trackMeasurementConsent(consentMeasurement);
        }
    },

    getGoogleAdId: function (callback) {
        if (AdjustBridge) {
            if (typeof callback === 'string' || callback instanceof String) {
                this.getGoogleAdIdCallbackName = callback;
            } else {
                this.getGoogleAdIdCallbackName = 'Adjust.adjust_getGoogleAdIdCallback';
                this.getGoogleAdIdCallbackFunction = callback;
            }
            AdjustBridge.getGoogleAdId(this.getGoogleAdIdCallbackName);
        }
    },

    adjust_getGoogleAdIdCallback: function (googleAdId) {
        if (AdjustBridge && this.getGoogleAdIdCallbackFunction) {
            this.getGoogleAdIdCallbackFunction(googleAdId);
        }
    },

    getAmazonAdId: function (callback) {
        if (AdjustBridge) {
            return AdjustBridge.getAmazonAdId();
        } else {
            return undefined;
        }
    },

    getAdid: function () {
        if (AdjustBridge) {
            return AdjustBridge.getAdid();
        } else {
            return undefined;
        }
    },

    getAttribution: function (callback) {
        if (AdjustBridge) {
            AdjustBridge.getAttribution(callback);
        }
    },

    getSdkVersion: function () {
        if (AdjustBridge) {
             return this.getSdkPrefix() + '@' + AdjustBridge.getSdkVersion();
        } else {
            return undefined;
        }
    },

    getSdkPrefix: function () {
        if (this.adjustConfig) {
            return this.adjustConfig.getSdkPrefix();
        } else {
            return 'web-bridge4.33.0';
        }
    },

    teardown: function() {
        if (AdjustBridge) {
            AdjustBridge.teardown();
        }
        this.adjustConfig = undefined;
        this.isEnabledCallbackName = undefined;
        this.isEnabledCallbackFunction = undefined;
        this.getGoogleAdIdCallbackName = undefined;
        this.getGoogleAdIdCallbackFunction = undefined;
    },
};
