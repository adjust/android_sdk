var Adjust = {
    initSdk: function (adjustConfig) {
        if (adjustConfig && !adjustConfig.getSdkPrefix()) {
            adjustConfig.setSdkPrefix(this.getSdkPrefix());
        }
        this.adjustConfig = adjustConfig;
        if (AdjustBridge) {
            AdjustBridge.initSdk(JSON.stringify(adjustConfig));
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

    enable: function () {
        if (AdjustBridge) {
            AdjustBridge.enable();
        }
    },

    disable: function () {
        if (AdjustBridge) {
            AdjustBridge.disable();
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

    switchToOfflineMode: function() {
        if (AdjustBridge) {
            AdjustBridge.switchToOfflineMode();
        }
    },

    switchBackToOnlineMode: function() {
        if (AdjustBridge) {
            AdjustBridge.switchBackToOnlineMode();
        }
    },

    addGlobalCallbackParameter: function(key, value) {
        if (AdjustBridge) {
            if (typeof key !== 'string' || typeof value !== 'string') {
                console.log('[Adjust]: Passed key or value is not of string type');
                return;
            }
            AdjustBridge.addGlobalCallbackParameter(key, value);
        }
    },

    addGlobalPartnerParameter: function(key, value) {
        if (AdjustBridge) {
            if (typeof key !== 'string' || typeof value !== 'string') {
                console.log('[Adjust]: Passed key or value is not of string type');
                return;
            }
            AdjustBridge.addGlobalPartnerParameter(key, value);
        }
    },

    removeGlobalCallbackParameter: function(key) {
        if (AdjustBridge) {
            if (typeof key !== 'string') {
                console.log('[Adjust]: Passed key is not of string type');
                return;
            }
            AdjustBridge.removeGlobalCallbackParameter(key);
        }
    },

    removeGlobalPartnerParameter: function(key) {
        if (AdjustBridge) {
            if (typeof key !== 'string') {
                console.log('[Adjust]: Passed key is not of string type');
                return;
            }
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

    endFirstSessionDelay: function() {
        if (AdjustBridge) {
            AdjustBridge.endFirstSessionDelay();
        }
    },

    enableCoppaComplianceInDelay: function() {
        if (AdjustBridge) {
            AdjustBridge.enableCoppaComplianceInDelay();
        }
    },

    disableCoppaComplianceInDelay: function() {
        if (AdjustBridge) {
            AdjustBridge.disableCoppaComplianceInDelay();
        }
    },

    enablePlayStoreKidsComplianceInDelay: function() {
        if (AdjustBridge) {
            AdjustBridge.enablePlayStoreKidsComplianceInDelay();
        }
    },

    disablePlayStoreKidsComplianceInDelay: function() {
        if (AdjustBridge) {
            AdjustBridge.disablePlayStoreKidsComplianceInDelay();
        }
    },

    setExternalDeviceIdInDelay: function(externalDeviceId) {
        if (AdjustBridge) {
            AdjustBridge.setExternalDeviceIdInDelay(externalDeviceId);
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
                AdjustBridge.getAmazonAdId(this.getAmazonIdCallbackSuccessName);
            }
    },

    adjust_getAmazonIdCallbackSuccess: function (amazonId) {
        if (AdjustBridge && this.getAmazonIdCallbackSuccessFunction) {
            this.getAmazonIdCallbackSuccessFunction(amazonId);
        }
    },

    getAttribution: function (callback) {
     if (AdjustBridge) {
            if (typeof callback === 'string' || callback instanceof String) {
                this.getAttributionCallbackName = callback;
            } else {
                this.getAttributionCallbackName = 'Adjust.adjust_getAttributionCallback';
                this.getAttributionCallbackFunction = callback;
            }
            AdjustBridge.getAttribution(this.getAttributionCallbackName);
        }
    },

    adjust_getAttributionCallback: function (attribution) {
        if (AdjustBridge && this.getAttributionCallbackFunction) {
            this.getAttributionCallbackFunction(attribution);
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
            this.getSdkVersionCallbackFunction(this.getSdkPrefix() + '@' + sdkVersion);
        }
    },

    getSdkPrefix: function () {
        if (this.adjustConfig) {
            return this.adjustConfig.getSdkPrefix();
        } else {
            return 'web-bridge5.2.0';
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
        this.getAttributionCallbackName = undefined;
        this.getAttributionCallbackFunction = undefined;
        this.getAmazonIdCallbackSuccessName = undefined;
        this.getAmazonIdCallbackSuccessFunction = undefined;
        this.getSdkVersionCallbackFunction = undefined;
        this.getSdkVersionCallbackName = undefined;
    },
};
