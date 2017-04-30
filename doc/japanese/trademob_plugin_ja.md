## Trademobプラグイン

Adjust SDKとTrademobプラグインのdependencyを追加してください。

```
compile 'com.adjust.sdk:adjust-android-trademob:4.11.3'
```

Or integrate adjust with Trademob events by following these steps:
もしくは、以下の手順でAdjustとTrademobイベントを連携できます。

1. `plugin/Trademob`フォルダを[releases page](https://github.com/adjust/android_sdk/releases)からダウンロードアーカイブに置いてください。

2. Android Studio上で`adjust`モジュールを開き、`plugin`パッケージフォルダを`adjust/java/com/adjust/sdk`に置いてください。

3. ダウンロードした`plugin/Trademob/com/adjust/sdk/plugin`フォルダから`AdjustTrademob.java`と`TrademobItem.java`ファイルをドラッグし、`adjust`プロジェクトの`plugin`フォルダに入れてください。

プラグインに関するご質問は、`eugenio.warglien@trademob.com`までご連絡ください。

これで下記の例のようにTrademobイベントを利用できます。

### リストを見る

```java
import com.adjust.sdk.plugin.AdjustTrademob;

AdjustEvent event = new AdjustEvent("{viewListingEventToken}");

List<String> items = Arrays.asList("itemId1", "itemId2", "itemId3");

Map<String, String> metadata = new HashMap<>();
metadata.put("info1", "value1");
metadata.put("info2", "value2");

AdjustTrademob.injectViewListingIntoEvent(event, items, metadata);

Adjust.trackEvent(event);
```

### 商品を見る

```java
import com.adjust.sdk.plugin.AdjustTrademob;

AdjustEvent event = new AdjustEvent("{viewItemEventToken}");

Map<String, String> metadata = new HashMap<>();
metadata.put("info1", "value1");
metadata.put("info2", "value2");

AdjustTrademob.injectViewItemIntoEvent(event, "itemId1", metadata);

Adjust.trackEvent(event);
```

### 買い物かごに追加

```java
import com.adjust.sdk.plugin.AdjustTrademob;
import com.adjust.sdk.plugin.TrademobItem;

AdjustEvent event = new AdjustEvent("{basketEventToken}");

TrademobItem itemId1 = new TrademobItem("itemId1", 2, 54f);
TrademobItem itemId2 = new TrademobItem("itemId2", 1, 3f);
TrademobItem itemId3 = new TrademobItem("itemId3", 4, 25f);

List<TrademobItem> items = Arrays.asList(itemId1, itemId2, itemId3);

AdjustTrademob.injectAddToBasketIntoEvent(event, items, null);

Adjust.trackEvent(event);
```

### チェックアウト

```java
import com.adjust.sdk.plugin.AdjustTrademob;
import com.adjust.sdk.plugin.TrademobItem;

AdjustEvent event = new AdjustEvent("{checkoutEventToken}");

TrademobItem itemId1 = new TrademobItem("itemId1", 2, 54f);
TrademobItem itemId2 = new TrademobItem("itemId2", 1, 3f);
TrademobItem itemId3 = new TrademobItem("itemId3", 4, 25f);

List<TrademobItem> items = Arrays.asList(itemId1, itemId2, itemId3);

Map<String, String> metadata = new HashMap<>();
metadata.put("info1", "value1");
metadata.put("info2", "value2");

AdjustTrademob.injectCheckoutIntoEvent(event, items, metadata);

Adjust.trackEvent(event);
```
