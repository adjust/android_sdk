## OAID plugin

For specific markets, OAID can be used for attribution on Android. In order to use this feature, please complete the required steps within your Adjust Dashboard and then use this plugin.

This OAID plugin respects the behavior of the Adjust Android SDK in terms of device ID reading **while additionally** allowing the Adjust SDK to read the OAID values of a device.

**Important:** This Adjust plugin is meant to be used only in apps that are **NOT being published to the Google Play Store**.

Before using this plugin, please make sure that you have read official [Android SDK README][readme] and successfully completed Adjust SDK integration into your app. After that, please make sure to perform these additional steps if you want to enable Adjust SDK to collect and track OAID identifier.

### Add OAID plugin to your app

If you are using Maven, add the following OAID plugin dependency to your `build.gradle` file next to already existing dependency to Adjust SDK:

```
implementation 'com.adjust.sdk:adjust-android:4.18.2'
implementation 'com.adjust.sdk:adjust-android-oaid:4.18.2'
```

You can also add Adjust OAID plugin as JAR file which can be downloaded from our [releases page][releases].

### Proguard settings

Official `README` suggests which rules should be added assuming that your app will be published in Google Play Store. If this is not the case, feel free to remove all the rules related to Google Play Services and install referrer libraries. In that case, rules you need to add should look like this:

```
-keep public class com.adjust.sdk.** { *; }
```

### Use the plugin

Finally, in order to read OAID values, you need to call `AdjustOaid.readOaid()` before starting the SDK:

```java
AdjustOaid.readOaid();

// ...

Adjust.onCreate(config);
```

You can call a method `AdjustOaid.doNotReadOaid()` to stop the SDK from reading OAID values then onwards.


[readme]:    ../../../README.md
[releases]:  https://github.com/adjust/android_sdk/releases