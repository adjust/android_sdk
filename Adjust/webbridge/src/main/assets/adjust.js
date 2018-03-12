var Adjust = {
    saveBridge: function(bridge) {
        console.log("saveBridge");
        this.bridge = bridge;
    },

    onCreate: function (adjustConfig) {
        console.log("onCreate: " + JSON.stringify(adjustConfig));

        if (!this.bridge) {
            this.bridge = adjustConfig.getBridge();
        }

        if (!this.bridge) {
            return;
        }

        this.bridge.onCreate(JSON.stringify(adjustConfig))
    },

    trackEvent: function (adjustEvent) {
        if (this.bridge) {
            this.bridge.trackEvent(JSON.stringify(adjustEvent));
        }
    },

    onResume: function () {
        if (this.bridge) {
            this.bridge.onResume();
        }
    },

    onPause: function () {
        if (this.bridge) {
            this.bridge.onPause();
        }
    },

    setEnabled: function (enabled) {
        if (this.bridge) {
            this.bridge.setEnabled(enabled);
        }
    },

    isEnabled: function (callback) {
        if (this.bridge) {
            this.bridge.isEnabled(callback);
        }
    },

    appWillOpenUrl: function (url) {
        if (this.bridge) {
            this.bridge.appWillOpenUrl(url);
        }
    },

    setReferrer: function (referrer) {
        if (this.bridge) {
            this.bridge.setReferrer(referrer);
        }
    },

    setOfflineMode: function(isOffline) {
        if (this.bridge) {
            this.bridge.setOfflineMode(isOffline);
        }
    },

    sendFirstPackages: function() {
        if (this.bridge) {
            this.bridge.sendFirstPackages();
        }
    },

    addSessionCallbackParameter: function(key, value) {
        if (this.bridge) {
            this.bridge.addSessionCallbackParameter(key, value);
        }
    },

    addSessionPartnerParameter: function(key, value) {
        if (this.bridge) {
            this.bridge.addSessionPartnerParameter(key, value);
        }
    },

    removeSessionCallbackParameter: function(key) {
        if (this.bridge) {
            this.bridge.removeSessionCallbackParameter(key);
        }
    },

    removeSessionPartnerParameter: function(key) {
        if (this.bridge) {
            this.bridge.addSessionPartnerParameter(key);
        }
    },

    resetSessionCallbackParameters: function() {
        if (this.bridge) {
            this.bridge.resetSessionCallbackParameters();
        }
    },

    resetSessionPartnerParameters: function() {
        if (this.bridge) {
            this.bridge.resetSessionPartnerParameters();
        }
    },

    setPushToken: function(token) {
        if (this.bridge) {
            this.bridge.setPushToken(token);
        }
    },

    getGoogleAdId: function (callback) {
        if (this.bridge) {
            this.bridge.getGoogleAdId(callback);
        }
    },

    getAmazonAdId: function (callback) {
        if (this.bridge) {
            this.bridge.getAmazonAdId(callback);
        }
    },

    getAmazonAdId: function (callback) {
        if (this.bridge) {
            this.bridge.getAmazonAdId(callback);
        }
    },

    getAdid: function (callback) {
        if (this.bridge) {
            this.bridge.getAdid(callback);
        }
    },

    getAttribution: function (callback) {
        if (this.bridge) {
            this.bridge.getAttribution(callback);
        }
    },

    setTestOptions: function (testOptions) {
        if (this.bridge) {
            this.bridge.setTestOptions(testOptions);
        }
    },
};

// module.exports = Adjust;
