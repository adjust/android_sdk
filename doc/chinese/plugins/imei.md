## IMEI插件

对于特定市场，IMEI 及 MEID 可被用于安卓的归因。如希望启用该功能，请按照我们[文档][imei_doc]中的指引，于 Adjust 控制面板中完成必要的设置，然后添加本插件。

关于设备 ID 的读取，本 IMEI 插件依然遵循 Adjust 安卓 SDK 的默认逻辑，并在此基础上**额外地**允许 Adjust 安卓 SDK 读取设备的 IMEI 和 MEID 值。
	
**重要事项：** 本 IMEI 插件**仅**适用于**非Google Play商店**中发布的应用。

在使用此插件前，请确保已经阅读 Adjust 官方 [Android SDK 的集成指南][readme]，并成功将 Adjust SDK 集成于您的应用中。然后，请按照以下额外步骤设置，以使 Adjust SDK 开始收集并跟踪 IMEI 及 MEID。

### 添加 IMEI 插件至应用中

如果您使用的是 Maven，请在您的 `build.gradle` 文件中，于已有的Adjust SDK 依赖处，添加 IMEI 插件的依赖，如：

```
implementation 'com.adjust.sdk:adjust-android:4.16.0'
implementation 'com.adjust.sdk:adjust-android-imei:4.16.0'
```

您也可以在我们的[发布页面][releases]中下载和添加 JAR 文件形式的 Adjust IMEI 插件。

### 添加权限

如果您的 `AndroidManifest.xml` 文件中尚未包含以下权限，请添加：

```xml
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

请注意，对于安卓6.0后的版本，有可能需要 [请求应用权限](https://developer.android.com/training/permissions/requesting)，除非安卓操作系统已被更改。

### Proguard 设置

官方 `README` 对需要发布到 Google Play 商店的应用的 Proguard 规则进行了说明。如果该情况不适用于您的应用，您可以移除与 Google Play 服务库以及 Install Referrer（安装引荐来源）库相关的规则，最后设置将为：

```
-keep public class com.adjust.sdk.** { *; }
```

### 使用插件

最后，请在启动 SDK 前，调用 `AdjustImei.readImei()` 以读取 IMEI 和 MEID 值：

```java
AdjustImei.readImei();

// ...

Adjust.onCreate(config);
```

如要 SDK 停止读取 IMEI 和 MEID 值，可调用 `AdjustIMEI.doNotReadImei()` 方法。

### 结语

**请务必注意** IMEI 和 MEID 是持久识别符（persistent identifier），这将是您的责任确保从您终端用户处收集和处理该项个人信息是合法的。

[readme]:  ../../README.md
[releases]: https://github.com/adjust/adjust_android_sdk/releases
[imei_doc]: https://docs.adjust.com/zh/imei-and-meid-attribution-for-android
[gps_adid]: https://github.com/adjust/android_sdk/blob/master/doc/english/gps_adid.md

