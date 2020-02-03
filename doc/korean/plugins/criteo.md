## Criteo 플러그인

Maven을 사용하고 있는 경우, 기존 Adjust SDK dependency 옆에 있는 `build.gradle` 파일에 다음의 OAID plugin dependency을 추가하세요. 다음의 Criteo plugin dependency을 추가하세요.

```gradle
implementation 'com.adjust.sdk:adjust-android:4.16.0'
implementation 'com.adjust.sdk:adjust-android-criteo:4.16.0'
```

Adjust Criteo plugin을 JAR 파일로 추가할 수도 있습니다. Adjust [releases page][releases]에서 다운로드하세요.

이제 다음 예시와 같이 각기 다른 Criteo 이벤트를 연동할 수 있습니다.

### View Listing

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{viewListingEventToken}");
List<String> productIds = Arrays.asList(&quot;productId1&quot;, &quot;productId2&quot;, &quot;productId3&quot;);
AdjustCriteo.injectViewListingIntoEvent(event, productIds);
Adjust.trackEvent(event);
```

### View Product

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{viewProductEventToken}");
AdjustCriteo.injectViewProductIntoEvent(event, "productId1");
Adjust.trackEvent(event);
```

### Cart

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{cartEventToken}");
CriteoProduct product1 = new CriteoProduct(100, 1, "productId1");
CriteoProduct product2 = new CriteoProduct(77.7f, 3, "productId2");
CriteoProduct product3 = new CriteoProduct(50, 2, "productId3");

List<CriteoProduct> products = Arrays.asList(product1, product2, product3);
AdjustCriteo.injectCartIntoEvent(event, products);
Adjust.trackEvent(event);
```

### Transaction confirmation

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{transactionConfirmedEventToken}");
CriteoProduct product1 = new CriteoProduct(100, 1, "productId1");
CriteoProduct product2 = new CriteoProduct(77.7f, 3, "productId2");
CriteoProduct product3 = new CriteoProduct(50, 2, "productId3");

List<CriteoProduct> products = Arrays.asList(product1, product2, product3);
AdjustCriteo.injectTransactionConfirmedIntoEvent(event, products, "transactionId", "newCustomerId");
Adjust.trackEvent(event);
```

### User Level

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{userLevelEventToken}");
AdjustCriteo.injectUserLevelIntoEvent(event, 1);
Adjust.trackEvent(event);
```

### User Status

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{userStatusEventToken}");
AdjustCriteo.injectUserStatusIntoEvent(event, "uiStatusValue");
Adjust.trackEvent(event);
```

### Achievement Unlocked

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{achievementUnlockedEventToken}");
AdjustCriteo.injectAchievementUnlockedIntoEvent(event, "AchievementUnlocked");
Adjust.trackEvent(event);
```

### 커스텀 이벤트

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("\{customEvent2EventToken\}");
AdjustCriteo.injectCustomEventIntoEvent(event, "uiDataValue");
Adjust.trackEvent(event);
```

### 커스텀 이벤트 2

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{customEvent2EventToken}");
AdjustCriteo.injectCustomEvent2IntoEvent(event, "uiData2Value", 3);
Adjust.trackEvent(event);
```

### Hashed Email

`injectHashedEmailIntoCriteoEvents` 메소드를 사용하여 모든 Criteo 이벤트에 해시 된 이메일을 첨부할 수 있습니다. 해시된 이메일은 앱 라이프사이클에서 모든 Criteo 이벤트와 함께 전송되므로 앱을 다시 시작하면 다시 설정해야합니다. `injectHashedEmailIntoCriteoEvents` 메소드를 'null'로 설정하여 해시 된 이메일을 제거할 수 있습니다.

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustCriteo.injectHashedEmailIntoCriteoEvents("8455938a1db5c475a87d76edacb6284e");
```

### Search dates

`injectViewSearchDatesIntoCriteoEvent` 메소드를 사용하여 모든 Criteo 이벤트에 체크인 및 체크 아웃 날짜를 첨부 할 수 있습니다. 앱 라이프사이클에서 모든 Criteo 이벤트와 함께 날짜가 전송되므로 앱을 다시 시작하면 다시 설정해야합니다.

`injectViewSearchDatesIntoCriteoEvents` 날짜를 'null'로 설정하여 검색 날짜를 제거할 수 있습니다.

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustCriteo.injectViewSearchDatesIntoCriteoEvents("2015-01-01", "2015-01-07");
```

### Partner ID

`injectPartnerIdIntoCriteoEvents` 메소드를 사용하여 모든 Criteo 이벤트에 파트너 ID를 첨부할 수 있습니다. 파트너 ID는 앱 라이프사이클에서 모든 Criteo 이벤트와 함께 전송되므로 앱을 다시 시작하면 다시 설정해야합니다. `injectPartnerIdIntoCriteoEvents` 메소드를 'null'로 설정하여 파트너 ID를 제거 할 수 있습니다.

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustCriteo.injectPartnerIdIntoCriteoEvents("{CriteoPartnerId}");
```

### 딥 링크 보내기

딥 링크를 허용하는 각 활동에 대해 onCreate 메소드를 찾고 다음 호출을 추가하십시오.

```java
import com.adjust.sdk.plugin.AdjustCriteo;

protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    Uri data = intent.getData();

    AdjustEvent event = new AdjustEvent("{deeplinkEventToken}");
    AdjustCriteo.injectDeeplinkIntoEvent(event, data);
    Adjust.trackEvent(event);
}
```

### 고객 ID

`injectCustomerIdIntoCriteoEvents` 메소드를 사용하여 모든 Criteo 이벤트에 고객 ID를 첨부 할 수 있습니다. 고객 ID는 애플리케이션 수명주기 동안 모든 Criteo 이벤트와 함께 전송되므로 앱을 다시 시작할 때 다시 설정해야합니다.

`injectCustomerIdIntoCriteoEvents` 값을 'null'로 설정하여 고객 ID를 제거 할 수 있습니다.

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustCriteo.injectCustomerIdIntoCriteoEvents("{CriteoCustomerId}");
```

### 사용자 분할

`injectUserSegmentIntoCriteoEvents` 메소드를 사용하여 모든 Criteo 이벤트에 사용자 분할을 첨부 할 수 있습니다. 고객 ID는 앱 라이프사이클에서 모든 Criteo 이벤트와 함께 전송되므로 앱을 다시 시작할 때 다시 설정해야합니다.

`injectUserSegmentIntoCriteoEvents` 값을 'null'로 설정하여 사용자 분할을 제거 할 수 있습니다.

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustCriteo.injectUserSegmentIntoCriteoEvents("{CriteoUserSegment}");
```


[releases]:  https://github.com/adjust/android_sdk/releases
