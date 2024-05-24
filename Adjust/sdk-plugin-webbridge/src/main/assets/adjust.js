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

    addGlobalCallbackParameter: function(key, value) {
        if (AdjustBridge) {
            AdjustBridge.addGlobalCallbackParameter(key, value);
        }
    },

    addGlobalPartnerParameter: function(key, value) {
        if (AdjustBridge) {
            AdjustBridge.addGlobalPartnerParameter(key, value);
        }
    },

    removeGlobalCallbackParameter: function(key) {
        if (AdjustBridge) {
            AdjustBridge.removeGlobalCallbackParameter(key);
        }
    },

    removeGlobalPartnerParameter: function(key) {
        if (AdjustBridge) {
            AdjustBridge.removeGlobalPartnerParameter(key);
        }
    },

    removeGlobalCallbackParameters: function() {
        if (AdjustBridge) {
            AdjustBridge.removeGlobalCallbackParameters();
        }
    },

    removeGlobalPartnerParameters: function() {
        if (AdjustBridge) {
            AdjustBridge.removeGlobalPartnerParameters();
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

    enableCoppaCompliance: function () {
        if (AdjustBridge) {
            AdjustBridge.enableCoppaCompliance();
        }
    },

    disableCoppaCompliance: function () {
        if (AdjustBridge) {
            AdjustBridge.disableCoppaCompliance();
        }
    },

    enablePlayStoreKidsApp: function () {
        if (AdjustBridge) {
            AdjustBridge.enablePlayStoreKidsApp();
        }
    },

    disablePlayStoreKidsApp: function () {
        if (AdjustBridge) {
            AdjustBridge.disablePlayStoreKidsApp();
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

    getAdid: function (callback) {
     if (AdjustBridge) {
            if (typeof callback === 'string' || callback instanceof String) {
                this.getAdIdCallbackName = callback;
            } else {
                this.getAdIdCallbackName = 'Adjust.adjust_getAdIdCallback';
                this.getAdIdCallbackFunction = callback;
            }
            AdjustBridge.getAdid(this.getAdIdCallbackName);
        }
    },

    adjust_getAdIdCallback: function (adId) {
        if (AdjustBridge && this.getAdIdCallbackFunction) {
            this.getAdIdCallbackFunction(adId);
        }
    },

        getAmazonAdId: function (callbackSuccess,callbackFail) {
             if (AdjustBridge) {
                    if (typeof callbackSuccess === 'string' || callbackSuccess instanceof String) {
                        this.getAmazonIdCallbackSuccessName = callbackSuccess;
                    } else {
                        this.getAmazonIdCallbackSuccessName = 'Adjust.adjust_getAmazonIdCallbackSuccess';
                        this.getAmazonIdCallbackSuccessFunction = callbackSuccess;
                    }
                    if (typeof callbackFail === 'string' || callbackFail instanceof String) {
                        this.getAmazonIdCallbackFailName = callbackFail;
                    } else {
                        this.getAmazonIdCallbackFailName = 'Adjust.adjust_getAmazonIdCallbackFail';
                        this.getAmazonIdCallbackFailFunction = callbackFail;
                    }
                    AdjustBridge.getAmazonAdId(this.getAmazonIdCallbackSuccessName,this.getAmazonIdCallbackFailName);
                }
        },

        adjust_getAmazonIdCallbackSuccess: function (amazonId) {
            if (AdjustBridge && this.getAmazonIdCallbackSuccessFunction) {
                this.getAmazonIdCallbackSuccessFunction(amazonId);
            }
        },
        adjust_getAmazonIdCallbackFail: function (message) {
            if (AdjustBridge && this.getAmazonIdCallbackFailFunction) {
                this.getAmazonIdCallbackFailName(message);
            }
        },

    getAttribution: function (callback) {
        if (AdjustBridge) {
            AdjustBridge.getAttribution(callback);
        }
    },

    getSdkVersion: function (callback) {
        if (AdjustBridge) {
                if (typeof callback === 'string' || callback instanceof String) {
                    this.getSdkVersionCallbackName = callback;
                } else {
                    this.getSdkVersionCallbackName = 'Adjust.adjust_getSdkVersionCallback';
                    this.getSdkVersionCallbackFunction = callback;
                }
                AdjustBridge.getSdkVersion(this.getSdkVersionCallbackName);
            }
    },

    adjust_getSdkVersionCallback: function (sdkVersion) {
        if (AdjustBridge && this.getSdkVersionCallbackFunction) {
            this.getSdkVersionCallbackFunction(sdkVersion);
        }
    },

    getSdkPrefix: function () {
        if (this.adjustConfig) {
            return this.adjustConfig.getSdkPrefix();
        } else {
            return 'web-bridge5.0.0';
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
        this.getAdIdCallbackName = undefined;
        this.getAdIdCallbackFunction = undefined;
        this.getAmazonIdCallbackSuccessName = undefined;
        this.getAmazonIdCallbackSuccessFunction = undefined;
        this.getAmazonIdCallbackFailName = undefined;
        this.getAmazonIdCallbackFailFunction = undefined;
        this.getSdkVersionCallbackFunction = undefined;
        this.getSdkVersionCallbackName = undefined;
    },
};
