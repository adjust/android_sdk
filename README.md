## Summary

This is the Android SDK of adjust™. You can read more about adjust™ at
adjust.com.

## Example app

There is an example app inside the [`example` directory][example]. You can open
the Android project to see an example on how the adjust SDK can be integrated.

## Basic Installation

These are the minimal steps required to integrate the adjust SDK into your
Android project. We are going to assume that you use Android Studio for your
Android development and target an Android API level 9 (Gingerbread) or later.

If you're using the [Maven Repository][maven] you can start with [step 3](#step3).

### 1. Get the SDK

Download the latest version from our [releases page][releases]. Extract the
archive in a folder of your choice.

### 2. Create the Adjust project

In the Android Studio menu select `File → Import Module...`.

![][import_module]

In the `Source directory` field, locate the folder you extracted in step 1.
Select and choose the folder `./android_sdk/Adjust/adjust`.  Make sure the
module name `:adjust` appears before finishing.

![][select_module]

The `adjust` module should be imported into your Android Studio project
afterwards.

![][imported_module]

### <a id="step3"></a>3. Add the adjust library to your project

Open the `build.gradle` file of your app and find the `dependencies` block. Add
the following line:

```
compile project(":adjust")
```

![][gradle_adjust]

If you are using Maven, add this line instead:

```
compile 'com.adjust.sdk:adjust-android:4.2.3'
```

### 4. Add Google Play Services

Since the 1st of August of 2014, apps in the Google Play Store must use the
[Google Advertising ID][google_ad_id] to uniquely identify devices. To allow
the adjust SDK to use the Google Advertising ID, you must integrate the [Google
Play Services][google_play_services]. If you haven't done this yet, follow
these steps:

1. Open the `build.gradle` file of your app and find the `dependencies` block. Add the
following line:

    ```
    compile 'com.google.android.gms:play-services-analytics:8.3.0'
    ```

    ![][gradle_gps]

2. Skip this step if you are using version 7 or later of Google Play Services:
   In the Package Explorer open the `AndroidManifest.xml` of your Android
   project.  Add the following `meta-data` tag inside the `<application>`
   element.


    ```xml
    <meta-data android:name="com.google.android.gms.version"
               android:value="@integer/google_play_services_version" />
    ```

    ![][manifest_gps]

### 5. Add permissions

In the Package Explorer open the `AndroidManifest.xml` of your Android project.
Add the `uses-permission` tag for `INTERNET` if it's not present already.

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

If you are *not* targeting the Google Play Store, add both of these permissions instead:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

![][manifest_permissions]

If you are using Proguard, add these lines to your Proguard file:

```
-keep class com.adjust.sdk.plugin.MacAddressUtil { 
    java.lang.String getMacAddress(android.content.Context); 
}
-keep class com.adjust.sdk.plugin.AndroidIdUtil { 
    java.lang.String getAndroidId(android.content.Context); 
}
-keep class com.google.android.gms.common.ConnectionResult { 
    int SUCCESS; 
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info 
        getAdvertisingIdInfo (android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId ();
    boolean isLimitAdTrackingEnabled();
}
```

If you are *not* targeting the Google Play Store, you can remove the
`com.google.android.gms` rules.

![][proguard]

### <a id="broadcast_receiver"></a>6. Add broadcast receiver

In your `AndroidManifest.xml` add the following `receiver` tag inside the
`application` tag.

```xml
<receiver
    android:name="com.adjust.sdk.AdjustReferrerReceiver"
    android:exported="true" >
    <intent-filter>
        <action android:name="com.android.vending.INSTALL_REFERRER" />
    </intent-filter>
</receiver>
```

![][receiver]

We use this broadcast receiver to retrieve the install referrer, in order to
improve conversion tracking.

If you are already using a different broadcast receiver for the
`INSTALL_REFERRER` intent, follow [these instructions][referrer] to add the
Adjust receiver.

### 7. Integrate Adjust into your app

To start with, we'll set up basic session tracking.

#### Basic Setup

We recommend using a global android [Application][android_application] class to
initialize the SDK. If don't have one in your app already, follow these steps:

1. Create a class that extends `Application`.
    ![][application_class]

2. Open the `AndroidManifest.xml` file of your app and locate the `<application>` element.
3. Add the attribute `android:name` and set it to the name of your new application class pefixed by a dot.

    In our example app we use an `Application` class named `GlobalApplication`, so the manifest file is configured as:
    ```xml
     <application
       android:name=".GlobalApplication"
       ... >
         ...
    </application>
    ```

    ![][manifest_application]

4. In your `Application` class find or create the `onCreate` method and add the
following code to initialize the adjust SDK:

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
    
    ![][application_config]
    
    Replace `{YourAppToken}` with your app token. You can find this in your
    [dashboard].
    
    Depending on whether you build your app for testing or for production, you must
    set `environment` with one of these values:
    
    ```java
    String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
    String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
    ```
    
    **Important:** This value should be set to `AdjustConfig.ENVIRONMENT_SANDBOX`
    if and only if you or someone else is testing your app. Make sure to set the
    environment to `AdjustConfig.ENVIRONMENT_PRODUCTION` just before you publish
    the app. Set it back to `AdjustConfig.ENVIRONMENT_SANDBOX` when you start
    developing and testing it again.
    
    We use this environment to distinguish between real traffic and test traffic
    from test devices. It is very important that you keep this value meaningful at
    all times! This is especially important if you are tracking revenue.


5. Add a private class that implements the `ActivityLifecycleCallbacks` interface. 
If you don't have access to this interface, your app is targeting an Android api level inferior to 14. 
You will have to update manually each Activity by following this [instructions][activity_resume_pause].
If you had `Adjust.onResume` and `Adjust.onPause` calls on each Activity of your app before,
you should remove them.

    ![][activity_lifecycle_class]

6. Edit the `onActivityResumed(Activity activity)` method and add a call to `Adjust.onResume()`.
Edit the `onActivityPaused(Activity activity)` method and add a call to `Adjust.onPause()`.

    ![][activity_lifecycle_methods]
    
7. Add on the `onCreate()` method where the adjust SDK is configured and add call  `registerActivityLifecycleCallbacks` with a instance of the created `ActivityLifecycleCallbacks` class.

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
            
            //...
        }
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
    ```
    
    ![][activity_lifecycle_register]

#### Adjust Logging

You can increase or decrease the amount of logs you see in tests by calling
`setLogLevel` on your `AdjustConfig` instance with one of the following
parameters:

```java
config.setLogLevel(LogLevel.VERBOSE);   // enable all logging
config.setLogLevel(LogLevel.DEBUG);     // enable more logging
config.setLogLevel(LogLevel.INFO);      // the default
config.setLogLevel(LogLevel.WARN);      // disable info logging
config.setLogLevel(LogLevel.ERROR);     // disable warnings as well
config.setLogLevel(LogLevel.ASSERT);    // disable errors as well
```

### 8. Build your app

Build and run your Android app. In your LogCat viewer you can set the filter
`tag:Adjust` to hide all other logs. After your app has launched you should see
the following Adjust log: `Install tracked`

![][log_message]

## Additional Features

Once you have integrated the adjust SDK into your project, you can take
advantage of the following features.

### 9. Add tracking of custom events

You can use adjust to track any event in your app. Suppose you want to track
every tap on a button. You would have to create a new event token in your
[dashboard]. Let's say that event token is `abc123`. In your button's `onClick`
method you could then add the following lines to track the click:

```java
AdjustEvent event = new AdjustEvent("abc123");
Adjust.trackEvent(event);
```

The event instance can be used to configure the event even more before tracking
it.

### 10. Add callback parameters

You can register a callback URL for your events in your [dashboard]. We will
send a GET request to that URL whenever the event gets tracked. You can add
callback parameters to that event by calling `addCallbackParameter` on the
event instance before tracking it. We will then append these parameters to your
callback URL.

For example, suppose you have registered the URL
`http://www.adjust.com/callback` then track an event like this:

```java
AdjustEvent event = new AdjustEvent("abc123");

event.addCallbackParameter("key", "value");
event.addCallbackParameter("foo", "bar");

Adjust.trackEvent(event);
```

In that case we would track the event and send a request to:

```
http://www.adjust.com/callback?key=value&foo=bar
```

It should be mentioned that we support a variety of placeholders like
`{android_id}` that can be used as parameter values. In the resulting callback
this placeholder would be replaced with the AndroidID of the current device.
Also note that we don't store any of your custom parameters, but only append
them to your callbacks. If you haven't registered a callback for an event,
these parameters won't even be read.

You can read more about using URL callbacks, including a full list of available
values, in our [callbacks guide][callbacks-guide].


### 11. Partner parameters

You can also add parameters to be transmitted to network partners, for the
integrations that have been activated in your adjust dashboard.

This works similarly to the callback parameters mentioned above, but can be
added by calling the `addPartnerParameter` method on your `AdjustEvent` instance.

```java
AdjustEvent event = new AdjustEvent("abc123");

event.addPartnerParameter("key", "value");
event.addPartnerParameter("foo", "bar");

Adjust.trackEvent(event);
```

You can read more about special partners and these integrations in our [guide
to special partners.][special-partners]

### 12. Add tracking of revenue

If your users can generate revenue by tapping on advertisements or making
in-app purchases you can track those revenues with events. Lets say a tap is
worth one Euro cent. You could then track the revenue event like this:

```java
AdjustEvent event = new AdjustEvent("abc123");
event.setRevenue(0.01, "EUR");
Adjust.trackEvent(event);
```

This can be combined with callback parameters of course.

When you set a currency token, adjust will automatically convert the incoming revenues into a reporting revenue of your choice. Read more about [currency conversion here.][currency-conversion]

You can read more about revenue and event tracking in the [event tracking
guide.][event-tracking]

### 13. Set up deep link reattributions

You can set up the adjust SDK to handle deep links that are used to open your
app. We will only read certain adjust specific parameters. This is essential if
you are planning to run retargeting or re-engagement campaigns with deep links.

For each activity that accepts deep links, find the `onCreate` method and add
the folowing call to adjust:

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    Uri data = intent.getData();
    Adjust.appWillOpenUrl(data);
    //...
}
```

### 14. Enable event buffering

If your app makes heavy use of event tracking, you might want to delay some
HTTP requests in order to send them in one batch every minute. You can enable
event buffering with your `AdjustConfig` instance:

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

config.setEventBufferingEnabled(true);

Adjust.onCreate(config);
```

### <a id="attribution_changed_listener"></a>15. Set listener for attribution changes

You can register a listener to be notified of tracker attribution changes. Due
to the different sources considered for attribution, this information can not
be provided synchronously. The simplest way is to create a single anonymous
listener:

Please make sure to consider our [applicable attribution data
policies][attribution-data].

With the `AdjustConfig` instance, before starting the SDK, add the anonymous listener:

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
    @Override
    public void onAttributionChanged(AdjustAttribution attribution) {
    }
});

Adjust.onCreate(config);
```

Alternatively, you could implement the `OnAttributionChangedListener`
interface in your `Application` class and set it as listener:

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);
config.setOnAttributionChangedListener(this);
Adjust.onCreate(config);
```

The listener function will be called when the SDK receives the final attribution
information. Within the listener function you have access to the `attribution`
parameter. Here is a quick summary of its properties:

- `String trackerToken` the tracker token of the current install.
- `String trackerName` the tracker name of the current install.
- `String network` the network grouping level of the current install.
- `String campaign` the campaign grouping level of the current install.
- `String adgroup` the ad group grouping level of the current install.
- `String creative` the creative grouping level of the current install.
- `String clickLabel` the click label of the current install.

### 16. Disable tracking

You can disable the adjust SDK from tracking any activities of the current
device by calling `setEnabled` with parameter `false`. This setting is
remembered between sessions.

```java
Adjust.setEnabled(false);
```

You can check if the adjust SDK is currently enabled by calling the function
`isEnabled`. It is always possible to activate the adjust SDK by invoking
`setEnabled` with the enabled parameter as `true`.

### 17. Offline mode

You can put the adjust SDK in offline mode to suspend transmission to our servers, 
while retaining tracked data to be sent later. While in offline mode, all information is saved
in a file, so be careful not to trigger too many events while in offline mode.

You can activate offline mode by calling `setOfflineMode` with the parameter `true`.

```java
Adjust.setOfflineMode(true);
```

Conversely, you can deactivate offline mode by calling `setOfflineMode` with `false`.
When the adjust SDK is put back in online mode, all saved information is send to our servers 
with the correct time information.

Unlike disabling tracking, this setting is *not remembered*
bettween sessions. This means that the SDK is in online mode whenever it is started,
even if the app was terminated in offline mode.

### 18. Device IDs

Certain services (such as Google Analytics) require you to coordinate Device and Client IDs in order to prevent duplicate reporting. 

To obtain the IDFA, call the function `idfa`:

If you need to obtain the Google Advertising ID, There is a restriction that only allows it to be read in a background thread.
 If you call the function `getGoogleAdId` with the context and a `OnDeviceIdsRead` instance, it will work in any situation

```java
Adjust.getGoogleAdId(this, new OnDeviceIdsRead() {
    @Override
    public void onGoogleAdIdRead(String googleAdId) {
        // ...
    }
});
```

Inside the method `onGoogleAdIdRead` of the `OnDeviceIdsRead` instance, you will have access to Google Advertising ID as the variable `googleAdId`.

## Troubleshooting

### I'm seeing the "Session failed (Ignoring too frequent session. ...)" error.

This error typically occurs when testing installs. Uninstalling and reinstalling the app is not 
enough to trigger a new install. The servers will determine that the SDK has lost its 
locally aggregated session data and ignore the erroneous message, given the information 
available on the servers about the device.

This behaviour can be cumbersome during tests, but is necessary in order to have the sandbox 
behaviour match production as much as possible.

You can reset the session data of the device in our servers. Check the error 
message in the logs:

```
Session failed (Ignoring too frequent session. Last session: YYYY-MM-DDTHH:mm:ss, this session: YYYY-MM-DDTHH:mm:ss, interval: XXs, min interval: 20m) (app_token: {yourAppToken}, adid: {adidValue})
```

With the `{yourAppToken}` and `{adidValue}` values filled in below, open the following link:

```
http://app.adjust.com/forget_device?app_token={yourAppToken}&adid={adidValue}
```

When the device is forgotten, the link just returns `Forgot device`. If the device was 
already forgotten or the values were incorrect, the link returns `Device not found`.

### Is my broadcast receiver capturing the install referrer?

If you followed the instructions in the [guide](#broadcast_receiver), the broadcast receiver 
should be configured to send the install referrer to our SDK and to our servers.

You can test this by triggering a test install referrer manually. 
Replace `com.your.appid` with your app ID and run the following command with the 
[adb](http://developer.android.com/tools/help/adb.html) tool that comes with Android Studio:

```
adb shell am broadcast -a com.android.vending.INSTALL_REFERRER -n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver --es "referrer" "adjust_reftag%3Dabc1234%26tracking_id%3D123456789%26utm_source%3Dnetwork%26utm_medium%3Dbanner%26utm_campaign%3Dcampaign"
```

If you are already using a different broadcast receiver for the `INSTALL_REFERRER` intent and followed this [guide][referrer], replace `com.adjust.sdk.AdjustReferrerReceiver` with your broadcast receiver.

You can also remove the `-n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver` parameter so that all the apps in the device will receive the `INSTALL_REFERRER` intent.

If you set the log level to `verbose`, you should be able to see the log from reading 
the referrer:

````
V/Adjust: Reading query string (adjust_reftag=abc1234&tracking_id=123456789&utm_source=network&utm_medium=banner&utm_campaign=campaign) from reftag
```

And a click package added to the SDK's package handler:

```
V/Adjust: Path:      /sdk_click
    ClientSdk: android4.2.3
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

If you perform this test before launching the app, you won't see the package being sent. 
The package will be sent once the app is launched.

### Can I trigger an event at application launch?

Not how you might intuitively think. The `onCreate` method on the global `Application` class is called not only
at application launch, but also when a system or application event is captured by the app.

Our SDK is prepared for initialization at this time, but not actually started. 
This will only happen when an activity is started, i.e., when a user actually launches the app.

That's why triggering an event at this time will not do what you would expect.
Such calls will start the adjust SDK and send the events, even when the app was not launched by the user - 
at a time that depends on external factors of the app. 

Triggering events at application launch will thus result in inaccuracies in the number 
of installs and sessions tracked.

If you want to trigger an event after the install, use the [attribution changed listener](#attribution_changed_listener).

If you want to trigger an event when the app is launched, use the `onCreate` method of the Activity which is started.

[dashboard]:     http://adjust.com
[releases]:      https://github.com/adjust/adjust_android_sdk/releases
[import_module]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/01_import_module.png
[select_module]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/02_select_module.png
[imported_module]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/03_imported_module.png
[gradle_adjust]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/04_gradle_adjust.png
[gradle_gps]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/05_gradle_gps.png
[manifest_gps]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/06_manifest_gps.png
[manifest_permissions]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/07_manifest_permissions.png
[proguard]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/08_proguard_new.png
[receiver]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/09_receiver.png
[application_class]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/11_application_class.png
[manifest_application]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/12_manifest_application.png
[application_config]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/13_application_config.png
[log_message]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/15_log_message.png
[activity_lifecycle_class]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/16_activity_lifecycle_class.png
[activity_lifecycle_methods]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/17_activity_lifecycle_methods.png
[activity_lifecycle_register]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/18_activity_lifecycle_register.png

[referrer]:      doc/referrer.md
[attribution-data]:     https://github.com/adjust/sdks/blob/master/doc/attribution-data.md
[google_play_services]: http://developer.android.com/google/play-services/setup.html
[android_application]:  http://developer.android.com/reference/android/app/Application.html
[application_name]:     http://developer.android.com/guide/topics/manifest/application-element.html#nm
[google_ad_id]:         https://support.google.com/googleplay/android-developer/answer/6048248?hl=en
[callbacks-guide]:      https://docs.adjust.com/en/callbacks
[event-tracking]:       https://docs.adjust.com/en/event-tracking
[special-partners]:     https://docs.adjust.com/en/special-partners
[maven]:                http://maven.org
[example]:              https://github.com/adjust/android_sdk/tree/master/Adjust/example
[currency-conversion]:  https://docs.adjust.com/en/event-tracking/#tracking-purchases-in-different-currencies
[activity_resume_pause]: doc/activity_resume_pause.md

## License

The adjust SDK is licensed under the MIT License.

Copyright (c) 2012-2015 adjust GmbH,
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
