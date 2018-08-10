## Facebook pixel integration

[The Facebook Pixel](https://www.facebook.com/business/help/952192354843755) is a web-only analytics tool from Facebook. In the past it was impossible to use the Facebook SDK to track Pixel events in an app's webview. Since the release of [FB SDK](https://developers.facebook.com/docs/analytics) v4.34, it's now possible to do so, and use the [Hybrid Mobile App Events](https://developers.facebook.com/docs/app-events/hybrid-app-events) 
to convert Facebook Pixel events into Facebook App events.

It is also now possible to use the Facebook Pixel in your app's webview with the Adjust SDK, without integrating the FB SDK.

### Facebook integration

#### Facebook App ID

There is no need to integrate the FB SDK; however, you must follow a few of the same integration steps from the FB SDK in order for the Adjust SDK to integrate the Facebook Pixel.

As is described in the [FB SDK Android SDK guide](https://developers.facebook.com/docs/android/getting-started/#app_id) 
you will need to add your Facebook App ID to the app. You can follow the steps in that guide, but we've also copied them here below:

1. Open your `strings.xml` file. Example path: `/app/src/main/res/values/strings.xml`.

2. Add a new string with the name `facebook_app_id` and value as your Facebook App ID

3. Open `AndroidManifest.xml`

4. Add a `uses-permission` element to the manifest:

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

5. Add a `meta-data` element to the `application` element:

```xml
<application android:label="@string/app_name" ...>
    ...
    <meta-data android:name="com.facebook.sdk.ApplicationId" android:value="@string/facebook_app_id"/>
    ...
</application>
```

#### Facebook Pixel configuration

Follow Facebook's guide on how to integrate the Facebook Pixel. The Javascript code should look something like this:

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

Now, just as described in the [Hybrid Mobile App Events guide](https://developers.facebook.com/docs/app-events/hybrid-app-events)
`Update Your Pixel` section, you'll need to update the Facebook Pixel code like this:

```js
fbq('init', <YOUR_PIXEL_ID>);
fbq('set', 'mobileBridge', <YOUR_PIXEL_ID>, <YOUR_FB_APP_ID>);
```

### Adjust integration

#### Register the Facebook SDK Javascript interface

Follow the integration guide for [Android web view](web_views.md) apps. 
In the section where you register and get the Adjust bridge default instance (see below):

```java
AdjustBridge.registerAndGetInstance(getApplication(), webview);
```

save the return instance, as `adjustBridgeInstance`, for example, and then add the following line:

```java
adjustBridgeInstance.registerFacebookSDKJSInterface();
```

#### Event name configuration

The Adjust web bridge SDK translates Facebook Pixel events into Adjust events.

For this reason it's necessary to map Facebook Pixels to specific Adjust events, or to 
configure a default Adjust event token ***before*** tracking any Facebook Pixel event, 
including the copy-pasted `fbq('track', 'PageView');` from the Facebook Pixel configuration.

To map Facebook Pixel events and Adjust events, call `addFbPixelMapping(fbEventNameKey, adjEventTokenValue)` 
in the `adjustConfig` instance before initializing the Adjust SDK. An example of mapping could be:

```js
adjustConfig.addFbPixelMapping('fb_mobile_search', adjustEventTokenForSearch);
adjustConfig.addFbPixelMapping('fb_mobile_purchase', adjustEventTokenForPurchase);
```

Note that this would match when tracking the Facebook pixel events `fbq('track', 'Search', ...);` and
`fbq('track', 'Purchase', ...);` respectively. Unfortunatly, we do not have access to the entire mapping scheme between the event names tracked in Javascript and the event names used by the FB SDK. 

To help you, here is the event name information we've found so far:

| Pixel event name | Corresponding Facebook app event name
| ---------------- | -------------------------------------
| ViewContent      | fb_mobile_content_view
| Search           | fb_mobile_search
| AddToCart        | fb_mobile_add_to_cart
| AddToWishlist    | fb_mobile_add_to_wishlist
| InitiateCheckout | fb_mobile_initiated_checkout
| AddPaymentInfo   | fb_mobile_add_payment_info
| Purchase         | fb_mobile_purchase
| CompleteRegistration | fb_mobile_complete_registration

This may not be an exhaustive list; it's also possible that Facebook will add to or update the current list. While testing, check the Adjust logs for warnings such as:

```
There is not a default event token configured or a mapping found for event named: 'fb_mobile_search'. It won't be tracked as an adjust event
```

There is also the option to use a default Adjust event even if you do not have mapping configured. 
Just call `adjustConfig.setFbPixelDefaultEventToken(defaultEventToken);` before initializing the Adjust SDK.


