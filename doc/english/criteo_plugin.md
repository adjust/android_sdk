## Criteo plugin

Add the dependency of the adjust sdk with the Criteo plugin:

```
compile 'com.adjust.sdk:adjust-android-criteo:4.11.5'
```

Or integrate adjust with Criteo events by following these steps:

1. Locate the `plugin/Criteo` folder inside the downloaded archive from our
   [releases page](https://github.com/adjust/android_sdk/releases).

2. Open the `adjust` module in Android Studio and locate the
   `plugin` package folder in `adjust/java/com/adjust/sdk`.

3. Drag the `AdjustCriteo.java` and `CriteoProduct.java` files from the
   downloaded `plugin/Criteo/com/adjust/sdk/plugin` folder into the `plugin` folder in the `adjust` project.

Now you can integrate each of the different Criteo events, like in the
following examples:

### View Listing

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{viewListingEventToken}");

List<String> productIds = Arrays.asList("productId1", "productId2", "productId3");

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

### Custom Event

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{customEventEventToken}");

AdjustCriteo.injectCustomEventIntoEvent(event, "uiDataValue");

Adjust.trackEvent(event);
```

### Custom Event 2

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustEvent event = new AdjustEvent("{customEvent2EventToken}");

AdjustCriteo.injectCustomEvent2IntoEvent(event, "uiData2Value", 3);

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

### Search dates

It's possible to attach a check-in and check-out date to every Criteo event with the `injectViewSearchDatesIntoCriteoEvent` method. The dates will be sent with every Criteo event for the duration of the application lifecycle, so it must be set again when the app is re-lauched.

The search dates can be removed by setting the `injectViewSearchDatesIntoCriteoEvents` dates with `null`.

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustCriteo.injectViewSearchDatesIntoCriteoEvents("2015-01-01", "2015-01-07");
```

### Partner id

It's possible to attach a partner id in every Criteo event with the `injectPartnerIdIntoCriteoEvents` method.
The partner id will be sent with every Criteo event for the duration of the application lifecycle,
so it must be set again when the app is re-lauched.
The partner id can be removed by setting the `injectPartnerIdIntoCriteoEvents` method with `null`.

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustCriteo.injectPartnerIdIntoCriteoEvents("{CriteoPartnerId}");
```

### Send deeplink

For each activity that accepts deep links, find the onCreate method and add the folowing call:

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

#### Customer ID

It's possible to attach the customer ID to every Criteo event with the `injectCustomerIdIntoCriteoEvents` method. The customer ID will be sent with every Criteo event for the duration of the application life cycle, so it must be set again when the app is re-launched.

The customer ID can be removed by setting the `injectCustomerIdIntoCriteoEvents` value to `null`.

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustCriteo.injectCustomerIdIntoCriteoEvents("{CriteoCustomerId}");
```

#### User Segment

It's possible to attach the user segment to every Criteo event with the `injectUserSegmentIntoCriteoEvents` method. The user segment will be sent with every Criteo event for the duration of the application life cycle, so it must be set again when the app is re-launched.

The user segment can be removed by setting the `injectUserSegmentIntoCriteoEvents` value to `null`.

```java
import com.adjust.sdk.plugin.AdjustCriteo;

AdjustCriteo.injectUserSegmentIntoCriteoEvents("{CriteoUserSegment}");
```
