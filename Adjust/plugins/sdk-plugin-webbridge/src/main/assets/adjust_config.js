function AdjustConfig(appToken, environment, legacy) {
    this.allowSuppressLogLevel = null;

    if (arguments.length === 2) {
        // new format does not require bridge as first parameter
        this.appToken = appToken;
        this.environment = environment;
    } else if (arguments.length === 3) {
        // new format with allowSuppressLogLevel
        if (typeof(legacy) == typeof(true)) {
            this.appToken = appToken;
            this.environment = environment;
            this.allowSuppressLogLevel = legacy;
        } else {
            // old format with first argument being the bridge instance
            this.bridge = appToken;
            this.appToken = environment;
            this.environment = legacy;
        }
    }

    this.isSendingInBackgroundEnabled = null;
    this.logLevel = null;
    this.sdkPrefix = null;
    this.processName = null;
    this.defaultTracker = null;
    this.externalDeviceId = null;
    this.attributionCallbackName = null;
    this.attributionCallbackFunction = null;
    this.isCostDataInAttributionEnabled = null;
    this.eventSuccessCallbackName = null;
    this.eventSuccessCallbackFunction = null;
    this.eventFailureCallbackName = null;
    this.eventFailureCallbackFunction = null;
    this.sessionSuccessCallbackName = null;
    this.sessionSuccessCallbackFunction = null;
    this.sessionFailureCallbackName = null;
    this.sessionFailureCallbackFunction = null;
    this.isOpeningDeferredDeeplinkEnabled = null;
    this.deferredDeeplinkCallbackName = null;
    this.deferredDeeplinkCallbackFunction = null;
    this.fbPixelDefaultEventToken = null;
    this.fbPixelMapping = [];
    this.urlStrategy = [];
    this.useSubDomain = null;
    this.isDataResidency = null;
    this.isPreinstallTrackingEnabled = null;
    this.preinstallFilePath = null;
    this.fbAppId = null;
    this.shouldReadDeviceIdsOnce = null;
    this.eventDeduplicationIdsMaxSize = null;
}

AdjustConfig.EnvironmentSandbox = 'sandbox';
AdjustConfig.EnvironmentProduction = 'production';

AdjustConfig.LogLevelVerbose = 'VERBOSE',
AdjustConfig.LogLevelDebug = 'DEBUG',
AdjustConfig.LogLevelInfo = 'INFO',
AdjustConfig.LogLevelWarn = 'WARN',
AdjustConfig.LogLevelError = 'ERROR',
AdjustConfig.LogLevelAssert = 'ASSERT',
AdjustConfig.LogLevelSuppress = 'SUPPRESS',

AdjustConfig.prototype.getBridge = function() {
    return this.bridge;
};

AdjustConfig.prototype.enableSendingInBackground = function() {
    this.isSendingInBackgroundEnabled = true;
};

AdjustConfig.prototype.setLogLevel = function(logLevel) {
    this.logLevel = logLevel;
};

AdjustConfig.prototype.getSdkPrefix = function() {
    return this.sdkPrefix;
};

AdjustConfig.prototype.setSdkPrefix = function(sdkPrefix) {
    this.sdkPrefix = sdkPrefix;
};

AdjustConfig.prototype.setProcessName = function(processName) {
    this.processName = processName;
};

AdjustConfig.prototype.setDefaultTracker = function(defaultTracker) {
    this.defaultTracker = defaultTracker;
};

AdjustConfig.prototype.setExternalDeviceId = function(externalDeviceId) {
    this.externalDeviceId = externalDeviceId;
};

AdjustConfig.prototype.setAttributionCallback = function(callback) {
    if (typeof callback === 'string' || callback instanceof String) {
        this.attributionCallbackName = callback;
    } else {
        this.attributionCallbackName = 'Adjust.getConfig().adjust_attributionCallback';
        this.attributionCallbackFunction = callback;
    }
};

AdjustConfig.prototype.adjust_attributionCallback = function(attribution) {
    if (this.attributionCallbackFunction) {
        this.attributionCallbackFunction(attribution);
    }
};

AdjustConfig.prototype.enableCostDataInAttribution = function() {
    this.isCostDataInAttributionEnabled = true;
};

AdjustConfig.prototype.setEventSuccessCallback = function(callback) {
    if (typeof callback === 'string' || callback instanceof String) {
        this.eventSuccessCallbackName = callback;
    } else {
        this.eventSuccessCallbackName = 'Adjust.getConfig().adjust_eventSuccessCallback';
        this.eventSuccessCallbackFunction = callback;
    }
};

AdjustConfig.prototype.adjust_eventSuccessCallback = function(eventSuccess) {
    if (this.eventSuccessCallbackFunction) {
        this.eventSuccessCallbackFunction(eventSuccess);
    }
};

AdjustConfig.prototype.setEventFailureCallback = function(callback) {
    if (typeof callback === 'string' || callback instanceof String) {
        this.eventFailureCallbackName = callback;
    } else {
        this.eventFailureCallbackName = 'Adjust.getConfig().adjust_eventFailureCallback';
        this.eventFailureCallbackFunction = callback;
    }
};

AdjustConfig.prototype.adjust_eventFailureCallback = function(eventFailure) {
    if (this.eventFailureCallbackFunction) {
        this.eventFailureCallbackFunction(eventFailure);
    }
};

AdjustConfig.prototype.setSessionSuccessCallback = function(callback) {
    if (typeof callback === 'string' || callback instanceof String) {
        this.sessionSuccessCallbackName = callback;
    } else {
        this.sessionSuccessCallbackName = 'Adjust.getConfig().adjust_sessionSuccessCallback';
        this.sessionSuccessCallbackFunction = callback;
    }
};

AdjustConfig.prototype.adjust_sessionSuccessCallback = function(sessionSuccess) {
    if (this.sessionSuccessCallbackFunction) {
        this.sessionSuccessCallbackFunction(sessionSuccess);
    }
};

AdjustConfig.prototype.setSessionFailureCallback = function(callback) {
    if (typeof callback === 'string' || callback instanceof String) {
        this.sessionFailureCallbackName = callback;
    } else {
        this.sessionFailureCallbackName = 'Adjust.getConfig().adjust_sessionFailureCallback';
        this.sessionFailureCallbackFunction = callback;
    }
};

AdjustConfig.prototype.adjust_sessionFailureCallback = function(sessionFailure) {
    if (this.sessionFailureCallbackFunction) {
        this.sessionFailureCallbackFunction(sessionFailure);
    }
};

AdjustConfig.prototype.disableDeferredDeeplinkOpening = function() {
    this.isOpeningDeferredDeeplinkEnabled = false;
};

AdjustConfig.prototype.setDeferredDeeplinkCallback = function(callback) {
    if (typeof callback === 'string' || callback instanceof String) {
        this.deferredDeeplinkCallbackName = callback;
    } else {
        this.deferredDeeplinkCallbackName = 'Adjust.getConfig().adjust_deferredDeeplinkCallback';
        this.deferredDeeplinkCallbackFunction = callback;
    }
};

AdjustConfig.prototype.adjust_deferredDeeplinkCallback = function(deeplink) {
    if (this.deferredDeeplinkCallbackFunction) {
        this.deferredDeeplinkCallbackFunction(deeplink);
    }
};

AdjustConfig.prototype.setReadMobileEquipmentIdentity = function(readMobileEquipmentIdentity) {};

AdjustConfig.prototype.setFbPixelDefaultEventToken = function(fbPixelDefaultEventToken) {
    this.fbPixelDefaultEventToken = fbPixelDefaultEventToken;
};

AdjustConfig.prototype.addFbPixelMapping = function(fbEventNameKey, adjEventTokenValue) {
    this.fbPixelMapping.push(fbEventNameKey);
    this.fbPixelMapping.push(adjEventTokenValue);
};

AdjustConfig.prototype.setUrlStrategy = function(urlStrategy, useSubDomain , isDataResidency) {
    this.urlStrategy = urlStrategy;
    this.useSubDomain = useSubDomain;
    this.isDataResidency = isDataResidency;
};

AdjustConfig.prototype.enablePreinstallTracking = function() {
    this.isPreinstallTrackingEnabled = true;
};

AdjustConfig.prototype.setPreinstallFilePath = function(preinstallFilePath) {
    this.preinstallFilePath = preinstallFilePath;
};

AdjustConfig.prototype.setFbAppId = function(fbAppId) {
    this.fbAppId = fbAppId;
};

AdjustConfig.prototype.readDeviceIdsOnce = function() {
    this.shouldReadDeviceIdsOnce = true;
};

AdjustConfig.prototype.setEventDeduplicationIdsMaxSize = function(eventDeduplicationIdsMaxSize) {
    this.eventDeduplicationIdsMaxSize = eventDeduplicationIdsMaxSize;
};
