**该指南即将过期。**

该自述文件即将过期。现在，您可以前往我们的帮助中心查看此 SDK 文档。

* [English][en-helpcenter]
* [中文][zh-helpcenter]
* [日本語][ja-helpcenter]
* [한국어][ko-helpcenter]

## 摘要

这是 Adjust™ 的安卓 SDK 包。您可以在 [adjust.com] 了解更多有关 Adjust™ 的信息。

阅读本文的其他语言版本：[English][en-readme]、[中文][zh-readme]、[日本語][ja-readme]、[한국어][ko-readme]。

## 目录

### 快速入门

   * [应用示例](#qs-example-apps)
   * [入门指南](#qs-getting-started)
      * [添加 SDK 至您的项目](#qs-add-sdk)
      * [添加 Google Play 服务](#qs-gps)
      * [添加权限](#qs-permissions)
      * [Proguard 设置](#qs-proguard)
      * [Install referrer](#qs-install-referrer)
         * [Google Play Referrer API](#qs-gpr-api)
         * [Google Play Store intent](#qs-gps-intent)
         * [华为 Referrer API](#qs-huawei-referrer-api)
   * [集成 SDK 至您的应用](#qs-integrate-sdk)
      * [基本设置](#qs-basic-setup)
         * [原生应用 SDK](#qs-basic-setup-native)
         * [Web Views SDK](#qs-basic-setup-web)
      * [会话跟踪](#qs-session-tracking)
         * [API level 14 及以上版本](#qs-session-tracking-api-14)
         * [API level 9 - 13 版本](#qs-session-tracking-api-9)
      * [SDK 签名](#qs-sdk-signature)
      * [Adjust 日志记录](#qs-adjust-logging)
      * [构建您的应用](#qs-build-the-app)

### 深度链接

   * [深度链接概览](#dl)
   * [标准深度链接场景](#dl-standard)
   * [延迟深度链接场景](#dl-deferred)
   * [通过深度链接的再归因](#dl-reattribution)
   * [链接解析](#link-resolution)

### 事件跟踪

   * [跟踪事件](#et-tracking)
   * [跟踪收入](#et-revenue)
   * [收入重复数据删除](#et-revenue-deduplication)
   * [应用收入验证](#et-purchase-verification)

### 自定义参数

   * [自定义参数概览](#cp)
   * [事件参数](#cp-event-parameters)
      * [事件回传参数](#cp-event-callback-parameters)
      * [事件合作伙伴参数](#cp-event-partner-parameters)
      * [事件回传标识符](#cp-event-callback-id)
   * [会话参数](#cp-session-parameters)
      * [会话回传参数](#cp-session-callback-parameters)
      * [会话合作伙伴参数](#cp-session-partner-parameters)
      * [延迟启动](#cp-delay-start)

### 其他功能

   * [推送标签 (卸载跟踪)](#af-push-token)
   * [归因回传](#af-attribution-callback)
   * [广告收入跟踪](#af-ad-revenue)
   * [订阅跟踪](#af-subscriptions)
   * [会话与事件回传](#af-session-event-callbacks)
   * [用户归因](#af-user-attribution)
   * [设备 ID](#af-device-ids)
      * [Google Play 服务广告 ID](#af-gps-adid)
      * [Amazon 广告 ID](#af-amazon-adid)
      * [Adjust 设备 ID](#af-adid)
   * [预安装应用](#af-preinstalled-apps)
   * [离线模式](#af-offline-mode)
   * [禁用跟踪](#af-disable-tracking)
   * [事件缓冲](#af-event-buffering)
   * [后台跟踪](#af-background-tracking)
   * [GDPR 被遗忘权](#af-gdpr-forget-me)
   * [第三方分享](#af-third-party-sharing)
      * [禁用第三方分享](#af-disable-third-party-sharing)
      * [启用第三方分享](#af-enable-third-party-sharing)
   * [许可监测](#af-measurement-consent)

### 测试与故障排查

   * [显示 "session failed (Ignoring too frequent session...)" 出错信息](#tt-session-failed)
   * [我的广播接收器是否能成功获取 install referrer？](#tt-broadcast-receiver)
   * [我能否在应用激活时触发事件？](#tt-event-at-launch)

### 许可


## 快速入门

### <a id="qs-example-apps"></a>应用示例

在 [`example-app-java`][example-java]、[`example-app-kotlin`][example-kotlin] 和 [`example-app-keyboard`][example-keyboard] 目录中，您可以找到安卓示例应用；在 [`example-webbridge` ][example-webbridge] 目录中，您可以找到使用 web view 的示例应用；[`example-app-tv`][example-tv] 目录中，您可以找到安卓 TV 示例应用。您可以打开安卓项目查看这些示例，了解如何集成 Adjust SDK。

### <a id="qs-getting-started"></a>快速入门

此处为将 Adjust SDK 集成进安卓应用时需进行的最低要求步骤。我们假定您使用 Android Studio 进行安卓开发。Adjust SDK 集成支持的最低安卓 API 级别为 **9 (Gingerbread)**。

### <a id="qs-add-sdk"></a>添加 SDK 至您的项目

如果您使用的是 Maven，请添加下行到您的 `build.gradle` 文件：

```gradle
implementation 'com.adjust.sdk:adjust-android:4.28.7'
implementation 'com.android.installreferrer:installreferrer:2.2'
```

如果您想在应用 web view 中使用 Adjust SDK，请也添加下列附加依赖项：

```gradle
implementation 'com.adjust.sdk:adjust-android-webbridge:4.28.7'
```

**请注意:** web view 扩展支持的最低安卓 API 级别为 17 (Jelly Bean)。

您还可以将 Adjust SDK 和 web view 扩展作为 JAR 文件来添加，这可从我们的[发布页面][releases]中下载。

### <a id="qs-gps"></a>添加 Google Play 服务

自 2014 年 8 月 1 日起，Google Play 商店中的应用必须使用 [Google 广告 ID] 来对设备进行唯一标识。为了让 Adjust SDK 能使用 Google 广告 ID，请务必集成 [Google Play 服务]。如果您尚未完成该集成，请将 dependency 加入 Google Play 服务库中，具体请将以下依赖项添加到应用 `build.gradle` 文件的 `dependencies` 块中：

```gradle
implementation 'com.google.android.gms:play-services-ads-identifier:17.0.0'
```

**请注意**：Adjust SDK 未与 Google Play 服务库中 `play-services-analytics` 的任何特定版本绑定。您可以使用最新版本的库，也可以按需要使用任意其他版本。

### <a id="qs-permissions"></a>添加权限

Adjust SDK 需要下列权限。如果尚未添加，请将权限加入您的 `AndroidManifest.xml` 文件中：

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

如果您的**发布目标非 Google Play 商店**，请同时添加以下权限：

```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
```

### <a id="qs-proguard"></a>Proguard 设置

如果您使用的是 Proguard，请将如下代码行添加至您的 Proguard 文件：

```
-keep class com.adjust.sdk.**{ *; }
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

如果您的**发布目标非 Google Play 商店**，请使用下列 `com.adjust.sdk` 包规则：

```
-keep public class com.adjust.sdk.** { *; }
```

### <a id="qs-install-referrer"></a>Install referrer

为了将应用的安装归因到正确的来源，Adjust 需要获取有关 **install referrer** (安装引荐来源) 的信息。这可以通过 **Google Play Referrer API** 或使用广播接收器（broadcast receiver) 捕捉 **Google Play Store intent** 来获得。

**重要提示**：Google 引入 Google Play Referrer API 是为了提供一种更可靠、更安全的方法，以获取 Install Referrer 信息并帮助归因服务商对抗点击劫持。我们**强烈建议**您的应用支持 Google Play Referrer API。相比之下，通过 Google Play Store intent 获取 install referrer 的方法则安全性较低。Google Play Store intent 暂时与新的 Google Play Referrer API 并行存在，但将来会被弃用。

#### <a id="qs-gpr-api"></a>Google Play Referrer API

要在应用中支持 Google Play Referrer API，请务必按照[添加 SDK 至您的项目](#qs-add-sdk)一节中的说明正确操作，并确保将下列代码行加入`build.gradle` 文件中：

```
implementation 'com.android.installreferrer:installreferrer:2.2'
```

请仔细遵循[Proguard 设置](#qs-proguard)一节中的说明操作。请确保您已经添加了说明中所提及的全部规则，尤其是本功能必需的规则：

```
-keep public class com.android.installreferrer.** { *; }
```

要支持该功能，您需要采用 **Adjust SDK v4.12.0 或更新版本**。

#### <a id="qs-gps-intent"></a>Google Play Store intent

**请注意**：Google [宣布](https://android-developers.googleblog.com/2019/11/still-using-installbroadcast-switch-to.html)，2020 年 3 月 1 日起弃用通过 `INSTALL_REFERRER` intent 发送 referrer 信息的做法。如果您在使用上述方法访问 referrer 信息，请迁移至 [Google Play Referrer API](#qs-gpr-api)方法。

您应当使用广播接收器捕捉 Google Play Store `INSTALL_REFERRER` intent。如果您**未使用自己的广播接收器**来接收 `INSTALL_REFERRER` intent ，那么请在 `AndroidManifest.xml` 的 `application` 标签中添加如下 `receiver` 标签。

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

我们使用这个广播接收器来检索 install referrer，并将其传送给后端。

如果您使用不同的广播接收器接收 `INSTALL_REFERRER` intent，请按照[此说明][referrer]，以正确方式向 Adjust 广播接收器发送 ping 命令。

#### <a id="qs-huawei-referrer-api"></a>华为 Referrer API

从版本 4.21.1 开始，Adjust SDK 将支持对装有华为 App Gallery 10.4 或更新版本的设备进行安装跟踪。无需其他集成步骤，就可以开始使用华为 Referrer API。

### <a id="qs-integrate-sdk"></a>集成 SDK 至您的应用

我们从设置基本会话跟踪开始。

### <a id="qs-basic-setup"></a>基本设置

如果您要将 SDK 集成至原生应用，请按照[原生应用 SDK](#qs-basic-setup-native)一节中的说明操作。如果您要将 SDK 集成到使用 web view 的应用中，请按照下方 [Web view SDK](#qs-basic-setup-web) 中的说明操作。

#### <a id="qs-basic-setup-native"></a>原生应用 SDK

我们建议您使用全局安卓[应用程序][android-application]类进行 SDK 初始化。如果应用中还没有此类，请按照下列步骤操作：

- 创建一个扩展 `Application` 的类。
- 打开应用的 `AndroidManifest.xml` 文件，找到 `<application>` 元素。
- 添加 `android:name` 属性，将其设置为您的新应用程序类的名称。

    在示例应用中，我们将` Application` 类命名为 `GlobalApplication` 。因此，manifest 文件会被设置为：
    ```xml
     <application
       android:name=".GlobalApplication"
       <!-- ... -->
     </application>
    ```

- 在 `Application` 类中，找到或创建 `onCreate` 方法。添加下列代码，初始化 Adjust SDK：

    ```java
    import com.adjust.sdk.Adjust;
    import com.adjust.sdk.AdjustConfig;

    public class GlobalApplication extends Application {
        @Override
        public void onCreate() {
            super.onCreate();

            string appToken = "{YourAppToken}";
            String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
            AdjustConfig config = new AdjustConfig(this, appToken, environment);
            Adjust.onCreate(config);
        }
    }
    ```

用您的应用识别码 (app token) 替换 `{YourAppToken}`。您可以在[控制面板]上找到该识别码。

下一步，您必须将 `environment` (环境模式) 设为 sandbox 或生产模式：

```java
String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
```

**重要提示**只有在您或其他人正在测试您的应用时，才应将该值设为 `AdjustConfig.ENVIRONMENT_SANDBOX` 。在发布应用之前，请务必将环境改设为`AdjustConfig.ENVIRONMENT_PRODUCTION`。再次研发和测试时，请将其重新设置为`AdjustConfig.ENVIRONMENT_SANDBOX`。

我们按照设置的环境来区分真实流量和来自测试设备的测试流量。非常重要的是，您必须始终根据您的目前状态更新环境！

#### <a id="qs-basic-setup-web"></a>Web Views SDK

在获得 `WebView` 对象的引用后：

- 调用 `webView.getSettings().setJavaScriptEnabled(true)`,以便在 web view 中启用 Javascript
- 调用 `AdjustBridge.registerAndGetInstance(getApplication(), webview)`，来启动 `AdjustBridgeInstance` 默认实例。
- 这也会将 Adjust 网桥注册为 web view 的 Javascript 接口
- 如有必要，请调用 `AdjustBridge.setWebView()` 来设置新的`WebView`。  
- 调用 `AdjustBridge.unregister()` ，以取消注册 `AdjustBridgeInstance` 和 `WebView`.  

完成这些步骤后，您的 activity 应该以如下形式呈现：

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

        AdjustBridge.registerAndGetInstance(getApplication(),webview);
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

完成以上步骤后，您就成功将 Adjust bridge 添加进自己的应用了。Javascript bridge 现已启用，可在 Adjust 原生安卓 SDK 和您的页面间进行通讯，它会被加载进 web view 中。

在您的 HTML 文件中，导入位于 assets 文件夹根目录中的 Adjust Javascript 文件。如果您的 HTML 文件也存在，请按如下方式导入：

```html
<script type="text/javascript" src="adjust.js"></script>
<script type="text/javascript" src="adjust_event.js"></script>
<script type="text/javascript" src="adjust_third_party_sharing.js"></script>
<script type="text/javascript" src="adjust_config.js"></script>
```

一旦您添加对 Javascript 文件的引用后，在 HTML 文件中使用它们来初始化 Adjust SDK：

```js
let yourAppToken = '{YourAppToken}';
let environment = AdjustConfig.EnvironmentSandbox;
let adjustConfig = new AdjustConfig(yourAppToken, environment);

Adjust.onCreate(adjustConfig);
```

用您的应用识别码 (app token) 替换 `{YourAppToken}`。您可以在[控制面板]上找到该应用识别码。

然后，取决于您处于测试或是生产模式，请将 `environment` (环境模式)设为以下对应值：

```js
let environment = AdjustConfig.EnvironmentSandbox;
let environment = AdjustConfig.EnvironmentProduction;
```

**重要提示:** 只有在您或其他人正在测试您的应用时，才应将该值设为 `AdjustConfig.EnvironmentSandbox` 。在发布应用之前，请务必将环境设置为``AdjustConfig.EnvironmentProduction` 。如果再次开始开发和测试，请将其重新设置为`AdjustConfig.EnvironmentSandbox` 。

我们按照设置的环境来区分真实流量和来自测试设备的测试流量。非常重要的是，您必须始终根据您的目前状态更新环境！

### <a id="qs-session-tracking"></a>会话跟踪

**请注意**：这一步**非常重要**。请**确保您在应用中正确设置它**。正确完成本步骤，可确保 Adjust SDK 正确跟踪您应用中的会话。

#### <a id="qs-session-tracking-api-14"></a>API level 14 及以上版本

- 添加一个私有类 (private class) 以实现 `ActivityLifecycleCallbacks` 接口。如果您不能访问该接口，则表示您的应用仅支持安卓 API level 14 以下版本。在这种情况下，请按照此[说明](#qs-session-tracking-api-9)，手动更新每项 Activity。如果您在之前已经对应用的每个Activity调用了 `Adjust.onResume` 和 `Adjust.onPause` ，请将其全部移除。
- 编辑 `onActivityResumed(Activity activity)` 方法，并添加 `Adjust.onResume()` 调用。编辑
`onActivityPaused(Activity activity)` 方法，并添加对 `Adjust.onPause()` 的调用。
- 在设置 Adjust SDK 的位置添加 `onCreate()` 方法，并添加调用 `registerActivityLifecycleCallbacks` 以及被创建的 `ActivityLifecycleCallbacks` 类实例。

    ```java
    import com.adjust.sdk.Adjust;
    import com.adjust.sdk.AdjustConfig;

    public class GlobalApplication extends Application {
        @Override
        public void onCreate() {
            super.onCreate();

            string appToken = "{YourAppToken}";
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

#### <a id="qs-session-tracking-api-9"></a>API level 9 - 13 版本

如果您的应用 gradle 中的 `minSdkVersion` 是在 `9` 至 `13` 版本之间，您应当考虑至少升级至版本 14 以简化集成流程。请查看官方安卓[控制面板][android-dashboard]，了解目前市场上各主要版本的占比。

为了进行准确的会话跟踪，每当任一 Activity 重新开始或者暂停时都需要调用某个 Adjust SDK 方法。否则 SDK 可能会错过一个会话开始或者会话结束。请遵循以下步骤对您的应用中的**每个 Activity**进行正确设置：

- 在 Activity 的 `onResume` 方法中调用 `Adjust.onResume()`。必要时创建该方法。
- 在 Activity 的 `onPause` 方法中调用 `Adjust.onPause()`。必要时创建该方法。

完成以上步骤后，您的 Activity 应如下：

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

对您的应用中的**每个 Activity** 重复以上步骤。如果您在之后创建新的 Activity，也请按照以上步骤设置。取决于您的编码方式，您也可通过设置所有 Activitiy 的通用超类来实现它。

### <a id="qs-sdk-signature"></a>SDK 签名

账户管理员必须启用 SDK 签名。如果您希望使用该功能，请联系 Adjust 支持(support@adjust.com)。

如果您已经在账户中启用了 SDK 签名，并可访问 Adjust 控制面板的应用密钥，请使用以下方法来集成 SDK 签名到您的应用。

在您的 config 实例中调用 `setAppSecret` 来设置应用密钥：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

### <a id="qs-adjust-logging"></a>Adjust 日志

您可以增加或减少在测试中看到的日志数量，方法是用以下参数之一来调用 config 实例上的 `setLogLevel`：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

如果您希望禁用所有日志输出，除了将日志级别设置为抑制以外，您还应该对配置对象使用构建函数，它将获取 boolean 参数来显示是否应该支持抑制日志级别：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

### <a id="qs-build-the-app"></a>构建您的应用

创建并运行您的安卓应用。在 `LogCat` 查看工具中，设置筛选 `tag:Adjust` ，以隐藏所有其他日志记录。应用启动后，您应当可以看到下列 Adjust 日志：`Install tracked` (安装已跟踪)。

## 深度链接

### <a id="dl"></a>深度链接概述

如果您使用的是已启用深度链接的 Adjust 跟踪链接，则可以接收有关深度链接 URL 及其内容的相关信息。不论用户的设备上已经安装了应用 (标准深度链接场景)，或者尚未安装应用 (延迟深度链接场景)，用户都可与链接交互。在标准深度链接场景中，安卓平台自身会支持您获取关于深度链接内容的信息。但是，安卓平台不提供对延迟深度链接场景的支持。在此情况下，Adjust SDK 可以帮助您获取有关深度链接内容的信息。

### <a id="dl-standard"></a>标准深度链接场景

如果用户已经安装了您的应用，您希望在用户点击带有 `deep_link` (深度链接) 参数的 Adjust 跟踪链接后打开应用，则必须在应用中启用深度链接。请选择需要的**唯一方案名称** (unique scheme name)，并将其分配至您希望在用户点击链接后应用打开时启动的 Activity 中。您可以在 `AndroidManifest.xml` 文件中进行设置。请在 manifest 文件中添加 `intent-filter` 至您指定的 Activity 定义，并分配指定的方案名至 `android:scheme` 属性值：

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

如果您希望在跟踪链接被点击后打开应用，请在 Adjust 跟踪链接的 `deep_link` 参数中使用指定的 Scheme 名称。未添加任何深度链接信息的跟踪链接将如下所示：

```
https://app.adjust.com/abc123?deep_link=adjustExample%3A%2F%2F
```

请记住：链接中的 `deep_link` 参数值**必须采用 URL 编码形式**。

如上所述完成应用设置，当用户点击跟踪链接时，您的应用将连带 `MainActivity` intent 打开。在 `MainActivity` 类中，您将自动获取关于 `deep_link` 参数的内容信息。虽然该内容在链接中已编码，但是它在发送给您后**不会**被编码。

`AndroidManifest.xml` 文件中的 `android:launchMode` Activity 设置决定 Activity 文件中 `deep_link` 参数内容的传递位置。请查看安卓[官方文档][android-launch-modes]，了解有关 `android:launchMode` 可能属性值的更多信息。

通过 `intent` 对象发送至您指定的 Activity 的深度链接内容信息将可能被传递至两个位置 — Activity 的 `onCreate` 或 `onNewIntent` 方法。一旦应用被打开，方法被触发后，您将获得在点击链接中被传递至 `deep_link` 参数中的实际深度链接。您可以使用这些信息为应用增加一些附加逻辑。

您可以按以下两种方式提取深度链接内容：

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Intent intent = getIntent();
    Uri data = intent.getData();
    // data.toString()-> This is your deep_link parameter value.
}
```

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    Uri data = intent.getData();
    // data.toString()-> This is your deep_link parameter value.
}
```

### <a id="dl-deferred"></a>延迟深度链接场景

用户点击含有 `deep_link` 参数的 Adjust 跟踪链接，但在点击发生时用户设备上并未安装应用，就会发生延迟深度链接场景。用户点击链接时，会被重定向至 Play 应用商店来下载和安装应用。在首次应用打开后，`deep_link` 参数内容将被发送至您的应用。

Adjust SDK 默认自动打开延迟深度链接，无须额外设置。

#### 延迟深度链接回传

如果您希望控制 Adjust SDK 是否打开延迟深度链接，可以通过在配置对象中设置回传的方式实现。

<table>
<tr>
<td>
<b>原生应用 SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

// Evaluate the deeplink to be launched.
config.setOnDeeplinkResponseListener(newOnDeeplinkResponseListener() {
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

Adjust SDK 从后台接收到关于深度链接内容的信息后，将在监听器内向您传递相关内容信息，并等待您的 `boolean` 返回值。该返回值决定是否由 Adjust SDK 启动您从深度链接已分配方案名称的 activity (如标准深度链接场景一样)。

如果您的返回值为 `true`，我们将启动该 activity，触发在[标准深度链接场景](#dl-standard)一节中所描述的场景。如果您不希望 SDK 启动 Activity，您可以从监听器返回 `false` 值，并根据深度链接内容自行决定下一步应用中的动作。
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
adjustConfig.setDeferredDeeplinkCallback(function(deeplink) {});

Adjust.onCreate(adjustConfig);
```

在延迟深度链接场景中，您可对配置对象进行一个额外设置。一旦 Adjust SDK 获得延迟深度链接信息，您便可选择我们的 SDK 是否应该打开链接。您可在配置对象上调用 `setOpenDeferredDeeplink` 方法进行设置：

```js
// ...

function deferredDeeplinkCallback(deeplink) {}

let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setOpenDeferredDeeplink(true);
adjustConfig.setDeferredDeeplinkCallback(deferredDeeplinkCallback);

Adjust.start(adjustConfig);

```

请记住，如果您不设置回传，**在默认情况下 Adjust SDK 将始终尝试启动链接**。
</td>
</tr>
</table>

### <a id="dl-reattribution"></a>通过深度链接的再归因

Adjust 支持您使用深度链接开展再交互推广活动。请查看我们的[官方文档][reattribution-with-deeplinks]，了解更多信息。

如果您正在使用该功能，为了准确地再归因用户，您需要在应用中设置一个额外调用至 Adjust SDK。

一旦您已经在应用中收到深度链接内容信息，请添加调用至 `Adjust.appWillOpenUrl(Uri, Context)` 方法。添加该调用后，Adjust SDK 将发送信息至 Adjust 后台，查看深度链接中是否有任何新的归因信息。如果您的用户因为点击带有深度链接内容的 Adjust 跟踪链接而被再归因，您将会看到应用中的[归因回传](#af-attribution-callback)被触发并附有该用户的新归因信息。

`Adjust.appWillOpenUrl(Uri,Context)` 调用应当以下列方式呈现：

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

**请注意**：安卓 SDK v4.14.0 发布后，`Adjust.appWillOpenUrl(Uri)` 方法已被标记为 **弃用**。请换用 `Adjust.appWillOpenUrl(Uri,Context)` 方法。

**Web view 特别提示**：您还可如下所示在 web view 中使用 Javascript 中的 `Adjust.appWillOpenUrl` 函数进行调用：

```js
Adjust.appWillOpenUrl(deeplinkUrl);
```

### <a id="link-resolution"></a>链接解析

通过电子邮件服务提供商 (ESP) 投放深度链接且需要使用自定义跟踪链接来跟踪点击时，可以使用 `AdjustLinkResolution` 类的 `resolveLink` 方法进行链接解析。这样，当用户在应用中打开深度链接时，您就能记录用户与电子邮件推广活动的互动了。

`resolveLink` 方法携带下列参数：

- `url` - 打开应用程序的深度链接
- `resolveUrlSuffixArray` - 需要解析的、已设置推广活动的自定义域名
- `adjustLinkResolutionCallback` - 将包含最终 URL 的回传

如果接收到的链接不属于 `resolveUrlSuffixArray` 中指定的任何域名，那么回传就会原样转发深度链接 URL；如果链接包含所指定的域名，那么 SDK 就会尝试解析链接，并将得出的深度链接返回至 `callback` 参数。您也可以使用 `Adjust.appWillOpenUrl` 方法，在 Adjust SDK 中针对返回的深度链接进行再归因。

> **请注意**：在尝试解析 URL 时，SDK 会自动追溯最多 10 个重定向 (redirect)，并将其中最新的 URL 返回为 `回传` URL，也就是说，如果要追溯的重定向超过 10 个，那么 SDK 就会返回 **第 10 个重定向 URL**。

**示例**

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

## 事件跟踪

### <a id="et-tracking"></a>事件跟踪

您可以使用 Adjust 来跟踪应用中的任何事件。假设您想跟踪特定按钮的所有点击。要做到这一点，您需要在[控制面板]中创建新的事件识别码。假设事件识别码为 `abc123`。在按钮的 `onClick` 方法中，添加以下行来跟踪点击：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

### <a id="et-revenue"></a>跟踪收入

如果您的用户可通过点击广告或进行应用内购买的方式为您带来收入，您也可以通过事件来跟踪此类收入。假设一次点击能带来一欧分的收入。您可以这样来跟踪收入事件：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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
adjustEvent.setRevenue(0.01,'EUR');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

您可以将其与回传参数相结合。

设置货币识别码后，Adjust 会自动将收入转化为您所选的报告收入。[在这里][currency-conversion]了解更多货币换算信息。

如果您想要跟踪应用内购买，请确保仅在购买完成且商品已购买后才调用 `trackEvent`。要想避免跟踪实际未产生的收入，这点十分重要。

要进一步了解 Adjust 收入和事件跟踪相关信息，请参阅[事件跟踪指南][event-tracking]。

### <a id="et-revenue-deduplication"></a>收入去重

您也可以选择添加交易 ID，以避免跟踪重复收入。这样，最后 10 个交易 ID 将被记录下来，交易 ID 重复的收入事件则会被跳过。这对于跟踪应用内购买尤其有用。请在下方查看示例：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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
adjustEvent.setRevenue(0.01,'EUR');
adjustEvent.setOrderId('{OrderId}');
Adjust.trackEvent(event);
```
</td>
</tr>
</table>

### <a id="et-purchase-verification"></a>应用内收入验证

如果您想要验证应用内购买，可以使用 [Adjust 收入验证][android-purchase-verification]，这是一款服务器端收据验证工具。点击链接了解更多信息。   

## 自定义参数

### <a id="cp"></a>自定义参数概述

除了 Adjust SDK 默认收集的数据点之外，您还可以使用 Adjust SDK 进行跟踪，并根据需要添加任意数量的自定义值（用户 ID、产品 ID 等）到事件或会话中。自定义参数仅作为原始数据提供，且 **不会** 出现在 Adjust 控制面板中。

针对内部使用而收集的值，请使用**回传参数**，并对与外部合作伙伴共享的值使用**合作伙伴参数**。如果某个值（如产品 ID）既会被用于内部，也会与外部合作伙伴分享，我们建议同时使用回传和合作伙伴参数来跟踪该值。


### <a id="cp-event-parameters"></a>事件参数

### <a id="cp-event-callback-parameters"></a>事件回传参数

您可以在 [控制面板] 中为事件输入回传 URL。这样，只要跟踪到事件，我们都会向该 URL 发送 GET 请求。您可以在跟踪前调用事件实例的 `addCallbackParameter` ，向该事件添加回传参数。然后我们会将这些参数附加至您的回传 URL。

例如，如果您已注册 URL `http://www.example.com/callback`，则您将这样跟踪事件：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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
adjustEvent.addCallbackParameter('key','value');
adjustEvent.addCallbackParameter('foo','bar');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

在这种情况下，我们会跟踪该事件并发送请求至：

```
http://www.example.com/callback?key=value&foo=bar
```

Adjust 支持各种占位符，例如可以用作参数值的 `{gps_adid}`。我们会在产生的回传中将占位符 (在该情况下) 替换为当前设备的 Google Play 服务 ID。请注意，我们不会存储您的任何自定义参数。我们 **仅** 将这些参数附加到您的回传中。如果您尚未针对事件注册回传，这些参数甚至不会被读取。

若想进一步了解 URL 回传，查看可用参数的完整列表，请参阅我们的 [回传指南][callbacks-guide]。

### <a id="cp-event-partner-parameters"></a>事件合作伙伴参数

参数在控制面板中激活后，您可以将其发送至渠道合作伙伴。

方式和上述提及的回传参数类似，可以通过调用事件实例上的 `addPartnerParameter` 方法来添加。

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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
adjustEvent.addPartnerParameter('key','value');
adjustEvent.addPartnerParameter('foo','bar');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

您可以在我们的 [特殊合作伙伴指南][special-partners] 中了解更多有关特殊合作伙伴以及这些集成的信息。

### <a id="cp-event-callback-id"></a>事件回传 ID

您可以为想要跟踪的每个事件添加自定义字符串 ID。我们会在事件成功和/或失败回传中报告该标识符，以便您了解哪些事件已被成功跟踪。通过调用事件实例上的 `setCallbackId` 方法来设置此标识符：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

### <a id="cp-session-parameters"></a>会话参数

会话参数被保存在本地，并随每个 Adjust SDK **事件** 和 **会话** 一同发送。您添加这些参数找呢的任何一个后，下次就无需再度添加，因为这些参数已经被保存。添加同样的参数两次不会有任何影响。

这些会话参数在 Adjust SDK 启动之前可以被调用，以确保它们在安装时可被发送。如果您需要在安装同时发送参数，但只有在 SDK 启动后才能获取所需的值，您可以通过[延迟](#delay-start) Adjust SDK 第一次启动以允许该行为。

### <a id="cp-session-callback-parameters"></a>会话回传参数

注册在[事件](#event-callback-parameters)中的相同回传参数也可以被保存发送至 Adjust SDK 的每一个事件和会话中。

会话回传参数的接口与事件回传参数的接口类似。该参数是通过调用 `Adjust.addSessionCallbackParameter(String key, String value)`: 被添加，而不是通过添加 Key 和值至事件:

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

会话回传参数将与被添加至事件的回传参数合并。被添加至事件的回传参数拥有高于会话回传参数的优先级。这意味着，当被添加至事件的回传参数拥有与会话回传参数同样的 Key 时，以被添加至事件的回传参数值为准。

您可以通过传递 Key 至 `Adjust.removeSessionCallbackParameter(String key)` 的方式来删除特定会话回传参数。

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

如果您希望删除会话回调参数中所有的 Key 及相应值，可以通过 `Adjust.resetSessionCallbackParameters(`) 方式重置：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

### <a id="cp-session-partner-parameters"></a>会话合作伙伴参数

与 [会话回传参数](#session-callback-parameters) 的方式一样，会话合作伙伴参数也会与 SDK 的每个事件或会话一同发送。

这些参数会传送至在 Adjust [控制面板] 中已激活相关集成的渠道合作伙伴。

会话合作伙伴参数接口与事件合作伙伴参数接口类似。该参数是通过调用 `Adjust.addSessionPartnerParameter(String key, String value)` 被添加，而不是通过添加 Key 和值至事件:

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

会话合作伙伴参数将与被添加至事件的合作伙伴参数合并。被添加至事件的合作伙伴参数具有高于会话合作伙伴参数的优先级。这意味着，当被添加至事件的合作伙伴参数拥有与会话合作伙伴参数同样的 Key 时，以被添加至事件的合作伙伴参数值为准。

您可以通过传递 Key 至 `Adjust.removeSessionPartnerParameter(String key)` 方法，删除特定的会话合作伙伴参数：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

如果您希望删除会话合作伙伴参数中所有的 Key 及其相应值，可以通过 `Adjust.resetSessionPartnerParameters()`. 方法重置。

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

### <a id="cp-delay-start"></a>延迟启动

延迟 Adjust SDK 的启动可以为您的应用提供更充裕的时间，来接收所有想要随安装发送的会话参数（例如：唯一标识符）。

利用 config 实例中的 `setDelayStart` 方法，以秒为单位设置初始延迟时间：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

在此示例中，Adjust SDK 不会在 5.5 秒内发送初始安装会话以及创建的任何事件。5.5 秒后 (或您在其此期间调用 `Adjust.sendFirstPackages()`)，每个会话参数会添加至延迟的安装会话和事件，并且 Adjust SDK 会继续照常运行。

**您最多可以将 Adjust SDK 的启动时间延长 10 秒**。


## 其他功能

将 Adjust SDK 集成到项目中后，您即可利用以下功能：

### <a id="af-push-token"></a>推送标签 (卸载跟踪)

推送标签用于受众分群工具和客户回传；也是跟踪卸载和重装所需的信息。

要向我们发送推送通知标签，请在获得标签 (或每当标签值变更) 时向 Adjust 添加下列调用：

<table>
<tr>
<td>
<b>原生 SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.setPushToken(pushNotificationsToken, context);
```

更新后的签名加入了 `context`，可允许 SDK 涵盖更多场景，确保推送标签被发送。因此，我们建议您使用以上签名方式。

尽管如此，我们仍支持之前没有 `context` 的相同方法签名。

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

### <a id="af-attribution-callback"></a>归因回传

您可以注册一个监听器 (listener)，以获取跟踪链接归因变化的通知。考虑到归因的不同来源，归因信息无法被同步提供。

请查看我们的[归因数据政策][attribution-data]了解更多信息。

使用 config 实例，在启动 SDK 之前添加归因回传：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

config.setOnAttributionChangedListener(newOnAttributionChangedListener() {
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

SDK 收到最终归因数据后，监听器函数会被调用。在监听器函数中，您可以访问 `attribution` 参数。以下是其属性的摘要：

- `trackerToken` 目前归因的跟踪码字符串。
- `trackerName` 目前归因的跟踪链接名称字符串。
- `network` 目前归因的渠道分组层字符串。
- `campaign` 目前归因的推广分组层字符串。
- `adgroup` 目前归因的广告组分组层字符串。
- `creative` 目前归因的素材分组层字符串。
- `clickLabel` 目前归因的点击标签字符串。
- `adid` Adjust 设备标识符字符串。
- `costType` 成本类型字符串。
- `costAmount` 成本金额。
- `costCurrency` 成本币种字符串。

**请注意**：只有在`AdjustConfig` 中通过调用 `setNeedsCost` 方法来进行配置后，`costType`、 `costAmount` 和 `costCurrency` 成本数据才可用。如果未进行配置，或已配置但这些字段不属于归因的一部分，那么字段值就会为 `null`。此功能仅适用于 SDK 4.25.0 及以上版本。

### <a id="af-subscriptions"></a>订阅跟踪

**请注意**：此功能仅适用于原生 SDK 4.22.0 及以上版本。

您可以用 Adjust SDK 跟踪 Play 应用商店的订阅，并验证这些订阅是否有效。订阅购买成功后，请向 Adjust SDK 进行如下调用：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

订阅跟踪参数：

- [price](https://developer.android.com/reference/com/android/billingclient/api/SkuDetails#getpriceamountmicros)
- [currency](https://developer.android.com/reference/com/android/billingclient/api/SkuDetails#getpricecurrencycode)
- [sku](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getsku)
- [orderId](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getorderid)
- [signature](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getsignature)
- [purchaseToken](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getpurchasetoken)
- [purchaseTime](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getpurchasetime)

与事件跟踪一样，您也可以向订阅对象附加回传和合作伙伴参数：

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
subscription.addCallbackParameter("key","value");
subscription.addCallbackParameter("foo","bar");

// add partner parameters
subscription.addPartnerParameter("key","value");
subscription.addPartnerParameter("foo","bar");

Adjust.trackPlayStoreSubscription(subscription);
```

### <a id="af-ad-revenue"></a>广告收入跟踪

**注意**：此功能仅适用于原生 SDK 4.18.0 及以上版本。

您可以通过调用以下方法，使用 Adjust SDK 对广告收入进行跟踪：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.trackAdRevenue(source, payload);
```
</td>
</tr>
</table>

您需要传递的方法参数包括：

- `source` - 表明广告收入来源信息的` String` 对象。
- `payload` - 包含广告收入 JSON 的` JSONObject` 对象。

目前，我们支持以下 `source` 参数值：

- `AD_REVENUE_MOPUB` - 代表 MoPub 广告聚合平台（更多相关信息，请查看 [集成指南][sdk2sdk-mopub])

### <a id="af-session-event-callbacks"></a>会话和事件回传

您可以注册一个监听器，以在事件或者会话被跟踪时获取通知。监听器共有 4 个：一个用来跟踪成功事件，一个跟踪失败事件，一个跟踪成功会话，一个跟踪失败会话。您可以在创建配置对象后添加任意数量的监听器：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

// Set event success tracking delegate.
config.setOnEventTrackingSucceededListener(newOnEventTrackingSucceededListener() {
    @Override
    public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
        // ...
    }
});

// Set event failure tracking delegate.
config.setOnEventTrackingFailedListener(newOnEventTrackingFailedListener() {
    @Override
    public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
        // ...
    }
});

// Set session success tracking delegate.
config.setOnSessionTrackingSucceededListener(newOnSessionTrackingSucceededListener() {
    @Override
    public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
        // ...
    }
});

// Set session failure tracking delegate.
config.setOnSessionTrackingFailedListener(newOnSessionTrackingFailedListener() {
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

SDK 尝试向服务器发送包 (package) 后，将会调用监听器函数。在监听器函数中，您可以访问专门用于监听器的响应数据对象。成功会话响应数据对象字段的摘要如下：

- `message` 服务器信息字符串或者 SDK 记录的错误信息。
- `Timestamp` 服务器时间戳字符串。
- `Adid` 由 Adjust 提供的设备唯一标识符字符串。
- `JsonResponse` JSON 对象及服务器响应。

两个事件响应数据对象都包含：

- 如果跟踪的包是一个事件，那么 `EventToken` 代表事件识别码字符串。
- `CallbackId` 为事件对象设置的自定义[回传 ID](#cp-event-callback-id)字符串。

事件和会话跟踪失败的对象也均包含：

- `willRetry` 布尔，表示稍后是否会尝试重新发送数据包。

### <a id="af-user-attribution"></a>用户归因

如同之前在[归因回传一节](#af-attribution-callback)阐述的那样，只要归因信息发生更改，就会触发此回传，想要随时访问用户当前的归因信息，您可通过调用 `Adjust` 实例的以下方法来实现：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

**请注意**：Adjust SDK 版本必须为 v4.11.0 或**更高**，才能进行该调用。

**注意**：只有在我们的后台跟踪到应用安装并触发归因回传后，您才能获取当前的归因信息。因此，在 SDK 初始化以及归因回传触发前，您**无法**访问用户的归因值。

### <a id="af-device-ids"></a>设备 ID

Adjust SDK 支持您接收设备标识符。

### <a id="af-gps-adid"></a>Google Play 服务广告 ID

某些服务 (如 Google Analytics) 要求您协调广告 ID 及客户 ID 以避免重复报告。

<table>
<tr>
<td>
<b>原生应用 SDK</b>
</td>
</tr>
<tr>
<td>

在获取 Google 广告 ID 时您会遇到限制：Google 广告 ID 只能在后台线程中读取。如果调用带上下文 (context) 的 `getGoogleAdId` 函数和 `OnDeviceIdsRead` 实例，那么在任何情况下都能成功：

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

如需获取设备的 Google 广告设备 ID，您必须传递一个回传函数到 `Adjust.getGoogleAdId`，其将在参数中接收 Google 广告 ID，如下所示：

```js
Adjust.getGoogleAdId(function(googleAdId) {
    // ...
});
```
</td>
</tr>
</table>

### <a id="af-amazon-adid"></a>Amazon 广告 ID

如果您需要获取 Amazon 广告 ID，请调用` Adjust` 实例的下列方法：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

### <a id="af-adid"></a>Adjust 设备 ID

我们的后端会为每个安装您应用的设备生成唯一的 **Adjust 设备标识符** (即 **adid**).为了获得此标识符，请调用 `Adjust` 实例的下列方法：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

**请注意**：Adjust SDK 版本必须为 v4.11.0 或**更高**，才能进行该调用。

**请注意**：只有在我们的后台跟踪到应用安装后，您才能获取有关 **adid** 的信息。因此，在 SDK 初始化以及成功跟踪到应用安装前，您**无法**访问 **adid** 值。

### <a id="af-preinstalled-apps"></a>预安装应用

您可以使用 Adjust SDK，识别设备上已在生产过程中预安装您应用的用户。Adjust 可提供两个解决方案，其中一个采用系统负载，另一个采用默认跟踪链接。 

总体来说，我们推荐您使用 system payload 解决方案。但是，在有些使用情境下，可能需要用到跟踪链接。请访问我们的[帮助中心](https://help.adjust.com/zh/article/pre-install-tracking)，了解 Adjust 的预装合作伙伴与合作伙伴的集成。如果您不确定该采用哪种解决方案，请联系 integration@adjust.com。

#### 使用 system payload

**SDK v4.23.0 及更高版本**支持该x解决方案。

创建设置对象后，请在参数为 true 的前提下，调用 `setPreinstallTrackingEnabled`：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

#### 采用默认跟踪链接

- 在 [控制面板] 中创建新的跟踪链接。
- 打开应用委托，设置配置的默认跟踪链接：

  <table>
  <tr>
  <td>
  <b>原生应用 SDK</b>
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

- 用您在步骤 1 中创建的跟踪码替换 `{TrackerToken}`。请注意，控制面板显示的是跟踪链接 (包括 `http://app.adjust.com/`)。而在源代码中，您应该仅输入 6 个或 7 个字符的识别码，而不是整个跟踪链接。

- 创建并运行应用。您应该可以在 LogCat 中看到以下行：

  ```
  默认跟踪链接：'abc123'
  ```

### <a id="af-offline-mode"></a>离线模式

您可以将 Adjust SDK 设为离线模式，暂停向我们的服务器传输数据 (但仍然保存跟踪数据用于之后发送)。Adjust SDK 处于离线模式时，所有信息都会保存在一个文件中。请注意不要在离线模式下触发太多事件。

调用参数为 `true` 的 `setOfflineMode` 即可激活脱机模式。

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

相反地，您可以调用 `setOfflineMode`，启用参数为 `false`，以终止离线模式。当 Adjust SDK 调回在线模式后，保存的所有信息都会发送到我们的服务器，并保留正确的时间信息。

与禁用跟踪不同，此设置在会话之间将**不被保存**。也就是说，即使应用在处于离线模式时停用，SDK 每次启动时都必定处于在线模式。


### <a id="af-disable-tracking"></a>禁用跟踪

您可以通过调用 `setEnabled`，启用参数为 `false`，来禁用 Adjust SDK 对当前设备的一切跟踪功能。**该设置在会话间保存。**。

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

您可以调用 `isEnabled` 函数，查看 Adjust SDK 目前是否启用。您始终可以通过调用启用参数设置为 `true` 的 `setEnabled` 来激活 Adjust SDK。

### <a id="af-event-buffering"></a>事件缓冲

如果您的应用大量使用事件跟踪，您可能想要延迟部分网络请求，以便每分钟按批量发送。您可以利用配置实例来启用事件缓冲：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

### <a id="af-background-tracking"></a>后台跟踪

Adjust SDK 的默认行为是当应用处于后台时暂停发送网络请求。您可以在配置实例中更改此设置：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

### <a id="af-gdpr-forget-me"></a>GDPR 被遗忘权

根据欧盟的《一般数据保护条例》(GDPR) 第 17 条规定，用户行使被遗忘权时，您可以通知 Adjust。调用以下方法时，Adjust SDK 将会收到指示向 Adjust 后端传达用户选择被遗忘的信息：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

收到此信息后，Adjust 将清除用户数据，并且 Adjust SDK 将停止跟踪该用户。以后不会再向 Adjust 发送来自此设备的请求。

请注意，即便在测试环境中，此决定也是永久性的，**不可逆转**。

## <a id="af-third-party-sharing"></a>具体用户的第三方数据分享

当有用户禁用、启用或重启第三方合作伙伴数据分享时，您可以通知 Adjust。

### <a id="af-disable-third-party-sharing"></a>为具体用户禁用第三方数据分享

请调用以下方法，指示 Adjust SDK 将用户禁用数据分享的选择传递给 Adjust 后端：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

收到此信息后，Adjust 会停止向合作伙伴分享该用户的数据，而 Adjust SDK 将会继续如常运行。

### <a id="af-enable-third-party-sharing"></a>为具体用户启用或重启第三方数据分享

请调用以下方法，指示 Adjust SDK 将用户启用或变更数据分享的选择传递给 Adjust 后端：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

收到此信息后，Adjust 会就是否与合作伙伴分享该用户的数据做出相应变更，而 Adjust SDK 将会继续如常运行。

请调用以下方法，指示 Adjust SDK 向 Adjust 后端发送精细选项：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

### <a id="af-measurement-consent"></a>监测具体用户的许可

要在 Adjust 控制面板中启用或禁用数据隐私设置，包括许可有效期和用户数据留存期，您需要安装以下方法。

请调用以下方法，指示 Adjust SDK 将数据隐私设置传递给 Adjust 后端：

<table>
<tr>
<td>
<b>原生应用 SDK</b>
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

收到此信息后，Adjust 会启用或禁用许可监测。而 Adjust SDK 将会继续如常运行。

## 测试与故障排查

### <a id="tt-session-failed"></a>显示 "session failed (Ignoring too frequent session...)" 出错信息。

该错误一般发生在安装测试时。单凭卸载和重装应用不足以触发新安装。由于我们服务器已经有该设备的纪录，服务器会认定该设备的 SDK 丢失了本地聚合的会话数据，并忽略该错误信息。

虽然该行为可能在测试阶段比较麻烦，但却是必须的，目的是为了让 sandbox 环境的行为尽可能地符合生产环境的行为。

如果您拥有应用编辑员级别 (或更高级别) 的权限，则可以使用我们的[测试控制台][testing_console]直接从 Adjust 控制面板重置任何设备的应用会话数据。 

一旦设备被正确遗忘，测试控制台将返回 `Forgot device`。如果设备已经被遗忘 (或者值不正确)，链接将返回 `Advertising ID not found`。

遗忘设备将不会逆转 GDPR 遗忘调用。

如果您当前的包允许访问，您还可以使用我们的[开发者 API][dev_api] 检查和忘记设备。

### <a id="tt-broadcast-receiver"></a>我的广播接收器是否能成功获取 Install Referrer？

如果您按照[指南](#qs-gps-intent)描述的步骤来设置, 广播接收器就应该可以将 install referrer 发送到 SDK 以及我们的服务器。

您可以手动触发测试 Install Referrer，检查设置情况。将 `com.your.appid` 替换为您的应用 ID，并使用 Android Studio 自带的 [adb](http://developer.android.com/tools/help/adb.html) 工具运行下列命令：

```
adb shell am broadcast -a com.android.vending.INSTALL_REFERRER -n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver --es "referrer" "adjust_reftag%3Dabc1234%26tracking_id%3D123456789%26utm_source%3Dnetwork%26utm_medium%3Dbanner%26utm_campaign%3Dcampaign"
```

如果您已经按照该[指南][referrer]说明，设置了另一个广播接收器使用 `INSTALL_REFERRER` intent ，请用您的广播接收器替换 `com.adjust.sdk.AdjustReferrerReceiver` 。

您也可以删除 `-n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver` 参数，让设备上的所有应用接收 `INSTALL_REFERRER` intent。

如果您将日志级别设置为 `verbose`，就应该可以通过读取 referrer 查看日志：

```
V/Adjust: Referrer to parse (adjust_reftag=abc1234&tracking_id=123456789&utm_source=network&utm_medium=banner&utm_campaign=campaign) from reftag
```

以及添加点击包 (click package) 到 SDK 包处理器：

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

如果您在启动应用前执行以上测试，数据包将不会被发送。数据包将于应用启动后被发送。

**重要提示**：我们建议您不要使用 `adb` 工具测试该功能。为了测试完整的 referrer 内容 (在由 `&` 分隔多个参数的情况下)，如果使用 `adb`，您就需要对内容进行编码以便发送给广播接收器。如未编码，`adb` 将在第一个 `&` 符号后切断 referrer，并向您的广播接收器发送错误内容。

如果您希望查看应用如何接收未编码的 referrer 值，我们建议您使用我们的示例应用，并更改传递的内容，以便被 `MainActivity.java` 文件内的 `onFireIntentClick` 方法中的 intent 触发：

```java
public void onFireIntentClick(View v) {
    Intent intent = new Intent("com.android.vending.INSTALL_REFERRER");
    intent.setPackage("com.adjust.examples");
    intent.putExtra("referrer", "utm_source=test&utm_medium=test&utm_term=test&utm_content=test&utm_campaign=test");
    sendBroadcast(intent);
}
```

您可随意使用自选内容更改 `putExtra` 方法的第二个参数。

### <a id="tt-event-at-launch"></a>我能否在应用激活时触发事件？

此时触发事件可能无法达到您预期的效果。原因如下：

全局 `Application` 类上的 `onCreate` 方法不仅在应用启动时调用，而且在应用捕捉到系统或应用事件时调用。

此时，我们的 SDK 已经准备初始化了，但是还没有正式启动。只有当 activity 发生时，比如当用户真正激活应用时，SDK 才会正式启动。

即使用户没有激活应用，此时触发事件将启动 Adjust SDK 并发送事件 — 具体时间取决于应用的外部因素。

在应用激活时触发事件会导致被跟踪的安装及会话数量报告不准确。

如果您想在安装后触发事件，请使用[归因回传](#af-attribution-callback)。

如果您想在应用激活时触发事件，请为指定 activity 使用 `onCreate` 方法。

[dashboard]:  http://adjust.com/zh
[控制面板]:     http://adjust.com/zh
[adjust.com]: http://adjust.com/zh

[en-readme]:  ../../README.md
[zh-readme]:  ../chinese/README.md
[ja-readme]:  ../japanese/README.md
[ko-readme]:  ../korean/README.md

[example-java]:       ../../Adjust/example-app-java
[example-kotlin]:     ../../Adjust/example-app-kotlin
[example-keyboard]:     ../../Adjust/example-app-keyboard
[example-tv]:         ../../Adjust/example-app-tv
[example-webbridge]:  ../../Adjust/example-app-webbridge

[maven]:                          http://maven.org
[referrer]:                       https://github.com/adjust/android_sdk/blob/master/doc/chinese/misc/multiple-receivers.md
[releases]:                       https://github.com/adjust/android_sdk/releases
[google-ad-id]:                   https://support.google.com/googleplay/android-developer/answer/6048248?hl=en
[Google 广告 ID]:                  https://support.google.com/googleplay/android-developer/answer/6048248?hl=en
[event-tracking]:                 https://docs.adjust.com/zh/event-tracking
[callbacks-guide]:                https://docs.adjust.com/zh/callbacks
[new-referrer-api]:               https://developer.android.com/google/play/installreferrer/library.html
[special-partners]:               https://docs.adjust.com/zh/special-partners
[attribution-data]:               https://github.com/adjust/sdks/blob/master/doc/attribution-data.md
[android-dashboard]:              http://developer.android.com/about/dashboards/index.html
[currency-conversion]:            https://docs.adjust.com/zh/event-tracking/#tracking-purchases-in-different-currencies
[android-application]:            http://developer.android.com/reference/android/app/Application.html
[android-launch-modes]:           https://developer.android.com/guide/topics/manifest/activity-element.html
[google-play-services]:           http://developer.android.com/google/play-services/setup.html
[Google Play 服务]:                http://developer.android.com/google/play-services/setup.html
[reattribution-with-deeplinks]:   https://docs.adjust.com/zh/deeplinking/#manually-appending-attribution-data-to-a-deep-link
[android-purchase-verification]:  https://github.com/adjust/android_purchase_sdk
[testing_console]: https://docs.adjust.com/zh/testing-console/#how-to-clear-your-advertising-id-from-adjust-between-tests
[dev_api]: https://docs.adjust.com/zh/adjust-for-developers/

[sdk2sdk-mopub]:    ../../doc/chinese/sdk-to-sdk/mopub.md
[集成指南]:          https://github.com/adjust/android_sdk/tree/master/doc/chinese

[en-helpcenter]: https://help.adjust.com/en/developer/android-sdk-documentation
[zh-helpcenter]: https://help.adjust.com/zh/developer/android-sdk-documentation
[ja-helpcenter]: https://help.adjust.com/ja/developer/android-sdk-documentation
[ko-helpcenter]: https://help.adjust.com/ko/developer/android-sdk-documentation

## <a id="license"></a>许可

Adjust SDK 拥有MIT 许可证。

版权所有(c) 2012-2021 Adjust GmbH，http://www.adjust.com

特此免费授予获得本软件及相关文档文件（“软件”）副本的任何人，得以无限制地处理本软件，其范围包括但不限于使用、复制、修改、合并、发布、分发、再许可和/或销售本软件的副本；具备本软件上述权限之人员需遵守以下条件：

上述版权声明和本许可声明，应包含在本软件的所有副本或主要部分中。

本软件“按原样”提供，不提供任何形式的明示或暗示保证，包括对适销性、适用于特定用途或非侵权性的保证。任何情况下，作者或版权所有者都不应承担任何索赔、损害赔偿或其他责任，无论是因软件或使用或其他软件处理引起的或与其相关的合同行为、侵权行为或其他行为。


