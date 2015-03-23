## Criteo plugin

Integrate adjust with Criteo events by following these steps:

1. Locate the `plugin` folder inside the downloaded archive from our
   [releases page](https://github.com/adjust/android_sdk/releases).

2. Open the `adjust` module in Android Studio and locate the
   `plugin` package folder in `adjust/java/com/adjust/sdk`.

3. Drag the `AdjustCriteo.java` and `CriteoProduct.java` files from the
   downloaded `plugin` folder into the `plugin` folder in the `adjust` project.

Now you can integrate each of the different Criteo events, like in the
following examples:

### View Homepage

```java
AdjustEvent event = new AdjustEvent("{viewHomepageEventToken}");

Adjust.trackEvent(event);
```

### View Search

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{viewSearchEventToken}");
AdjustCriteo.injectViewSearchIntoEvent(event, "2015-01-01", "2015-01-07");

Adjust.trackEvent(event);
```

### View Listing

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{viewListingEventToken}");

List<String> productIds = Arrays.asList("productId1", "productId2", "productId3");

AdjustCriteo.injectViewListingIntoEvent(event, productIds, "customerId1");

Adjust.trackEvent(event);
```

### View Product

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{viewProductEventToken}");

AdjustCriteo.injectViewProductIntoEvent(event, "productId1", "customerId1");

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

AdjustCriteo.injectCartIntoEvent(event, products, "customerId1");

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

AdjustCriteo.injectTransactionConfirmedIntoEvent(event, products, "customerId1");

Adjust.trackEvent(event);
```

### User Level

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{userLevelEventToken}");

AdjustCriteo.injectUserLevelIntoEvent(event, 1, "customerId1");

Adjust.trackEvent(event);
```

### User Status

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

### Custom Event

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{customEventEventToken}");

AdjustCriteo.injectCustomEventIntoEvent(event, "uiDataValue", "customerId1");

Adjust.trackEvent(event);
```

### Custom Event 2

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{customEvent2EventToken}");

AdjustCriteo.injectCustomEvent2IntoEvent(event, "uiData2Value", 3, "customerId1");

Adjust.trackEvent(event);
```

### Hashed Email

It's possible to attach an hashed email in every Criteo event with the `injectHashedEmailIntoCriteoEvents` method.
The hashed email will be sent with every Criteo event for the duration of the application lifecycle,
so it must be set again when the app is re-lauched.
The hashed email can be removed by setting the `injectHashedEmailIntoCriteoEvents` method with `null`.

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustCriteo.injectHashedEmailIntoCriteoEvents("8455938a1db5c475a87d76edacb6284e");
```
