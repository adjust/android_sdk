# Track AdMob ad revenue with Adjust SDK

[Adjust Android SDK README][android-readme]

Minimum SDK version required for this feature:

- **Adjust SDK v4.28.0**

> Note: In order to enable this feature, please reach out to your Google point of contact. Your point of contact will be able to activate the feature for you to access it.

If you want to track your ad revenue with the Admob SDK, you can use our SDK-to-SDK integration to pass this information to the Adjust backend. To do this, you will need to construct an Adjust ad revenue object containing the information you wish to record, then pass the object to the `trackAdRevenue` method.

> Note: If you have any questions about ad revenue tracking with Admob, please contact your dedicated account manager or send an email to [support@adjust.com](mailto:support@adjust.com).

### Example

```java
rewardedAd.setOnPaidEventListener(new OnPaidEventListener() {
    @Override
    public void onPaidEvent(AdValue adValue) {
        // ...
        // send ad revenue info to Adjust
        AdjustAdRevenue adRevenue = new AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB);
        adRevenue.setRevenue(adValue.getValueMicros() / 1000000.0, adValue.getCurrencyCode());
        Adjust.trackAdRevenue(adRevenue);
    }
}
```

For more information on how to properly integrate and set up AdMob SDK, please check out the [official documentation](https://developers.google.com/admob/android/early-access/paid-events).

[android-readme]:    ../../../README.md
