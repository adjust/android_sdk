## OAID plugin

OAID is a new advertising ID available in devices either with HMS (Huawei Mobile Service) version 2.6.2 (& later) or which are compatible to MSA (Mobile Security Alliance) sdk. You can use it to attribute and track Android devices in markets where Google Play Services is not available. 

The OAID plugin enables the Adjust Android SDK to read a device’s OAID value *in addition* to the other device IDs it searches for by default. 

Before getting started, make sure you have read the official [Android SDK README][readme] and successfully integrated the Adjust SDK into your app.

To enable the Adjust SDK to collect and track OAID, follow these steps.

### Add the OAID plugin to your app

If you are using Maven, add the following OAID plugin dependency to your `build.gradle` file next to the existing Adjust SDK dependency:

```
implementation 'com.adjust.sdk:adjust-android:4.19.0'
implementation 'com.adjust.sdk:adjust-android-oaid:4.19.0'
```

You can also add the Adjust OAID plugin as JAR file, which you can download from our [releases page][releases].

### Add the MSA sdk to your app

You can find [here][msasdk] the official instructions how to integrate the MSA sdk.  Below are just the consolidated steps.  But please note that the official instructions might be updated in the meanwhile.

If you want OAID plugin to read oaid using MSA sdk, copy the MSA sdk (AAR file) to the libs directory of your project and set the dependency.

You also need to copy the supplierconfig.json to the assets directory of your project.  In the supplierconfig.json, you need to set the appId for each of the supplier.  Example below:
```
{
  "supplier":{
    "vivo":{
      "appid":"<value>"
    },
    "xiaomi":{
      "appid":"<value>"
    },
    "huawei":{
      "appid":"<value>"
    },
    "oppo":{
      "appid":"<value>"
    }
  }
}
```
Here, in order to get the appId, you need to register your app into corresponding supplier's app store.

Additionally, since MSA sdk AAR includes the native SO files, 

1. You can add ABI filters to the build gradle.
```
ndk {
abiFilters 'armeabi-v7a','x86','arm64-v8a','x86_64','armeabi'
}
```

2. You can opt not to strip symbols from the SO files.
```
packagingOptions { 
doNotStrip "*/armeabi-v7a/*.so"
doNotStrip "*/x86/*.so" 
doNotStrip "*/arm64-v8a/*.so" 
doNotStrip “*/x86_64/*.so" 
doNotStrip "armeabi.so"
}
```

### Proguard settings

If you’re using Proguard and will not publish your app in the Google Play Store, you can remove all of the rules related to Google Play Services and install referrer libraries in the [SDK README][readme proguard].

Use all `com.adjust.sdk` package rules like this:

```
-keep public class com.adjust.sdk.** { *; }
```

Also, if you are adding the msa sdk AAR as dependency, then add the following rules:

```
-keep class com.bun.miitmdid.core.** { *; }
```

### Use the plugin

To read OAID values, call `AdjustOaid.readOaid(applicationContext)` before starting the SDK:

```java
AdjustOaid.readOaid(applicationContext);

// ...

Adjust.onCreate(config);
```

To stop the SDK from reading OAID values, call `AdjustOaid.doNotReadOaid()`.


[readme]:    ../../../README.md
[releases]:  https://github.com/adjust/android_sdk/releases
[readme proguard]: https://github.com/adjust/android_sdk#qs-proguard
[msasdk]:  https://dev.vivo.com.cn/documentCenter/doc/253
