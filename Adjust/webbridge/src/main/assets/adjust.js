var Adjust = {
    onCreate: function (adjustConfig) {
        console.log("onCreate: " + JSON.stringify(adjustConfig));
        if (AdjustBridge) {
            AdjustBridge.onCreate(JSON.stringify(adjustConfig));
        }
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
        if (AdjustBridge) {
            AdjustBridge.isEnabled(callback);
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
            AdjustBridge.addSessionPartnerParameter(key);
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

    getGoogleAdId: function (callback) {
        if (AdjustBridge) {
            AdjustBridge.getGoogleAdId(callback);
        }
    },

    getAmazonAdId: function (callback) {
        if (AdjustBridge) {
            AdjustBridge.getAmazonAdId(callback);
        }
    },

    getAmazonAdId: function (callback) {
        if (AdjustBridge) {
            AdjustBridge.getAmazonAdId(callback);
        }
    },

    getAdid: function (callback) {
        if (AdjustBridge) {
            AdjustBridge.getAdid(callback);
        }
    },

    getAttribution: function (callback) {
        if (AdjustBridge) {
            AdjustBridge.getAttribution(callback);
        }
    },

    setTestOptions: function (testOptions) {
        if (AdjustBridge) {
            AdjustBridge.setTestOptions(testOptions);
        }
    },
};

// module.exports = Adjust;
