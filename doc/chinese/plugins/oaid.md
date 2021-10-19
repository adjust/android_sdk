## OAID 插件

OAID 是由移动安全联盟 (MSA) 推出的广告 ID。所有中国国内的手机厂商都应当提供此 ID。利用 OAID，您可以在谷歌服务不可用的市场中实现安卓设备的跟踪和归因。

使用 OAID 插件后，Adjust 安卓 SDK 除读取默认搜索的设备 ID 外，还将可以读取设备的 OAID 值。运行 MSA SDK 的所有设备及搭载 HMS (华为移动服务) 的华为设备均可进行 OAID 的读取。 

在开始之前，请确保您已阅读官方的 [安卓 SDK 自述文件][自述文件]，并已成功将 Adjust SDK 集成到您的应用中。

如欲启用 Adjust SDK 以收集和跟踪 OAID，请按以下步骤操作。如只需使用插件读取华为设备的 OAID，则您可以跳过将 MSA SDK 集成至应用 的步骤。

### 将 OAID 插件添加至您的应用

如果您使用的是 Maven，请将以下 OAID 插件依赖项添加到现有 Adjust SDK 依赖项旁的 `build.gradle` 文件：

```
implementation 'com.adjust.sdk:adjust-android:4.28.6'
implementation 'com.adjust.sdk:adjust-android-oaid:4.28.6'
```

您还可以将 Adjust OAID 插件作为 JAR 文件进行添加，该文件可从我们的 [版本页面][releases] 下载。

### 将 MSA SDK 添加至您的应用

注意：对于华为设备，您无需通过添加 MSA SDK 来读取 OAID。OAID 插件可直接调用华为移动服务来读取该值。

如需启用 OAID 插件读取使用 MSA SDK 的 OAID 值，请将 MSA SDK (AAR 文件) 复制到项目的 libs 目录中并设置依赖。您还需要将 Supplierconfig.json 复制到项目的assets目录中。

您可以在 [此处][msasdk] 找到 MSA SDK 和相关详细说明。


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
[msasdk]:  http://www.msa-alliance.cn/col.jsp?id=120
