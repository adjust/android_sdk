# Track ironSource ad revenue with Adjust SDK

[Adjust Android SDK README][android-readme]

Minimum SDK version required for this feature:

- **Adjust SDK v4.28.0**

If you want to track your ad revenue with the ironSource SDK, you can use our SDK-to-SDK integration to pass this information to the Adjust backend. To do this, call the `trackAdRevenue` method with the source `AD_REVENUE_IRONSOURCE`.

> Note: If you have any questions about ad revenue tracking with ironSource, please contact your dedicated account manager or send an email to [support@adjust.com](mailto:support@adjust.com)

### Example

```java
public void onImpressionSuccess (ImpressionData impressionData) {
    AdjustAdRevenue adjustAdRevenue = new AdjustAdRevenue(AdjustConfig.AD_REVENUE_IRONSOURCE);
    adjustAdRevenue.setRevenue(impressionData.getRevenue(), "USD");
    // optional fields
    adjustAdRevenue.setAdRevenueNetwork(impressionData.getAdNetwork());
    adjustAdRevenue.setAdRevenueUnit(impressionData.getAdUnit());
    adjustAdRevenue.setAdRevenuePlacement(impressionData.getPlacement());
    // track Adjust ad revenue
    Adjust.trackAdRevenue(adjustAdRevenue);
}
```

[android-readme]:    ../../../README.md
