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
