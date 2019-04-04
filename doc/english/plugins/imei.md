## IMEI plugin

For specific markets, IMEI and MEID can be used for attribution on Android. In order to use this feature, please complete the [required steps][imei-doc] within your Adjust Dashboard and then use this plugin.

This IMEI plugin respects the behavior of the Adjust Android SDK in terms of device ID reading **while additionally** allowing the Adjust SDK to read the IMEI and MEID values of a device.

**Important:** This Adjust plugin is meant to be used only in apps that are **NOT being published to the Google Play Store**.

Before using this plugin, please make sure that you have read official [Android SDK README][readme] and successfully completed Adjust SDK integration into your app. After that, please make sure to perform these additional steps if you want to enable Adjust SDK to collect and track IMEI identifier.

### Add IMEI plugin to your app

If you are using Maven, add the following IMEI plugin dependency to your `build.gradle` file next to already existing dependency to Adjust SDK:

```
implementation 'com.adjust.sdk:adjust-android:4.16.0'
implementation 'com.adjust.sdk:adjust-android-imei:4.16.0'
```

You can also add Adjust IMEI plugin as JAR file which can be downloaded from our [releases page][releases].

### Add permission

Add the following permission, if it is not already present in your `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

Remember that after `Android 6.0` it might be necessary to [request app permission](https://developer.android.com/training/permissions/requesting) if the Android OS has not already been altered to avoid it.

### Proguard settings

Official `README` suggests which rules should be added assuming that your app will be published in Google Play Store. If this is not the case, feel free to remove all the rules related to Google Play Services and install referrer libraries. In that case, rules you need to add should look like this:

```
-keep public class com.adjust.sdk.** { *; }
```

### Use the plugin

Finally, in order to read IMEI and MEID values, you need to call `AdjustImei.readImei()` before starting the SDK:

```java
AdjustImei.readImei();

// ...

Adjust.onCreate(config);
```

You can call a method `AdjustImei.doNotReadImei()` to stop the SDK from reading IMEI and MEID values.

### Final note

**Please keep in mind** that IMEI and MEID are persistent identifiers and that it is your responsibility to ensure that the collection and processing of this personal data from your app's end-users is lawful.

[readme]:    ../../../README.md
[releases]:  https://github.com/adjust/android_sdk/releases
[imei-doc]:  https://docs.adjust.com/en/imei-and-meid-attribution-for-android
[gps-adid]:  ../gps_adid.md
