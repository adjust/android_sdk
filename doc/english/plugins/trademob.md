## Trademob plugin

If you are using Maven, add the following Trademob plugin dependency to your `build.gradle` file next to already existing dependency to Adjust SDK:

```gradle
implementation 'com.adjust.sdk:adjust-android:4.16.0'
implementation 'com.adjust.sdk:adjust-android-trademob:4.16.0'
```

You can also add Adjust Trademob plugin as JAR file which can be downloaded from our [releases page][releases].

For questions regarding this plugin, please reach out to `eugenio.warglien@trademob.com`

You can now use Trademob event in the following ways:

### View Listing

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

### View Item

```java
import com.adjust.sdk.plugin.AdjustTrademob;

AdjustEvent event = new AdjustEvent("{viewItemEventToken}");
Map<String, String> metadata = new HashMap<>();
metadata.put("info1", "value1");
metadata.put("info2", "value2");

AdjustTrademob.injectViewItemIntoEvent(event, "itemId1", metadata);
Adjust.trackEvent(event);
```

### Add to Basket

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

### Checkout

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


[releases]:  https://github.com/adjust/android_sdk/releases
