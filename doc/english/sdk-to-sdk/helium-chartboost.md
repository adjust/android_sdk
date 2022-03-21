# Track Helium Chartboost ad revenue with Adjust SDK

[Adjust Android SDK README][android-readme]

Minimum SDK version required for this feature:

- **Adjust SDK v4.29.1**

If you want to track your ad revenue with the Helium SDK, you can use our SDK-to-SDK integration to pass this information to the Adjust backend. To do this, call the `trackAdRevenue` method with the source `AD_REVENUE_HELIUM_CHARTBOOST`.

> Note: If you have any questions about ad revenue tracking with Helium Chartboost, please contact your dedicated account manager or send an email to [support@adjust.com](mailto:support@adjust.com)

### Example

```java
public void ilrdObserver (final HeliumImpressionData impData) {
    final AdjustAdRevenue adjustAdRevenue = new AdjustAdRevenue(AdjustConfig.AD_REVENUE_HELIUM_CHARTBOOST);
    // extract the ILRD payload
    final JSONObject json = impData.getIlrdInfo();
    try {
        final double adRevenue = json.getDouble("ad_revenue");
        final String currencyType = json.getString("currency_type");
        adjustAdRevenue.setRevenue(adRevenue, currencyType);
    } catch (JSONException e) {
        // error handling as either revenue or currency was not present
        return;
    }
    // optional fields
    final String networkName = json.optString("network_name");     // Helium demand network name
    final String placementName = json.optString("placement_name"); // Helium placement name
    final String lineItemName = json.optString("line_item_name");  // Helium line item name
    adjustAdRevenue.setAdRevenueNetwork(networkName);
    adjustAdRevenue.setAdRevenueUnit(placementName);
    adjustAdRevenue.setAdRevenuePlacement(lineItemName);
    // track Adjust ad revenue
    Adjust.trackAdRevenue(adjustAdRevenue);
}
```

[android-readme]:    ../../../README.md
