## Criteoプラグイン

adjust SDKとCriteoプラグインのdependencyを追加してください。

```
compile 'com.adjust.sdk:adjust-android-criteo:4.7.0'
```

もしくは、以下の手順でadjustとCriteoを統合できます。

1. `plugin/Criteo`フォルダを[releases page](https://github.com/adjust/android_sdk/releases)からダウンロードアーカイブに置いてください。

2. Android Studio上で`adjust`モジュールを開き、`plugin`パッケージフォルダを
   `adjust/java/com/adjust/sdk`に置いてください。

3. ダウンロードした`plugin/Criteo/com/adjust/sdk/plugin`から`AdjustCriteo.java`と
   `CriteoProduct.java`ファイルをドラッグし、`adjust`プロジェクトの`plugin`フォルダに入れてください。

下記の例のように、Criteoの各イベントを統合できます。

### リスティング

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{viewListingEventToken}");

List<String> productIds = Arrays.asList("productId1", "productId2", "productId3");

AdjustCriteo.injectViewListingIntoEvent(event, productIds, "customerId1");

Adjust.trackEvent(event);
```

### プロダクト

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{viewProductEventToken}");

AdjustCriteo.injectViewProductIntoEvent(event, "productId1", "customerId1");

Adjust.trackEvent(event);
```

### カート

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{cartEventToken}");

CriteoProduct product1 = new CriteoProduct(100, 1, "productId1");
CriteoProduct product2 = new CriteoProduct(77.7f, 3, "productId2");
CriteoProduct product3 = new CriteoProduct(50, 2, "productId3");

List<CriteoProduct> products = Arrays.asList(product1, product2, product3);

AdjustCriteo.injectCartIntoEvent(event, products, "customerId1");

Adjust.trackEvent(event);
```

### トランザクションの確認

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{transactionConfirmedEventToken}");

CriteoProduct product1 = new CriteoProduct(100, 1, "productId1");
CriteoProduct product2 = new CriteoProduct(77.7f, 3, "productId2");
CriteoProduct product3 = new CriteoProduct(50, 2, "productId3");

List<CriteoProduct> products = Arrays.asList(product1, product2, product3);

AdjustCriteo.injectTransactionConfirmedIntoEvent(event, products, "transactionId", "customerId1");

Adjust.trackEvent(event);
```

### ユーザーのレベル

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{userLevelEventToken}");

AdjustCriteo.injectUserLevelIntoEvent(event, 1, "customerId1");

Adjust.trackEvent(event);
```

### ユーザーのステータス

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{userStatusEventToken}");

AdjustCriteo.injectUserStatusIntoEvent(event, "uiStatusValue", "customerId1");

Adjust.trackEvent(event);
```

### Achievement Unlocked

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{achievementUnlockedEventToken}");

AdjustCriteo.injectAchievementUnlockedIntoEvent(event, "AchievementUnlocked", "customerId1");

Adjust.trackEvent(event);
```

### カスタムイベント

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{customEventEventToken}");

AdjustCriteo.injectCustomEventIntoEvent(event, "uiDataValue", "customerId1");

Adjust.trackEvent(event);
```

### カスタムイベント 2

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{customEvent2EventToken}");

AdjustCriteo.injectCustomEvent2IntoEvent(event, "uiData2Value", 3, "customerId1");

Adjust.trackEvent(event);
```

### ハッシュEmail

`injectHashedEmailIntoCriteoEvents`メソッドを使って、各Criteoメソッドにハッシュ化されたEmailアドレスを付与することができます。
ハッシュ化されたEmailアドレスはアプリの一ライフサイクル中にCriteoの各メソッドに送信されますので、アプリが再起動された時に再びセットされる必要があります。
`injectHashedEmailIntoCriteoEvents`を`null`に設定することで、ハッシュEmailを削除することができます。

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustCriteo.injectHashedEmailIntoCriteoEvents("8455938a1db5c475a87d76edacb6284e");
```

### 検索日

`injectViewSearchDatesIntoCriteoEvent`メソッドを使って、各Criteoメソッドにチェックインの日付とチェックアウトの日付を付与することができます。これらの日付はアプリの一ライフサイクル中にCriteoの各メソッドに送信されますので、アプリが再起動された時に再びセットされる必要があります。

`injectViewSearchDatesIntoCriteoEvents`を`null`に設定することで、これらの検索日を削除することができます。

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustCriteo.injectViewSearchDatesIntoCriteoEvents("2015-01-01", "2015-01-07");
```

### パートナーID

`injectPartnerIdIntoCriteoEvent`を使って、各CriteoメソッドにパートナーIDを付与することができます。このIDはアプリの一ライフサイクル中にCriteoの各メソッドに送信されますので、アプリが再起動された時に再びセットされる必要があります。
`injectPartnerIdIntoCriteoEvent`を`null`に設定することで、パートナーIDを削除することができます。

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustCriteo.injectPartnerIdIntoCriteoEvents("{CriteoPartnerId}");
```

### ディープリンクの送信

ディープリンクを受け取るアクティビティでそれぞれ、onCreate メソッドに以下のコールを追加してください。

```java
import com.adjust.sdk.plugin.AdjustCriteo;

protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    Uri data = intent.getData();
    
    AdjustEvent event = new AdjustEvent("{deeplinkEventToken}");
    AdjustCriteo.injectDeeplinkIntoEvent(event, data);
    Adjust.trackEvent(event);
    
    //...
}
```
