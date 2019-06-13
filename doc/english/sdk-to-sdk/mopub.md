## Track MoPub ad revenue with Adjust SDK

[Adjust Android SDK README][android_readme]

[MoPub Android documentation][mopub-docs]

Minimal SDK version required for this feature:

- **Adjust SDK v4.18.0**
- **MoPub SDK v5.7.0**

Inside of your MoPub SDK `onImpression` callback method implementation, make sure to invoke `trackAdRevenue` method of Adjust SDK like this:

```java
public void onImpression(@NonNull final String adUnitId, @Nullable final ImpressionData impressionData) {
    // Pass impression data JSON to Adjust SDK.
    Adjust.trackAdRevenue(AdjustConfig.AD_REVENUE_MOPUB, impressionData.getJsonRepresentation());
}
```

In case you have any questions about ad revenue tracking with MoPub, please contact your dedicated account manager or send an email to support@adjust.com.

[mopub-docs]:        https://developers.mopub.com/publishers/android/impression-data/
[android_readme]:    https://github.com/adjust/android_sdk
