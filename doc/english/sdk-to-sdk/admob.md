# Track AdMob ad revenue with Adjust SDK

[Adjust Android SDK README][android-readme]

Minimum SDK version required for this feature:

- **Adjust SDK v4.28.0**

If you want to track your ad revenue with the Admob SDK, you can use our SDK-to-SDK integration to pass this information to the Adjust backend. To do this, you will need to construct an Adjust ad revenue object containing the information you wish to record, then pass the object to the `trackAdRevenue` method.

> Note: If you have any questions about ad revenue tracking with Admob, please contact your dedicated account manager or send an email to [support@adjust.com](mailto:support@adjust.com)

### Example

```java
// initialise with AdMob source
AdjustAdRevenue adjustAdRevenue = new AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB);

// set revenue and currency
adjustAdRevenue.setRevenue(6.66, "USD");

// optional parameters
adjustAdRevenue.setAdImpressionsCount(10);
adjustAdRevenue.setAdRevenueNetwork("network");
adjustAdRevenue.setAdRevenueUnit("unit");
adjustAdRevenue.setAdRevenuePlacement("placement");

// callback & partner parameters
adjustAdRevenue.addCallbackParameter("key", "value");
adjustAdRevenue.addPartnerParameter("key", "value");

// track ad revenue
Adjust.trackAdRevenue(adjustAdRevenue);
```

[android-readme]:    ../../../README.md
