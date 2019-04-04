## Facebook Pixelの統合


[Facebook Pixel](https://www.facebook.com/business/help/952192354843755) はFacebookが提供するウェブサイト専用の分析ツールです。以前は、アプリ内のweb viewでPixelイベントをトラッキングするのにFacebook SDKを利用できませんでした。[FB SDK](https://developers.facebook.com/docs/analytics) v4.34のリリース以降はトラッキングが可能になり、[Hybrid Mobile App Events](https://developers.facebook.com/docs/app-events/hybrid-app-events) を使用して、Facebook PixelイベントをFacebook アプリイベントに変換します。

また、FB SDKを統合しなくても、Adjust SDKを使用してアプリ内のweb viewでFacebook Pixelを利用できるようになりました。

## Facebookの統合

### アプリサンプル

[`example-fbpixel` ディレクトリ][example-fbpixel]にあるアプリサンプルを見ると、Adjustのweb view SDKを使用してどのようにFacebook Pixelイベントをトラッキングできるかがわかります。

### FacebookアプリID

FB SDKを統合する必要はありませんが、Adjust SDKがFacebook Pixelを統合するために、一部FB SDKと同じ統合手順に従う必要があります。

まず[FB SDK Android SDKガイド](https://developers.facebook.com/docs/android/getting-started/#app_id) に記載の通り、対象のFacebookアプリIDをアプリに追加する必要があります。
統合手順は上記リンクガイドに記載がありますが、以下にも転載致します。

- `strings.xml` ファイルを開けます. パスの例: `/app/src/main/res/values/strings.xml`.
- 名前`facebook_app_id`と対象のFacebook App IDに新しい文字列を追加します。
- `AndroidManifest.xml`を開きます。
- `uses-permission` エレメントを以下のマニフェストに追加します。


  ```xml
  <uses-permission android:name="android.permission.INTERNET"/>
  ```

- `meta-data`エレメントを`application`エレメントに追加します。

```xml
  <application android:label="@string/app_name" ...>
    ...
    <meta-data android:name="com.facebook.sdk.ApplicationId" android:value="@string/facebook_app_id"/>
    ...
  </application>
  ```

### Facebook Pixelの設定

Facebook Pixelの統合方法については、Facebookのガイドに従ってください。Javascriptコードは以下のように記述する必要があります。

```js
<!-- Facebook Pixel Code -->
<script>
  !function(f,b,e,v,n,t,s)
    ...
  fbq('init', <YOUR_PIXEL_ID>);
  fbq('track', 'PageView');
</script>
...
<!-- End Facebook Pixel Code -->
```

この後、[Hybrid Mobile App Eventsガイド](https://developers.facebook.com/docs/app-events/hybrid-app-events) の`Update Your Pixel`セクションに記載の通り、Facebook Pixelのコードを以下のように変更するだけです。


```js
fbq('init', <YOUR_PIXEL_ID>);
fbq('set', 'mobileBridge', <YOUR_PIXEL_ID>, <YOUR_FB_APP_ID>);
```


**注意**：最初に`'init'`メソッドを呼び出し、直後に`'set'`メソッドを呼び出すことが**非常に重要**です。HTMLのウェブページに貼り付けるFacebookが提供する（上記に示すような）スクリプトスニペットには、`'init'`メソッドの呼び出しの直後にページビューイベントの`'track'`メソッドが含まれています。このページビューイベントを正しくトラッキングするには、両者の間に必ず`'set'`メソッドを呼び出してください。

## Adjustの統合

### Facebook SDK Javascriptインターフェイスの登録


[Android web view SDK](web_views_ja.md) アプリの統合ガイドを参照ください。登録のセクションで、Adjustブリッジのデフォルトのインスタンスを取得します（以下参照）。

```java
AdjustBridge.registerAndGetInstance(getApplication(), webview);
```

save the return instance, as `adjustBridgeInstance`, for example, and then add the following line:

```java
adjustBridgeInstance.registerFacebookSDKJSInterface();
```

### Event名の設定

Adjust web bridge SDKは、Facebook PixelイベントをAdjustイベントに変換します。

このため、Facebook Pixel設定の`fbq('track', 'PageView');`をコピーペーストで追加し、Adjust SDKを開始してFacebook Pixelイベントをトラッキングする ***前*** に、Facebook Pixelsを特定のAdjustイベントにマッピングするか、デフォルトのAdjustイベントトークンを設定する必要があります。

Facebook PixelイベントをAdjustイベントにマッピングするには、Adjust SDKを初期化する前に`adjustConfig`インスタンスの`addFbPixelMapping(fbEventNameKey, adjEventTokenValue)`を呼び出します。マッピングの例は以下の通りです。


```js
adjustConfig.addFbPixelMapping('fb_mobile_search', adjustEventTokenForSearch);
adjustConfig.addFbPixelMapping('fb_mobile_purchase', adjustEventTokenForPurchase);
```

注意：これは、以下のFacebook Pixelイベントをトラッキングする際の`fbq('track', 'Search', ...);`および`fbq('track', 'Purchase', ...);`にそれぞれ対応します。残念ながら、Javascriptでトラッキングされるイベント名とFB SDKで使用されるイベント名との間のすべてのマッピングスキームにはアクセスできません。

参考として、以下はAdjustがこれまで確認したイベント名の情報になります。

| Pixelイベント名 | 対応するFacebookアプリのイベント名
| ---------------- | -------------------------------------
| ViewContent      | fb_mobile_content_view
| Search           | fb_mobile_search
| AddToCart        | fb_mobile_add_to_cart
| AddToWishlist    | fb_mobile_add_to_wishlist
| InitiateCheckout | fb_mobile_initiated_checkout
| AddPaymentInfo   | fb_mobile_add_payment_info
| Purchase         | fb_mobile_purchase
| CompleteRegistration | fb_mobile_complete_registration


これは完全なリストではない可能性があります。また、Facebookが現在のリストに追加や更新を加える可能性もあります。テスト中は、Adjustログで以下のような警告を確認してください。

```
There is not a default event token configured or a mapping found for event named: 'fb_mobile_search'. It won't be tracked as an adjust event
```

```
イベント名'fb_mobile_search'について、設定されたデフォルトイベントトークンが存在しないか、マッピングが見つかりません。Adjustイベントとしてトラッキングできません。
```

また、マッピングを設定しない場合でもデフォルトのAdjustイベントの使用は可能です。Adjust SDKを初期化する前に、`adjustConfig.setFbPixelDefaultEventToken(defaultEventToken);`を呼び出してください。


[example-fbpixel]:  ../../Adjust/example-app-fbpixel
