## OAID 插件

OAID 是一款新的广告 ID，适用于搭载华为移动服务 (Huawei Mobile Service, HMS) 版本 2.6.2 或更高版本的设备。在 Google Play 服务不适用的市场中，您可以使用此插件对安卓设备进行归因和跟踪。 

OAID 插件支持 Adjust 安卓 SDK 读取设备的 OAID 值 *以及*其默认搜索的其他设备 ID。 

在开始之前，请确保您已阅读官方的 [安卓 SDK 自述文件][readme]，并已成功将 Adjust SDK 集成到您的应用中。

如欲启用 Adjust SDK 以收集和跟踪 OAID，请按以下步骤操作。

### 将 OAID 插件添加至您的应用

如果您使用的是 Maven，请将以下 OAID 插件依赖项添加到现有 Adjust SDK 依赖项旁的 `build.gradle` 文件：

```
implementation 'com.adjust.sdk:adjust-android:4.18.2'
implementation 'com.adjust.sdk:adjust-android-oaid:4.18.2'
```

您还可以将 Adjust OAID 插件作为 JAR 文件进行添加，该文件可从我们的 [版本页面][releases] 下载。

### ProGuard 设置

如果您使用的是 ProGuard 并且不会在 Google Play 商店中发布您的应用，则您可以在 [SDK 自述文件][readme proguard] 中删除所有与 Google Play 服务和 Install Referrer 库相关的规则。

按如下方式使用所有 `com.adjust.sdk` 包规则：

```
-keep public class com.adjust.sdk.** { *; }
```

### 使用插件

如欲读取 OAID 值，请在启动 SDK 前调用 `AdjustOaid.readOaid()`：

```java
AdjustOaid.readOaid();

// ...

Adjust.onCreate(config);
```

如欲阻止 SDK 读取 OAID 值，请调用 `AdjustOaid.doNotReadOaid()`.


[readme]:  ../../chinese/README.md
[releases]: https://github.com/adjust/android_sdk/releases
[readme proguard]:  ../../chinese/README.md#qs-proguard
