// generate random callback ID with prefix (similar to iOS implementation)
window.randomCallbackIdWithPrefix = function(prefix) {
    const randomString = (Math.random() + 1).toString(36).substring(7);
    return prefix + "_" + randomString;
};

var Adjust = {
    // map to store callbacks by their unique callback ID
    _callbackMap: {},
    _namedCallbackMap: {},
    _bridgeToken: null,
    _bridgeTokenRequested: false,
    _pendingBridgeCalls: [],

    _handleGetterCallback: function(callback, callbackId) {
        this._callbackMap[callbackId] = callback;
    },

    _registerNamedCallback: function(callbackName, callback) {
        if (!callbackName || typeof callback !== 'function') {
            return;
        }
        this._namedCallbackMap[callbackName] = callback;
    },

    _registerConfigCallbacks: function(adjustConfig) {
        if (!adjustConfig) {
            return;
        }

        var registerIfPresent = function(callbackName, callbackFn) {
            if (!callbackName) {
                return;
            }
            if (typeof callbackFn === 'function') {
                Adjust._registerNamedCallback(callbackName, callbackFn);
                return;
            }
            var resolved = Adjust._resolveCallbackByPath(callbackName);
            if (typeof resolved === 'function') {
                Adjust._registerNamedCallback(callbackName, resolved);
            }
        };

        registerIfPresent(adjustConfig.attributionCallbackName, adjustConfig.attributionCallbackFunction);
        registerIfPresent(adjustConfig.eventSuccessCallbackName, adjustConfig.eventSuccessCallbackFunction);
        registerIfPresent(adjustConfig.eventFailureCallbackName, adjustConfig.eventFailureCallbackFunction);
        registerIfPresent(adjustConfig.sessionSuccessCallbackName, adjustConfig.sessionSuccessCallbackFunction);
        registerIfPresent(adjustConfig.sessionFailureCallbackName, adjustConfig.sessionFailureCallbackFunction);
        registerIfPresent(adjustConfig.deferredDeeplinkCallbackName, adjustConfig.deferredDeeplinkCallbackFunction);
        registerIfPresent(adjustConfig.remoteTriggerCallbackName, adjustConfig.remoteTriggerCallbackFunction);

        var registerInternal = function(callbackName, callbackFn) {
            if (!callbackName || typeof callbackFn !== 'function') {
                return;
            }
            if (Adjust._namedCallbackMap[callbackName]) {
                return;
            }
            Adjust._registerNamedCallback(callbackName, callbackFn.bind(adjustConfig));
        };

        registerInternal(adjustConfig.attributionCallbackName, adjustConfig.adjust_attributionCallback);
        registerInternal(adjustConfig.eventSuccessCallbackName, adjustConfig.adjust_eventSuccessCallback);
        registerInternal(adjustConfig.eventFailureCallbackName, adjustConfig.adjust_eventFailureCallback);
        registerInternal(adjustConfig.sessionSuccessCallbackName, adjustConfig.adjust_sessionSuccessCallback);
        registerInternal(adjustConfig.sessionFailureCallbackName, adjustConfig.adjust_sessionFailureCallback);
        registerInternal(adjustConfig.deferredDeeplinkCallbackName, adjustConfig.adjust_deferredDeeplinkCallback);
        registerInternal(adjustConfig.remoteTriggerCallbackName, adjustConfig.adjust_remoteTriggerCallback);
    },

    _resolveCallbackByPath: function(path) {
        if (!path || typeof path !== 'string') {
            return null;
        }
        if (path.indexOf('(') !== -1 || path.indexOf(')') !== -1) {
            return null;
        }
        var parts = path.split('.');
        var context = window;
        for (var i = 0; i < parts.length; i++) {
            if (!context) {
                return null;
            }
            context = context[parts[i]];
        }
        return context;
    },

    _invokeCallback: function(callbackName, callback, value) {
        if (callbackName && callbackName.indexOf("adjust_getAttribution") !== -1) {
            if (typeof value === 'string') {
                callback(JSON.parse(value));
            } else {
                callback(value);
            }
            return;
        }
        callback(value);
    },

    _nativeCallback: function(callbackName, value) {
        if (!callbackName) {
            return;
        }
        var getterCallback = this._callbackMap[callbackName];
        if (getterCallback) {
            this._invokeCallback(callbackName, getterCallback, value);
            delete this._callbackMap[callbackName];
            return;
        }

        var namedCallback = this._namedCallbackMap[callbackName];
        if (namedCallback) {
            this._invokeCallback(callbackName, namedCallback, value);
        }
    },

    _setBridgeToken: function(token) {
        this._bridgeToken = token;
        this._bridgeTokenRequested = false;
        if (!this._pendingBridgeCalls.length) {
            return;
        }
        var pending = this._pendingBridgeCalls;
        this._pendingBridgeCalls = [];
        for (var i = 0; i < pending.length; i++) {
            var entry = pending[i];
            this._callBridge(entry.method, entry.args);
        }
    },

    _callBridge: function(method, args) {
        if (!AdjustBridge) {
            return undefined;
        }
        if (window.top !== window.self) {
            return undefined;
        }
        var callArgs = args ? args.slice() : [];
        if (!this._bridgeToken) {
            if (!this._bridgeTokenRequested && typeof AdjustBridge.requestBridgeToken === 'function') {
                this._bridgeTokenRequested = true;
                AdjustBridge.requestBridgeToken();
            }
            this._pendingBridgeCalls.push({ method: method, args: callArgs });
            return undefined;
        }
        callArgs.push(this._bridgeToken);
        return AdjustBridge[method].apply(AdjustBridge, callArgs);
    },

    initSdk: function (adjustConfig) {
        if (adjustConfig && !adjustConfig.getSdkPrefix()) {
            adjustConfig.setSdkPrefix(this.getSdkPrefix());
        }
        this.adjustConfig = adjustConfig;
        this._registerConfigCallbacks(adjustConfig);
        this._callBridge('initSdk', [JSON.stringify(adjustConfig)]);
    },

    getConfig: function () {
        return this.adjustConfig;
    },

    trackEvent: function (adjustEvent) {
        this._callBridge('trackEvent', [JSON.stringify(adjustEvent)]);
    },

    onResume: function () {
        this._callBridge('onResume');
    },

    onPause: function () {
        this._callBridge('onPause');
    },

    enable: function () {
        this._callBridge('enable');
    },

    disable: function () {
        this._callBridge('disable');
    },

    isEnabled: function (callback) {
        if (!AdjustBridge) {
            return undefined;
        }
        // supports legacy return with callback
        if (arguments.length === 1) {
            // generate unique callback ID
            const callbackId = window.randomCallbackIdWithPrefix("adjust_isEnabled");
            this._handleGetterCallback(callback, callbackId);
            this._callBridge('isEnabled', [callbackId]);
        } else {
            return AdjustBridge.isEnabled();
        }
    },

    setReferrer: function (referrer) {
        this._callBridge('setReferrer', [referrer]);
    },

    switchToOfflineMode: function() {
        this._callBridge('switchToOfflineMode');
    },

    switchBackToOnlineMode: function() {
        this._callBridge('switchBackToOnlineMode');
    },

    addGlobalCallbackParameter: function(key, value) {
        if (AdjustBridge) {
            if (typeof key !== 'string' || typeof value !== 'string') {
                console.log('[Adjust]: Passed key or value is not of string type');
                return;
            }
            this._callBridge('addGlobalCallbackParameter', [key, value]);
        }
    },

    addGlobalPartnerParameter: function(key, value) {
        if (AdjustBridge) {
            if (typeof key !== 'string' || typeof value !== 'string') {
                console.log('[Adjust]: Passed key or value is not of string type');
                return;
            }
            this._callBridge('addGlobalPartnerParameter', [key, value]);
        }
    },

    removeGlobalCallbackParameter: function(key) {
        if (AdjustBridge) {
            if (typeof key !== 'string') {
                console.log('[Adjust]: Passed key is not of string type');
                return;
            }
            this._callBridge('removeGlobalCallbackParameter', [key]);
        }
    },

    removeGlobalPartnerParameter: function(key) {
        if (AdjustBridge) {
            if (typeof key !== 'string') {
                console.log('[Adjust]: Passed key is not of string type');
                return;
            }
            this._callBridge('removeGlobalPartnerParameter', [key]);
        }
    },

    removeGlobalCallbackParameters: function() {
        this._callBridge('removeGlobalCallbackParameters');
    },

    removeGlobalPartnerParameters: function() {
        this._callBridge('removeGlobalPartnerParameters');
    },

    gdprForgetMe: function() {
        this._callBridge('gdprForgetMe');
    },

    trackThirdPartySharing: function(adjustThirdPartySharing) {
        this._callBridge('trackThirdPartySharing', [JSON.stringify(adjustThirdPartySharing)]);
    },

    trackMeasurementConsent: function(consentMeasurement) {
        this._callBridge('trackMeasurementConsent', [consentMeasurement]);
    },

    endFirstSessionDelay: function() {
        this._callBridge('endFirstSessionDelay');
    },

    enableCoppaComplianceInDelay: function() {
        this._callBridge('enableCoppaComplianceInDelay');
    },

    disableCoppaComplianceInDelay: function() {
        this._callBridge('disableCoppaComplianceInDelay');
    },

    enablePlayStoreKidsComplianceInDelay: function() {
        this._callBridge('enablePlayStoreKidsComplianceInDelay');
    },

    disablePlayStoreKidsComplianceInDelay: function() {
        this._callBridge('disablePlayStoreKidsComplianceInDelay');
    },

    setExternalDeviceIdInDelay: function(externalDeviceId) {
        this._callBridge('setExternalDeviceIdInDelay', [externalDeviceId]);
    },

    getGoogleAdId: function (callback) {
        if (AdjustBridge) {
            const callbackId = window.randomCallbackIdWithPrefix("adjust_getGoogleAdId");
            this._handleGetterCallback(callback, callbackId);
            this._callBridge('getGoogleAdId', [callbackId]);
        }
    },

    getAdid: function (callback) {
        if (AdjustBridge) {
            const callbackId = window.randomCallbackIdWithPrefix("adjust_getAdid");
            this._handleGetterCallback(callback, callbackId);
            this._callBridge('getAdid', [callbackId]);
        }
    },

    getAdidWithTimeout: function (timeoutInMilliSec, callback) {
        if (AdjustBridge) {
            const callbackId = window.randomCallbackIdWithPrefix("adjust_getAdidWithTimeout");
            this._handleGetterCallback(callback, callbackId);
            this._callBridge('getAdidWithTimeout', [timeoutInMilliSec, callbackId]);
        }
    },

    getAmazonAdId: function (callbackSuccess, callbackFail) {
        if (AdjustBridge) {
            const callbackId = window.randomCallbackIdWithPrefix("adjust_getAmazonAdId");
            this._handleGetterCallback(callbackSuccess, callbackId);
            // note: Java side only supports success callback, callbackFail is ignored
            this._callBridge('getAmazonAdId', [callbackId]);
        }
    },

    getAttribution: function (callback) {
        if (AdjustBridge) {
            const callbackId = window.randomCallbackIdWithPrefix("adjust_getAttribution");
            this._handleGetterCallback(callback, callbackId);
            this._callBridge('getAttribution', [callbackId]);
        }
    },

    getAttributionWithTimeout: function (timeoutInMilliSec, callback) {
        if (AdjustBridge) {
            const callbackId = window.randomCallbackIdWithPrefix("adjust_getAttributionWithTimeout");
            this._handleGetterCallback(callback, callbackId);
            this._callBridge('getAttributionWithTimeout', [timeoutInMilliSec, callbackId]);
        }
    },

    getThirdPartySharingSettingsWithTimeout: function (timeoutInMilliSec, callback) {
        if (AdjustBridge) {
            const callbackId = window.randomCallbackIdWithPrefix("adjust_getThirdPartySharingSettingsWithTimeout");
            this._handleGetterCallback(callback, callbackId);
            this._callBridge('getThirdPartySharingSettingsWithTimeout', [timeoutInMilliSec, callbackId]);
        }
    },

    getSdkVersion: function (callback) {
        if (AdjustBridge) {
            const callbackId = window.randomCallbackIdWithPrefix("adjust_getSdkVersion");
            // wrap callback to add SDK prefix before passing to _handleGetterCallback
            const wrappedCallback = function(sdkVersion) {
                callback(Adjust.getSdkPrefix() + '@' + sdkVersion);
            };
            this._handleGetterCallback(wrappedCallback, callbackId);
            this._callBridge('getSdkVersion', [callbackId]);
        }
    },

    getSdkPrefix: function () {
        if (this.adjustConfig) {
            return this.adjustConfig.getSdkPrefix();
        } else {
            return 'web-bridge5.6.1';
        }
    },

    teardown: function() {
        if (AdjustBridge) {
            this._callBridge('teardown');
        }
        this.adjustConfig = undefined;
        this._callbackMap = {};
        this._namedCallbackMap = {};
        this._bridgeToken = null;
        this._bridgeTokenRequested = false;
        this._pendingBridgeCalls = [];
    },
};
