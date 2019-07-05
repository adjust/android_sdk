## MoPubの広告収益をAdjust SDKで計測

[Adjust Android SDK README][android-readme]

[MoPub Android documentation][mopub-docs]

本機能には以下のSDKバージョンとそれ以降のバージョンが必須となります：

- **Adjust SDK v4.18.0**
- **MoPub SDK v5.7.0**

MoPub SDKの`onImpression`コールバックメソッドの実装内で、以下のようにAdjust SDKの`trackAdRevenue`メソッドを呼び出す必要があります。

```java
public void onImpression(@NonNull final String adUnitId, @Nullable final ImpressionData impressionData) {
    // インプレッションデータのJSONをAdjust SDKにパスします。
    Adjust.trackAdRevenue(AdjustConfig.AD_REVENUE_MOPUB, impressionData.getJsonRepresentation());
}
```

MoPub連携による広告収益計測についてご質問がございましたら、担当のアカウントマネージャーもしくはsupport@adjust.comまでお問い合わせください。

[mopub-docs]:        https://developers.mopub.com/publishers/android/impression-data/
[android-readme]:    ../../japanese/README.md
