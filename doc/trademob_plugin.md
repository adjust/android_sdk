## Trademob plugin

Integrate adjust with Trademob events by following these steps:

1. Locate the `plugin` folder inside the downloaded archive from our
   [releases page](https://github.com/adjust/android_sdk/releases).

2. Open the `adjust` module in Android Studio and locate the
   `plugin` package folder in `adjust/java/com/adjust/sdk`.

3. Drag the `AdjustTrademob.java` and `TrademobItem.java` files from the
   downloaded `plugin` folder into the `plugin` folder in the `adjust` project.

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

### Add to Busket

```java
import com.adjust.sdk.plugin.AdjustTrademob;
import com.adjust.sdk.plugin.TrademobItem;

AdjustEvent event = new AdjustEvent("{basketEventToken}");

TrademobItem itemId1 = new TrademobItem("itemId1", 2, 54f);
TrademobItem itemId2 = new TrademobItem("itemId2", 1, 3f);
TrademobItem itemId3 = new TrademobItem("itemId3", 4, 25f);

List<TrademobItem> items = Arrays.asList(itemId1, itemId2, itemId3);

AdjustTrademob.injectAddToBusketIntoEvent(event, items, null);

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
