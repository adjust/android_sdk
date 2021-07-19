# 通过 Adjust SDK 跟踪 ironSource 广告收入

[Adjust 安卓 SDK 自述文件][android-readme]

此功能最低 SDK 版本要求：

- **Adjust SDK v4.28.0**

如果您想使用 ironSource SDK 跟踪广告收入，可以借助我们的 SDK-to-SDK 集成，将数据发送到 Adjust 后端。为此，请通过来源 `AD_REVENUE_IRONSOURCE` 调用 `trackAdRevenue` 方法。

> 请注意：如果您对 ironSource 广告收入跟踪有任何疑问，请联系您的专属客户经理，或发送邮件至 [support@adjust.com](mailto:support@adjust.com)

### 示例

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
[android-readme]:    https://github.com/adjust/android_sdk/blob/master/doc/chinese/README.md
