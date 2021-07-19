# ironSourceの広告収益をAdjust SDKで計測

[Adjust Android SDK README][android-readme]

この機能に必須のSDKバージョン：

- **Adjust SDK v4.28.0**

ironSource SDKで広告収益を計測する場合は、AdjustのSDK間連携の機能を使用することで、この情報をAdjustバックエンドに渡すことができます。これを行うには、ソースに`AD_REVENUE_IRONSOURCE`を指定して`trackAdRevenue`を呼び出します。

> 注：ironSourceによる広告収益計測についてご質問がありましたら、担当のアカウントマネージャー、または[support@adjust.com](mailto:support@adjust.com)までお問い合わせください。

### サンプル

```java
public void onImpressionSuccess (ImpressionData impressionData) {
    AdjustAdRevenue adjustAdRevenue = new AdjustAdRevenue(AdjustConfig.AD_REVENUE_IRONSOURCE);
    adjustAdRevenue.setRevenue(impressionData.getRevenue(),"USD");
    // optional fields
    adjustAdRevenue.setAdRevenueNetwork(impressionData.getAdNetwork());
    adjustAdRevenue.setAdRevenueUnit(impressionData.getAdUnit());
    adjustAdRevenue.setAdRevenuePlacement(impressionData.getPlacement());
    // track Adjust ad revenue
    Adjust.trackAdRevenue(adjustAdRevenue);
}
```

[android-readme]:    https://github.com/adjust/android_sdk/blob/master/doc/japanese/README.md
