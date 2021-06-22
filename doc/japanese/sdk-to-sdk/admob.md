# AdMobの広告収益をAdjust SDKで計測

[Adjust Android SDK README][android-readme]

この機能に必須のSDKバージョン：

- **Adjust SDK v4.28.0**

Admob SDKで広告収益を計測する場合は、AdjustのSDK間連携の機能を使用することで、この情報をAdjustバックエンドに渡すことができます。これを行うには、記録する情報を含むAdjust広告収益オブジェクトを作成し、そのオブジェクトを`trackAdRevenue`メソッドに渡す必要があります。

> 注：Admobによる広告収益計測についてご質問がありましたら、担当のアカウントマネージャー、または[support@adjust.com](mailto:support@adjust.com)までお問い合わせください。

### サンプル

```java
rewardedAd = new RewardedAd(this, AD_UNIT_ID);
// set paid event listener
rewardedAd.setOnPaidEventListener(new OnPaidEventListener() {
    @Override
    public void onPaidEvent(AdValue adValue) {
        // ...
        // send ad revenue info to Adjust
        AdjustAdRevenue adRevenue = new AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB);
        adRevenue.setRevenue(adValue.getValueMicros() / 1000000, adValue.getCurrencyCode());
        Adjust.trackAdRevenue(adRevenue);
    }
}
```

[android-readme]:    https://github.com/adjust/android_sdk/blob/master/doc/japanese/README.md
