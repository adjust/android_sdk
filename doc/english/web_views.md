## Summary

This is the guide to the Android SDK of adjust.com™ for Android apps which are using web views. You can read more about 
adjust.com™ at [adjust.com].

## Table of contents

* [Basic integration](#basic-integration)
    * [Add native adjust Android SDK](#native-add)
    * [Add AdjustBridge to your project](#bridge-add)
    * [Integrate AdjustBridge into your app](#bridge-integrate-app)
    * [Integrate AdjustBridge into your web view](#bridge-integrate-web)
    * [AdjustBridge logging](#adjust-logging)
    * [Build your app](#build-the-app)
* [Additional features](#additional-features)
    * [Event tracking](#event-tracking)
        * [Revenue tracking](#revenue-tracking)
        * [Callback parameters](#callback-parameters)
        * [Partner parameters](#partner-parameters)
    * [Attribution callback](#attribution-callback)
    * [Event and session callbacks](#event-session-callbacks)
    * [Event buffering](#event-buffering)
    * [Disable tracking](#disable-tracking)
    * [Offline mode](#offline-mode)
    * [Background tracking](#background-tracking)
    * [Device IDs](#device-ids)
    * [Deep linking](#deeplink)
        * [Standard deep linking scenario](#deeplinking-standard)
        * [Deferred deep linking scenario](#deeplinking-deferred)
        * [Reattribution via deep links](#deeplinking-reattribution)
* [License](#license)

## <a id="basic-integration"></a>Basic integration

### <a id="native-add">Add native adjust Android SDK

In oder to use the adjust SDK in your web views, you need to add adjust's native Android SDK to your app. To install the native Android SDK, follow the `Basic integration` chapter of our [Android SDK README][android-sdk-basic-integration].

### <a id="bridge-add">Add AdjustBridge to your project

Copy AdjustBridge `.java` files to your project. You can add them to the package of your choice, but we recommend you to follow the package naming convention we use by default in our source files. In order to do this, please create `com.adjust.sdk.bridge` package inside your project and copy AdjustBridge `.java` files in there.

Copy AdjustBridge `.js` files to your project. Add them to your `assets` folder.

![][bridge_add]

### <a id="bridge-integrate-app"></a>Integrate AdjustBridge into your app

In order to properly initialise the AdjustBridge instance, you need to pass the `Application` reference to it and the `WebView` object in which you are loading your HTML page.

We advise you to define your custom `Application` class and pass the reference in its `onCreate` method:

```java
public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AdjustBridge.setApplicationContext(this);
    }
}
```

After you have obtained the reference to your `WebView` object, pass it to AdjustBridge:

```java
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView) findViewById(R.id.webView);
        AdjustBridge.setWebView(webView);

        // ...
    }
}
```

In case you don't want to add a custom `Application` class, you can pass the application reference now as well:

```java
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView) findViewById(R.id.webView);
        
        AdjustBridge.setApplicationContext(getApplication());
        AdjustBridge.setWebView(webView);

        // ...
    }
}
```

By completing these steps, AdjustBridge has everything it needs to enable the communication between our native SDK and your page loaded in web view. Now you need to properly initialise your web view to use the AdjustBridge Javascript interface:

```java
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView) findViewById(R.id.webView);
        
        AdjustBridge.setApplicationContext(getApplication());
        AdjustBridge.setWebView(webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(AdjustBridge.getDefaultInstance(), "AdjustBridge");

        try {
            webView.loadUrl("file:///android_asset/AdjustExample-WebView.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

By completing this, you have successfully added AdjustBridge to your app and enabled the Javascript bridge to communicate 
between adjust's native Android SDK and your page which will be loaded in web view.

### <a id="bridge-integrate-web"></a>Integrate AdjustBridge into your web view

In your HTML file, add references to the adjust Javascript files:

```html
<script type="text/javascript" src="adjust.js"></script>
<script type="text/javascript" src="adjust_event.js"></script>
<script type="text/javascript" src="adjust_config.js"></script>
```

Once you added references to the Javascript files, you can use them in your HTML file to initialise the adjust SDK:

```js
// ...

var yourAppToken = '{YourAppToken}'
var environment = AdjustConfig.EnvironmentSandbox
var adjustConfig = new AdjustConfig(AdjustBridge, yourAppToken, environment)

Adjust.onCreate(adjustConfig)

// ...
```

![][bridge_init_js_android]

Replace `{YourAppToken}` with your app token. You can find this in your [dashboard].

Depending on whether you build your app for testing or for production, you must set `environment` with one of these values:

```js
var environment = AdjustConfig.EnvironmentSandbox
var environment = AdjustConfig.EnvironmentProduction
```

**Important:** This value should be set to `AdjustConfig.EnvironmentSandbox` if and only if you or someone else is testing 
your app. Make sure to set the environment to `AdjustConfig.EnvironmentProduction` just before you publish the app. Set it 
back to `AdjustConfig.EnvironmentSandbox` when you start developing and testing it again.

We use this environment to distinguish between real traffic and test traffic from test devices. It is very important that 
you keep this value meaningful at all times! This is especially important if you are tracking revenue.

### <a id="adjust-logging">AdjustBridge logging

You can increase or decrease the amount of logs you see in tests by calling `setLogLevel` on your `AdjustConfig` instance 
with one of the following parameters:

```objc
adjustConfig.setLogLevel(AdjustConfig.LogLevelVerbose) // enable all logging
adjustConfig.setLogLevel(AdjustConfig.LogLevelDebug)   // enable more logging
adjustConfig.setLogLevel(AdjustConfig.LogLevelInfo)    // the default
adjustConfig.setLogLevel(AdjustConfig.LogLevelWarn)    // disable info logging
adjustConfig.setLogLevel(AdjustConfig.LogLevelError)   // disable warnings as well
adjustConfig.setLogLevel(AdjustConfig.LogLevelAssert)  // disable errors as well
```

### <a id="build-the-app">Build your app

Build and run your app. If the build succeeds, you should carefully read the SDK logs in the console. After the app launches
for the first time, you should see the info log `Install tracked`.

![][bridge_install_tracked]

## <a id="additional-features">Additional features

Once you integrate the adjust SDK into your project, you can take advantage of the following features.

### <a id="event-tracking">Event tracking

You can use adjust to track events. Let's say you want to track every tap on a particular button. You would create a new 
event token in your [dashboard], which has an associated event token - looking something like `abc123`. In your button's 
`onclick` method you would then add the following lines to track the tap:

```js
var adjustEvent = new AdjustEvent('abc123')
Adjust.trackEvent(adjustEvent)
```

When tapping the button you should now see `Event tracked` in the logs.

The event instance can be used to configure the event even more before tracking it.

#### <a id="revenue-tracking">Revenue tracking

If your users can generate revenue by tapping on advertisements or making in-app purchases you can track those revenues with
events. Let's say a tap is worth one Euro cent. You could then track the revenue event like this:

```js
var adjustEvent = new AdjustEvent('abc123')
adjustEvent.setRevenue(0.01, 'EUR')

Adjust.trackEvent(adjustEvent)
```

This can be combined with callback parameters of course.

When you set a currency token, adjust will automatically convert the incoming revenues into a reporting revenue of your 
choice. Read more about [currency conversion here.][currency-conversion]

You can read more about revenue and event tracking in the [event tracking guide][event-tracking-guide].

#### <a id="callback-parameters">Callback parameters

You can register a callback URL for your events in your [dashboard]. We will send a GET request to that URL whenever the 
event gets tracked. You can add callback parameters to that event by calling `addCallbackParameter` on the event before 
tracking it. We will then append these parameters to your callback URL.

For example, suppose you have registered the URL `http://www.adjust.com/callback` then track an event like this:

```js
var adjustEvent = new AdjustEvent('abc123')
adjustEvent.addCallbackParameter('key', 'value')
adjustEvent.addCallbackParameter('foo', 'bar')

Adjust.trackEvent(adjustEvent)
```

In that case we would track the event and send a request to:

    http://www.adjust.com/callback?key=value&foo=bar

It should be mentioned that we support a variety of placeholders like `{gps_adid}` that can be used as parameter values. In the 
resulting callback this placeholder would be replaced with the Google Play Services ID of the current device. Also note that we 
don't store any of your custom parameters, but only append them to your callbacks. If you haven't registered a callback for 
an event, these parameters won't even be read.

You can read more about using URL callbacks, including a full list of available values, in our 
[callbacks guide][callbacks-guide].

#### <a id="partner-parameters">Partner parameters

You can also add parameters to be transmitted to network partners, for the integrations that have been activated in your 
adjust dashboard.

This works similarly to the callback parameters mentioned above, but can be added by calling the `addPartnerParameter` 
method on your `AdjustEvent` instance.

```js
var adjustEvent = new AdjustEvent('abc123')
adjustEvent.addPartnerParameter('key', 'value')
adjustEvent.addPartnerParameter('foo', 'bar')

Adjust.trackEvent(adjustEvent)
```

You can read more about special partners and these integrations in our [guide to special partners][special-partners].

### <a id="attribution-callback">Attribution callback

You can register a callback method to be notified of attribution changes. Due to the different sources considered 
for attribution, this information cannot by provided synchronously.

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
          'Click label = ' + attribution.clickLabel)
}

// ...

var yourAppToken = '{YourAppToken}'
var environment = AdjustConfig.EnvironmentSandbox
var adjustConfig = new AdjustConfig(AdjustBridge, yourAppToken, environment)

adjustConfig.setAttributionCallback(attributionCallback)

Adjust.onCreate(adjustConfig)

// ...
```

The callback method will get triggered when the SDK receives final attribution data. Within the callback you have access to 
the `attribution` parameter. Here is a quick summary of its properties:

- `var trackerToken` the tracker token of the current install.
- `var trackerName` the tracker name of the current install.
- `var network` the network grouping level of the current install.
- `var campaign` the campaign grouping level of the current install.
- `var adgroup` the ad group grouping level of the current install.
- `var creative` the creative grouping level of the current install.
- `var clickLabel` the click label of the current install.

### <a id="event-session-callbacks">Event and session callbacks

You can register a listener to be notified when events or sessions are tracked. There are four listeners: one for tracking 
successful events, one for tracking failed events, one for tracking successful sessions and one for tracking failed 
sessions.

Follow these steps and implement the following callback methods to track successful events:

```js
function eventSuccessCallback(eventSuccess) {
    alert('Message = ' + eventSuccess.message + '\n' +
          'Timestamp = ' + eventSuccess.timestamp + '\n' +
          'Adid = ' + eventSuccess.adid + '\n' +
          'Event token = ' + eventSuccess.eventToken)
}

// ...

adjustConfig.setEventSuccessCallback(eventSuccessCallback)
```

The following delegate callback function to track failed events:

```js
function eventFailureCallback(eventFailure) {
    alert('Message = ' + eventFailure.message + '\n' +
          'Timestamp = ' + eventFailure.timestamp + '\n' +
          'Adid = ' + eventFailure.adid + '\n' +
          'Event token = ' + eventFailure.eventToken + '\n' +
          'Will retry = ' + eventFailure.willRetry)
}

adjustConfig.setEventFailureCallback(eventFailureCallback)
```

To track successful sessions:

```js
function sessionSuccessCallback(sessionSuccess) {
    alert('Message = ' + sessionSuccess.message + '\n' +
          'Timestamp = ' + sessionSuccess.timestamp + '\n' +
          'Adid = ' + sessionSuccess.adid)
}

adjustConfig.setSessionSuccessCallback(sessionSuccessCallback)
```

And to track failed sessions:

```js
function sessionFailureCallback(sessionFailure) {
    alert('Message = ' + sessionFailure.message + '\n' +
          'Timestamp = ' + sessionFailure.timestamp + '\n' +
          'Adid = ' + sessionFailure.adid + '\n' +
          'Will retry = ' + sessionFailure.willRetry)
}

adjustConfig.setSessionFailureCallback(sessionFailureCallback)
```

The callback methods will be called after the SDK tries to send a package to the server. Within the callback methods you 
have access to a response data object specifically for that callback. Here is a quick summary of the session response data 
properties:

- `var message` the message from the server or the error logged by the SDK.
- `var timeStamp` timestamp from the server.
- `var adid` a unique device identifier provided by adjust.
- `var jsonResponse` the JSON object with the response from the server.

Both event response data objects contain:

- `var eventToken` the event token, if the package tracked was an event.

And both event and session failed objects also contain:

- `var willRetry` indicates there will be an attempt to resend the package at a later time.

### <a id="disable-tracking">Disable tracking

You can disable the adjust SDK from tracking any activities of the current device by calling `setEnabled` with parameter 
`false`. **This setting is remembered between sessions**, but it can only be activated after the first session.

```js
Adjust.setEnabled(false)
```

<a id="is-enabled">You can check if the adjust SDK is currently enabled by calling the function `isEnabled`. It is always 
possible to activate the adjust SDK by invoking `setEnabled` with the enabled parameter as `true`.

```js
Adjust.isEnabled(function(isEnabled) {
    if (isEnabled) {
        // SDK is enabled.    
    } else {
        // SDK is disabled.
    }
})
```

### <a id="offline-mode">Offline mode

You can put the adjust SDK in offline mode to suspend transmission to our servers while retaining tracked data to be sent 
later. While in offline mode, all information is saved in a file, so be careful not to trigger too many events while in 
offline mode.

You can activate offline mode by calling `setOfflineMode` with the parameter `true`.

```js
Adjust.setOfflineMode(true)
```

Conversely, you can deactivate offline mode by calling `setOfflineMode` with `false`. When the adjust SDK is put back in 
online mode, all saved information is send to our servers with the correct time information.

Unlike disabling tracking, this setting is **not remembered bettween sessions**. This means that the SDK is in online mode 
whenever it is started, even if the app was terminated in offline mode.

### <a id="event-buffering">Event buffering

If your app makes heavy use of event tracking, you might want to delay some HTTP requests in order to send them in one batch
every minute. You can enable event buffering with your `AdjustConfig` instance:

```js
adjustConfig.setEventBufferingEnabled(true)
```

### <a id="background-tracking">Background tracking

The default behaviour of the adjust SDK is to pause sending HTTP requests while the app is in the background. You can change
this behaviour in your `AdjustConfig` instance:

```js
adjustConfig.setSendInBackground(true)
```

### <a id="device-ids">Device IDs

Certain services (such as Google Analytics) require you to coordinate Device and Client IDs in order to prevent duplicate 
reporting. 

To obtain the device Google Advertising identifier, call the function `getGoogleAdId`:

```js
Adjust.getGoogleAdId(function(googleAdId) {
    // ...
});
```

### <a id="deeplink">14. Deep linking

To read the deep linking mechanism description in more detail, please consult our official 
[Android SDK README][android-readme-deeplinking].

In order to get deep link info inside your web view, you need to make the calls from native Android app. As described in the
deep linking section of the official SDK README, deep link content can be delivered to one of two Activity lifecycle 
methods. In order to deliver deep link info to your page in web view, just override the appropriate one and add a call to the
`AdjustBridge.deeplinkReceived` method:

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    Uri data = intent.getData();
    AdjustBridge.deeplinkReceived(data);
}
```

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    WebView webView = (WebView) findViewById(R.id.webView);
    
    AdjustBridge.setApplicationContext(getApplication());
    AdjustBridge.setWebView(webView);

    webView.getSettings().setJavaScriptEnabled(true);
    webView.setWebChromeClient(new WebChromeClient());
    webView.setWebViewClient(new WebViewClient());
    webView.addJavascriptInterface(AdjustBridge.getDefaultInstance(), "AdjustBridge");

    try {
        webView.loadUrl("file:///android_asset/AdjustExample-WebView.html");

        Intent intent = getIntent();
        Uri data = intent.getData();

        AdjustBridge.deeplinkReceived(data);
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

#### <a id="deeplinking-standard">Standard deep linking scenario

In order to get deeplink URL info back to your web view, you should register a callback method in your HTML script called 
`adjust_deeplink`. This method will then get triggered by the adjust SDK once your app gets opened after clicking on a tracker
URL with deeplink information in it.

```js
function adjust_deeplink(deeplink) {
    alert('Deeplink content:\n' + deeplink)
}
```

#### <a id="deeplinking-deferred">Deferred deep linking scenario

You can register a callback method to get notified before a deferred deeplink is opened and decide whether the adjust SDK 
should open it or not.

This callback is also set on `AdjustConfig` instance:

```js
adjustConfig.setDeferredDeeplinkCallback(function(deferredDeeplink) {
    // In this example, we're just displaying alert with deferred deeplink URL content.
    alert('Deferred deeplink:\n' + deferredDeeplink)
})
```

The callback function will be called after the SDK receives a deferred deeplink from the server and before the SDK tries to 
open it. 

With another setting on the `AdjustConfig` instance, you have the possibility to tell our SDK to open this 
link or not. You can do this by calling the `setOpenDeferredDeeplink` method:

```js
adjustConfig.setOpenDeferredDeeplink(true)
// Or if you don't want our SDK to open the link:
adjustConfig.setOpenDeferredDeeplink(false)
```

If you do not specify anything, by default, our SDK will try to open the link.

#### <a id="deeplinking-reattribution">Reattribution via deep links

With AdjustBridge, user reattribution with usage of deep links is supported out of box and no additional effort is needed. 
For more information about this topic, please check our [official Android SDK README][android-sdk-reattribution].

[dashboard]:   http://adjust.com
[adjust.com]:  http://adjust.com

[callbacks-guide]:               https://docs.adjust.com/en/callbacks
[special-partners]:              https://docs.adjust.com/en/special-partners
[event-tracking-guide]:          https://docs.adjust.com/en/event-tracking
[android-sdk-deeplinking]:       https://github.com/adjust/android_sdk/blob/master/README.md#deeplinking
[android-sdk-reattribution]:     https://github.com/adjust/android_sdk#deeplinking-reattribution
[android-sdk-basic-integration]: https://github.com/adjust/android_sdk/blob/master/README.md#basic-integration

[bridge_add]:              https://raw.githubusercontent.com/adjust/sdks/master/Resources/android/bridge/bridge_add.png
[bridge_init_js_android]:  https://raw.githubusercontent.com/adjust/sdks/master/Resources/android/bridge/bridge_init_js_android.png
[bridge_install_tracked]:  https://raw.githubusercontent.com/adjust/sdks/master/Resources/android/bridge/bridge_install_tracked.png

## <a id="license">License

The adjust SDK is licensed under the MIT License.

Copyright (c) 2012-2016 adjust GmbH,
http://www.adjust.com

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
