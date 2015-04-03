## Sociomantic plugin

Integrate adjust with Sociomantic events by following these steps:

1. Locate the `plugin` folder inside the downloaded archive from our [releases page](https://github.com/adjust/android_sdk/releases).

2. Open the `adjust` module in Android Studio and locate the
   `plugin` package folder in `adjust/java/com/adjust/sdk`.

3. Drag the `AdjustSociomantic.java` file from the
   downloaded `plugin` folder into the `plugin` folder in the `adjust` project.

4. You know have access to the Sociomantic events methods as well as constants that you should use for property names of your dictionaries:

    ```java
    final static String SCMCategory;
    final static String SCMProductName;
    final static String SCMSalePrice;
    final static String SCMAmount;
    final static String SCMCurrency;
    final static String SCMProductURL;
    final static String SCMProductImageURL;
    final static String SCMBrand;
    final static String SCMDescription;
    final static String SCMTimestamp;
    final static String SCMValidityTimestamp;
    final static String SCMQuantity;
    final static String SCMScore;
    final static String SCMProductID;
    final static String SCMActionConfirmed;
    final static String SCMCustomerAgeGroup;
    final static String SCMCustomerEducation;
    final static String SCMCustomerGender;
    final static String SCMCustomerID;
    final static String SCMCustomerMHash;
    final static String SCMCustomerSegment;
    final static String SCMCustomerTargeting;
    final static String SCMTransaction;
    ```

5. Now you can integrate each of the different Sociomantic events, like in the following examples:

### Customer Event

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(HOMEPAGE_TOKEN);
Map<String, String> customerData = new HashMap<>();
customerData.put(AdjustSociomantic.SCMCustomerAgeGroup, "0");

AdjustSociomantic.injectCustomerDataIntoEvent(event, customerData);

Adjust.trackEvent(event);
```

### View Home Page

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(HOMEPAGE_TOKEN);

Adjust.trackEvent(event);
```

### View Listing

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(LISTING_TOKEN);
List<String> categories = Arrays.asList("cat1", "cat2", "cat3");
String date = "1427792434"

AdjustSociomantic.injectViewListingIntoEvent(event, categories);

Adjust.trackEvent(event);

// You also can provide a date like this
AdjustSociomantic.injectViewListingIntoEvent(event, categories, date);

Adjust.trackEvent(event);
```

### View Product

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(PRODUCT_VIEW_TOKEN);
Map<String, Object> product = new HashMap<>();
List<String> categories = Arrays.asList("cat1", "cat2", "cat3");
product.put(AdjustSociomantic.SCMCategory, categories);

AdjustSociomantic.injectProductIntoEvent(event, "123456");

Adjust.trackEvent(event);

// You can also provide product information

AdjustSociomantic.injectProductIntoEvent(event, "123456", product);

Adjust.trackEvent(event);
```
*Available product parameters for reporting product view*

<table>
<colgroup>
    <col width="8%" />
    <col width="5%" />
    <col width="21%" />
    <col width="64%" />
</colgroup>
<thead>
<tr class="header">
    <th align="left">Parameter name</th>
    <th align="left">Requirement</th>
    <th align="left">Description</th>
    <th align="left">Note</th>
</tr>
</thead>
<tbody>

<tr class="odd">
    <td align="left">SCMCategory</td>
    <td align="left">Required*</td>
    <td align="left">Product category (entire category path)</td>
    <td align="left">Category information provided in the tracking code on category or listing pages should match the category information provided in the feed or in the tracking code of product pages.</td>
</tr>
<tr class="even">
    <td align="left">SCMProductName</td>
    <td align="left">Required*</td>
    <td align="left">Product name</td>
    <td align="left">Special characters should not be encoded but provided in proper UTF-8. Do not use any HTML markup.</td>
</tr>
<tr class="odd">
    <td align="left">SCMSalePrice</td>
    <td align="left">Required*</td>
    <td align="left">Sale price as decimal value (e.g. 2.99)</td>
    <td align="left">Please use a dot as a decimal separator and do not use any thousand separators.</td>
</tr>
<tr class="even">
    <td align="left">SCMAmount</td>
    <td align="left">Required*</td>
    <td align="left">Regular price as decimal value (e.g. 3.99)</td>
    <td align="left">Please use a dot as a decimal separator and do not use any thousand separators.</td>
</tr>
<tr class="odd">
    <td align="left">SCMCurrency</td>
    <td align="left">Required*</td>
    <td align="left">Currency code in ISO 4217 format (e.g. EUR)</td>
    <td align="left">Fixed currency code. Should have been provided to you in the tracking code examples.</td>
</tr>
<tr class="even">
    <td align="left">SCMProductURL></td>
    <td align="left">Required*</td>
    <td align="left">Product URL (deeplink)</td>
    <td align="left">Please provide a working deeplink ideally without any click tracking parameter (Google Analytics, HURRA, Eulerian, etc.), Please always use deeplinks with http://</td>
</tr>
<tr class="odd">
    <td align="left">SCMProductImageURL</td>
    <td align="left">Required*</td>
    <td align="left">Product image URL</td>
    <td align="left">Please provide images in a reasonable size. For an optimal appearance in the ads the images should be at least 200x200px and should have the same aspect ratio.</td>
</tr>
<tr class="even">
    <td align="left">SCMBrand</td>
    <td align="left">Required*</td>
    <td align="left">Product brand</td>
    <td align="left">Special characters should not be encoded but provided in proper UTF-8 (Same as SCMProductName above). Do not use any HTML markup.</td>
</tr>
<tr class="odd">
    <td align="left">SCMDescription</td>
    <td align="left">Optional</td>
    <td align="left">Short product description</td>
    <td align="left">Special characters should not be encoded but provided in proper UTF-8 (Same as SCMProductName above). Do not use any HTML markup.</td>
</tr>
<tr class="even">
    <td align="left">SCMTimestamp</td>
    <td align="left">Optional</td>
    <td align="left">Timestamp until when the product is available (please use GMT time)</td>
    <td align="left">Please provide the date a visitor has searched for. It should be an NSTimeInterval wrapped in NSNumber (see example).</td>
</tr>
<tr class="odd">
    <td align="left">SCMValidityTimestamp</td>
    <td align="left">Optional</td>
    <td align="left">Timestamp until when the product is available (please use GMT time)</td>
    <td align="left">Please provide the unix timestamp until when the product is available. Please use 0 for products that are always available. It should be an NSTimeInterval wrapped in NSNumber (Same as SCMTimestamp above).</td>
</tr>
<tr class="even">
    <td align="left">SCMQuantity</td>
    <td align="left">Optional</td>
    <td align="left">Number of products in stock</td>
    <td align="left">Please integrate this field only after discussion with your personal Sociomantic contact</td>
</tr>
<tr class="odd">
    <td align="left">SCMScore</td>
    <td align="left">Optional</td>
    <td align="left">Priority score of the product (value range is between 0 to 10.0)</td>
    <td align="left">Please integrate this field only after discussion with your personal Sociomantic contact</td>
</tr>

</tbody>
</table>

\*optional, if provided in the feed

If you’re not certain what setup you should use please contact your Technical Account Manager at Sociomantic.

### Cart

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(BASKET_TOKEN);
Map<String, Object> product1 = new HashMap<>();
product1.put(AdjustSociomantic.SCMProductID, "1");
product1.put(AdjustSociomantic.SCMAmount, 42);
product1.put(AdjustSociomantic.SCMCurrency, "EUR";
product1.put(AdjustSociomantic.SCMQuantity, 1);

Map<String, Object> product2 = new HashMap<>();
product2.put(AdjustSociomantic.SCMProductID, "2");

String product3 = "3";

List<Object> products = Arrays.asList(product1, product2, product3);

AdjustSociomantic.injectCartIntoEvent(event, products);

Adjust.trackEvent(event);
```

*Available cart parameters for reporting cart view*

<table>
<colgroup>
    <col width="8%" />
    <col width="5%" />
    <col width="21%" />
    <col width="64%" />
</colgroup>
<thead>
<tr class="header">
    <th align="left">Parameter name</th>
    <th align="left">Requirement</th>
    <th align="left">Description</th>
    <th align="left">Note</th>
</tr>
</thead>
<tbody>
<tr class="odd">
    <td align="left">SCMProductID</td>
    <td align="left">Required</td>
    <td align="left">Product ID</td>
    <td align="left">Please provide the product ID without any subIDs for any color or size variations.</td>
</tr>
<tr class="even">
    <td align="left">SCMAmount</td>
    <td align="left">Optional</td>
    <td align="left">Product price as decimal value (e.g. 2.99)</td>
    <td align="left">Please use a dot as a decimal separator and do not use any thousand separators. Please only provide price per product, even if quantity has a value larger than 1.</td>
</tr>
<tr class="odd">
    <td align="left">SCMCurrency</td>
    <td align="left">Optional</td>
    <td align="left">Currency code in ISO 4217 format (e.g. EUR)</td>
    <td align="left">Fixed currency code. Should have been provided to you in the tracking code examples.</td>
</tr>
<tr class="even">
    <td align="left">SCMQuantity</td>
    <td align="left">Optional</td>
    <td align="left">Quantity of the product selected</td>
    <td align="left">Please use an integer value.</td>
</tr>

</tbody>
</table>

### Unconfirmed Transaction

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(SALE_TOKEN);
Map<String, Object> product1 = new HashMap<>();
product1.put(AdjustSociomantic.SCMProductID, "1");
product1.put(AdjustSociomantic.SCMAmount, 42);
product1.put(AdjustSociomantic.SCMQuantity, 1);


Map<String, Object> product2 = new HashMap<>();
product2.put(AdjustSociomantic.SCMProductID, "2");

String product3 = "3";

List<Object> products = Arrays.asList(product1, product2, product3);

AdjustSociomantic.injectTransactionIntoEvent(event, "123456", products);

Adjust.trackEvent(event);
```

Or with parameters:

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(SALE_TOKEN);
Map<String, Object> product1 = new HashMap<>();
product1.put(AdjustSociomantic.SCMProductID, "1");
product1.put(AdjustSociomantic.SCMAmount, 42);
product1.put(AdjustSociomantic.SCMQuantity, 1);


Map<String, Object> product2 = new HashMap<>();
product2.put(AdjustSociomantic.SCMProductID, "2");

String product3 = "3";

List<Object> products = Arrays.asList(product1, product2, product3);

Map<String, Object> parameters = new HashMap<>();
parameters.put(AdjustSociomantic.SCMCurrency, "EUR");
parameters.put(AdjustSociomantic.SCMAmount, 42);

AdjustSociomantic.injectTransactionIntoEvent(event, "123456", products, parameters);

Adjust.trackEvent(event);
```

### Confirmed Transaction

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(SALE_TOKEN);
Map<String, Object> product1 = new HashMap<>();
product1.put(AdjustSociomantic.SCMProductID, "1");
product1.put(AdjustSociomantic.SCMAmount, 42);
product1.put(AdjustSociomantic.SCMQuantity, 1);


Map<String, Object> product2 = new HashMap<>();
product2.put(AdjustSociomantic.SCMProductID, "2");

String product3 = "3";

List<Object> products = Arrays.asList(product1, product2, product3);

AdjustSociomantic.injectConfirmedTransactionIntoEvent(event, "123456", products);

Adjust.trackEvent(event);
```

Or with parameters:

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(SALE_TOKEN);
Map<String, Object> product1 = new HashMap<>();
product1.put(AdjustSociomantic.SCMProductID, "1");
product1.put(AdjustSociomantic.SCMAmount, 42);
product1.put(AdjustSociomantic.SCMQuantity, 1);


Map<String, Object> product2 = new HashMap<>();
product2.put(AdjustSociomantic.SCMProductID, "2");

String product3 = "3";

List<Object> products = Arrays.asList(product1, product2, product3);

Map<String, Object> parameters = new HashMap<>();
parameters.put(AdjustSociomantic.SCMCurrency, "EUR");
parameters.put(AdjustSociomantic.SCMAmount, 42);

AdjustSociomantic.injectConfirmedTransactionIntoEvent(event, "123456", products, parameters);

Adjust.trackEvent(event);
```

*Available cart parameters for reporting transaction view*

See cart parameters

*Available transaction parameters for reporting transaction views*

<table>
<colgroup>
    <col width="8%" />
    <col width="5%" />
    <col width="21%" />
    <col width="64%" />
</colgroup>
<thead>
<tr class="header">
    <th align="left">Parameter name</th>
    <th align="left">Requirement</th>
    <th align="left">Description</th>
    <th align="left">Note</th>
</tr>
</thead>
<tbody>
<tr class="odd">
    <td align="left">SCMAmount</td>
    <td align="left">Optional</td>
    <td align="left">Product price as decimal value (e.g. 2.99)</td>
    <td align="left">Please use a dot as a decimal separator and do not use any thousand separators. Please only provide price per product, even if quantity has a value larger than 1.</td>
</tr>
<tr class="even">
    <td align="left">SCMCurrency</td>
    <td align="left">Optional</td>
    <td align="left">Currency code in ISO 4217 format (e.g. EUR)</td>
    <td align="left">Fixed currency code. Should have been provided to you in the tracking code examples.</td>
</tr>
<tr class="odd">
    <td align="left">SCMQuantity</td>
    <td align="left">Optional</td>
    <td align="left">Quantity of the product selected</td>
    <td align="left">Please use an integer value.</td>
</tr>

</tbody>
</table>

### Lead Event

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(LEAD_TOKEN);

AdjustSociomantic.injectLeadIntoEvent(event, "123456");

Adjust.trackEvent(event);
```

Or confirmed lead:

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(LEAD_TOKEN);

AdjustSociomantic.injectLeadIntoEvent(event, "123456", Boolean.TRUE);

Adjust.trackEvent(event);
```
