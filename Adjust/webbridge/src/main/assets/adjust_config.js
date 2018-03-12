function AdjustConfig(appToken, environment, legacy) {
    if (arguments.length === 2) {
        this.appToken = appToken;
        this.environment = environment;
    } else if (arguments.length === 3) {
        this.bridge = appToken;
        this.appToken = environment;
        this.environment = legacy;
    }

    this.eventBufferingEnabled = null;
    this.sendInBackground = null;
    this.logLevel = null;
    this.sdkPrefix = 'web-bridge4.12.0';
    this.processName = null;
    this.defaultTracker = null;
    this.attributionCallback = null;
    this.deviceKnown = null;
    this.eventSuccessCallback = null;
    this.eventFailureCallback = null;
    this.sessionSuccessCallback = null;
    this.sessionFailureCallback = null;
    this.openDeferredDeeplink = null;
    this.deferredDeeplinkCallback = null;
    this.delayStart = null;
    this.userAgent = null;
    this.secretId = null;
    this.info1 = null;
    this.info2 = null;
    this.info3 = null;
    this.info4 = null;
    this.readMobileEquipmentIdentity = null;
}

AdjustConfig.EnvironmentSandbox     = 'sandbox';
AdjustConfig.EnvironmentProduction  = 'production';

AdjustConfig.LogLevelVerbose        = 'VERBOSE',
AdjustConfig.LogLevelDebug          = 'DEBUG',
AdjustConfig.LogLevelInfo           = 'INFO',
AdjustConfig.LogLevelWarn           = 'WARN',
AdjustConfig.LogLevelError          = 'ERROR',
AdjustConfig.LogLevelAssert         = 'ASSERT',

AdjustConfig.prototype.getBridge = function() {
    return this.bridge;
};

AdjustConfig.prototype.setEventBufferingEnabled = function(isEnabled) {
    this.eventBufferingEnabled = isEnabled;
};

AdjustConfig.prototype.setSendInBackground = function(isEnabled) {
    this.sendInBackground = isEnabled;
};

AdjustConfig.prototype.setLogLevel = function(logLevel) {
    this.logLevel = logLevel;
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

AdjustConfig.prototype.setAttributionCallback = function(callback) {
    this.attributionCallback = callback;
};

AdjustConfig.prototype.getAttributionCallback = function() {
    return this.attributionCallback;
};

AdjustConfig.prototype.setDeviceKnown = function(deviceKnown) {
    this.deviceKnown = deviceKnown;
};

AdjustConfig.prototype.setEventSuccessCallback = function(callback) {
    this.eventSuccessCallback = callback;
};

AdjustConfig.prototype.getEventSuccessCallback = function() {
    return this.eventSuccessCallback;
};

AdjustConfig.prototype.setEventFailureCallback = function(callback) {
    this.eventFailureCallback = callback;
};

AdjustConfig.prototype.getEventFailureCallback = function() {
    return this.eventFailureCallback;
};

AdjustConfig.prototype.setSessionSuccessCallback = function(callback) {
    this.sessionSuccessCallback = callback;
};

AdjustConfig.prototype.getSessionSuccessCallback = function() {
    return this.sessionSuccessCallback;
};

AdjustConfig.prototype.setSessionFailureCallback = function(callback) {
    this.sessionFailureCallback = callback;
};

AdjustConfig.prototype.getSessionFailureCallback = function() {
    return this.sessionFailureCallback;
};

AdjustConfig.prototype.setOpenDeferredDeeplink = function(shouldOpen) {
    this.openDeferredDeeplink = shouldOpen;
};

AdjustConfig.prototype.setDeferredDeeplinkCallback = function(callback) {
    this.deferredDeeplinkCallback = callback;
};

AdjustConfig.prototype.getDeferredDeeplinkCallback = function() {
    return this.deferredDeeplinkCallback;
};

AdjustConfig.prototype.setDelayStart = function(delayStart) {
    this.setDelayStart = delayStart;
};

AdjustConfig.prototype.setUserAgent = function(userAgent) {
    this.userAgent = userAgent;
};

AdjustConfig.prototype.setAppSecret = function(secretId, info1, info2, info3, info4) {
    this.secretId = secretId;
    this.info1 = info1;
    this.info2 = info2;
    this.info3 = info3;
    this.info4 = info4;
};

AdjustConfig.prototype.setReadMobileEquipmentIdentity = function(readMobileEquipmentIdentity) {
    this.readMobileEquipmentIdentity = readMobileEquipmentIdentity;
};
