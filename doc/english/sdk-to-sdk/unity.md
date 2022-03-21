# Track Unity ad revenue with Adjust SDK

[Adjust Android SDK README][android-readme]

Minimum SDK version required for this feature:

- **Adjust SDK v4.29.1**

If you want to track your ad revenue with the Unity SDK, you can use our SDK-to-SDK integration to pass this information to the Adjust backend. To do this, call the `trackAdRevenue` method with the source `AD_REVENUE_UNITY`.

> Note: If you have any questions about ad revenue tracking with Unity, please contact your dedicated account manager or send an email to [support@adjust.com](mailto:support@adjust.com)

For more information, see the Unity Mediation [API documentation](https://docs.unity.com/mediation/APIReferenceAndroid.html) and [impression event documentation](https://docs.unity.com/mediation/SDKIntegrationAndroidImpressionEvents.html).

### Example

```java
// implement an impression listener
final IImpressionListener impressionListener = new IImpressionListener() {
    @Override
    public void onImpression(@NonNull String adUnitId, @Nullable ImpressionData impressionData) {
        // send impression data to Adjust
        AdjustAdRevenue adjustAdRevenue = new AdjustAdRevenue(AdjustConfig.AD_REVENUE_UNITY);
        adjustAdRevenue.setRevenue(impressionData.getPublisherRevenuePerImpression(), impressionData.getCurrency());
        // optional fields
        adjustAdRevenue.setAdRevenueNetwork(impressionData.getAdSourceName());
        adjustAdRevenue.setAdRevenueUnit(impressionData.getAdUnitId());
        adjustAdRevenue.setAdRevenuePlacement(impressionData.getAdSourceInstance());
        // track Adjust ad revenue
        Adjust.trackAdRevenue(adjustAdRevenue);
    }
};

// subscribe the impression listener to the impression event publisher
ImpressionEventPublisher.subscribe(impressionListener);

// unsubscribe the impression listener from the impression event publisher
ImpressionEventPublisher.unsubscribe(impressionListener);
```

[android-readme]:    ../../../README.md
