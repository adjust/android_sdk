package com.adjust.sdk;

import android.content.Context;
import android.net.Uri;

/**
 * Created by pfms on 15/12/14.
 */
public interface IActivityHandler {
    void init(AdjustConfig config);

    void onResume();

    void onPause();

    void trackEvent(AdjustEvent event);

    void finishedTrackingActivity(ResponseData responseData);

    void setEnabled(boolean enabled);

    boolean isEnabled();
    void isEnabled(OnIsEnabledListener onIsEnabledListener);

    void processDeeplink(Uri url, long clickTime);

    void processAndResolveDeeplink(Uri url, long clickTime, OnDeeplinkResolvedListener callback);

    boolean updateAttributionI(AdjustAttribution attribution);

    void launchEventResponseTasks(EventResponseData eventResponseData);

    void launchSessionResponseTasks(SessionResponseData sessionResponseData);

    void launchSdkClickResponseTasks(SdkClickResponseData sdkClickResponseData);

    void launchAttributionResponseTasks(AttributionResponseData attributionResponseData);

    void launchPurchaseVerificationResponseTasks(PurchaseVerificationResponseData purchaseVerificationResponseData);

    void sendReftagReferrer();

    void sendPreinstallReferrer();

    void sendInstallReferrer(ReferrerDetails referrerDetails, String referrerApi);

    void setOfflineMode(boolean enabled);

    void setAskingAttribution(boolean askingAttribution);

    void addGlobalCallbackParameter(String key, String value);

    void addGlobalPartnerParameter(String key, String value);

    void removeGlobalCallbackParameter(String key);

    void removeGlobalPartnerParameter(String key);

    void removeGlobalCallbackParameters();

    void removeGlobalPartnerParameters();

    void teardown();

    void setPushToken(String token, boolean preSaved);

    void gdprForgetMe();

    void trackThirdPartySharing(AdjustThirdPartySharing adjustThirdPartySharing);

    void trackMeasurementConsent(boolean consentMeasurement);

    void trackAdRevenue(AdjustAdRevenue adjustAdRevenue);

    void trackPlayStoreSubscription(AdjustPlayStoreSubscription subscription);

    void verifyPlayStorePurchase(AdjustPlayStorePurchase purchase, OnPurchaseVerificationFinishedListener callback);

    void verifyAndTrackPlayStorePurchase(AdjustEvent event, OnPurchaseVerificationFinishedListener callback);

    void gotOptOutResponse();

    Context getContext();

    void getAdid(OnAdidReadListener onAdidReadListener);

    void getAttribution(OnAttributionReadListener onAttributionReadListener);

    AdjustConfig getAdjustConfig();

    DeviceInfo getDeviceInfo();

    ActivityState getActivityState();

    GlobalParameters getGlobalParameters();

    ActivityHandler.InternalState getInternalState();

}
