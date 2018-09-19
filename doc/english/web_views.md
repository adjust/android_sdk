## Summary

This is the guide to the Android SDK of Adjust™ for Android apps which are using web views. You can read more about Adjust™ at [adjust.com].

## Table of contents

* [Example app](#example-app)
* [Basic integration](#basic-integration)
   * [Add native Adjust Android SDK](#native-add)
   * [Add Adjust web bridge SDK to your project](#bridge-add)
   * [Add Google Play Services](#sdk-gps)
   * [Add permissions](#sdk-permissions)
   * [Proguard settings](#sdk-proguard)
   * [Install referrer](#install-referrer)
      * [Google Play Referrer API](#gpr-api)
      * [Google Play Store intent](#gps-intent)
   * [Integrate Adjust web bridge SDK into your app](#bridge-integrate-app)
   * [Integrate Adjust web bridge SDK into your web view](#bridge-integrate-web)
   * [Session tracking](#session-tracking)
      * [API level 14 and higher](#session-tracking-api14)
      * [API level between 9 and 13](#session-tracking-api9)
   * [Adjust logging](#adjust-logging)
   * [Build your app](#build-the-app)
* [Additional features](#additional-features)
   * [Event tracking](#event-tracking)
      * [Track revenue](#revenue-tracking)
      * [Revenue deduplication](#revenue-deduplication)
      * [Callback parameters](#callback-parameters)
      * [Partner parameters](#partner-parameters)
      * [Callback identifier](#callback-id)
   * [Session parameters](#session-parameters)
      * [Session callback parameters](#session-callback-parameters)
      * [Session partner parameters](#session-partner-parameters)
      * [Delay start](#delay-start)
   * [Attribution callback](#attribution-callback)
   * [Session and event callbacks](#session-event-callbacks)
   * [Disable tracking](#disable-tracking)
   * [Offline mode](#offline-mode)
   * [Event buffering](#event-buffering)
   * [GDPR right to be forgotten](#gdpr-forget-me)
   * [SDK signature](#sdk-signature)
   * [Background tracking](#background-tracking)
   * [Device IDs](#device-ids)
      * [Google Play Services advertising identifier](#di-gps-adid)
      * [Amazon advertising identifier](#di-amz-adid)
      * [Adjust device identifier](#di-adid)
   * [User attribution](#user-attribution)
   * [Push token](#push-token)
   * [Pre-installed trackers](#pre-installed-trackers)
   * [Deep linking](#deeplinking)
      * [Standard deep linking scenario](#deeplinking-standard)
      * [Deferred deep linking scenario](#deeplinking-deferred)
      * [Reattribution via deep links](#deeplinking-reattribution)
* [License](#license)

## <a id="example-app"></a>Example app

There is an example app inside the [`example-webbridge` directory][example-webbridge]. You can open the Android project to see an example on how the Adjust SDK can be integrated.

## <a id="basic-integration"></a>Basic integration

These are the minimal steps required to integrate the Adjust SDK into your Android project. We are going to assume that you use Android Studio for your Android development and target an Android API level 9 (Gingerbread) or later.

### <a id="native-add">Add native Adjust Android SDK

If you are using Maven, add the following to your `build.gradle` file:

```
compile 'com.adjust.sdk:adjust-android:4.15.1'
compile 'com.android.installreferrer:installreferrer:1.0'
```

**Note**: If you are using `Gradle 3.0.0 or above`, make sure to use the `implementation` keyword instead of `compile` as follows:

```
implementation 'com.adjust.sdk:adjust-android:4.15.1'
implementation 'com.android.installreferrer:installreferrer:1.0'
```

This applies when adding the Google Play Services dependency to your `build.gradle` file.

---

You can also add the Adjust SDK to your project as a JAR library. The latest SDK version's JAR library can be found on our [releases page][releases].

### <a id="bridge-add"></a>Add Adjust web bridge SDK to your project

If you are using Maven, add the following to your `build.gradle` file:

```
compile 'com.adjust.sdk:adjust-android-webbridge:4.15.0'
```

**Note**: If you are using `Gradle 3.0.0 or above`, make sure to use the `implementation` keyword instead of `compile` as follows:

```
implementation 'com.adjust.sdk:adjust-android-webbridge:4.15.0'
```

---

You can also add the Adjust web bridge SDK to your project as a JAR library. The latest SDK version's JAR library can be found on our [releases page][releases].

### <a id="sdk-gps"></a>Add Google Play Services

Since the 1st of August of 2014, apps in the Google Play Store must use the [Google Advertising ID][google_ad_id] to uniquely identify devices. To allow the Adjust SDK to use the Google Advertising ID, you must integrate the [Google Play Services][google_play_services]. If you haven't done this yet, follow these steps:

1. Open the `build.gradle` file of your app and find the `dependencies` block. Add the following line:

    ```
    compile 'com.google.android.gms:play-services-analytics:11.8.0'
    ```

    ![][gradle_gps]

    **Note**: The Adjust SDK is not tied to any specific version of the `play-services-analytics` part of the Google Play Services
    library, so feel free to always use the latest version of it (or whichever you might need)

2. **Skip this step if you are using version 7 or later of Google Play Services**:
   In the Package Explorer open the `AndroidManifest.xml` of your Android
   project.  Add the following `meta-data` tag inside the `<application>`
   element.

    ```xml
    <meta-data android:name="com.google.android.gms.version"
               android:value="@integer/google_play_services_version" />
    ```

### <a id="sdk-permissions"></a>Add permissions

Please add the following permissions, which the Adjust SDK needs, if they are not already present in your `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

If you are **not targeting the Google Play Store**, please also add the following permission:

```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
```

### <a id="sdk-proguard"></a>Proguard settings

If you are using Proguard, add these lines to your Proguard file:

```
-keep public class com.adjust.sdk.** { *; }
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }
```

If you are **not targeting the Google Play Store**, you can remove the `com.google.android.gms` rules.

### <a id="install-referrer"></a>Install referrer

In order to correctly attribute an install of your app to its source, Adjust needs information about the **install referrer**. This can be obtained by using the **Google Play Referrer API** or by catching the **Google Play Store intent** with a broadcast receiver.

**Important**: The Google Play Referrer API is newly introduced by Google with the express purpose of providing a more reliable and secure way of obtaining install referrer information and to aid attribution providers in the fight against click injection. It is **strongly advised** that you support this in your application. The Google Play Store intent is a less secure way of obtaining install referrer information. It will continue to exist in parallel with the new Google Play Referrer API temporarily, but it is set to be deprecated in future.

#### <a id="gpr-api"></a>Google Play Referrer API

In order to support this in your app, please make sure that you have followed the [Add the SDK to your project](#sdk-add) chapter properly and that you have following line added to your `build.gradle` file:

```
compile 'com.android.installreferrer:installreferrer:1.0'
```

Also, make sure that you have paid attention to the [Proguard settings](#sdk-proguard) chapter and that you have added all the rules mentioned in it, especially the one needed for this feature:

```
-keep public class com.android.installreferrer.** { *; }
```

This feature is supported if you are using **Adjust SDK v4.12.0 or above**.

#### <a id="gps-intent"></a>Google Play Store intent

The Google Play Store `INSTALL_REFERRER` intent should be captured with a broadcast receiver. If you are **not using your own broadcast receiver** to receive the `INSTALL_REFERRER` intent, add the following `receiver` tag inside the `application` tag in your `AndroidManifest.xml`.

```xml
<receiver
    android:name="com.adjust.sdk.AdjustReferrerReceiver"
    android:permission="android.permission.INSTALL_PACKAGES"
    android:exported="true" >
    <intent-filter>
        <action android:name="com.android.vending.INSTALL_REFERRER" />
    </intent-filter>
</receiver>
```

![][receiver]

We use this broadcast receiver to retrieve the install referrer and pass it to our backend.

If you are already using a different broadcast receiver for the `INSTALL_REFERRER` intent, follow [these instructions][referrer] to add the Adjust broadcast receiver.

### <a id="bridge-integrate-app"></a> Integrate Adjust web bridge SDK into your app

After you have obtained the reference to your `WebView` object, you need to:

- Call `webView.getSettings().setJavaScriptEnabled(true);`, to enable Javascript in the web view.
- Start the default instance of `AdjustBridgeInstance` by calling `AdjustBridge.registerAndGetInstance(getApplication(), webview)`
- This will also register the Adjust bridge as an Javascript Interface to the web view.

After this steps, your Activity should look like this:

```java
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView) findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());

	AdjustBridge.registerAndGetInstance(getApplication(), webview);
	
        try {
            webView.loadUrl("file:///android_asset/AdjustExample-WebView.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

By completing this, you have successfully added Adjust bridge to your app and enabled the Javascript bridge to communicate
between Adjust's native Android SDK and your page which will be loaded in web view.

### <a id="bridge-integrate-web"></a> Integrate Adjust web bridge SDK into your web view

In your HTML file, you need to import the Adjust Javascript files, that are located in the root of the assets folder. If your HTML file is there as well, you can import them like this:

```html
<script type="text/javascript" src="adjust.js"></script>
<script type="text/javascript" src="adjust_event.js"></script>
<script type="text/javascript" src="adjust_config.js"></script>
```

Once you added references to the Javascript files, you can use them in your HTML file to initialise the Adjust SDK:

```js
// ...

let yourAppToken = '{YourAppToken}';
let environment = AdjustConfig.EnvironmentSandbox;
let adjustConfig = new AdjustConfig(yourAppToken, environment);

Adjust.onCreate(adjustConfig);

// ...
```

Replace `{YourAppToken}` with your app token. You can find this in your [dashboard].

Depending on whether you build your app for testing or for production, you must set `environment` with one of these values:

```js
let environment = AdjustConfig.EnvironmentSandbox;
let environment = AdjustConfig.EnvironmentProduction;
```

**Important:** This value should be set to `AdjustConfig.EnvironmentSandbox` if and only if you or someone else is testing
your app. Make sure to set the environment to `AdjustConfig.EnvironmentProduction` just before you publish the app. Set it
back to `AdjustConfig.EnvironmentSandbox` when you start developing and testing it again.

We use this environment to distinguish between real traffic and test traffic from test devices. It is very important that
you keep this value meaningful at all times! This is especially important if you are tracking revenue.

### <a id="session-tracking"></a>Session tracking

**Note**: This step is **really important** and please **make sure that you implement it properly in your app**. By implementing it, you will enable proper session tracking by the Adjust SDK in your app.

### <a id="session-tracking-api14"></a>API level 14 and higher

If your app `minSdkVersion` in gradle is bigger or equal to `14`, the Adjust sdk web bridge automatically register Activity Lifecycle callbacks to onResume and onPause events.

### <a id="session-tracking-api9"></a>API level between 9 and 13

If your app `minSdkVersion` in gradle is between `9` and `13`, consider updating it to at least `14` to simplify the integration process in the long term. Consult the official Android [dashboard][android-dashboard] to know the latest market share of the major versions.

To provide proper session tracking it is required to call certain Adjust SDK methods every time any Activity resumes or pauses. Otherwise the SDK might miss a session start or session end. In order to do so you should **follow these steps for each Activity of your app**:

1. Open the source file of your Activity.
2. Add the `import` statement at the top of the file.
3. In your Activity's `onResume` method call `Adjust.onResume()`. Create the method if needed.
4. In your Activity's `onPause` method call `Adjust.onPause()`. Create the method if needed.

After these steps your activity should look like this:

```java
import com.adjust.sdk.Adjust;
// ...
public class YourActivity extends Activity {
    protected void onResume() {
        super.onResume();
        Adjust.onResume();
    }
    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }
    // ...
}
```

![][activity]

Repeat these steps for **every** Activity of your app. Don't forget these steps when you create new Activities in the future. Depending on your coding style you might want to implement this in a common superclass of all your Activities.

### <a id="adjust-logging"></a>AdjustBridge logging

You can increase or decrease the amount of logs you see in tests by calling `setLogLevel` on your `AdjustConfig` instance
with one of the following parameters:

```js
adjustConfig.setLogLevel(AdjustConfig.LogLevelVerbose);  // enable all logging
adjustConfig.setLogLevel(AdjustConfig.LogLevelDebug);    // enable more logging
adjustConfig.setLogLevel(AdjustConfig.LogLevelInfo);     // the default
adjustConfig.setLogLevel(AdjustConfig.LogLevelWarn);     // disable info logging
adjustConfig.setLogLevel(AdjustConfig.LogLevelError);    // disable warnings as well
adjustConfig.setLogLevel(AdjustConfig.LogLevelAssert);   // disable errors as well
adjustConfig.setLogLevel(AdjustConfig.LogLevelSuppress); // disable all log output
```

In case you want all your log output to be disabled, beside setting the log level to suppress, you should also use constructor for `AdjustConfig` object which gets boolean parameter indicating whether suppress log level should be supported  or not:

```js
// ...

let yourAppToken = '{YourAppToken}';
let environment = AdjustConfig.EnvironmentSandbox;
let adjustConfig = new AdjustConfig(yourAppToken, environment, true);

Adjust.onCreate(adjustConfig);

// ...
```

### <a id="build-the-app"></a>Build your app

Build and run your app. If the build succeeds, you should carefully read the SDK logs in the console. After the app launches
for the first time, you should see the info log `Install tracked`.

![][bridge_install_tracked]

## <a id="additional-features"></a>Additional features

Once you integrate the Adjust SDK into your project, you can take advantage of the following features.

### <a id="event-tracking"></a>Event tracking

You can use adjust to track any event in your app. Suppose you want to track every tap on a button. You would have to create a new event token in your [dashboard]. Let's say that event token is `abc123`. In your button's `onClick` method you could then add the following lines to track the click:

```js
let adjustEvent = new AdjustEvent('abc123');
Adjust.trackEvent(adjustEvent);
```

### <a id="revenue-tracking"></a>Revenue tracking

If your users can generate revenue by tapping on advertisements or making in-app purchases you can track those revenues with events. Lets say a tap is worth one Euro cent. You could then track the revenue event like this:

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.setRevenue(0.01, 'EUR');

Adjust.trackEvent(adjustEvent);
```

When you set a currency token, Adjust will automatically convert the incoming revenues into a reporting revenue of your
choice. Read more about [currency conversion here.][currency-conversion]

You can read more about revenue and event tracking in the [event tracking guide][event-tracking-guide].

### <a id="revenue-deduplication"></a>Revenue deduplication

You can also add an optional order ID to avoid tracking duplicate revenues. The last ten order IDs are remembered, and revenue events with duplicate order IDs are skipped. This is especially useful for in-app purchase tracking. You can see an  example below.

If you want to track in-app purchases, please make sure to call `trackEvent` only if the purchase is finished and item is purchased. That way you can avoid tracking revenue that is not actually being generated.

```js
let adjustEvent = new AdjustEvent('abc123');

adjustEvent.setRevenue(0.01, 'EUR');
adjustEvent.setRevenue(0.01, 'EUR');
adjustEvent.setOrderId('{OrderId}');

Adjust.trackEvent(event);
```

#### <a id="callback-parameters"></a>Callback parameters

You can register a callback URL for your events in your [dashboard]. We will send a GET request to that URL whenever the event is tracked. You can add callback parameters to that event by calling `addCallbackParameter` to the event instance before tracking it. We will then append these parameters to your callback URL.

For example, suppose you have registered the URL `http://www.adjust.com/callback` then track an event like this:

```js
let adjustEvent = new AdjustEvent('abc123');

adjustEvent.addCallbackParameter('key', 'value');
adjustEvent.addCallbackParameter('foo', 'bar');

Adjust.trackEvent(adjustEvent);
```

In that case we would track the event and send a request to:

    http://www.adjust.com/callback?key=value&foo=bar

It should be mentioned that we support a variety of placeholders like `{gps_adid}` that can be used as parameter values. In the resulting callback this placeholder would be replaced with the Google Play Services ID of the current device. Also note that we don't store any of your custom parameters, but only append them to your callbacks. If you haven't registered a callback for an event, these parameters won't even be read.

You can read more about using URL callbacks, including a full list of available values, in our [callbacks guide][callbacks-guide].

#### <a id="partner-parameters"></a>Partner parameters

You can also add parameters to be transmitted to network partners, which have been activated in your Adjust dashboard.

This works similarly to the callback parameters mentioned above, but can be added by calling the `addPartnerParameter` method on your `AdjustEvent` instance.

```js
let adjustEvent = new AdjustEvent('abc123');

adjustEvent.addPartnerParameter('key', 'value');
adjustEvent.addPartnerParameter('foo', 'bar');

Adjust.trackEvent(adjustEvent);
```

You can read more about special partners and these integrations in our [guide to special partners][special-partners].

### <a id="callback-id"></a>Callback identifier

You can also add custom string identifier to each event you want to track. This identifier will later be reported in event success and/or event failure callbacks to enable you to keep track on which event was successfully tracked or not. You can set this identifier by calling the `setCallbackId` method on your `AdjustEvent` instance:

```js
let adjustEvent = new AdjustEvent('abc123');

adjustEvent.setCallbackId('Your-Custom-Id');

Adjust.trackEvent(adjustEvent);
```

### <a id="session-parameters"></a>Set up session parameters

Some parameters are saved to be sent in every **event** and **session** of the Adjust SDK. Once you have added any of these parameters, you don't need to add them every time, since they will be saved locally. If you add the same parameter twice, there will be no effect.

These session parameters can be called before the Adjust SDK is launched to make sure they are sent even on install. If you need to send them with an install, but can only obtain the needed values after launch, it's possible to [delay](#delay-start) the first launch of the Adjust SDK to allow this behaviour.

### <a id="session-callback-parameters"></a>Session callback parameters

The same callback parameters that are registered for [events](#callback-parameters) can be also saved to be sent in every  event or session of the Adjust SDK.

The session callback parameters have a similar interface to the event callback parameters. Instead of adding the key and it's value to an event, it's added through a call to `Adjust.addSessionCallbackParameter(key, value)`:

```js
Adjust.addSessionCallbackParameter('foo', 'bar');
```

The session callback parameters will be merged with the callback parameters added to an event. The callback parameters added to an event have precedence over the session callback parameters. Meaning that, when adding a callback parameter to an event with the same key to one added from the session, the value that prevails is the callback parameter added to the event.

It's possible to remove a specific session callback parameter by passing the desiring key to the method `Adjust.removeSessionCallbackParameter(key)`.

```js
Adjust.removeSessionCallbackParameter('foo');
```

If you wish to remove all keys and their corresponding values from the session callback parameters, you can reset it with the method `Adjust.resetSessionCallbackParameters()`.

```js
Adjust.resetSessionCallbackParameters();
```

### <a id="session-partner-parameters"></a>Session partner parameters

In the same way that there are [session callback parameters](#session-callback-parameters) sent in every event or session of the Adjust SDK, there is also session partner parameters.

These will be transmitted to network partners, for the integrations that have been activated in your Adjust [dashboard].

The session partner parameters have a similar interface to the event partner parameters. Instead of adding the key and it's value to an event, it's added through a call to `Adjust.addSessionPartnerParameter(key, value)`:

```js
Adjust.addSessionPartnerParameter('foo', 'bar');
```

The session partner parameters will be merged with the partner parameters added to an event. The partner parameters added to an event have precedence over the session partner parameters. Meaning that, when adding a partner parameter to an event with the same key to one added from the session, the value that prevails is the partner parameter added to the event.

It's possible to remove a specific session partner parameter by passing the desiring key to the method `Adjust.removeSessionPartnerParameter(key)`.

```js
Adjust.removeSessionPartnerParameter('foo');
```

If you wish to remove all keys and their corresponding values from the session partner parameters, you can reset it with the method `Adjust.resetSessionPartnerParameters()`.

```js
Adjust.resetSessionPartnerParameters();
```

### <a id="delay-start"></a>Delay start

Delaying the start of the Adjust SDK allows your app some time to obtain session parameters, such as unique identifiers, to be sent on install.

Set the initial delay time in seconds with the method `setDelayStart` in the `AdjustConfig` instance:

```js
adjustConfig.setDelayStart(5.5);
```

In this case, this will make the Adjust SDK not send the initial install session and any event created for 5.5 seconds. After this time is expired or if you call `Adjust.sendFirstPackages()` in the meanwhile, every session parameter will be added to the delayed install session and events and the Adjust SDK will resume as usual.

**The maximum delay start time of the adjust SDK is 10 seconds**.

### <a id="attribution-callback"></a>Attribution callback

You can register a callback function to be notified of tracker attribution changes. Due to the different sources considered for attribution, this information can not be provided synchronously.

Please make sure to consider our [applicable attribution data policies][attribution-data].

As the callback method is configured using the `AdjustConfig` instance, you should call `setAttributionCallback` before
calling `Adjust.onCreate(adjustConfig)`.

```js
function attributionCallback(attribution) {
    alert('Tracker token = ' + attribution.trackerToken + '\n' +
          'Tracker name = ' + attribution.trackerName + '\n' +
          'Network = ' + attribution.network + '\n' +
          'Campaign = ' + attribution.campaign + '\n' +
          'Adgroup = ' + attribution.adgroup + '\n' +
          'Creative = ' + attribution.creative + '\n' +
          'Click label = ' + attribution.clickLabel);
}

// ...

let yourAppToken = '{YourAppToken}';
let environment = AdjustConfig.EnvironmentSandbox;
let adjustConfig = new AdjustConfig(yourAppToken, environment);

adjustConfig.setAttributionCallback(attributionCallback);

Adjust.onCreate(adjustConfig);

// ...
```

The callback function will get triggered when the SDK receives final attribution data. Within the callback you have access to the `attribution` parameter. Here is a quick summary of its properties:

- `(string) trackerToken` the tracker token of the current install.
- `(string) trackerName` the tracker name of the current install.
- `(string) network` the network grouping level of the current install.
- `(string) campaign` the campaign grouping level of the current install.
- `(string) adgroup` the ad group grouping level of the current install.
- `(string) creative` the creative grouping level of the current install.
- `(string) clickLabel` the click label of the current install.
- `(string) adid` the Adjust device identifier.

### <a id="session-event-callbacks"></a>Session and event callbacks

You can register a function to be notified when events or sessions are tracked. There are four function callbacks: one for tracking successful events, one for tracking failed events, one for tracking successful sessions and one for tracking failed sessions. You can add any number of callbacks after creating the `AdjustConfig` object:

```js

let adjustConfig = new AdjustConfig(yourAppToken, environment);

adjustConfig.setEventSuccessCallback(eventSuccessCallback);
adjustConfig.setEventFailureCallback(eventFailureCallback);
adjustConfig.setSessionSuccessCallback(sessionSuccessCallback);
adjustConfig.setSessionFailureCallback(sessionFailureCallback);

Adjust.onCreate(adjustConfig);
```

The listener function will be called after the SDK tries to send a package to the server. Within the listener function you have access to a response data object specifically for the listener. Here is a quick summary of the success session response data object fields:

- `(string) message` the message from the server or the error logged by the SDK.
- `(string) timeStamp` timestamp from the server.
- `(string) adid` a unique device identifier provided by Adjust.
- `(JSON) jsonResponse` the JSON object with the response from the server.

Both event response data objects contain:

- `(string) eventToken` the event token, if the package tracked was an event.
- `(string) callbackId` the custom defined callback ID set on event object.

And both event and session failed objects also contain:

- `(boolean) willRetry` indicates there will be an attempt to resend the package at a later time.

### <a id="disable-tracking"></a>Disable tracking

You can disable the Adjust SDK from tracking any activities of the current device by calling `setEnabled` with parameter `false`. **This setting is remembered between sessions**.

```js
Adjust.setEnabled(false);
```

You can check if the Adjust SDK is currently enabled by calling the function `isEnabled`. It is always possible to activatе the Adjust SDK by invoking `setEnabled` with the enabled parameter as `true`.

### <a id="offline-mode"></a>Offline mode

You can put the Adjust SDK in offline mode to suspend transmission to our servers, while retaining tracked data to be sent later. While in offline mode, all information is saved in a file, so be careful not to trigger too many events while in offline mode.

You can activate offline mode by calling `setOfflineMode` with the parameter `true`.

```js
Adjust.setOfflineMode(true);
```

Conversely, you can deactivate offline mode by calling `setOfflineMode` with `false`. When the Adjust SDK is put back in online mode, all saved information is sent to our servers with the correct time information.

Unlike disabling tracking, this setting is **not remembered** between sessions. This means that the SDK is in online mode whenever it is started, even if the app was terminated in offline mode.

### <a id="event-buffering"></a>Event buffering

If your app makes heavy use of event tracking, you might want to delay some HTTP requests in order to send them in one batch
every minute. You can enable event buffering with your `AdjustConfig` instance:

```js
adjustConfig.setEventBufferingEnabled(true);
```

### <a id="gdpr-forget-me"></a>GDPR right to be forgotten

In accordance with article 17 of the EU's General Data Protection Regulation (GDPR), you can notify Adjust when a user has exercised their right to be forgotten. Calling the following method will instruct the Adjust SDK to communicate the user's choice to be forgotten to the Adjust backend:

```js
Adjust.gdprForgetMe();
```

Upon receiving this information, Adjust will erase the user's data and the Adjust SDK will stop tracking the user. No requests from this device will be sent to Adjust in the future.

### <a id="sdk-signature"></a>SDK signature

An account manager must activate the Adjust SDK signature. Contact Adjust support (support@adjust.com) if you are interested in using this feature.

If the SDK signature has already been enabled on your account and you have access to App Secrets in your Adjust Dashboard, please use the method below to integrate the SDK signature into your app.

An App Secret is set by calling `setAppSecret` on your `AdjustConfig` instance:

```js
let adjustConfig = new AdjustConfig(yourAppToken, environment);

adjustConfig.setAppSecret(secretId, info1, info2, info3, info4);

Adjust.onCreate(adjustConfig);
```

### <a id="background-tracking"></a>Background tracking

The default behaviour of the Adjust SDK is to pause sending HTTP requests while the app is in the background. You can change this in your `AdjustConfig` instance:

```js
adjustConfig.setSendInBackground(true);
```

### <a id="device-ids"></a>Device IDs

The Adjust SDK offers you possibility to obtain some of the device identifiers.

### <a id="di-gps-adid"></a>Google Play Services advertising identifier

Certain services (such as Google Analytics) require you to coordinate Device and Client IDs in order to prevent duplicate
reporting.

To obtain the device Google Advertising identifier, it's necessary to pass a callback function to `Adjust.getGoogleAdId` that will receive the google ad id in it's argument, like this:

```js
Adjust.getGoogleAdId(function(googleAdId) {
    // ...
});
```

### <a id="di-amz-adid"></a>Amazon advertising identifier

If you need to obtain the Amazon Advertising ID, you can make a call to following method on `Adjust` instance:

```js
let amazonAdId = Adjust.getAmazonAdId();
```

### <a id="di-adid"></a>Adjust device identifier

For each device with your app installed on it, Adjust backend generates unique **Adjust device identifier** (**adid**). In order to obtain this identifier, you can make a call to following method on `Adjust` instance:

```js
let adid = Adjust.getAdid();
```

**Note**: Information about **adid** is available after app installation has been tracked by the Adjust backend. From that moment on, Adjust SDK has information about your device **adid** and you can access it with this method. So, **it is not possible** to access **adid** value before the SDK has been initialised and installation of your app was tracked successfully.

### <a id="user-attribution"></a>User attribution

Like described in [attribution callback section](#attribution-callback), this callback get triggered providing you info about new attribution when ever it changes. In case you want to access info about your user's current attribution when ever you need it, you can make a call to following method of the `Adjust` instance:

```js
let attribution = Adjust.getAttribution();
```

**Note**: Information about current attribution is available after app installation has been tracked by the Adjust backend and attribution callback has been initially triggered. From that moment on, Adjust SDK has information about your user's attribution and you can access it with this method. So, **it is not possible** to access user's attribution value before the SDK has been initialized and attribution callback has been initially triggered.

### <a id="push-token"></a>Push token

Push tokens are used for Audience Builder and client callbacks, and they are required for uninstall and reinstall tracking.

To send us the push notification token, add the following call to Adjust once you have obtained your token or when ever it's value is changed:

```js
Adjust.setPushToken(pushNotificationsToken);
```

This call can also be made from the native side of the app in Java, with a similar method:

```java
Adjust.setPushToken(pushNotificationsToken, context);
```

### <a id="pre-installed-trackers"></a>Pre-installed trackers

If you want to use the Adjust SDK to recognize users whose devices came with your app pre-installed, follow these steps.

1. Create a new tracker in your [dashboard].
2. Open your app delegate and add set the default tracker of your `AdjustConfig`:

  ```js
let adjustConfig = new AdjustConfig(yourAppToken, environment);
  config.setDefaultTracker('{TrackerToken}');
  Adjust.onCreate(config);
  ```

Replace `{TrackerToken}` with the tracker token you created in step 1. Please note that the Dashboard displays a tracker URL (including `http://app.adjust.com/`). In your source code, you should specify only the six-character token and not the entire URL.

3. Build and run your app. You should see a line like the following in your LogCat:

    ```
    Default tracker: 'abc123'
    ```

### <a id="deeplinking"></a>Deep linking

If you are using an Adjust tracker URL with the option to deep link into your app, there is the possibility to get information about the deep link URL and its content. Hitting the URL can happen when the user has your app already installed (standard deep linking scenario) or if they don't have the app on their device (deferred deep linking scenario). In the standard deep linking scenario, the Android platform natively offers the possibility for you to get the information about the deep link content. The deferred deep linking scenario is something which the Android platform doesn't support out of the box, and, in this case, the Adjust SDK will offer you the mechanism you need to get the information about the deep link content.

### <a id="deeplinking-standard"></a>Standard deep linking scenario

If a user has your app installed and you want it to launch after hitting an Adjust tracker URL with the `deep_link` parameter in it, you need to enable deep linking in your app. This is done by choosing a desired **unique scheme name** and assigning it to the Activity you want to launch once your app opens following a user clicking on the tracker URL. This is set in the `AndroidManifest.xml`. You need to add the `intent-filter` section to your desired Activity definition in the manifest file and assign an `android:scheme` property value with the desired scheme name:

```xml
<activity
    android:name=".MainActivity"
    android:configChanges="orientation|keyboardHidden"
    android:label="@string/app_name"
    android:screenOrientation="portrait">

    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>

    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="adjustExample" />
    </intent-filter>
</activity>
```

With this now set, you need to use the assigned scheme name in the Adjust tracker URL's `deep_link` parameter if you want your app to launch once the tracker URL is clicked. A tracker URL without any information added to the deep link can be built to look something like this:

```
https://app.adjust.com/abc123?deep_link=adjustExample%3A%2F%2F
```

Please, have in mind that the `deep_link` parameter value in the URL **must be URL encoded**.

After clicking this tracker URL, and with the app set as described above, your app will launch along with the `MainActivity` intent. Inside the `MainActivity` class, you will automatically be provided with the information about the `deep_link` parameter content. Once this content is delivered to you, it **will not be encoded**, although it was encoded in the URL.

Depending on the `android:launchMode` setting of your Activity in the `AndroidManifest.xml` file, information about the `deep_link` parameter content will be delivered to the appropriate place in the Activity file. For more information about the possible values of the `android:launchMode` property, check [the official Android documentation][android-launch-modes].

There are two places within your desired Activity where information about the deep link content will be delivered via the `Intent` object--either in the Activity's `onCreate` or `onNewIntent` methods. Once your app has launched and one of these methods has been triggered, you will be able to get the actual deep link passed in the `deep_link` parameter in the click URL. You can then use this information to conduct some additional logic in your app.

You can extract the deep link content from these two methods like this:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Intent intent = getIntent();
    Uri data = intent.getData();

    // data.toString() -> This is your deep_link parameter value.
}
```

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    Uri data = intent.getData();

    // data.toString() -> This is your deep_link parameter value.
}
```

### <a id="deeplinking-deferred"></a>Deferred deep linking scenario

The deferred deep linking scenario occurs when a user clicks on an Adjust tracker URL with a `deep_link` parameter in it, but does not have the app installed on the device at click time. After that, the user will be redirected to the Play Store to download and install your app. After opening it for the first time, the `deep_link` parameter content will be delivered to your app.

In order to get information about the URL content in a deferred deep linking scenario, you should set a callback method on the `AdjustConfig` object which will receive one string parameter where the content of the URL will be delivered. You should set this method on the config object by calling the method `setDeferredDeeplinkCallback`:

```js
let adjustConfig = new AdjustConfig(yourAppToken, environment);

adjustConfig.setDeferredDeeplinkCallback(function (deeplink) {
	// ...
});

Adjust.onCreate(adjustConfig);
```

In deferred deep linking scenario, there is one additional setting which can be set on the AdjustConfig object. Once the Adjust SDK gets the deferred deep link information, we offer you the possibility to choose whether our SDK should open this URL or not. You can choose to set this option by calling the `setOpenDeferredDeeplink` method on the config object:

```js
// ...

function deferredDeeplinkCallback(deeplink) {
   console.log('Deeplink URL: ' + deeplinkURL);
   // ...
}

let adjustConfig = new AdjustConfig(yourAppToken, environment);

adjustConfig.setOpenDeferredDeeplink(true);
adjustConfig.setDeferredDeeplinkCallback(deferredDeeplinkCallback);

Adjust.start(adjustConfig);

```

If nothing is set, **the Adjust SDK will always try to launch the URL by default**.

### <a id="deeplinking-reattribution"></a>Reattribution via deep links

Adjust enables you to run re-engagement campaigns through deep links. For more information on how to do that, please check our [official docs][reattribution-with-deeplinks].

If you are using this feature, in order for your user to be properly reattributed, you need to make one additional call to the native Adjust SDK in your app.

Once you have received deep link content information in your app, add a call to the `Adjust.appWillOpenUrl` method. By making this call, the Adjust SDK will try to find if there is any new attribution information inside of the deep link. If there is any, it will be sent to the Adjust backend. If your user should be reattributed due to a click on the Adjust tracker URL with deep link content, you will see the [attribution callback](#attribution-callback) in your app being triggered with new attribution info for this user.

The call to `Adjust.appWillOpenUrl` should be done like this:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Intent intent = getIntent();
    Uri data = intent.getData();

    Adjust.appWillOpenUrl(data);
}
```

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    Uri data = intent.getData();

    Adjust.appWillOpenUrl(data);
}
```

This call can also be made from the web view with the function `Adjust.appWillOpenUrl` in Javascript like this:

```js
Adjust.appWillOpenUrl(deeplinkUrl);
```

[dashboard]:   http://adjust.com
[adjust.com]:  http://adjust.com

[example-webbridge]:             Adjust/example-webbridge
[releases]:                      https://github.com/adjust/android_sdk/releases
[google_ad_id]:                  https://support.google.com/googleplay/android-developer/answer/6048248?hl=en
[google_play_services]:          http://developer.android.com/google/play-services/setup.html
[referrer]:                      doc/english/referrer.md
[android-dashboard]:             http://developer.android.com/about/dashboards/index.html
[currency-conversion]:           https://docs.adjust.com/en/event-tracking/#tracking-purchases-in-different-currencies
[event-tracking-guide]:          https://docs.adjust.com/en/event-tracking
[callbacks-guide]:               https://docs.adjust.com/en/callbacks
[special-partners]:              https://docs.adjust.com/en/special-partners
[attribution-data]:              https://github.com/adjust/sdks/blob/master/doc/attribution-data.md
[android-launch-modes]:          https://developer.android.com/guide/topics/manifest/activity-element.html
[reattribution-with-deeplinks]:  https://docs.adjust.com/en/deeplinking/#manually-appending-attribution-data-to-a-deep-link


[gradle_gps]:              https://raw.github.com/adjust/sdks/master/Resources/android/v4/05_gradle_gps.png
[receiver]:                https://raw.github.com/adjust/sdks/master/Resources/android/v4/09_receiver.png
[bridge_init_js_android]:  https://raw.githubusercontent.com/adjust/sdks/master/Resources/android/bridge/bridge_init_js_android.png
[activity]:                https://raw.github.com/adjust/sdks/master/Resources/android/v4/14_activity.png
[bridge_install_tracked]:  https://raw.githubusercontent.com/adjust/sdks/master/Resources/android/bridge/bridge_install_tracked.png


## <a id="license"></a>License

The Adjust SDK is licensed under the MIT License.

Copyright (c) 2012-2018 Adjust GmbH, http://www.adjust.com

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
