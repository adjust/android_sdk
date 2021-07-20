# AppLovin MAXの広告収益をAdjust SDKで計測

[Adjust Android SDK README][android-readme]

この機能に必須のSDKバージョン：

- **Adjust SDK v4.28.0**

AppLovin MAX SDKで広告収益を計測する場合は、AdjustのSDK間連携の機能を使用することで、この情報をAdjustバックエンドに渡すことができます。これを行うには、ソースに`AD_REVENUE_APPLOVIN_MAX`を指定して`trackAdRevenue`を呼び出します。

> 注：AppLovin MAXによる広告収益計測についてご質問がありましたら、担当のアカウントマネージャー、または[support@adjust.com](mailto:support@adjust.com)までお問い合わせください。

### サンプル

```java
@Override
public void onAdRevenuePaid(final MaxAd ad) {
    AdjustAdRevenue adjustAdRevenue = new AdjustAdRevenue( AdjustConfig.AD_REVENUE_APPLOVIN_MAX);
    adjustAdRevenue.setRevenue(ad.getRevenue(),"USD");
    adjustAdRevenue.setAdRevenueNetwork(ad.getNetworkName());
    adjustAdRevenue.setAdRevenueUnit(ad.getAdUnitId());
    adjustAdRevenue.setAdRevenuePlacement(ad.getPlacement());

    Adjust.trackAdRevenue(adjustAdRevenue);
}

```

[android-readme]:    https://github.com/adjust/android_sdk/blob/master/doc/japanese/README.md
