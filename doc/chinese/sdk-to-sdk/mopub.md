## 通过 Adjust SDK 跟踪 MoPub 广告收入

[Adjust 安卓 SDK 自述文件][android-readme]

[MoPub 安卓文档][mopub-docs]

此功能最低 SDK 版本要求：

- **Adjust SDK v4.18.0**
- **MoPub SDK v5.7.0**

在实施 MoPub SDK `onImpression` 回传方法时，请确保按照如下方式调用 Adjust SDK 的 `trackAdRevenue` 方法：

```java
public void onImpression(@NonNull final String adUnitId, @Nullable final ImpressionData impressionData) {
    // Pass impression data JSON to Adjust SDK.
    Adjust.trackAdRevenue(AdjustConfig.AD_REVENUE_MOPUB, impressionData.getJsonRepresentation());
}
```

如果您对 MoPub 广告收入跟踪有任何疑问，请联系您的专属客户经理，或发送邮件至 support@adjust.com。

[mopub-docs]:        https://developers.mopub.com/publishers/android/impression-data/
[android-readme]:    ../../chinese/README.md
