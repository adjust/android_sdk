# Track AppLovin MAX ad revenue with Adjust SDK

[Adjust Android SDK README][android-readme]

Minimum SDK version required for this feature:

- **Adjust SDK v4.28.0**

If you want to track your ad revenue with the AppLovin MAX SDK, you can use our SDK-to-SDK integration to pass this information to the Adjust backend. To do this, call the `trackAdRevenue` method with the source `AD_REVENUE_APPLOVIN_MAX`.

> Note: If you have any questions about ad revenue tracking with AppLovin MAX, please contact your dedicated account manager or send an email to [support@adjust.com](mailto:support@adjust.com)

### Example

```java
@Override
public void onAdRevenuePaid(final MaxAd ad)
{
    AdjustAdRevenue adjustAdRevenue = new AdjustAdRevenue( AdjustConfig.AD_REVENUE_APPLOVIN_MAX);
    adjustAdRevenue.setRevenue(ad.getRevenue(), "USD");
    adjustAdRevenue.setAdRevenueNetwork(ad.getNetworkName());
    adjustAdRevenue.setAdRevenueUnit(ad.getAdUnitId());
    adjustAdRevenue.setAdRevenuePlacement(ad.getPlacement());

    Adjust.trackAdRevenue( adjustAdRevenue);
}

```

[android-readme]:    ../../../README.md
