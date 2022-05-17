**This guide is being retired.**

This README file will be retired shortly. The SDK documentation can now be found on our help center:

* [English][en-helpcenter]
* [中文][zh-helpcenter]
* [日本語][ja-helpcenter]
* [한국어][ko-helpcenter]

## Summary

This is the Android SDK of Adjust™. You can read more about Adjust™ at [adjust.com].

Read this in other languages: [English][en-readme], [中文][zh-readme], [日本語][ja-readme], [한국어][ko-readme].

## Table of contents

### Quick start

   * [Example apps](#qs-example-apps)
   * [Getting started](#qs-getting-started)
      * [Add the SDK to your project](#qs-add-sdk)
      * [Add Google Play Services](#qs-gps)
      * [Add permissions](#qs-permissions)
      * [Proguard settings](#qs-proguard)
      * [Install referrer](#qs-install-referrer)
         * [Google Play Referrer API](#qs-gpr-api)
         * [Google Play Store intent](#qs-gps-intent)
         * [Huawei Referrer API](#qs-huawei-referrer-api)
   * [Integrate the SDK into your app](#qs-integrate-sdk)
      * [Basic setup](#qs-basic-setup)
         * [Native App SDK](#qs-basic-setup-native)
         * [Web Views SDK](#qs-basic-setup-web)
      * [Session tracking](#qs-session-tracking)
         * [API level 14 and higher](#qs-session-tracking-api-14)
         * [API level between 9 and 13](#qs-session-tracking-api-9)
      * [SDK signature](#qs-sdk-signature)
      * [Adjust logging](#qs-adjust-logging)
      * [Build your app](#qs-build-the-app)

### Deep linking

   * [Deep linking overview](#dl)
   * [Standard deep linking scenario](#dl-standard)
   * [Deferred deep linking scenario](#dl-deferred)
   * [Reattribution via deep links](#dl-reattribution)
   * [Link resolution](#link-resolution)

### Event tracking

   * [Track event](#et-tracking)
   * [Track revenue](#et-revenue)
   * [Revenue deduplication](#et-revenue-deduplication)

### Custom parameters

   * [Custom parameters overview](#cp)
   * [Event parameters](#cp-event-parameters)
      * [Event callback parameters](#cp-event-callback-parameters)
      * [Event partner parameters](#cp-event-partner-parameters)
      * [Event callback identifier](#cp-event-callback-id)
   * [Session parameters](#cp-session-parameters)
      * [Session callback parameters](#cp-session-callback-parameters)
      * [Session partner parameters](#cp-session-partner-parameters)
      * [Delay start](#cp-delay-start)

### Additional features

   * [Push token (uninstall tracking)](#af-push-token)
   * [Attribution callback](#af-attribution-callback)
   * [Ad revenue tracking](#af-ad-revenue)
   * [Subscription tracking](#af-subscriptions)
   * [Session and event callbacks](#af-session-event-callbacks)
   * [User attribution](#af-user-attribution)
   * [Device IDs](#af-device-ids)
      * [Google Play Services advertising identifier](#af-gps-adid)
      * [Amazon advertising identifier](#af-amazon-adid)
      * [Adjust device identifier](#af-adid)
   * [Preinstalled apps](#af-preinstalled-apps)
   * [Offline mode](#af-offline-mode)
   * [Disable tracking](#af-disable-tracking)
   * [Event buffering](#af-event-buffering)
   * [Background tracking](#af-background-tracking)
   * [GDPR right to be forgotten](#af-gdpr-forget-me)
   * [Third-party sharing](#af-third-party-sharing)
      * [Disable third-party sharing](#af-disable-third-party-sharing)
      * [Enable third-party sharing](#af-enable-third-party-sharing)
   * [Consent measurement](#af-measurement-consent)
   * [Data residency](#af-data-residency)
   * [COPPA compliance](#af-coppa-compliance)
   * [Play Store Kids Apps](#af-play-store-kids-apps)

### Testing and troubleshooting

   * [I'm seeing the "session failed (Ignoring too frequent session...)" error](#tt-session-failed)
   * [Is my broadcast receiver capturing the install referrer?](#tt-broadcast-receiver)
   * [Can I trigger an event at application launch?](#tt-event-at-launch)

### License


## Quick start

### <a id="qs-example-apps"></a>Example apps

There are Android example apps inside the [`example-app-java`][example-java], [`example-app-kotlin`][example-kotlin] and [`example-app-keyboard`][example-keyboard] directories, as well as example app that uses web views inside the [`example-webbridge` directory][example-webbridge] and Android TV example app inside the [`example-app-tv`][example-tv] directory. You can open the Android project to see these examples on how the Adjust SDK can be integrated.

### <a id="qs-getting-started"></a>Getting started

These are the minimum required steps to integrate the Adjust SDK in your Android app. We assume that you are using Android Studio for your Android development. The minimum supported Android API level for the Adjust SDK integration is **9 (Gingerbread)**.

### <a id="qs-add-sdk"></a>Add the SDK to your project

If you are using Maven, add the following to your `build.gradle` file:

```gradle
implementation 'com.adjust.sdk:adjust-android:4.30.1'
implementation 'com.android.installreferrer:installreferrer:2.2'
```

If you would prefer to use the Adjust SDK inside web views in your app, please include this additional dependency as well:

```gradle
implementation 'com.adjust.sdk:adjust-android-webbridge:4.30.1'
```

**Note**: The minimum supported Android API level for the web view extension is 17 (Jelly Bean).

You can also add the Adjust SDK and web view extension as JAR files, which can be downloaded from our [releases page][releases].

### <a id="qs-gps"></a>Add Google Play Services

Since the 1st of August of 2014, apps in the Google Play Store must use the [Google Advertising ID][google-ad-id] to uniquely identify devices. To enable the Google Advertising ID for our SDK, you must integrate [Google Play Services][google-play-services]. If you haven't done this yet, please add dependency to the Google Play Services library by adding the following dependecy to your `dependencies` block of app's `build.gradle` file:

```gradle
implementation 'com.google.android.gms:play-services-ads-identifier:18.0.1'
```

**Note**: The Adjust SDK is not tied to any specific version of the `play-services-ads-identifier` part of the Google Play Services library. You can use the latest version of the library, or any other version you need.

### <a id="qs-permissions"></a>Add permissions

The Adjust SDK requires the following permissions. Please add them to your `AndroidManifest.xml` file if they are not already present:

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

#### <a id="gps-adid-permission"></a>Add permission to gather Google advertising ID

You will need to add the `com.google.android.gms.AD_ID` permission to read the device's advertising ID, when your apps are able to target Android 13 (API level 33) and above. Add the following line to your `AndroidManifest.xml` to enable the permission.

```xml
<uses-permission android:name="com.google.android.gms.permission.AD_ID"/>
```

For more information, see [Google's `AdvertisingIdClient.Info` documentation](https://developers.google.com/android/reference/com/google/android/gms/ads/identifier/AdvertisingIdClient.Info#public-string-getid).

### <a id="qs-proguard"></a>Proguard settings

If you are using Proguard, add these lines to your Proguard file:

```
-keep class com.adjust.sdk.** { *; }
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

If you are **not publishing your app in the Google Play Store**, use the following `com.adjust.sdk` package rules:

```
-keep public class com.adjust.sdk.** { *; }
```

### <a id="qs-install-referrer"></a>Install referrer

In order to correctly attribute an app install to its source, Adjust needs information about the **install referrer**. We can achieve this in two different ways: either by using the **Google Play Referrer API** or by collecting the **Google Play Store intent** with a broadcast receiver.

**Important**: Google introduced the Google Play Referrer API to provide a more reliable and secure way to obtain install referrer information and to aid attribution providers in the fight against click injection. We **strongly advise** you to support this in your application. The Google Play Store intent is a less secure way of obtaining install referrer information. For now it exists in parallel with the new Google Play Referrer API, but will be deprecated in the future.

#### <a id="qs-gpr-api"></a>Google Play Referrer API

In order to support the Google Play Referrer API in your app, please make sure that you have followed our chapter on [adding the SDK to your project](#qs-add-sdk) correctly and that you have following line added to your `build.gradle` file:

```
implementation 'com.android.installreferrer:installreferrer:2.2'
```

Please follow the directions for your [Proguard settings](#qs-proguard) carefully. Confirm that you have added all the rules mentioned in it, especially the one needed for this feature:

```
-keep public class com.android.installreferrer.** { *; }
```

This feature is supported if you are using **Adjust SDK v4.12.0 or above**.

#### <a id="qs-gps-intent"></a>Google Play Store intent

**Note**: Google has [announced](https://android-developers.googleblog.com/2019/11/still-using-installbroadcast-switch-to.html) deprecation of `INSTALL_REFERRER` intent usage to deliver referrer information as of March 1st 2020. If you are using this way of accessing referrer information, please migrate to [Google Play Referrer API](#qs-gpr-api) approach.

You should capture the Google Play Store `INSTALL_REFERRER` intent with a broadcast receiver. If you are **not using your own broadcast receiver** to receive the `INSTALL_REFERRER` intent, add the following `receiver` tag inside the `application` tag in your `AndroidManifest.xml`.

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

We use this broadcast receiver to retrieve the install referrer and pass it to our backend.

If you are using a different broadcast receiver for the `INSTALL_REFERRER` intent, follow [these instructions][referrer] to properly ping the Adjust broadcast receiver.

#### <a id="qs-huawei-referrer-api"></a>Huawei Referrer API

As of v4.21.1, the Adjust SDK supports install tracking on Huawei devices with Huawei App Gallery version 10.4 and higher. No additional integration steps are needed to start using the Huawei Referrer API.

### <a id="qs-integrate-sdk"></a>Integrate the SDK into your app

First, we'll set up basic session tracking.

### <a id="qs-basic-setup"></a>Basic setup

If you are integrating the SDK into a native app, follow the directions for a [Native App SDK](#qs-basic-setup-native). If you are integrating the SDK for usage inside web views, please follow the directions for a [Web Views SDK](#qs-basic-setup-web) below.

#### <a id="qs-basic-setup-native"></a>Native App SDK

We recommend using a global Android [Application][android-application] class to initialize the SDK. If you don't have one in your app, follow these steps:

- Create a class that extends the `Application`.
- Open the `AndroidManifest.xml` file of your app and locate the `<application>` element.
- Add the attribute `android:name` and set it to the name of your new application class.

    In our example app, we use an `Application` class named `GlobalApplication`. Therefore, we configure the manifest file as:
    ```xml
     <application
       android:name=".GlobalApplication"
       <!-- ... -->
     </application>
    ```

- In your `Application` class, find or create the `onCreate` method. Add the following code to initialize the Adjust SDK:

    ```java
    import com.adjust.sdk.Adjust;
    import com.adjust.sdk.AdjustConfig;

    public class GlobalApplication extends Application {
        @Override
        public void onCreate() {
            super.onCreate();

            String appToken = "{YourAppToken}";
            String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
            AdjustConfig config = new AdjustConfig(this, appToken, environment);
            Adjust.onCreate(config);
        }
    }
    ```

Replace `{YourAppToken}` with your app token. You can find this in your [dashboard].

Next, you must set the `environment` to either sandbox or production mode:

```java
String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
```

**Important:** Set the value to `AdjustConfig.ENVIRONMENT_SANDBOX` if (and only if) you or someone else is testing your app. Make sure to set the environment to `AdjustConfig.ENVIRONMENT_PRODUCTION` before you publish the app. Set it back to `AdjustConfig.ENVIRONMENT_SANDBOX` if you start developing and testing it again.

We use this environment to distinguish between real traffic and test traffic from test devices. Keeping the environment updated according to your current status is very important!

#### <a id="qs-basic-setup-web"></a>Web Views SDK

After you have obtained the reference to your `WebView` object:

- Call `webView.getSettings().setJavaScriptEnabled(true)`, to enable Javascript in the web view
- Start the default instance of `AdjustBridgeInstance` by calling `AdjustBridge.registerAndGetInstance(getApplication(), webview)`
- This will also register the Adjust bridge as a Javascript Interface to the web view
- Call `AdjustBridge.setWebView()` to set new `WebView` if needed.  
- Call `AdjustBridge.unregister()` to uregister the `AdjustBridgeInstance` and `WebView`.  

After these steps, your activity should look like this:

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
    
    @Override
    protected void onDestroy() {
        AdjustBridge.unregister();

        super.onDestroy();
    }
}
```

After you complete this step, you will have successfully added the Adjust bridge to your app. The Javascript bridge is now enabled to communicate between Adjust's native Android SDK and your page, which will be loaded in the web view.

In your HTML file, import the Adjust Javascript files which are located in the root of the assets folder. If your HTML file is there as well, import them like this:

```html
<script type="text/javascript" src="adjust.js"></script>
<script type="text/javascript" src="adjust_event.js"></script>
<script type="text/javascript" src="adjust_third_party_sharing.js"></script>
<script type="text/javascript" src="adjust_config.js"></script>
```

Once you add your references to the Javascript files, use them in your HTML file to initialise the Adjust SDK:

```js
let yourAppToken = '{YourAppToken}';
let environment = AdjustConfig.EnvironmentSandbox;
let adjustConfig = new AdjustConfig(yourAppToken, environment);

Adjust.onCreate(adjustConfig);
```

Replace `{YourAppToken}` with your app token. You can find this in your [dashboard].

Next, set your `environment` to the corresponding value, depending on whether you are still testing or are in production mode:

```js
let environment = AdjustConfig.EnvironmentSandbox;
let environment = AdjustConfig.EnvironmentProduction;
```

**Important:** Set your value to `AdjustConfig.EnvironmentSandbox` if (and only if) you or someone else is testing your app. Make sure you set the environment to `AdjustConfig.EnvironmentProduction` just before you publish the app. Set it back to `AdjustConfig.EnvironmentSandbox` if you start developing and testing again.

We use this environment to distinguish between real traffic and test traffic from test devices. Keeping it updated according to your current status is very important!

### <a id="qs-session-tracking"></a>Session tracking

**Note**: This step is **very important**. Please **make sure that you implement it properly in your app**. Completing this step correctly ensures that the Adjust SDK can properly track sessions in your app.

#### <a id="qs-session-tracking-api-14"></a>API level 14 and higher

- Add a private class that implements the `ActivityLifecycleCallbacks` interface. If you don't have access to this interface, your app is targeting an Android API level lower than 14. You will have to manually update each activity by following these [instructions](#qs-session-tracking-api-9). If you have `Adjust.onResume` and `Adjust.onPause` calls on each of your app's activities, you should remove them.
- Edit the `onActivityResumed(Activity activity)` method and add a call to `Adjust.onResume()`. Edit the
`onActivityPaused(Activity activity)` method and add a call to `Adjust.onPause()`.
- Add the `onCreate()` method with the Adjust SDK is configured and call  `registerActivityLifecycleCallbacks` with an instance of the created `ActivityLifecycleCallbacks` class.

    ```java
    import com.adjust.sdk.Adjust;
    import com.adjust.sdk.AdjustConfig;

    public class GlobalApplication extends Application {
        @Override
        public void onCreate() {
            super.onCreate();

            String appToken = "{YourAppToken}";
            String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
            AdjustConfig config = new AdjustConfig(this, appToken, environment);
            Adjust.onCreate(config);

            registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
        }

         private static final class AdjustLifecycleCallbacks implements ActivityLifecycleCallbacks {
             @Override
             public void onActivityResumed(Activity activity) {
                 Adjust.onResume();
             }

             @Override
             public void onActivityPaused(Activity activity) {
                 Adjust.onPause();
             }

             //...
         }
      }
    ```

#### <a id="qs-session-tracking-api-9"></a>API level between 9 and 13

If your app `minSdkVersion` in gradle is between `9` and `13`, consider updating it to at least `14` to simplify the integration process. Consult the official Android [dashboard][android-dashboard] to find out the latest market share of the major versions.

To provide proper session tracking, certain Adjust SDK methods are called every time an activity resumes or pauses (otherwise the SDK might miss a session start or end). In order to do so, follow these steps **for each Activity** of your app:

- In your Activity's `onResume` method, call `Adjust.onResume()`. Create the method if needed.
- In your Activity's `onPause` method, call `Adjust.onPause()`. Create the method if needed.

After these steps, your activity should look like this:

```java
import com.adjust.sdk.Adjust;

public class YourActivity extends Activity {
    protected void onResume() {
        super.onResume();
        Adjust.onResume();
    }
    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }
}
```

Repeat these steps for **every Activity** in your app. Don't forget to repeat these steps whenever you create a new activity in the future. Depending on your coding style, you might want to implement this in a common superclass of all your activities.

### <a id="qs-sdk-signature"></a>SDK signature

An account manager must activate the Adjust SDK Signature. Contact Adjust support (support@adjust.com) if you are interested in using this feature.

If the SDK signature has already been enabled on your account and you have access to App Secrets in your Adjust Dashboard, please use the method below to integrate the SDK signature into your app.

An App Secret is set by calling `setAppSecret` on your config instance:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);
config.setAppSecret(secretId, info1, info2, info3, info4);
Adjust.onCreate(config);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setAppSecret(secretId, info1, info2, info3, info4);
Adjust.onCreate(adjustConfig);
```
</td>
</tr>
</table>

### <a id="qs-adjust-logging"></a>Adjust logging

You can increase or decrease the amount of logs that you see during testing by calling `setLogLevel` on your config instance with one of the following parameters:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
config.setLogLevel(LogLevel.VERBOSE); // enable all logs
config.setLogLevel(LogLevel.DEBUG); // disable verbose logs
config.setLogLevel(LogLevel.INFO); // disable debug logs (default)
config.setLogLevel(LogLevel.WARN); // disable info logs
config.setLogLevel(LogLevel.ERROR); // disable warning logs
config.setLogLevel(LogLevel.ASSERT); // disable error logs
config.setLogLevel(LogLevel.SUPRESS); // disable all logs
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
adjustConfig.setLogLevel(AdjustConfig.LogLevelVerbose); // enable all logs
adjustConfig.setLogLevel(AdjustConfig.LogLevelDebug); // disable verbose logs
adjustConfig.setLogLevel(AdjustConfig.LogLevelInfo); // disable debug logs (default)
adjustConfig.setLogLevel(AdjustConfig.LogLevelWarn); // disable info logs
adjustConfig.setLogLevel(AdjustConfig.LogLevelError); // disable warning logs
adjustConfig.setLogLevel(AdjustConfig.LogLevelAssert); // disable error logs
adjustConfig.setLogLevel(AdjustConfig.LogLevelSuppress); // disable all logs
```
</td>
</tr>
</table>

If you want to disable all of your log output, set the log level to suppress, and use the constructor for config object (which gets boolean parameters indicating whether or not suppress log level should be supported):

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment, true);
config.setLogLevel(LogLevel.SUPRESS);
Adjust.onCreate(config);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustConfig = new AdjustConfig(yourAppToken, environment, true);
adjustConfig.setLogLevel(AdjustConfig.LogLevelSuppress);
Adjust.onCreate(adjustConfig);
```
</td>
</tr>
</table>

### <a id="qs-build-the-app"></a>Build your app

Build and run your Android app. In your `LogCat` viewer, set the filter `tag:Adjust` to hide all other logs. After your app has launched you should see the following Adjust log: `Install tracked`.

## Deep linking

### <a id="dl"></a>Deep linking Overview

If you are using Adjust tracker URLs with deeplinking enabled, it is possible to receive information about the deeplink URL and its content. Users may interact with the URL regardless of whether they have your app installed on their device (standard deep linking scenario) or not (deferred deep linking scenario). In the standard deep linking scenario, the Android platform natively offers the possibility for you to receive deep link content information. The Android platform does not automatically support deferred deep linking scenario; in this case, the Adjust SDK offers the mechanism you need to get the information about the deep link content.

### <a id="dl-standard"></a>Standard deep linking scenario

If a user has your app installed and you want it to launch after they engage with an Adjust tracker URL with the `deep_link` parameter in it, enable deeplinking in your app. This is done by choosing a desired **unique scheme name**. You'll assign it to the activity you want to launch once your app opens following a user selecting the tracker URL in the`AndroidManifest.xml` file. Add the `intent-filter` section to your desired activity definition in the manifest file and assign an `android:scheme` property value with the desired scheme name:

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

If you want your app to launch once the tracker URL is selected, use the assigned scheme name in the Adjust tracker URL's `deep_link` parameter. A tracker URL without any information added to the deeplink could look something like this:

```
https://app.adjust.com/abc123?deep_link=adjustExample%3A%2F%2F
```

Don't forget: **you must url encode** the `deep_link` parameter value in the URL.

With the app set as described above, your app will launch along with the `MainActivity` intent when a user selects the tracker URL. Inside the `MainActivity` class, you will automatically receive the information about the `deep_link` parameter content. Once you receive this content, it will **not** be encoded (even though it was encoded in the URL).

The activity setting of your `android:launchMode` within the `AndroidManifest.xml` file will determine the delivery location of the `deep_link` parameter content within the activity file. For more information about the possible values of the `android:launchMode` property, check out Android's [official documentation][android-launch-modes].

Deeplink content information within your desired activity is delivered via the `Intent` object, via either the activity's `onCreate` or `onNewIntent` methods. Once you've launched your app and have triggered one of these methods, you will be able to receive the actual deeplink passed in the `deep_link` parameter in the click URL. You can then use this information to conduct some additional logic in your app.

You can extract deeplink content from either two methods like so:

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

### <a id="dl-deferred"></a>Deferred deep linking scenario

Deferred deeplinking scenario occurs when a user clicks on an Adjust tracker URL with a `deep_link` parameter contained in it, but does not have the app installed on the device at click time. When the user clicks the URL, they will be redirected to the Play Store to download and install your app. After opening it for the first time, `deep_link` parameter content will be delivered to your app.

The Adjust SDK opens the deferred deep link by default. There is no extra configuration needed.

#### Deferred deep linking callback

If you wish to control if the Adjust SDK will open the deferred deep link, you can do it with a callback method in the config object.

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

// Evaluate the deeplink to be launched.
config.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
    @Override
    public boolean launchReceivedDeeplink(Uri deeplink) {
        // ...
        if (shouldAdjustSdkLaunchTheDeeplink(deeplink)) {
            return true;
        } else {
            return false;
        }
    }
});

Adjust.onCreate(config);
```

After the Adjust SDK receives the deep link information from our backend, the SDK will deliver you its content via the listener and expect the `boolean` return value from you. This return value represents your decision on whether or not the Adjust SDK should launch the activity to which you have assigned the scheme name from the deeplink (like in the standard deeplinking scenario).

If you return `true`, we will launch it, triggering the scenario described in the [Standard deep linking scenario](#dl-standard) chapter. If you do not want the SDK to launch the activity, return `false` from the listener, and (based on the deep link content) decide on your own what to do next in your app.
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setDeferredDeeplinkCallback(function (deeplink) {});

Adjust.onCreate(adjustConfig);
```

In this deferred deep linking scenario, there is one additional setting you can set on the config object. Once the Adjust SDK gets the deferred deep link information, you have the possibility to choose whether our SDK opens the URL or not. Set this option by calling the `setOpenDeferredDeeplink` method on the config object:

```js
// ...

function deferredDeeplinkCallback(deeplink) {}

let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setOpenDeferredDeeplink(true);
adjustConfig.setDeferredDeeplinkCallback(deferredDeeplinkCallback);

Adjust.start(adjustConfig);

```

Remember that if you do not set the callback, **the Adjust SDK will always attempt to launch the URL by default**.
</td>
</tr>
</table>

### <a id="dl-reattribution"></a>Reattribution via deeplinks

Adjust enables you to run re-engagement campaigns with deeplinks. For more information, please check our [official docs][reattribution-with-deeplinks].

If you are using this feature, you need to make one additional call to the Adjust SDK in your app for us to properly reattribute your users.

Once you have received the deeplink content in your app, add a call to the `Adjust.appWillOpenUrl(Uri, Context)` method. By making this call, the Adjust SDK will send information to the Adjust backend to check if there is any new attribution information inside of the deeplink. If your user is reattributed due to a click on the Adjust tracker URL with deeplink content, you will see the [attribution callback](#af-attribution-callback) triggered in your app with new attribution info for this user.

Here's how the call to `Adjust.appWillOpenUrl(Uri, Context)` should look:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Intent intent = getIntent();
    Uri data = intent.getData();
    Adjust.appWillOpenUrl(data, getApplicationContext());
}
```

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    Uri data = intent.getData();
    Adjust.appWillOpenUrl(data, getApplicationContext());
}
```

**Note**: `Adjust.appWillOpenUrl(Uri)` method is marked as **deprecated** as of Android SDK v4.14.0. Please use `Adjust.appWillOpenUrl(Uri, Context)` method instead.

**Note for web view**: This call can also be made from the web view with the function `Adjust.appWillOpenUrl` in Javascript like so:

```js
Adjust.appWillOpenUrl(deeplinkUrl);
```

### <a id="link-resolution"></a>Link resolution

If you are serving deep links from an Email Service Provider (ESP) and need to track clicks through a custom tracking link, you can use the `resolveLink` method of the  `AdjustLinkResolution` class to resolve the link. This ensures that you record the interaction with your email tracking campaigns when a deep link is opened in your application.

The `resolveLink` method takes the following parameters:

- `url` - the deep link that opened the application
- `resolveUrlSuffixArray` - the custom domains of the configured campaigns that need to be resolved
- `adjustLinkResolutionCallback` - the callback that will contain the final URL

If the link received does not belong to any of the domains specified in the `resolveUrlSuffixArray`, the callback will forward the deep link URL as is. If the link does contain one of the domains specified, the SDK will attempt to resolve the link and return the resulting deep link to the `callback` parameter. The returned deep link can also be reattributed in the Adjust SDK using the `Adjust.appWillOpenUrl` method.

> **Note**: The SDK will automatically follow up to ten redirects when attempting to resolve the URL. It will return the latest URL it has followed as the `callback` URL, meaning that if there are more than ten redirects to follow the **tenth redirect URL** will be returned.

**Example**

```java
AdjustLinkResolution.resolveLink(url, 
                                 new String[]{"example.com"},
                                 new AdjustLinkResolution.AdjustLinkResolutionCallback() {
    @Override
    public void resolvedLinkCallback(Uri resolvedLink) {
        Adjust.appWillOpenUrl(resolvedLink, getApplicationContext());
    }
});
```

## Event tracking

### <a id="et-tracking"></a>Track event

You can use Adjust to track any event in your app. Suppose you want to track every tap on a button. To do so, you'll create a new event token in your [dashboard]. Let's say that the event token is `abc123`. In your button's `onClick` method, add the following lines to track the click:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustEvent adjustEvent = new AdjustEvent("abc123");
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

### <a id="et-revenue"></a>Track revenue

If your users can generate revenue by tapping on advertisements or making in-app purchases, you can track those revenues too with events. Let's say a tap is worth one Euro cent. You can track the revenue event like this:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustEvent adjustEvent = new AdjustEvent("abc123");
adjustEvent.setRevenue(0.01, "EUR");
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.setRevenue(0.01, 'EUR');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

This can be combined with callback parameters.

When you set a currency token, Adjust will automatically convert the incoming revenues into a reporting revenue of your choice. Read more about [currency conversion ][currency-conversion] here.

If you want to track in-app purchases, please make sure to call `trackEvent` only if the purchase is finished and the item has been purchased. This is important in order avoid tracking revenue that was not actually generated.

You can read more about revenue and event tracking at Adjust in the [event tracking guide][event-tracking].

### <a id="et-revenue-deduplication"></a>Revenue deduplication

You can also add an optional order ID to avoid tracking duplicate revenues. By doing so, the last ten order IDs will be remembered and revenue events with duplicate order IDs are skipped. This is especially useful for tracking in-app purchases. You can see an  example below.

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustEvent adjustEvent = new AdjustEvent("abc123");
adjustEvent.setRevenue(0.01, "EUR");
adjustEvent.setOrderId("{OrderId}");
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.setRevenue(0.01, 'EUR');
adjustEvent.setOrderId('{OrderId}');
Adjust.trackEvent(event);
```
</td>
</tr>
</table>

## Custom parameters

### <a id="cp"></a>Custom parameters overview

In addition to the data points the Adjust SDK collects by default, you can use the Adjust SDK to track and add as many custom values as you need (user IDs, product IDs, etc.) to the event or session. Custom parameters are only available as raw data and will **not** appear in your Adjust dashboard.

You should use **callback parameters** for the values you collect for your own internal use, and **partner parameters** for those you share with external partners. If a value (e.g. product ID) is tracked both for internal use and external partner use, we recommend you track it with both callback and partner parameters.


### <a id="cp-event-parameters"></a>Event parameters

### <a id="cp-event-callback-parameters"></a>Event callback parameters

You can register a callback URL for your events in your [dashboard]. We will send a GET request to that URL whenever the event is tracked. You can add callback parameters to that event by calling `addCallbackParameter` to the event instance before tracking it. We will then append these parameters to your callback URL.

For example, if you've registered the URL `http://www.example.com/callback`, then you would track an event like this:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustEvent adjustEvent = new AdjustEvent("abc123");
adjustEvent.addCallbackParameter("key", "value");
adjustEvent.addCallbackParameter("foo", "bar");
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.addCallbackParameter('key', 'value');
adjustEvent.addCallbackParameter('foo', 'bar');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

In this case we would track the event and send a request to:

```
http://www.example.com/callback?key=value&foo=bar
```

Adjust supports a variety of placeholders, for example `{gps_adid}`, which can be used as parameter values. In the resulting callback, we would replace the placeholder (in this case)  with the Google Play Services ID of the current device. Please note that we don't store any of your custom parameters. We **only** append them to your callbacks. If you haven't registered a callback for an event, we will not even read these parameters.

You can read more about URL callbacks (including a full list of available values) in our [callbacks guide][callbacks-guide].

### <a id="cp-event-partner-parameters"></a>Event partner parameters

When your parameters are activated in the Adjust dashboard, you have the option to transmit them to your network partners.

This works similarly to the callback parameters mentioned above; add them by calling the `addPartnerParameter` method to your event instance.

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustEvent adjustEvent = new AdjustEvent("abc123");
adjustEvent.addPartnerParameter("key", "value");
adjustEvent.addPartnerParameter("foo", "bar");
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.addPartnerParameter('key', 'value');
adjustEvent.addPartnerParameter('foo', 'bar');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

You can read more about special partners and these integrations in our [guide to special partners][special-partners].

### <a id="cp-event-callback-id"></a>Event callback identifier

You can add custom string identifiers to each event you want to track. This identifier will later be reported in your event success and/or event failure callbacks. This lets you keep track of which event was successfully tracked. Set this identifier by calling the `setCallbackId` method on your event instance:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustEvent adjustEvent = new AdjustEvent("abc123");
adjustEvent.setCallbackId("Your-Custom-Id");
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.setCallbackId('Your-Custom-Id');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

### <a id="cp-session-parameters"></a>Session parameters

Session parameters are saved locally and sent in every **event** and **session** of the Adjust SDK. When you add any of these parameters, we will save them so you don't need to add them every time. Adding the same parameter twice will have no effect.

These session parameters can be called before the Adjust SDK is launched (to make sure they are sent on install). If you need to send them with an install, but can only obtain the needed values after launch, it's possible to [delay](#delay-start) the first launch of the Adjust SDK to allow for this behavior.

### <a id="cp-session-callback-parameters"></a>Session callback parameters

You can save any callback parameters registered for [events](#event-callback-parameters) to be sent in every event or session of the Adjust SDK.

The session callback parameters' interface is similar to the one for event callback parameters. Instead of adding the key and its value to an event, add them via a call to `Adjust.addSessionCallbackParameter(String key, String value)`:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.addSessionCallbackParameter("foo", "bar");
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.addSessionCallbackParameter('foo', 'bar');
```
</td>
</tr>
</table>

Session callback parameters merge together with the callback parameters you add to an event. Callback parameters added to an event take precedence over session callback parameters, meaning that if you add a callback parameter to an event with the same key to one added from the session, the value that prevails is the callback parameter added to the event.

It's possible to remove a specific session callback parameter by passing the desired key to the method: `Adjust.removeSessionCallbackParameter(String key)`.

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.removeSessionCallbackParameter("foo");
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.removeSessionCallbackParameter('foo');
```
</td>
</tr>
</table>

If you wish to remove all keys and their corresponding values from the session callback parameters, you can reset with the method `Adjust.resetSessionCallbackParameters()`.

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.resetSessionCallbackParameters();
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.resetSessionCallbackParameters();
```
</td>
</tr>
</table>

### <a id="cp-session-partner-parameters"></a>Session partner parameters

In the same way that [session callback parameters](#session-callback-parameters) are sent in every event or session of the Adjust SDK, there are also session partner parameters.

These are transmitted to network partners for all of the integrations activated in your Adjust [dashboard].

The session partner parameters interface is similar to the event partner parameters interface. Instead of adding the key and its value to an event, add it by calling `Adjust.addSessionPartnerParameter(String key, String value)`:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.addSessionPartnerParameter("foo", "bar");
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.addSessionPartnerParameter('foo', 'bar');
```
</td>
</tr>
</table>

The session partner parameters will be merged with the partner parameters added to an event. The partner parameters added to an event take precedence over the session partner parameters. This means that when adding a partner parameter to an event with the same key to one added from the session, the value that prevails is the partner parameter added to the event.

It's possible to remove a specific session partner parameter by passing the desiring key to the method `Adjust.removeSessionPartnerParameter(String key)`.

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.removeSessionPartnerParameter("foo");
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.removeSessionPartnerParameter('foo');
```
</td>
</tr>
</table>

If you wish to remove all keys and their corresponding values from the session partner parameters, reset it with the method `Adjust.resetSessionPartnerParameters()`.

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.resetSessionPartnerParameters();
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.resetSessionPartnerParameters();
```
</td>
</tr>
</table>

### <a id="cp-delay-start"></a>Delay start

Delaying the start of the Adjust SDK allows your app some time to obtain session parameters (such as unique identifiers) to be sent on install.

Set the initial delay time in seconds with the method `setDelayStart` in the config instance:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
adjustConfig.setDelayStart(5.5);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```java
adjustConfig.setDelayStart(5.5);
```
</td>
</tr>
</table>

In this example, this will prevent the Adjust SDK from sending the initial install session and any event created for 5.5 seconds. After the time expireds (or if you call `Adjust.sendFirstPackages()` during that time) every session parameter will be added to the delayed install session and events; the Adjust SDK will resume as usual.

**The maximum delay start time of the adjust SDK is 10 seconds**.


## Additional features

Once you have integrated the Adjust SDK into your project, you can take advantage of the following features:

### <a id="af-push-token"></a>Push token (uninstall tracking)

Push tokens are used for Audience Builder and client callbacks; they are also required for uninstall and reinstall tracking.

To send us the push notification token, add the following call to Adjust once you have obtained your token (or whenever its value changes):

<table>
<tr>
<td>
<b>Native SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.setPushToken(pushNotificationsToken, context);
```

This updated signature with `context` added allows the SDK to cover more scenarios to make sure the push token is sent. It is advised that you use the signature method above.

We do, however, still support the previous signature of the same method without the `context`.

</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.setPushToken(pushNotificationsToken);
```
</td>
</tr>
</table>

### <a id="af-attribution-callback"></a>Attribution callback

You can register a listener to be notified of tracker attribution changes. Due to the different sources we consider for attribution, we cannot provide this information synchronously.

Please see our [attribution data policies][attribution-data] for more information.

With the config instance, add the attribution callback before you start the SDK:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
    @Override
    public void onAttributionChanged(AdjustAttribution attribution) {}
});

Adjust.onCreate(config);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
function attributionCallback(attribution) {}

// ...

let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setAttributionCallback(attributionCallback);
Adjust.onCreate(adjustConfig);
```
</td>
</tr>
</table>

The listener function is called after the SDK receives the final attribution data. Within the listener function, you'll have access to the `attribution` parameter. Here is a quick summary of its properties:

- `trackerToken` the tracker token string of the current attribution.
- `trackerName` the tracker name string of the current attribution.
- `network` the network grouping level string of the current attribution.
- `campaign` the campaign grouping level string of the current attribution.
- `adgroup` the ad group grouping level string of the current attribution.
- `creative` the creative grouping level string of the current attribution.
- `clickLabel` the click label string of the current attribution.
- `adid` the Adjust device identifier string.
- `costType` the cost type string.
- `costAmount` the cost amount.
- `costCurrency` the cost currency string.

**Note**: The cost data - `costType`, `costAmount` & `costCurrency` are only available when configured in `AdjustConfig` by calling `setNeedsCost` method. If not configured or configured, but not being part of the attribution, these fields will have value `null`. This feature is available in SDK v4.25.0 and above.

### <a id="af-subscriptions"></a>Subscription tracking

**Note**: This feature is only available in the native SDK v4.22.0 and above.

You can track Play Store subscriptions and verify their validity with the Adjust SDK. After a subscription has been successfully purchased, make the following call to the Adjust SDK:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustPlayStoreSubscription subscription = new AdjustPlayStoreSubscription(
    price,
    currency,
    sku,
    orderId,
    signature,
    purchaseToken);
subscription.setPurchaseTime(purchaseTime);

Adjust.trackPlayStoreSubscription(subscription);
```
</td>
</tr>
</table>

Subscription tracking parameters:

- [price](https://developer.android.com/reference/com/android/billingclient/api/SkuDetails#getpriceamountmicros)
- [currency](https://developer.android.com/reference/com/android/billingclient/api/SkuDetails#getpricecurrencycode)
- [sku](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getsku)
- [orderId](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getorderid)
- [signature](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getsignature)
- [purchaseToken](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getpurchasetoken)
- [purchaseTime](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getpurchasetime)

Just like with event tracking, you can attach callback and partner parameters to the subscription object as well:

```java
AdjustPlayStoreSubscription subscription = new AdjustPlayStoreSubscription(
    price,
    currency,
    sku,
    orderId,
    signature,
    purchaseToken);
subscription.setPurchaseTime(purchaseTime);

// add callback parameters
subscription.addCallbackParameter("key", "value");
subscription.addCallbackParameter("foo", "bar");

// add partner parameters
subscription.addPartnerParameter("key", "value");
subscription.addPartnerParameter("foo", "bar");

Adjust.trackPlayStoreSubscription(subscription);
```

### <a id="af-ad-revenue"></a>Ad revenue tracking

**Note**: This ad revenue tracking API is available only in the native SDK v4.28.0 and above.

You can track ad revenue information with Adjust SDK by invoking the following method:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.trackAdRevenue(adjustAdRevenue);
```
</td>
</tr>
</table>

where `adjustAdRevenue` represents instance of `AdjustAdRevenue` class which is used to pass ad revenue source as well as other ad revenue related info.

Currently we support the below `source` parameter values:

- `AdjustConfig.AD_REVENUE_APPLOVIN_MAX` - representing AppLovin MAX platform.
- `AdjustConfig.AD_REVENUE_MOPUB` - representing MoPub platform.
- `AdjustConfig.AD_REVENUE_ADMOB` - representing AdMob platform.
- `AdjustConfig.AD_REVENUE_IRONSOURCE` - representing IronSource platform.
- `AdjustConfig.AD_REVENUE_ADMOST` - representing AdMost platform.
- `AdjustConfig.AD_REVENUE_UNITY` - representing Unity platform.
- `AdjustConfig.AD_REVENUE_HELIUM_CHARTBOOST` - representing Helium Chartboost platform.
- `AdjustConfig.AD_REVENUE_SOURCE_PUBLISHER` - representing Generic platform.

**Note**: Additional documentation which explains detailed integration with every of the supported sources will be provided outside of this README. Also, in order to use this feature, additional setup is needed for your app in Adjust dashboard, so make sure to get in touch with our support team to make sure that everything is set up correctly before you start to use this feature.

### <a id="af-session-event-callbacks"></a>Session and event callbacks

You can register a listener to be notified when events or sessions are tracked. There are four listeners: one for tracking successful events, one for tracking failed events, one for tracking successful sessions, and one for tracking failed sessions. Add as many listeners as you need after creating the config object like so:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

// Set event success tracking delegate.
config.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
    @Override
    public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
        // ...
    }
});

// Set event failure tracking delegate.
config.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
    @Override
    public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
        // ...
    }
});

// Set session success tracking delegate.
config.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
    @Override
    public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
        // ...
    }
});

// Set session failure tracking delegate.
config.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
    @Override
    public void onFinishedSessionTrackingFailed(AdjustSessionFailure sessionFailureResponseData) {
        // ...
    }
});

Adjust.onCreate(config);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
function eventSuccessCallback(eventSuccessResponseData) {}
function eventFailureCallback(eventFailureResponseData) {}
function sessionSuccessCallback(sessionSuccessResponseData) {}
function sessionFailureCallback(sessionFailureResponseData) {}

// ...

let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setEventSuccessCallback(eventSuccessCallback);
adjustConfig.setEventFailureCallback(eventFailureCallback);
adjustConfig.setSessionSuccessCallback(sessionSuccessCallback);
adjustConfig.setSessionFailureCallback(sessionFailureCallback);
Adjust.onCreate(adjustConfig);
```
</td>
</tr>
</table>

The listener function is called after the SDK tries to send a package to the server. Within the listener function you have access to a response data object specifically for the listener. Here is a quick summary of the success session response data object fields:

- `message` message string from the server (or the error logged by the SDK).
- `timestamp` timestamp string from the server.
- `adid` a unique string device identifier provided by Adjust.
- `jsonResponse` the JSON object with the reponse from the server.

Both event response data objects contain:

- `eventToken` the event token string, if the package tracked was an event.
- `callbackId` the custom defined [callback ID](#cp-event-callback-id) string set on the event object.

And both event and session failed objects also contain:

- `willRetry` boolean which indicates whether there will be a later attempt to resend the package.

### <a id="af-user-attribution"></a>User attribution

Like we described in the [attribution callback section](#af-attribution-callback), this callback is triggered whenever the attribution information changes. Access your user's current attribution information whenever you need it by making a call to the following method of the `Adjust` instance:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustAttribution attribution = Adjust.getAttribution();
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let attribution = Adjust.getAttribution();
```
</td>
</tr>
</table>

**Note**: You can only make this call using the Adjust SDK v4.11.0 or **above**.

**Note**: Current attribution information is only available after our backend tracks the app install and triggers the attribution callback. **It is not possible** to access a user's attribution value before the SDK has been initialized and the attribution callback has been triggered.

### <a id="af-device-ids"></a>Device IDs

The Adjust SDK offers you the possibility to obtain device identifiers.

### <a id="af-gps-adid"></a>Google Play Services Advertising Identifier

The Google Play Services Advertising Identifier (Google advertising ID) is a unique identifier for a device. Users can opt out of sharing their Google advertising ID by toggling the "Opt out of Ads Personalization" setting on their device. When a user has enabled this setting, the Adjust SDK returns a string of zeros when trying to read the Google advertising ID.

> **Important**: If you are targeting Android 12 and above (API level 31), you need to add the [`com.google.android.gms.AD_ID` permission](#gps-adid-permission) to your app. If you do not add this permission, you will not be able to read the Google advertising ID even if the user has not opted out of sharing their ID.

Certain services (such as Google Analytics) require you to coordinate advertising IDs and client IDs in order to prevent duplicate reporting.

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

If you need to obtain the Google Advertising ID, there is a restriction; it can only be read in a background thread. If you call the function `getGoogleAdId` with the context and a `OnDeviceIdsRead` instance, it will work in any situation:

```java
Adjust.getGoogleAdId(this, new OnDeviceIdsRead() {
    @Override
    public void onGoogleAdIdRead(String googleAdId) {}
});
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

To obtain the device's Google Advertising device identifier, it's necessary to pass a callback function to `Adjust.getGoogleAdId` which will receive the Google Advertising ID in its argument, like so:

```js
Adjust.getGoogleAdId(function(googleAdId) {
    // ...
});
```
</td>
</tr>
</table>

### <a id="af-amazon-adid"></a>Amazon advertising identifier

If you need to obtain the Amazon Advertising ID, call the following method on `Adjust` instance:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
String amazonAdId = Adjust.getAmazonAdId(context);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let amazonAdId = Adjust.getAmazonAdId();
```
</td>
</tr>
</table>

### <a id="af-adid"></a>Adjust device identifier

For each device with your app installed on it, our backend generates a unique **Adjust device identifier** (known as an **adid**). In order to obtain this identifier, call the following method on `Adjust` instance:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
String adid = Adjust.getAdid();
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adid = Adjust.getAdid();
```
</td>
</tr>
</table>

**Note**: You can only make this call in the Adjust SDK in v4.11.0 and **above**.

**Note**: Information about the **adid** is only available after our backend tracks the app instal. **It is not possible** to access the **adid** value before the SDK has been initialized and the installation of your app has been successfully tracked.

### <a id="af-preinstalled-apps"></a>Preinstalled apps

You can use the Adjust SDK to recognize users whose devices had your app preinstalled during manufacturing. Adjust offers two solutions: one which uses the system payload, and one which uses a default tracker. 

In general, we recommend using the system payload solution. However, there are certain use cases which may require the tracker. First check the available [implementation methods](https://help.adjust.com/en/article/pre-install-tracking#Implementation_methods) and your preinstall partner’s preferred method. If you are unsure which solution to implement, reach out to integration@adjust.com

#### Use the system payload

- The Content Provider, System Properties, or File System method is supported from SDK v4.23.0 and above.

- The System Installer Receiver method is supported from SDK v4.27.0 and above.

Enable the Adjust SDK to recognise preinstalled apps by calling `setPreinstallTrackingEnabled` with the parameter `true` after creating the config object:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
adjustConfig.setPreinstallTrackingEnabled(true);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
adjustConfig.setPreinstallTrackingEnabled(true);
```
</td>
</tr>
</table>

Depending upon your implmentation method, you may need to make a change to your AndroidManifest.xml file. Find the required code change using the table below.

<table>
<tr>
<td>
  <b>Method</b>
</td>
<td>
  <b>AndroidManifest.xml change</b>
</td>
</tr>
<tr>
<td>Content Provider</td>
<td>Add permission:</br>

```
<uses-permission android:name="com.adjust.preinstall.READ_PERMISSION"/>
```
</td>
</tr>
<tr>
<td>System Installer Receiver</td>
<td>Declare receiver:</br>

```xml
<receiver
    android:name="com.adjust.sdk.AdjustPreinstallReferrerReceiver"
    android:exported="true" >
    <intent-filter>
        <action android:name="com.attribution.SYSTEM_INSTALLER_REFERRER" />
    </intent-filter>
</receiver>
```
</td>
</tr>
</table>

#### Use a default tracker

- Create a new tracker in your [dashboard].
- Open your app delegate and set the default tracker of your config:

  <table>
  <tr>
  <td>
  <b>Native App SDK</b>
  </td>
  </tr>
  <tr>
  <td>

  ```java
  adjustConfig.setDefaultTracker("{TrackerToken}");
  ```
  </td>
  </tr>
  <tr>
  <td>
  <b>Web View SDK</b>
  </td>
  </tr>
  <tr>
  <td>

  ```js
  adjustConfig.setDefaultTracker('{TrackerToken}');
  ```
  </td>
  </tr>
  </table>

- Replace `{TrackerToken}` with the tracker token you created in step one. Please note that the dashboard displays a tracker URL (including `http://app.adjust.com/`). In your source code, you should specify only the six or seven-character token and not the entire URL.

- Build and run your app. You should see a line like the following in your LogCat:

  ```
  Default tracker: 'abc123'
  ```

### <a id="af-offline-mode"></a>Offline mode

You can put the Adjust SDK in offline mode to suspend transmission to our servers (while retaining tracked data to be sent later). While in offline mode, all information is saved in a file. Please be careful not to trigger too many events while in offline mode.

Activate offline mode by calling `setOfflineMode` with the parameter `true`.

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.setOfflineMode(true);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.setOfflineMode(true);
```
</td>
</tr>
</table>

Conversely, you can deactivate offline mode by calling `setOfflineMode` with `false`. When the Adjust SDK is put back into online mode, all saved information is sent to our servers with the correct time information.

Unlike disabling tracking, this setting is **not remembered** between sessions. This means the SDK is in online mode whenever it starts, even if the app was terminated in offline mode.


### <a id="af-disable-tracking"></a>Disable tracking

You can disable the Adjust SDK from tracking any activities of the current device by calling `setEnabled` with parameter `false`. **This setting is remembered between sessions**.

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.setEnabled(false);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.setEnabled(false);
```
</td>
</tr>
</table>

You can check to see if the Adjust SDK is currently enabled by calling the function `isEnabled`. It is always possible to activatе the Adjust SDK by invoking `setEnabled` with the enabled parameter as `true`.

### <a id="af-event-buffering"></a>Event buffering

If your app makes heavy use of event tracking, you might want to delay some network requests in order to send them in one batch every minute. You can enable event buffering with your config instance:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
adjustConfig.setEventBufferingEnabled(true);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
adjustConfig.setEventBufferingEnabled(true);
```
</td>
</tr>
</table>

### <a id="af-background-tracking"></a>Background tracking

The default behaviour of the Adjust SDK is to pause sending network requests while the app is in the background. You can change this in your config instance:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
adjustConfig.setSendInBackground(true);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
adjustConfig.setSendInBackground(true);
```
</td>
</tr>
</table>

### <a id="af-gdpr-forget-me"></a>GDPR right to be forgotten

In accordance with article 17 of the EU's General Data Protection Regulation (GDPR), you can notify Adjust when a user has exercised their right to be forgotten. Calling the following method will instruct the Adjust SDK to communicate the user's choice to be forgotten to the Adjust backend:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.gdprForgetMe(context);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.gdprForgetMe();
```
</td>
</tr>
</table>

Upon receiving this information, Adjust will erase the user's data and the Adjust SDK will stop tracking the user. No requests from this device will be sent to Adjust in the future.

Please note that even when testing, this decision is permanent. It **is not** reversible.

## <a id="af-third-party-sharing"></a>Third-party sharing for specific users

You can notify Adjust when a user disables, enables, and re-enables data sharing with third-party partners.

### <a id="af-disable-third-party-sharing"></a>Disable third-party sharing for specific users

Call the following method to instruct the Adjust SDK to communicate the user's choice to disable data sharing to the Adjust backend:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustThirdPartySharing adjustThirdPartySharing = new AdjustThirdPartySharing(false);
Adjust.trackThirdPartySharing(adjustThirdPartySharing);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustThirdPartySharing = new AdjustThirdPartySharing(false);
Adjust.trackThirdPartySharing(adjustThirdPartySharing);
```
</td>
</tr>
</table>

Upon receiving this information, Adjust will block the sharing of that specific user's data to partners and the Adjust SDK will continue to work as usual.

### <a id="af-enable-third-party-sharing"></a>Enable or re-enable third-party sharing for specific users

Call the following method to instruct the Adjust SDK to communicate the user's choice to share data or change data sharing, to the Adjust backend:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustThirdPartySharing adjustThirdPartySharing = new AdjustThirdPartySharing(true);
Adjust.trackThirdPartySharing(adjustThirdPartySharing);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustThirdPartySharing = new AdjustThirdPartySharing(true);
Adjust.trackThirdPartySharing(adjustThirdPartySharing);
```
</td>
</tr>
</table>

Upon receiving this information, Adjust changes sharing the specific user's data to partners. The Adjust SDK will continue to work as expected.

Call the following method to instruct the Adjust SDK to send the granular options to the Adjust backend:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustThirdPartySharing adjustThirdPartySharing = new AdjustThirdPartySharing(null);
adjustThirdPartySharing.addGranularOption("PartnerA", "foo", "bar");
Adjust.trackThirdPartySharing(adjustThirdPartySharing);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustThirdPartySharing = new AdjustThirdPartySharing(null);
adjustThirdPartySharing.addGranularOption("PartnerA", "foo", "bar");
Adjust.trackThirdPartySharing(adjustThirdPartySharing);
```
</td>
</tr>
</table>

### <a id="af-measurement-consent"></a>Consent measurement for specific users

To enable or disable the Data Privacy settings in the Adjust Dashboard, including the consent expiry period and the user data retention period, you need to implement the below method.

Call the following method to instruct the Adjust SDK to communicate the Data Privacy settings, to the Adjust backend:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.trackMeasurementConsent(true);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
Adjust.trackMeasurementConsent(true);
```
</td>
</tr>
</table>

Upon receiving this information, Adjust enables or disables consent measurement. The Adjust SDK will continue to work as expected.

### <a id="af-data-residency"></a>Data residency

In order to enable data residency feature, make sure to make a call to `setUrlStrategy` method of the `AdjustConfig` instance with one of the following constants:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
adjustConfig.setUrlStrategy(AdjustConfig.DATA_RESIDENCY_EU); // for EU data residency region
adjustConfig.setUrlStrategy(AdjustConfig.DATA_RESIDENCY_TR); // for Turkey data residency region
adjustConfig.setUrlStrategy(AdjustConfig.DATA_RESIDENCY_US); // for US data residency region
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
adjustConfig.setUrlStrategy(AdjustConfig.DataResidencyEU); // for EU data residency region
adjustConfig.setUrlStrategy(AdjustConfig.DataResidencyTR); // for Turkey data residency region
adjustConfig.setUrlStrategy(AdjustConfig.DataResidencyUS); // for US data residency region
```
</td>
</tr>
</table>

**Note:** Please, do not turn this setting on before making sure with the support team that this feature is enabled for your app because otherwise SDK traffic will get dropped.

### <a id="af-coppa-compliance"></a>COPPA compliance

By deafult Adjust SDK doesn't mark app as COPPA compliant. In order to mark your app as COPPA compliant, make sure to call `setCoppaCompliantEnabled` method of `AdjustConfig` instance with boolean parameter `true`:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
adjustConfig.setCoppaCompliantEnabled(true);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
adjustConfig.setCoppaCompliantEnabled(true);
```
</td>
</tr>
</table>

**Note:** By enabling this feature, third-party sharing will be automatically disabled for the users. If later during the app lifetime you decide not to mark app as COPPA compliant anymore, third-party sharing **will not be automatically re-enabled**. Instead, next to not marking your app as COPPA compliant anymore, you will need to explicitly re-enable third-party sharing in case you want to do that.

### <a id="af-play-store-kids-apps"></a>Play Store Kids Apps

By default Adjust SDK doesn't mark app as Play Store Kids App. In order to mark your app as the app which is targetting kids in Play Store, make sure to call `setPlayStoreKidsAppEnabled` method of `AdjustConfig` instance with boolean parameter `true`:

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
adjustConfig.setPlayStoreKidsAppEnabled(true);
```
</td>
</tr>
<tr>
<td>
<b>Web View SDK</b>
</td>
</tr>
<tr>
<td>

```js
adjustConfig.setPlayStoreKidsAppEnabled(true);
```
</td>
</tr>
</table>

## Testing and troubleshooting

### <a id="tt-session-failed"></a>I'm seeing the "Session failed (Ignoring too frequent session. ...)" error.

This error typically occurs when testing installs. Uninstalling and reinstalling the app is not enough to trigger a new install. The servers will determine that the SDK has lost its locally aggregated session data and ignore the erroneous message, given the information available on the servers about the device.

This behavior can be cumbersome during testing, but is necessary in order to have the sandbox behavior match production as much as possible.

You can reset your app's session data for any device directly from the Adjust Dashboard using our [Testing Console][testing_console] if you have Editor-level access (or higher) to the app. 

Once the device has been correctly forgotten, the Testing Console will return `Forgot device`. If the device was already forgotten (or if the values were incorrect) the link will return `Advertising ID not found`.

Forgetting the device will not reverse the GDPR forget call.

If your current package gives you access, you can also inspect and forget a device using our [Developer API][dev_api].

### <a id="tt-broadcast-receiver"></a>Is my broadcast receiver capturing the install referrer?

If you followed the instructions in the [guide](#qs-gps-intent), the broadcast receiver should be configured to send the install referrer to our SDK and to our servers.

You can test this by manually triggering a test install referrer. Replace `com.your.appid` with your app ID and run the following command with the [adb](http://developer.android.com/tools/help/adb.html) tool that comes with Android Studio:

```
adb shell am broadcast -a com.android.vending.INSTALL_REFERRER -n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver --es "referrer" "adjust_reftag%3Dabc1234%26tracking_id%3D123456789%26utm_source%3Dnetwork%26utm_medium%3Dbanner%26utm_campaign%3Dcampaign"
```

If you already use a different broadcast receiver for the `INSTALL_REFERRER` intent and followed this [guide][referrer], replace `com.adjust.sdk.AdjustReferrerReceiver` with your broadcast receiver.

You can also remove the `-n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver` parameter so that all the apps in the device will receive the `INSTALL_REFERRER` intent.

If you set the log level to `verbose`, you should be able to see the log from reading the referrer:

```
V/Adjust: Referrer to parse (adjust_reftag=abc1234&tracking_id=123456789&utm_source=network&utm_medium=banner&utm_campaign=campaign) from reftag
```

And a click package added to the SDK's package handler:

```
V/Adjust: Path:      /sdk_click
    ClientSdk: android4.6.0
    Parameters:
      app_token        abc123abc123
      click_time       yyyy-MM-dd'T'HH:mm:ss.SSS'Z'Z
      created_at       yyyy-MM-dd'T'HH:mm:ss.SSS'Z'Z
      environment      sandbox
      gps_adid         12345678-0abc-de12-3456-7890abcdef12
      needs_attribution_data 1
      referrer         adjust_reftag=abc1234&tracking_id=123456789&utm_source=network&utm_medium=banner&utm_campaign=campaign
      reftag           abc1234
      source           reftag
      tracking_enabled 1
```

If you perform this test before launching the app, you won't see the package being sent. The package will be sent once the app is launched.

**Important:** We encourage you to **not** use the `adb` tool for testing this particular feature. In order to test your full referrer content (in case you have multiple parameters separated with `&`), with `adb` you will need to encode that content in order to get it into your broadcast receiver. If you don't encode it, `adb` will cut your referrer after the first `&` sign and deliver wrong content to your broadcast receiver.

If you would like to see how your app receives an unencoded referrer value, we would encourage you to try our example app and alter the content being passed so that it fires with intent inside of the `onFireIntentClick` method inside of the  `MainActivity.java` file:

```java
public void onFireIntentClick(View v) {
    Intent intent = new Intent("com.android.vending.INSTALL_REFERRER");
    intent.setPackage("com.adjust.examples");
    intent.putExtra("referrer", "utm_source=test&utm_medium=test&utm_term=test&utm_content=test&utm_campaign=test");
    sendBroadcast(intent);
}
```

Feel free to alter the second parameter of `putExtra` method with content of your choice.

### <a id="tt-event-at-launch"></a>Can I trigger an event at application launch?

Triggering an event at this time might not do what you expect. Here's why:

The `onCreate` method on the global `Application` class is called not only at application launch, but also when a system or application event is captured by the app.

Our SDK is prepared for initialization at this time, but has not actually started. This will only happen when an activity takes place, i.e., when a user actually launches the app.

Triggering an event at this time would start the Adjust SDK and send the events, even though the app was not launched by the user - at a time that depends on factors external to the app.

Triggering events at application launch will thus result in inaccuracies in the number of installs and sessions tracked.

If you want to trigger an event after the install, use the [attribution callback](#af-attribution-callback).

If you want to trigger an event when the app is launched, use the `onCreate` method for the given activity.

[dashboard]:  http://adjust.com
[adjust.com]: http://adjust.com

[en-readme]:  README.md
[zh-readme]:  doc/chinese/README.md
[ja-readme]:  doc/japanese/README.md
[ko-readme]:  doc/korean/README.md

[en-helpcenter]: https://help.adjust.com/en/developer/android-sdk-documentation
[zh-helpcenter]: https://help.adjust.com/zh/developer/android-sdk-documentation
[ja-helpcenter]: https://help.adjust.com/ja/developer/android-sdk-documentation
[ko-helpcenter]: https://help.adjust.com/ko/developer/android-sdk-documentation

[example-java]:       Adjust/example-app-java
[example-kotlin]:     Adjust/example-app-kotlin
[example-keyboard]:     Adjust/example-app-keyboard
[example-tv]:         Adjust/example-app-tv
[example-webbridge]:  Adjust/example-app-webbridge

[maven]:                          http://maven.org
[referrer]:                       doc/english/misc/multiple-receivers.md
[releases]:                       https://github.com/adjust/android_sdk/releases
[google-ad-id]:                   https://support.google.com/googleplay/android-developer/answer/6048248?hl=en
[event-tracking]:                 https://docs.adjust.com/en/event-tracking
[callbacks-guide]:                https://docs.adjust.com/en/callbacks
[new-referrer-api]:               https://developer.android.com/google/play/installreferrer/library.html
[special-partners]:               https://docs.adjust.com/en/special-partners
[attribution-data]:               https://github.com/adjust/sdks/blob/master/doc/attribution-data.md
[android-dashboard]:              http://developer.android.com/about/dashboards/index.html
[currency-conversion]:            https://docs.adjust.com/en/event-tracking/#tracking-purchases-in-different-currencies
[android-application]:            http://developer.android.com/reference/android/app/Application.html
[android-launch-modes]:           https://developer.android.com/guide/topics/manifest/activity-element.html
[google-play-services]:           http://developer.android.com/google/play-services/setup.html
[reattribution-with-deeplinks]:   https://docs.adjust.com/en/deeplinking/#manually-appending-attribution-data-to-a-deep-link
[android-purchase-verification]:  https://github.com/adjust/android_purchase_sdk
[testing_console]: https://docs.adjust.com/en/testing-console/#how-to-clear-your-advertising-id-from-adjust-between-tests
[dev_api]: https://docs.adjust.com/en/adjust-for-developers/

[sdk2sdk-mopub]:    doc/english/sdk-to-sdk/mopub.md

## <a id="license"></a>License

The Adjust SDK is licensed under the MIT License.

Copyright (c) 2012-2019 Adjust GmbH, http://www.adjust.com

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
