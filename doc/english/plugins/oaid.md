## OAID plugin

OAID is a new advertising ID available in devices with HMS (Huawei Mobile Service) version 2.6.2 or later. You can  use it to attribute and track Android devices in markets where Google Play Services is not available. 

The OAID plugin enables the Adjust Android SDK to read a device’s OAID value *in addition* to the other device IDs it searches for by default. 

Before getting started, make sure you have read the official [Android SDK README][readme] and successfully integrated the Adjust SDK into your app.

To enable the Adjust SDK to collect and track OAID, follow these steps.

### Add the OAID plugin to your app

If you are using Maven, add the following OAID plugin dependency to your `build.gradle` file next to the existing Adjust SDK dependency:

```
implementation 'com.adjust.sdk:adjust-android:4.18.3'
implementation 'com.adjust.sdk:adjust-android-oaid:4.18.3'
```

You can also add the Adjust OAID plugin as JAR file, which you can download from our [releases page][releases].

### Proguard settings

If you’re using Proguard and will not publish your app in the Google Play Store, you can remove all of the rules related to Google Play Services and install referrer libraries in the [SDK README][readme proguard].

Use all `com.adjust.sdk` package rules like this:

```
-keep public class com.adjust.sdk.** { *; }
```

### Use the plugin

To read OAID values, call `AdjustOaid.readOaid()` before starting the SDK:

```java
AdjustOaid.readOaid();

// ...

Adjust.onCreate(config);
```

To stop the SDK from reading OAID values, call `AdjustOaid.doNotReadOaid()`.


[readme]:    ../../../README.md
[releases]:  https://github.com/adjust/android_sdk/releases
[readme proguard]: https://github.com/adjust/android_sdk#qs-proguard
