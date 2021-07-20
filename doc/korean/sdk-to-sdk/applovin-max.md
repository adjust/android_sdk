# Adjust SDK에서 AppLovin MAX 광고 매출 트래킹

[Adjust Android SDK README][android-readme]

이 기능에 필요한 최소 SDK 버전:

- **Adjust SDK v4.28.0**

AppLovin MAX SDK와 광고 매출을 트래킹하고 싶다면, Adjust의 SDK간 연동을 사용하여 Adjust 백엔드로 해당 정보를 전송할 수 있습니다. 이를 실행하려면 `trackAdRevenue` 메서드를 `AD_REVENUE_APPLOVIN_MAX` 소스와 함께 호출하시기 바랍니다.

> 참고: AppLovin MAX와의 광고 매출 트래킹에 관한 문의 사항은 담당 어카운트 매니저나 [support@adjust.com](mailto:support@adjust.com)으로 연락주시기 바랍니다.

### 예시

```java
@Override
public void onAdRevenuePaid(final MaxAd ad) {
    AdjustAdRevenue adjustAdRevenue = new AdjustAdRevenue( AdjustConfig.AD_REVENUE_APPLOVIN_MAX);
    adjustAdRevenue.setRevenue(ad.getRevenue(), "USD");
    adjustAdRevenue.setAdRevenueNetwork(ad.getNetworkName());
    adjustAdRevenue.setAdRevenueUnit(ad.getAdUnitId());
    adjustAdRevenue.setAdRevenuePlacement(ad.getPlacement());

    Adjust.trackAdRevenue(adjustAdRevenue);
}

```

[android-readme]:    https://github.com/adjust/android_sdk/blob/master/doc/korean/README.md

