var Adjust = {
    onCreate: function (adjustConfig) {
        this.bridge = adjustConfig.getBridge();

        if (this.bridge != null) {
            if (adjustConfig != null) {
                if (adjustConfig.getAttributionCallback() != null) {
                    this.bridge.setAttributionCallback(adjustConfig.getAttributionCallback())
                }

                if (adjustConfig.getEventSuccessCallback() != null) {
                    this.bridge.setEventSuccessCallback(adjustConfig.getEventSuccessCallback())
                }

                if (adjustConfig.getEventFailureCallback() != null) {
                    this.bridge.setEventFailureCallback(adjustConfig.getEventFailureCallback())
                }

                if (adjustConfig.getSessionSuccessCallback() != null) {
                    this.bridge.setSessionSuccessCallback(adjustConfig.getSessionSuccessCallback())
                }

                if (adjustConfig.getSessionFailureCallback() != null) {
                    this.bridge.setSessionFailureCallback(adjustConfig.getSessionFailureCallback())
                }

                if (adjustConfig.getDeferredDeeplinkCallback() != null) {
                    this.bridge.setDeferredDeeplinkCallback(adjustConfig.getDeferredDeeplinkCallback())
                }

                this.bridge.onCreate(JSON.stringify(adjustConfig))
            }
        }
    },

    trackEvent: function (adjustEvent) {
        if (this.bridge != null) {
            this.bridge.trackEvent(JSON.stringify(adjustEvent))
        }
    },

    setOfflineMode: function(isOffline) {
        if (this.bridge != null) {
            this.bridge.setOfflineMode(isOffline)
        }
    },

    setEnabled: function (enabled) {
        if (this.bridge != null) {
            this.bridge.setEnabled(enabled)
        }
    },

    isEnabled: function (callback) {
        if (this.bridge != null) {
            this.bridge.isEnabled(callback)
        }
    },

    getGoogleAdId: function (callback) {
        if (this.bridge != null) {
            this.bridge.getGoogleAdId(callback)
        }
    }
};

// module.exports = Adjust;
