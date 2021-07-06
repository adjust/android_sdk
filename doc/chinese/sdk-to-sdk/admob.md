# 通过 Adjust SDK 跟踪 AdMob 广告收入

[Adjust 安卓 SDK 自述文件][android-readme]

此功能最低 SDK 版本要求：

- **Adjust SDK v4.28.0**

如果您想使用 AdMob SDK 跟踪广告收入，可以借助我们的 SDK 到 SDK 集成，将数据发送到 Adjust 后端。要做到这一点，您需要构建 Adjust 广告收入对象，其中包含想记录的信息，然后将对象发送到 `trackAdRevenue` 方法。

> 请注意：如果您对 AdMob 广告收入跟踪有任何疑问，请联系您的专属客户经理，或发送邮件至 [support@adjust.com](mailto:support@adjust.com)。

### 示例

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

[android-readme]:    https://github.com/adjust/android_sdk/blob/master/doc/chinese/README.md
