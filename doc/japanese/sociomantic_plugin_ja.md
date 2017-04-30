## Sociomanticプラグイン

Sociomanticプラグインを連携させるには、adjust SDKとSociomanticプラグインのDependencyを追加してください。

```
compile 'com.adjust.sdk:adjust-android-sociomantic:4.11.0'
```

もしくは、次の手順に従ってSociomanticをAdjustに連携させてください。

1. `plugin/Sociomantic`フォルダを[releases page](https://github.com/adjust/android_sdk/releases)からダウンロードアーカイブに置いてください。

2. Android Studio上で`adjust`モジュールを開き、`plugin`パッケージフォルダを`adjust/java/com/adjust/sdk`に置いてください。

3. ダウンロードした`plugin/Sociomantic/com/adjust/sdk/plugin`フォルダから`AdjustSociomantic.java`ファイルをドラッグし、`adjust`プロジェクトの`plugin`フォルダに入れてください。

4. これで、ディクショナリのプロパティ名としてお使いの定数と同様に、Sociomanticのイベントメソッドを使えるようになります。

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

5. Sociomanticの送信の前に、下記のようなパートナーIDを設定してください。

    ```java
    import com.adjust.sdk.plugin.AdjustSociomantic;

    AdjustSociomantic.injectPartnerIdInSociomanticEvents("{sociomanticPartnerId}");
    ```

6. これで個々のSociomanticイベントを連携できます。例は以下をご参照ください。

### カスタマーイベント

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(HOMEPAGE_TOKEN);
Map<String, String> customerData = new HashMap<>();
customerData.put(AdjustSociomantic.SCMCustomerAgeGroup, "0");

AdjustSociomantic.injectCustomerDataIntoEvent(event, customerData);

Adjust.trackEvent(event);
```

### ホームページ閲覧

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(HOMEPAGE_TOKEN);

Adjust.trackEvent(event);
```

### リスティング閲覧

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

### プロダクト閲覧

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
*プロダクトのレポートに利用できるプロダクトパラメータ*

<table>
<colgroup>
    <col width="8%" />
    <col width="5%" />
    <col width="21%" />
    <col width="64%" />
</colgroup>
<thead>
<tr class="header">
    <th align="left">パラメータ名</th>
    <th align="left">条件</th>
    <th align="left">説明</th>
    <th align="left">備考</th>
</tr>
</thead>
<tbody>

<tr class="odd">
    <td align="left">SCMCategory</td>
    <td align="left">必須*</td>
    <td align="left">プロダクトのカテゴリ (カテゴリのパス全て)</td>
    <td align="left">カテゴリまたはリスティングページのトラッキングコード内にあるカテゴリ情報は、フィードもしくはプロダクトページのトラッキングコード内のカテゴリ情報と一致する必要があります。</td>
</tr>
<tr class="even">
    <td align="left">SCMProductName</td>
    <td align="left">必須*</td>
    <td align="left">プロダクト名</td>
    <td align="left">特殊文字を含めず、UFT-8でエンコードしてください。HTMLマークアップは使えません。</td>
</tr>
<tr class="odd">
    <td align="left">SCMSalePrice</td>
    <td align="left">必須*</td>
    <td align="left">小数値でのセール価格(例 2.99)</td>
    <td align="left">小数点にはドットをお使いください。ドットやカンマなどの3桁区切りは入れないでください。</td>
</tr>
<tr class="even">
    <td align="left">SCMAmount</td>
    <td align="left">必須*</td>
    <td align="left">小数値の通常価格 (例 3.99)</td>
    <td align="left">小数点にはドットをお使いください。ドットやカンマなどの3桁区切りは入れないでください。</td>
</tr>
<tr class="odd">
    <td align="left">SCMCurrency</td>
    <td align="left">必須*</td>
    <td align="left">ISO 4217 formatにおける通貨コード (例 EUR)</td>
    <td align="left">トラッキングコード例で確認できます。</td>
</tr>
<tr class="even">
    <td align="left">SCMProductURL></td>
    <td align="left">必須*</td>
    <td align="left">プロダクトURL (ディープリンク)</td>
    <td align="left">機能しているディープリンクを入れてください。できればGoogleアナリティクス、HURRA、Eulerianなどのトラッキングパラメータをつけないでください。。必ず http:// をつけたディープリンクをお使いください。</td>
</tr>
<tr class="odd">
    <td align="left">SCMProductImageURL</td>
    <td align="left">必須*</td>
    <td align="left">プロダクトの画像URL</td>
    <td align="left">適切な大きさの画像をご用意ください。広告内に任意で表示されるがそうの大きさは最低200x200pxで、同じアスペクト比である必要があります。</td>
</tr>
<tr class="even">
    <td align="left">SCMBrand</td>
    <td align="left">必須*</td>
    <td align="left">プロダクトのブランド</td>
    <td align="left">上記SCMProductNameと同様、特殊文字を含めず、UFT-8でエンコードしてください。HTMLマークアップは使えません。</td>
</tr>
<tr class="odd">
    <td align="left">SCMDescription</td>
    <td align="left">任意</td>
    <td align="left">プロダクトの短い説明</td>
    <td align="left">上記SCMProductNameと同様、特殊文字を含めず、UFT-8でエンコードしてください。HTMLマークアップは使えません。</td>
</tr>
<tr class="even">
    <td align="left">SCMTimestamp</td>
    <td align="left">任意</td>
    <td align="left">プロダクトが利用可能になるまでのタイムスタンプ (GMT時間で入力してください)</td>
    <td align="left">ユーザーが検索した日付を入れてください。NSTimeIntervalをNSNumberに入れ子してください。(例を参照)</td>
</tr>
<tr class="odd">
    <td align="left">SCMValidityTimestamp</td>
    <td align="left">任意</td>
    <td align="left">プロダクトが利用可能になるまでのタイムスタンプ (GMT時間で入力してください)</td>
    <td align="left">プロダクトが利用可能になるまでのunixのタイムスタンプを入れてください。常に利用可能なプロダクトは0を入れてください。ユーザーが検索した日付を入れてください。NSTimeIntervalをNSNumberに入れ子してください。(上記SCMTimestampと同様)</td>
</tr>
<tr class="even">
    <td align="left">SCMQuantity</td>
    <td align="left">任意</td>
    <td align="left">プロダクトの在庫数</td>
    <td align="left">必ずSociomanticの担当者にご相談の上、この欄に入力してください。</td>
</tr>
<tr class="odd">
    <td align="left">SCMScore</td>
    <td align="left">任意</td>
    <td align="left">プロダクトの優先度スコア (0 から 10.0 までの数値)</td>
    <td align="left">必ずSociomanticの担当者にご相談の上、この欄に入力してください。</td>
</tr>

</tbody>
</table>

\*任意。フィード上で確認できれば入力してください。

設定についてご質問があれば、Sociomanticの技術アカウントマネージャーまでご連絡ください。

### カート

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(BASKET_TOKEN);
Map<String, Object> product1 = new HashMap<>();
product1.put(AdjustSociomantic.SCMProductID, "1");
product1.put(AdjustSociomantic.SCMAmount, 42);
product1.put(AdjustSociomantic.SCMCurrency, "EUR");
product1.put(AdjustSociomantic.SCMQuantity, 1);

Map<String, Object> product2 = new HashMap<>();
product2.put(AdjustSociomantic.SCMProductID, "2");

String product3 = "3";

List<Object> products = Arrays.asList(product1, product2, product3);

AdjustSociomantic.injectCartIntoEvent(event, products);

Adjust.trackEvent(event);
```

*カートのレポートに利用できるプロダクトパラメータ*

<table>
<colgroup>
    <col width="8%" />
    <col width="5%" />
    <col width="21%" />
    <col width="64%" />
</colgroup>
<thead>
<tr class="header">
    <th align="left">パラメータ名</th>
    <th align="left">条件</th>
    <th align="left">説明</th>
    <th align="left">備考</th>
</tr>
</thead>
<tbody>
<tr class="odd">
    <td align="left">SCMProductID</td>
    <td align="left">必須</td>
    <td align="left">プロダクトID</td>
    <td align="left">カラーやサイズなどのバリエーションのサブIDは含めないでください。</td>
</tr>
<tr class="even">
    <td align="left">SCMAmount</td>
    <td align="left">任意</td>
    <td align="left">小数値での販売価格 (例 2.99)</td>
    <td align="left">小数点にはドットをお使いください。ドットやカンマなどの3桁区切りは入れないでください。個数が2以上であっても、1商品あたりの価格を入れてください。</td>
</tr>
<tr class="odd">
    <td align="left">SCMCurrency</td>
    <td align="left">任意</td>
    <td align="left">ISO 4217 formatにおける通貨コード (例 EUR)</td>
    <td align="left">トラッキングコード例で確認できます。</td>
</tr>
<tr class="even">
    <td align="left">SCMQuantity</td>
    <td align="left">任意</td>
    <td align="left">選択したプロダクトの個数</td>
    <td align="left">整数値を入れてください。</td>
</tr>

</tbody>
</table>

### 未確認トランザクション

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

パラメータがあれば

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

### 確認済みトランザクション

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

パラメータがあれば

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

*トランザクションのレポートに利用できるカートパラメータ*

カートパラメータをご参照ください。

*トランザクションのレポートに利用できるトランザクションパラメータ*

<table>
<colgroup>
    <col width="8%" />
    <col width="5%" />
    <col width="21%" />
    <col width="64%" />
</colgroup>
<thead>
<tr class="header">
    <th align="left">パラメータ名</th>
    <th align="left">条件</th>
    <th align="left">説明</th>
    <th align="left">備考</th>
</tr>
</thead>
<tbody>
<tr class="odd">
    <td align="left">SCMAmount</td>
    <td align="left">任意</td>
    <td align="left">小数値の販売価格 (例 2.99)</td>
    <td align="left">小数点にはドットをお使いください。ドットやカンマなどの3桁区切りは入れないでください。個数が2以上であっても、1商品あたりの価格を入れてください。</td>
</tr>
<tr class="even">
    <td align="left">SCMCurrency</td>
    <td align="left">任意</td>
    <td align="left">ISO 4217 formatにおける通貨コード (例 EUR)</td>
    <td align="left">トラッキングコード例で確認できます。</td>
</tr>
<tr class="odd">
    <td align="left">SCMQuantity</td>
    <td align="left">任意</td>
    <td align="left">選択したプロダクトの個数</td>
    <td align="left">整数値を入れてください。</td>
</tr>

</tbody>
</table>

### リードイベント

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(LEAD_TOKEN);

AdjustSociomantic.injectLeadIntoEvent(event, "123456");

Adjust.trackEvent(event);
```

リードが確認済みであれば

```java
import com.adjust.sdk.plugin.AdjustSociomantic;

AdjustEvent event = new AdjustEvent(LEAD_TOKEN);

AdjustSociomantic.injectLeadIntoEvent(event, "123456", Boolean.TRUE);

Adjust.trackEvent(event);
```
