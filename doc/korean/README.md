## 요약

이 항목에서는 Adjust의 Android SDK에 대해 설명합니다. Adjust에 대한 자세한 내용은 [adjust.com]을 참조하십시오.

제공되는 언어: [English][en-readme], [中文][zh-readme], [日本語][ja-readme], [한국어][ko-readme].

## 목차

### 빠른 시작

   * [앱 예제](#qs-example-apps)
   * [기본 연동](#qs-getting-started)
      * [프로젝트에 SDK 추가](#qs-add-sdk)
      * [Google Play 서비스 추가](#qs-gps)
      * [권한 추가](#qs-permissions)
      * [Proguard 설정](#qs-proguard)
      * [설치 referrer](#qs-install-referrer)
         * [Google Play referrer API](#qs-gpr-api)
         * [Google Play Store 인텐트](#qs-gps-intent)
   * [앱에 SDK 연동](#qs-integrate-sdk)
      * [기본 설정](#qs-basic-setup)
         * [네이티브 앱 SDK](#qs-basic-setup-native)
         * [웹뷰 SDK](#qs-basic-setup-web)
      * [세션 추적](#qs-session-tracking)
         * [API 레벨 14 이상](#qs-session-tracking-api-14)
         * [API 레벨 9-13](#qs-session-tracking-api-9)
      * [SDK 서명](#qs-sdk-signature)
      * [Adjust 로그 기록(logging)](#qs-adjust-logging)
      * [앱 빌드](#qs-build-the-app)

### 딥링크 (Deeplinking)

   * [딥링크 개요](#dl)
   * [기본 딥링크 시나리오](#dl-standard)
   * [지연된 딥링크 시나리오](#dl-deferred)
   * [딥링크를 통한 리어트리뷰션](#dl-reattribution)

### 이벤트 추적

   * [이벤트 추적](#et-tracking)
   * [매출 추적](#et-revenue)
   * [매출 중복 제거](#et-revenue-deduplication)
   * [인앱 구매 검증](#et-purchase-verification)

### 사용자 지정 파라미터

   * [사용자 지정 파라미터 개요](#cp)
   * [이벤트 파라미터](#cp-event-parameters)
      * [이벤트 콜백 파라미터](#cp-event-callback-parameters)
      * [이벤트 파트너 파라미터](#cp-event-partner-parameters)
      * [이벤트 콜백 식별자](#cp-event-callback-id)
   * [세션 파라미터](#cp-session-parameters)
      * [세션 콜백 파라미터](#cp-session-callback-parameters)
      * [세션 파트너 파라미터](#cp-session-partner-parameters)
      * [예약 시작(delay start)](#cp-delay-start)

### 부가 기능

   * [푸시 토큰(push token) - uninstall tracking](#af-push-token)
   * [어트리뷰션 콜백](#af-attribution-callback)
   * [광고 매출 트래킹](#af-ad-revenue)
   * [세션 및 이벤트 콜백](#af-session-event-callbacks)
   * [사용자 어트리뷰션](#af-user-attribution)
   * [기기 ID](#af-device-ids)
      * [Google Play 서비스 광고 식별자](#af-gps-adid)
      * [Amazon 광고 식별자](#af-amazon-adid)
      * [Adjust 기기 식별자](#af-adid)
   * [사전 설치 트래커(pre-installed trackers)](#af-pre-installed-trackers)
   * [오프라인 모드](#af-offline-mode)
   * [추적 사용 중지](#af-disable-tracking)
   * [이벤트 버퍼링(buffering)](#af-event-buffering)
   * [백그라운드 추적](#af-background-tracking)
   * [GDPR(일반 개인정보 보호법) 상의 잊힐 권리](#af-gdpr-forget-me)

### 테스트 및 문제 해결

   * [“session failed (Ignoring too frequent session...)” 오류가 표시됩니다.](#tt-session-failed)
   * [브로드캐스트 리시버가 설치 참조 페이지를 제대로 캡처하고 있나요?](#tt-broadcast-receiver)
   * [응용 프로그램 런칭 시 이벤트를 촉발할 수 있나요?](#tt-event-at-launch)

### 라이선스

## 빠른 시작

### <a id="qs-example-apps"></a>앱 예제

[`example-app-java`][example-java] 와 [`example-app-kotlin` directories][example-kotlin] 안에 안드로이드 예제앱이 있습니다. 웹뷰를 사용하는 네이티브 앱의 경우 [`example-webbridge`][example-webbridge] 디렉터리 안에 웹뷰를, Android TV 앱은[`example-app-tv`][example-tv] 안에 예제앱을 확인하시기 바랍니다. Android 프로젝트를 열어 Adjust SDK 연동 방법의 예를 확인할 수 있습니다.

### <a id="qs-getting-started"></a>기본 연동

다음은 Adjust SDK를 안드로이드 프로젝트와 연동하기 위해 최소한으로 수행해야 하는 절차입니다. 여기서는 Android Studio를 안드로이드 개발에 사용하고 API 레벨 **9(Gingerbread)** 이상을 대상으로 한다고 가정합니다.

### <a id="qs-add-sdk"></a>프로젝트에 SDK 추가

Maven을 사용하는 경우 `build.gradle` 파일에 다음 라인을 추가합니다.

```gradle
implementation 'com.adjust.sdk:adjust-android:4.21.1'
implementation 'com.android.installreferrer:installreferrer:1.1.2'
```

앱의 웹뷰 내에 있는 Adjust SDK를 사용하려면 다음과 같은 추가 종속성도 포함시키십시오.

```gradle
implementation 'com.adjust.sdk:adjust-android-webbridge:4.21.1'
```

또한 Adjust SDK 및 웹뷰 확장자를 JAR 파일로 추가할 수 있으며, 이 파일은 [릴리스 페이지][releases]에서 다운로드할 수 있습니다.

### <a id="qs-gps"></a>Google Play 서비스 추가

2014년 8월 1일 자로 Google Play Store의 앱은 [Google 광고 ID][google-ad-id]를 사용하여 장치를 고유하게 식별해야 합니다. Adjust SDK에서 Google 광고 ID를 사용할 수 있게 하려면 [Google Play 서비스][google-play-services]를 연동해야 합니다. 이를 아직 수행하지 않은 경우, 앱의 `build.gradle` 파일의 종속성 블록에 다음 종속성을 추가하여 Google Play 서비스 라이브러리에 종속성을 추가하십시오.

```gradle
implementation 'com.google.android.gms:play-services-analytics:16.0.4'
```

**참고:** Adjust SDK는 Google Play 서비스 라이브러리 내 `play-services-analytics` 부분의 어떤 특정 버전에도 연결되어 있지 않습니다. 가장 최신 또는 필요한 버전을 사용하면 됩니다.


### <a id="qs-permissions"></a>권한 추가

Adjust SDK에는 다음 권한이 필요합니다. Adjust SDK에 필요한 다음 권한이 `AndroidManifest.xml`에 없다면 추가하세요.

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Google Play Store가 대상이 **아닌** 경우 다음 권한을 추가합니다.

```xml
<uses-permission android:name="android.permission.WIFI_STATE" />
```

### <a id="qs-proguard"></a>Proguard 설정

Proguard를 사용 중인 경우 다음 행을 Proguard 파일에 추가합니다.

```
-keep class com.adjust.sdk.** { *; }
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

**앱을 Google Play Store에 게시하지 않은 경우**, 다음 `com.adjust.sdk` 패키지 규칙을 사용합니다.

```
-keep public class com.adjust.sdk.** { *; }
```

### <a id="qs-install-referrer"></a>설치 referrer

Adjust는 앱 설치를 소스에 제대로 어트리뷰트하기 위해 **설치 referrer** 관련 정보를 필요로 합니다. 이 정보는 **Google Play Referrer API**를 사용하거나 브로드캐스트 리시버로 **Google Play Store 인텐트**를 잡아 얻을 수 있습니다. 

**중요**: Google Play Referrer API는 보다 안전하고 믿을만한 설치 referrer 정보 획득 방식을 제공하는 동시에 클릭 인젝션으로부터 어트리뷰션 제공자를 보호할 목적으로 Google이 새롭게 도입한 방식입니다. 따라서 앱에서 지원할 것을 **강력히 권장합니다**. Google Play Store 인텐트는 이보다 좀 덜 안전한 설치 referrer 정보 획득 방식입니다. 당분간은 새로운 Google Play referrer API와 공존하지만 향후에는 더 이상 사용하지 않을 예정입니다..

#### <a id="qs-gpr-api"></a>Google Play Referrer API

앱에서 Google Play Referrer API를 지원하려면, [프로젝트에 SDK 추가](#qs-add-sdk)에 설명한 대로 확실하게 실행한 다음 `build.gradle` 파일에 다음 라인을 추가했는지 확인합니다.

```
implementation 'com.android.installreferrer:installreferrer:1.1.2'
```

그리고 [Proguard 설정](#qs-proguard)에서 언급한 내용을 살펴보세요. 특히 이 기능에 필요한 부분이 확실히 추가되었는지도 살펴보세요.

```
-keep public class com.android.installreferrer.** { *; }
```

이 기능은 **Adjust SDK v4.12.0 버전 이상** 사용 시 지원됩니다.

#### <a id="qs-gps-intent"></a>Google Play Store 인텐트

브로드캐스트 리시버로 Google Play Store `INSTALL_REFERRER` 인텐트를 캡쳐해야 합니다. `INSTALL_REFERRER` 수신을 위해 **고유 브로드캐스트 리시버를 사용하고 있지 않다면**, `AndroidManifest.xml` 내 `application` 태그에 다음과 같이 `receiver` 태그를 추가하세요.

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

Adjust는 이 브로드캐스트 리시버를 사용하여 설치 referrer를 검색 후 백엔드에 전달합니다.

`INSTALL_REFERRER` 인텐트 수신을 위해 다른 브로드캐스트 리시버를 사용 중이라면 [이 지침][referrer]에 따라 브로드캐스트 리시버를 추가하세요.

### <a id="qs-integrate-sdk"></a>앱에 SDK 연동

먼저 기본 세션 트래킹을 설정합니다.

### <a id="qs-basic-setup"></a>기본 설정

SDK를 네이티브 앱에 통합하는 경우 [네이티브 앱 SDK](#qs-basic-setup-native) 관련 지침을 따르십시오. 웹뷰 내에서 사용하기 위해 SDK를 통합하는 경우, 아래 [웹뷰 SDK](#qs-basic-setup-web) 관련 지침을 따르십시오.

#### <a id="qs-basic-setup-native"></a>네이티브 앱 SDK

전역 android [응용 프로그램][android-application] 클래스를 사용하여 SDK를 초기화하는 것이 좋습니다. 앱에 이 클래스가 아직 없는 경우 다음 단계를 수행하십시오.

- `Application`을 확장하는 클래스를 만듭니다.
- 앱의 `AndroidManifest.xml` 파일을 열고 `<application>` 요소를 찾습니다.
- `android:name` 특성을 추가하고 새 응용 프로그램 클래스 이름으로 설정합니다.

    앱 예제에서는 이름이 `GlobalApplication`인 `Application` 클래스를 사용합니다. 따라서 매니페스트 파일은 다음과 같이 구성됩니다.

    ```xml
     <application
       android:name=".GlobalApplication"
       <!-- ...-->
    </application>
    ```

- `Application` 클래스에서 `onCreate` 메서드를 추가하거나 만듭니다. 그리고 다음 코드를 추가하여 Adjust SDK를 초기화합니다.

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

    `{YourAppToken}`을 앱 토큰으로 대체합니다. 앱 토큰은 [대시보드][adjust.com]에서 찾을 수 있습니다.

    다음으로 `environment`를 샌드박스 모드나 프로덕션 모드로 설정해야 합니다.

    ```java
    String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
    String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
    ```

**중요:** 이 값은 앱을 테스트하는 경우에만 `AdjustConfig.ENVIRONMENT_SANDBOX`로 설정해야 합니다. 앱을 게시하기 전에 environment를 `AdjustConfig.ENVIRONMENT_PRODUCTION`으로 설정해야 합니다. 개발 및 테스트를 다시 시작할 경우에는 `AdjustConfig.ENVIRONMENT_SANDBOX`로 다시 설정하십시오.

이 environment는 실제 트래픽과 테스트 장치의 테스트 트래픽을 구별하기 위해 사용합니다. 이 단계는 매우 중요하므로 앱에 제대로 구현했는지 반드시 확인하세요.

#### <a id="qs-basic-setup-web"></a>웹뷰 SDK

사용 중인 `WebView` 개체에 대한 참조를 획득한 후,
- `webView.getSettings().setJavaScriptEnabled(true)`를 호출하고 웹뷰에서 자바스크립트를 활성화합니다.
- `AdjustBridge.registerAndGetInstance(getApplication(), webview)`를 호출하여 `AdjustBridgeInstance`의 기본 인스턴스를 시작합니다.
- 이렇게 하면 Adjust 브리지가 웹뷰의 자바스크립트 인터페이스로 등록됩니다.

단계 수행 후 작업이 다음과 같이 보여야 합니다.

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

        AdjustBridge.registerAndGetInstance(getApplication(), webview);
        try {
            webView.loadUrl("file:///android_asset/AdjustExample-WebView.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

이 단계를 완료하면 Adjust 브리지가 앱에 성공적으로 추가됩니다. 웹뷰에서 로드되는 사용자 페이지와 Adjust의 네이티브 안드로이드 SDK간 통신을 위해 이제 자바스크립트 브리지가 활성화됩니다.

HTML 파일에서 자산 폴더의 루트에 위치해 있는 Adjust 자바스크립트 파일을 가져옵니다. HTML 파일이 있으면 다음과 같이 가져옵니다.

```html
<script type="text/javascript" src="adjust.js"></script>
<script type="text/javascript" src="adjust_event.js"></script>
<script type="text/javascript" src="adjust_config.js"></script>
```

자바스크립트 파일에 참조를 추가했으면 이를 HTML 파일에서 사용하여 Adjust SDK를 초기화합니다.

```js
let yourAppToken = '{YourAppToken}';
let environment = AdjustConfig.EnvironmentSandbox;
let adjustConfig = new AdjustConfig(yourAppToken, environment);

Adjust.onCreate(adjustConfig);
```

`{YourAppToken}`을 앱 토큰으로 대체합니다. 앱 토큰은 [대시보드][dashboard]에서 찾을 수 있습니다.

앱을 테스트에 사용할지 아니면 프로덕션에 사용할지에 따라 `environment`를 다음 값 중 하나로 설정해야 합니다.

```js
let environment = AdjustConfig.EnvironmentSandbox;
let environment = AdjustConfig.EnvironmentProduction;
```

**중요**: 이 값은 앱을 테스트하는 경우에만 `AdjustConfig.EnvironmentSandbox`로 설정해야 합니다. 앱을 게시하기 전에 environment를 `AdjustConfig.EnvironmentProduction`으로 설정해야 합니다. 개발 및 테스트를 다시 시작할 경우에는 `AdjustConfig.EnvironmentSandbox`로 다시 설정하십시오.

이 environment는 실제 트래픽과 테스트 장치의 테스트 트래픽을 구별하기 위해 사용합니다. 이 값을 항상 유의미하게 유지해야 합니다!

### <a id="qs-session-tracking"></a>세션 추적

**참고: 이 단계는 매우 중요합니다. 앱에 제대로 구현했는지 반드시 확인하세요**. 구현하게 되면 Adjust SDK가 제공하는 세션 추적을 앱에서 올바르게 활성화할 수 있습니다.

#### <a id="qs-session-tracking-api-14"></a>API 레벨 14 이상

- `ActivityLifecycleCallbacks` 인터페이스를 구현하는 비공개 클래스를 추가합니다. 이 인터페이스에 액세스할 권한이 없다면, 앱의 대상 Android api 레벨이 14보다 낮기 때문입니다. 이러한 [지침](#qs-session-tracking-api-9)에 따라 각 작업을 수동으로 업데이트해야 합니다. 이전에 앱의 각 작업에 대한 `Adjust.onResume` 및 `Adjust.onPause` 호출이 있었을 경우 각 호출을 제거해야 합니다.

- `onActivityResumed(Activity activity)` 메서드를 편집하고 `Adjust.onResume()`에 호출을 추가합니다. 
- `onActivityPaused(Activity activity)` 메서드를 편집하고 `Adjust.onPause()`에 호출을 추가합니다.
-  Adjust SDK가 구성된 `onCreate()` 메서드를 추가하고 `registerActivityLifecycleCallbacks` 호출을 이전에 만든 `ActivityLifecycleCallbacks` 클래스의 인스턴스와 함께 추가합니다.

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

#### <a id="qs-session-tracking-api-9"></a>API 레벨 9-13

앱 `minSdkVersion`이 `9`와 `13` 사이일 경우, 장기 연동 절차를 간소화하기 위해 `14` 이상으로의 업그레이드를 고려해 주십시오. 주요 버전의 최신 시장 점유율을 확인하려면 안드로이드 [대시보드][android-dashboard]를 참조하십시오.

올바른 세션 추적을 위해서는 작업 재개 혹은 중지가 있을 때마다 Adjust SDK 메서드 몇 가지를 호출해야 합니다. **이 작업을 위해서는 앱 내 각 작업 시 다음 단계를 수행해야 합니다**.

- 작업 `onResume` 메소드에서 `Adjust.onResume()` 메소드를 호출합니다. 필요 시에는 이 메소드를 만듭니다.
- 작업 `onPause` 메소드에서 `Adjust.onPause()` 메소드를 호출합니다. 필요 시에는 이 메소드를 만듭니다.

단계 수행 후 작업이 다음과 같이 보여야 합니다.

```java
    import com.adjust.sdk.Adjust;
// ...
public class YourActivity extends Activity {
    protected void onResume() {
        super.onResume();
        Adjust.onResume();
    }
    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }
    // ...
}
```

**모든** 앱 액티비티에서 이같은 단계를 반복해야 합니다. 앞으로 새 작업을 생성할 때마다 이러한 단계를 반복하십시오. 코딩 유형에 따라 액티비티 전체에서 이를 공통 슈퍼클래스로 설정해야 할 수도 있습니다.

### <a id="qs-sdk-signature"></a>SDK 서명

계정 매니저가 Adjust SDK 서명을 활성화해야 합니다. 이 기능을 사용해 보고자 할 경우 Adjust 지원 팀(support@adjust.com)으로 연락해 주십시오.

SDK 서명이 계정에서 이미 사용 가능 상태로 Adjust 대시보드에서 App Secret에 액세스할 수 있는 상태라면, 아래 메소드를 사용하여 SDK 서명을 앱에 연동하십시오.

Config 인스턴스에서 `setAppSecret`를 호출하면 App Secret이 설정됩니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

### <a id="qs-adjust-logging"></a>Adjust 로그 기록

다음 파라미터 중 하나를 사용하여 Config 인스턴스에서 `setLogLevel`을 호출하면 테스트에 표시되는 로그의 양을 늘리거나 줄일 수 있습니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

로그 출력을 모두 비활성화하려면 억제할 로그 레벨을 설정하고 config 개체에 대한 생성자를 사용합니다(이 개체는 억제 로그 레벨에 대한 지원 필요 여부를 나타내는 부울 파라미터를 가집니다).

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

### <a id="qs-build-the-app"></a>앱 빌드

안드로이드 앱을 작성하고 실행합니다. `LogCat` 뷰어에서 `tag:Adjust` 필터를 설정하여 다른 모든 로그를 숨길 수 있습니다. 앱이 시작된 후에 다음 Adjust 로그가 표시됩니다. `Install tracked`.


## 딥링크

### <a id="dl"></a>딥링크 개요

URL에서 앱으로 딥링크를 거는 옵션이 있는 Adjust 트래커 URL을 사용하고 있다면, 딥링크 URL과 그 내용 관련 정보를 얻을 가능성이 있습니다. 해당 URL 클릭 시 사용자가 이미 앱을 설치한 상태(기본 딥링크)일 수도, 앱을 설치하지 않은 상태(지연된 딥링크)일 수도 있습니다. 기본 딥링크 상황에서 안드로이드는 딥링크 내용에 관한 정보 인출을 기본 지원합니다. 안드로이드는 지연된 딥링크를 기본 지원하지 않지만, Adjust SDK는 지연된 딥링크 정보를 인출하는 메커니즘을 제공합니다.

### <a id="dl-standard"></a>기본 딥링크 시나리오

사용자가 앱을 설치하고 `deep_link` 파라미터가 들어간 Adjust 트래커 URL을 클릭 시 런칭하도록 하려 할 경우, 앱에 딥링크를 활성화해야 합니다. 이는 원하는 고유 스킴명 (scheme name) 을 선택할 때 이루어집니다. 사용자가 `AndroidManifest.xml` 파일에서 트래커 URL을 선택한 후 앱을 열면 실행하려는 작업에 이를 할당하게 됩니다. `intent-filter` 섹션을 매니페스트 파일 내 원하는 작업 정의에 추가하고 `android:scheme` 어트리뷰션값을 원하는 스킴명과 함께 배정하면 됩니다.


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

이 설정이 끝나고 트래커 URL 클릭 시 앱이 런칭되도록 하려면, 배정한 스킴명을 Adjust 트래커 URL의 `deep_link` 파라미터에 사용해야 합니다. 딥링크에 정보가 추가되지 않은 트래커 URL은 아래와 같이 보일 것입니다.

```
https://app.adjust.com/abc123?deep_link=adjustExample%3A%2F%2F
```
  
URL 내 `deep_link` 파라미터 값은 **URL 인코딩이 되어야 한다**는 사실을 명심하세요.

앱이 위와 같이 설정된 상황에서 이 트래커 URL을 클릭하면 `MainActivity` 인텐트와 함께 앱이 런칭됩니다. `MainActivity` 클래스에서 `deep_link` 파라미터 내용이 자동으로 제공됩니다. URL에서 인코딩이 된 상태라 해도 내용이 전달된 후에는 **인코딩이 이루어지지 않습니다**.

`AndroidManifest.xml` 파일 내 `android:launchMode` 작업 설정에 따라 `deep_link` 파라미터 내용 정보가 작업 파일 내 적절한 위치로 전달됩니다. `android:launchMode` 어트리뷰션 값에 대한 더 자세한 정보는 [안드로이드 문서][android-launch-modes]에서 확인하세요.

딥링크 내용 정보는 `Intent` 개체를 통해 원하는 작업 내 `onCreate` 메서드 또는 `onNewIntent` 메서드로 전달됩니다. 앱을 런칭하고 두 개 메서드 중 하나가 촉발되면 실제 딥링크가 클릭한 URL 내 `deep_link` 파라미터로 전달되게 할 수 있습니다. 이렇게 하면 이 정보를 사용하여 앱에서 추가적 로직을 수행할 수 있게 됩니다.   

딥링크 내용은 이들 두 개 메서드에서 다음과 같이 추출할 수 있습니다.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Intent intent = getIntent();
    Uri data = intent.getData();
    // data.toString() -> This is your deep_link parameter value.
}
```

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    Uri data = intent.getData();
    // data.toString() -> This is your deep_link parameter value.
}
```

### <a id="dl-deferred"></a>지연된 딥링크 시나리오

지연된 딥링크는 사용자가 `deep_link` 파라미터가 들어 있는 Adjust 트래커 URL을 클릭했으나, 그 시점에 장치에 앱을 설치하지 않은 경우 발생합니다. 클릭 후 사용자는 Play Store로 재이동하여 앱을 다운로드하게 됩니다. 링크를 처음 연 후 `deep_link` 파라미터 내용이 앱으로 전달됩니다.

Adjust SDK는 기본적으로 지연된 딥링크를 엽니다. 추가 구성은 필요하지 않습니다.

#### 지연된 딥링크 콜백

Adjust SDK가 지연된 딥링크를 열도록 할지를 제어하려면 config 개체의 콜백 메서드로 이를 수행할 수 있습니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

// Evaluate the deeplink to be launched.
config.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
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

Adjust SDK가 백엔드에서 딥링크 내용 정보를 수신하면 그 내용이 리스너에 전달되며 `boolean` 리턴값을 요청합니다. 리턴값은 (기본 딥링크에서와 마찬가지로) Adjust SDK가 스킴명을 배정한 작업을 딥링크에서 런칭할 것인지 여부를 표시합니다.

`true`를 반환하면 작업이 실행되어 [기본 딥링크 시나리오](#dl-standard)챕터에서 설명한 시나리오를 실행합니다. SDK가 작업을 런칭하기를 원하지 않는다면, 리스너에서 `false`를 리턴하여 딥링크 내용을 토대로 앱에서 다음 작업을 어떻게 실행할 지 스스로 정할 수 있습니다.

</td>
</tr>
<tr>
<td>
<b>웹뷰 SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setDeferredDeeplinkCallback(function (deeplink) {});

Adjust.onCreate(adjustConfig);
```

이러한 지연된 딥링크 시나리오에서는 config 개체에 설정할 수 있는 추가 설정이 하나 있습니다. Adjust SDK가 지연된 딥링크 정보를 얻으면 SDK가 URL을 열도록 할지 선택할 수 있습니다. Config 개체에서 `setOpenDeferredDeeplink`메서드를 호출하여 이 옵션을 설정합니다.


```js
// ...

function deferredDeeplinkCallback(deeplink) {}

let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setOpenDeferredDeeplink(true);
adjustConfig.setDeferredDeeplinkCallback(deferredDeeplinkCallback);

Adjust.start(adjustConfig);

```

콜백을 설정하지 않으면 **Adjust SDK가 기본적으로 항상 URL 런칭을 시도하게 됩니다**.

</td>
</tr>
</table>

### <a id="dl-reattribution"></a>딥링크를 통한 리어트리뷰션

Adjust를 통해 딥링크를 사용하여 재관여 캠페인을 실행할 수 있습니다. 더 자세한 정보는 [공식 문서][reattribution-with-deeplinks]에서 확인하십시오.

이 기능을 사용 중이라면, 사용자를 올바로 리어트리뷰트하기 위해 앱에서 호출을 한 가지 더 수행해야 합니다.

앱에서 딥링크 내용을 수신했다면, `Adjust.appWillOpenUrl(Uri, Context)` 메서드 호출을 추가하세요. 이 호출이 이루어지면 Adjust SDK는 딥링크 내에 새로운 어트리뷰션 정보가 있는지 확인하고, 새 정보가 있으면 Adjust 백엔드로 송신합니다. 딥링크 정보가 담긴 Adjust 트래커 URL을 클릭한 사용자를 리어트리뷰트해야 할 경우 앱에서 해당 사용자의 새 어트리뷰션 정보로 [어트리뷰션 콜백](#af-attribution-callback)이 촉발되는 것을 확인할 수 있습니다.  

`Adjust.appWillOpenUrl(Uri, Context)` 호출은 다음과 같이 이루어집니다.

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

**참고**: `Adjust.appWillOpenUrl (Uri)` 메소드는 **Android SDK v4.14.0** 기준으로 사용되지 않게 되었습니다. 따라서 `Adjust.appWillOpenUrl (Uri, Context)` 메서드를 사용하시기 바랍니다.

**웹뷰 참고 사항**: 다음과 같은 자바스크립트의 함수 `Adjust.appWillOpenUrl`을 사용하여 웹뷰에서 이러한 호출을 할 수 있습니다.

```js
Adjust.appWillOpenUrl(deeplinkUrl);
```

## 이벤트 추적

### <a id="et-tracking"></a>이벤트 추적

Adjust를 사용하여 앱의 모든 이벤트를 추적할 수 있습니다. 버튼의 모든 탭을 추적하려고 한다고 가정합니다. 버튼의 모든 탭을 추적하려면 [대시보드][dashboard]에서 새 이벤트 토큰을 만들어야 합니다. 예를 들어 이벤트 토큰이 `abc123`일 경우 버튼의 `onClick` 메서드에 다음 라인을 추가하여 클릭을 추적할 수 있습니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

### <a id="et-revenue"></a>매출 추적

사용자가 광고를 누르거나 인앱 구매를 할 때 매출이 발생하는 경우, 이벤트를 사용하여 해당 매출을 추적할 수 있습니다. 예를 들어 탭 한 번이 1유로센트 가치인 경우 아래와 같이 매출 이벤트를 추적할 수 있습니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.setRevenue(0.01, 'EUR');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

물론 콜백 파라미터와도 연결할 수 있습니다.

통화 토큰을 설정할 경우, Adjust가 자동으로 들어오는 매출을 미리 지정한 보고용 통화로 전환해 줍니다. [통화 전환][currency-conversion]에서 확인하세요.

인앱 구매를 추적하려면 구매가 완료되고 실제 품목이 구매된 경우에만 `trackEvent`를 호출하십시오. 이는 실제로 발생하지 않은 매출을 추적하지 않도록 하기 위해 필요한 중요 조치입니다.

Adjust에서 매출과 이벤트를 추적하는 것에 대한 자세한 내용은 [이벤트 추적 가이드][event-tracking]를 참조하십시오.

### <a id="et-revenue-deduplication"></a>매출 중복 제거

거래 ID를 선택 사항으로 추가하여 매출 중복 추적을 피할 수 있습니다. 가장 최근에 사용한 거래 ID(Order ID) 10개를 기억하며, 똑같은 거래 ID(Order ID)로 이루어진 매출 이벤트는 중복 집계하지 않습니다. 인앱 구매 추적 시 특히 유용합니다. 사용 예는 아래에 나와 있습니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.setRevenue(0.01, 'EUR');
adjustEvent.setOrderId('{OrderId}');
Adjust.trackEvent(event);
```
</td>
</tr>
</table>

### <a id="et-purchase-verification"></a>인앱 구매 검증

Adjust의 서버 측 수신 확인 도구인 [구매 검증(Purchase Verification)][android-purchase-verification]을 사용하여 앱에서 이루어지는 구매의 유효성을 확인할 수 있습니다. 더 자세한 내용을 확인하려면 링크를 클릭하십시오.

## 사용자 지정 파라미터

### <a id="cp"></a>사용자 지정 파라미터 개요

Adjust SDK가 기본적으로 수집하는 데이터 포인트 이외에도 Adjust SDK를 사용하여 추적하고 필요한 만큼 사용자 지정 값(사용자 ID, 제품 ID 등)을 이벤트나 세션에 추가할 수 있습니다. 사용자 지정 파라미터는 미가공 데이터로만 사용 가능하며 Adjust 대시보드에 표시되지 않습니다.

자체 내부 용도로 수집한 값에는 **콜백 파라미터**를 사용해야 하고 외부 파트너와 공유하는 값에는 **파트너 파라미터**를 사용해야 합니다. 값(예: 제품 ID)을 내부 용도와 외부 파트너 용도로 모두 추적하는 경우, 콜백 파라미터와 파트너 파라미터를 모두 사용하여 추적하는 것이 좋습니다.

### <a id="cp-event-parameters"></a>이벤트 파라미터

### <a id="cp-event-callback-parameters"></a>이벤트 콜백 파라미터

[대시보드][dashboard]에서 이벤트 콜백 URL을 등록할 수 있습니다. 이벤트를 추적할 때마다 GET 요청이 해당 URL로 전송됩니다. 이벤트를 추적하기 전에 이벤트 인스턴스에서 `addCallbackParameter`를 호출하여 콜백 파라미터를 해당 이벤트에 추가할 수 있습니다. 그러면 해당 파라미터가 콜백 URL에 추가됩니다.

예를 들어 URL `http://www.example.com/callback`을 등록했다면 이벤트를 다음과 같이 추적할 수 있습니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.addCallbackParameter('key', 'value');
adjustEvent.addCallbackParameter('foo', 'bar');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

이 경우에는 이벤트를 추적하고 다음 주소로 요청이 전송됩니다.

```
http://www.example.com/callback?key=value&foo=bar
```

파라미터 값으로 사용할 수 있는 `{gps_adid}`와 같은 다양한 자리 표시자(placeholder)를 지원합니다. 그 결과로 생성한 콜백에서 이 자리 표시자는 현재 기기의 Google Play 서비스 ID로 대체됩니다. 사용자 지정 파라미터는 저장되지 않으며 콜백에만 추가됩니다. 이벤트에 대한 콜백을 등록하지 않은 경우 해당 파라미터는 읽을 수 없습니다.

사용 가능한 값의 전체 목록을 포함한 URL 콜백 사용에 대한 자세한 내용은 [콜백 설명서][callbacks-guide]를 참조하십시오.

### <a id="cp-event-partner-parameters"></a>이벤트 파트너 파라미터

Adjust 대시보드에서 활성화된 연동에 대해 네트워크 파트너로 전송할 파라미터도 추가할 수 있습니다.

이 파라미터는 위에서 언급한 콜백 파라미터와 유사하게 작동합니다. 이벤트 인스턴스에 `addPartnerParameter` 메서드를 호출하여 파라미터를 추가하십시오.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
</td>
</tr>
<tr>
<td>

```js
let adjustEvent = new AdjustEvent('abc123');
adjustEvent.addPartnerParameter('key', 'value');
adjustEvent.addPartnerParameter('foo', 'bar');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
</table>

특별 파트너와 그 연동에 대한 자세한 내용은 [특별 파트너 설명서][special-partners]를 참조하십시오.

### <a id="cp-event-callback-id"></a>이벤트 콜백 식별자

추적하고자 하는 각 이벤트에 개별 스트링 ID를 따로 붙일 수도 있습니다. 나중에 이벤트 성공/실패 콜백에서 해당 ID에 전달하여 이벤트 트래킹의 성공 또는 실패 여부를 추적할 수 있게 해 줍니다. Event 인스턴스에서 `setCallbackId`메서드를 호출하여 설정할 수 있습니다:

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

### <a id="cp-session-parameters"></a>세션 파라미터

일부 파라미터는 Adjust SDK 이벤트 및 세션 발생시마다 전송을 위해 저장합니다. 어느 파라미터든 한 번 저장하면 로컬에 바로 저장되므로 매번 새로 추가할 필요가 없습니다. 같은 파라미터를 두 번 저장해도 효력이 없습니다.

이 세션 파라미터는 설치 시에도 전송할 수 있도록 Adjust SDK 런칭 전에도 호출할 수 있습니다. 설치 시에 전송하지만 필요한 값은 런칭 후에야 들어갈 수 있게 하고 싶다면 Adjust SDK 런칭 시 [예약 시작](#cp-delay-start)을 걸 수 있습니다.

### <a id="cp-session-callback-parameters"></a>세션 콜백 파라미터

[이벤트](#event-callback-parameters)에 등록한 콜백 파라미터는 Adjust SDK 전체 이벤트 및 세션 시 전송할 목적으로 저장할 수 있습니다.

세션 콜백 파라미터는 이벤트 콜백 파라마터와 비슷한 인터페이스를 지녔습니다. 이벤트에 키, 값을 추가하는 대신 `Adjust.addSessionCallbackParameter(String key, String value)` 메서드를 호출하여 추가합니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

세션 콜백 파라미터는 이벤트에 추가된 콜백 파라미터와 합쳐집니다. 이벤트에 추가된 콜백 파라미터는 세션 콜백 파라미터보다 우선합니다. 이는 세션에서 추가된 것과 같은 키로 이벤트에 콜백 파라미터를 추가한 경우, 이벤트에 추가된 콜백 파라미터 값이 우선한다는 의미입니다.

원하는 키를 아래와 같은 메서드로 전달하여 특정 세션 콜백 파라미터를 제거할 수 있습니다. `Adjust.removeSessionCallbackParameter(String key)`.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

세션 콜백 파라미터의 키와 값을 전부 없애고 싶다면 `Adjust.resetSessionCallbackParameters()` 메서드로 재설정하면 됩니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

### <a id="cp-session-partner-parameters"></a>세션 파트너 파라미터

Adjust SDK 내 모든 이벤트 및 세션에서 전송되는 [세션 콜백 파라미터](#session-callback-parameters)가 있는 것처럼, 세션 파트너 파라미터도 있습니다.

이들 파라미터는 Adjust [대시보드][dashboard]에서 연동을 활성화한 네트워크 파트너에게 전송할 수 있습니다.

세션 파트너 파라미터는 이벤트 파트너 파라미터와 인터페이스가 비슷합니다. 이벤트에 키, 값을 추가하는 대신 `Adjust.addSessionPartnerParameter(String key, String value)` 메서드를 호출하여 추가합니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

세션 파트너 파라미터는 이벤트에 추가한 파트너 파라미터와 합쳐집니다. 이벤트에 추가된 파트너 파라미터는 세션 파트너 파라미터보다 우선시됩니다. 세션에서와 같은 키로 이벤트에 파트너 파라미터를 추가한 경우, 새로 추가한 파트너 파라미터가 우선권을 가집니다.

원하는 키를 `Adjust.removeSessionPartnerParameter(String key)` 메서드로 전달하여 특정 세션 파트너 파라미터를 제거할 수 있습니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

세션 파트너 파라미터의 키와 값을 전부 없애고 싶다면 `Adjust.resetSessionPartnerParameters()` 메서드로 재설정하면 됩니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

### <a id="cp-delay-start"></a>예약 시작(delay start)

Adjust SDK에 예약 시작을 걸면 앱이 고유 식별자 등의 세션 파라미터를 얻어 설치 시에 전송할 시간을 벌 수 있습니다.

Config 인스턴스의 `setDelayStart` 메소드에서 예약 시작 시각을 초 단위로 설정하세요.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

이 경우 Adjust SDK는 최초 인스톨 세션 및 생성된 이벤트를 5.5초간 기다렸다가 전송합니다. 이 시간이 지난 후, 또는 그 사이에 `Adjust.sendFirstPackages()`을 호출했을 경우 모든 세션 파라미터가 지연된 인스톨 세션 및 이벤트에 추가되며 Adjust SDK는 원래대로 돌아옵니다.

**Adjust SDK의 최대 지연 예약 시작 시간은 10초입니다.**


## 추가 기능

Adjust SDK를 프로젝트와 연동한 후에는 다음 기능을 사용할 수 있습니다.

### <a id="af-push-token"></a>푸시 토큰 (uninstall tracking)

푸시 토큰은 Audience Builder와 클라이언트 콜백에 사용되며, 앱 제거(uninstall) 및 재설치 (reinstall) 트래킹을 위해 필수입니다.

푸시 알림 토큰을 전송하려면 앱에서 토큰을 받거나 값 변화가 있을 때마다 아래와 같이 Adjust에 대한 호출을 추가하세요.

<table>
<tr>
<td>
<b>네이티브 SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.setPushToken(pushNotificationsToken, context);
```

`context`를 추가하여 업데이트된 이 서명을 사용하여 SDK가 더욱 다양한 상황에서 푸시 토큰을 전송할 수 있습니다. 위의 서명 메서드를 사용하는 것이 좋습니다.

하지만, `context`가 없는 동일한 메서드의 이전 서명도 계속해서 지원됩니다.

</td>
</tr>
<tr>
<td>
<b>웹뷰 SDK</b>
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

### <a id="af-attribution-callback"></a>어트리뷰션 콜백

리스너(listener)를 등록하여 트래커 어트리뷰션 변경 알림을 받을 수 있습니다. 어트리뷰션에서 고려하는 소스가 각각 다르기 때문에 이 정보는 동시간에 제공할 수 없습니다.

해당 [어트리뷰션 데이터 정책][attribution-data]을 반드시 고려하세요.

Config 인스턴스를 사용하여 어트리뷰션 콜백을 추가한 다음 SDK를 시작하십시오.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
    @Override
    public void onAttributionChanged(AdjustAttribution attribution) {}
});

Adjust.onCreate(config);
```
</td>
</tr>
<tr>
<td>
<b>웹뷰 SDK</b>
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

리스너 함수는 SDK가 최종 어트리뷰션 데이터를 접수한 후 호출됩니다. 리스너 함수에서는 `attribution` 파라미터에 액세스할 수 있습니다. 해당 파라미터에 대한 개요는 다음과 같습니다.

- `trackerToken` 현재 설치된 트래커 토큰.
- `trackerName` 현재 설치된 트래커 이름.
- `network` 현재 설치된 네트워크 그룹화 기준.
- `campaign` 현재 설치된 캠페인 그룹화 기준.
- `adgroup` 현재 설치된 광고 그룹 그룹화 기준.
- `creative` 현재 설치된 크리에이티브 그룹화 기준.
- `clickLabel` 현재 설치된 클릭 레이블.
- `adid` Adjust 기기 식별자.

### <a id="af-ad-revenue"></a>광고 매출 트래킹

**참고**: 이 기능은 네이티브 SDK v4.18.0 이상에서만 사용할 수 있습니다.

다음 메서드를 호출하여 Adjust SDK로 광고 매출 정보를 트래킹할 수 있습니다.

<table> 
<tr> 
<td> 
<b>네이티브 앱 SDK</b>
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

전달해야 하는 메서드 파라미터는 다음과 같습니다.
- `source` - 광고 매출 정보의 소스를 나타내는 `String` 객체
- `payload` - 광고 매출 JSON을 포함하는 `JSONObject` 객체

애드저스트는 현재 다음의 `source` 파라미터 값을 지원합니다.
- `AD_REVENUE_MOPUB` - MoPub 미디에이션 플랫폼을 나타냄(자세한 정보는 [연동 가이드][sdk2sdk-mopub] 확인)

### <a id="af-session-event-callbacks"></a>세션 및 이벤트 콜백

리스너를 등록하여 이벤트나 세션 추적 시 알림을 받을 수 있습니다. 리스너에는 4가지가 있습니다. 이벤트 추적 성공, 이벤트 추적 실패, 세션 추적 성공, 그리고 세션 추적 실패입니다. 아래와 같은 config 개체를 생성한 다음 필요한 만큼 리스너를 추가하십시오.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
</td>
</tr>
<tr>
<td>

```java
AdjustConfig config = new AdjustConfig(this, appToken, environment);

// Set event success tracking delegate.
config.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
    @Override
    public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
        // ...
    }
});

// Set event failure tracking delegate.
config.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
    @Override
    public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
        // ...
    }
});

// Set session success tracking delegate.
config.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
    @Override
    public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
        // ...
    }
});

// Set session failure tracking delegate.
config.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
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
<b>웹뷰 SDK</b>
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

리스너 함수는 SDK가 서버에 패키지 전송을 시도한 후에 호출됩니다. 리스너 함수에서 리스너에 대한 특정 응답 데이터 개체에 액세스할 수 있습니다. 세션 성공 시 응답 데이터 개체 필드 개요는 다음과 같습니다.

- `message` 서버에서 전송한 메시지 또는 SDK가 기록한 오류.
- `timestamp` 서버에서 전송한 데이터의 타임스탬프.
- `adid` Adjust가 제공하는 고유 기기 식별자.
- `jsonResponse` 서버로부터의 응답이 있는 JSON 개체.

이벤트 응답 데이터 개체 두 가지에는 다음 정보가 포함됩니다.

- `eventToken` 추적 패키지가 이벤트인 경우 이벤트 토큰.
- `callbackId`: 이벤트 개체에 설정된 사용자 지정 [콜백 ID](#cp-event-callback-id) 문자열.

그리고 이벤트 및 세션 실패 개체에는 다음 정보도 포함됩니다.

- boolean `willRetry` 나중에 패키지 재전송 시도가 있을 것임을 나타냅니다.

### <a id="af-user-attribution"></a>사용자 어트리뷰션

[어트리뷰션 콜백 섹션](#af-attribution-callback)에서 설명한 바와 같이, 이 콜백은 변동이 있을 때마다 새로운 어트리뷰션 관련 정보를 전달할 목적으로 촉발됩니다. 사용자의 현재 어트리뷰션 값 관련 정보를 언제든 억세스하고 싶다면, `Adjust` 인스턴스의 다음 메서드를 호출하면 됩니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

**참고**: 이 호출은 **Adjust SDK v4.11.0** 이상 버전에서만 수행할 수 있습니다.

**참고**: 사용자의 현재 어트리뷰션 관련 정보는 Adjust 백엔드가 앱 인스톨을 추적하여 최초 어트리뷰션 콜백이 촉발된 후에만 얻을 수 있습니다. 그 순간부터 Adjus SDK는 사용자 어트리뷰션 정보를 갖게 되며 이 메서드로 액세스할 수 있습니다.

### <a id="af-device-ids"></a>기기 ID

Adjust SDK로 기기 식별자 몇 가지를 획득할 수 있습니다.

### <a id="af-gps-adid"></a>Google Play 서비스 광고 식별자

Google Analytics와 같은 서비스를 사용하려면 중복 보고가 발생하지 않도록 기기 ID와 클라이언트 ID를 조정해야 합니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
</td>
</tr>
<tr>
<td>
  
Google 광고 ID가 필요한 경우 제한 사항으로 인해 백그라운드 스레드에서만 ID를 읽을 수 있습니다. `getGoogleAdId` 함수를 컨텍스트 및 `OnDeviceIdsRead` 인스턴스와 함께 호출하면 상황에 관계 없이 작동합니다.

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
<b>웹뷰 SDK</b>
</td>
</tr>
<tr>
<td>

기기의 Google 광고 기기 식별자를 얻으려면 콜백 함수를 `Adjust.getGoogleAdId`로 전달해야 합니다. 이 콜백 함수는 다음과 같이 인수에 Google 광고 ID를 받습니다.

```js
Adjust.getGoogleAdId(function(googleAdId) {
    // ...
});
```
</td>
</tr>
</table>

### <a id="af-amazon-adid"></a>Amazon 광고 식별자

Amazon 광고 ID를 얻으려면 `Adjust` 인스턴스에서 다음 메서드를 호출하면 됩니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

### <a id="af-adid"></a>Adjust 기기 식별자

Adjust 백엔드는 앱을 인스톨한 장치에서 고유한 Adjust 기기 식별자 (adid)를 생성합니다. 이 식별자를 얻으려면 `Adjust` 인스턴스에서 다음 메서드를 호출하면 됩니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

**참고**: 이 호출은 **Adjust SDK v4.11.0** 이상 버전에서만 수행할 수 있습니다.

**참고**: adid 관련 정보는 Adjust 백엔드가 앱 설치를 추적한 후에만 얻을 수 있습니다. 따라서 SDK가 초기화되고 앱 설치 추적이 성공적으로 이루어지기 전에는 adid 액세스가 불가능합니다.


### <a id="af-pre-installed-trackers"></a>사전 설치 트래커

Adjust SDK를 사용하여 앱이 사전 설치된 장치를 지닌 사용자를 인식하고 싶다면 다음 절차를 따르세요.

- [대시보드][dashboard]에 새 트래커를 생성합니다.

- 앱 델리게이트를 열고 config 인스턴스의 기본값 트래커를 다음과 같이 설정합니다.

  <table>
  <tr>
  <td>
  <b>네이티브 앱 SDK</b>
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
  <b>웹뷰 SDK</b>
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

- `{TrackerToken}`을 2에서 생성한 트래커 토큰으로 대체합니다. 대시보드에는 트래커 URL(`http://app.adjust.com/` 포함)이 표시된다는 사실을 명심하시기 바랍니다. 소스코드에서는 전체 URL을 표시할 수 없으며 6자로 이루어진 토큰만을 명시해야 합니다.

- 앱 빌드를 실행하세요. LogCat에서 다음 라인을 볼 수 있을 것입니다.

``` 
Default tracker: 'abc123'
```

### <a id="af-offline-mode"></a>오프라인 모드

Adjust SDK를 오프라인 모드로 전환하여 서버로 전송하는 작업을 일시 중단하고 추적 데이터를 보관하여 나중에 보낼 수 있습니다. 오프라인 모드일 때는 모든 정보가 파일에 저장되므로 너무 많은 이벤트를 촉발(trigger)하지 않도록 주의하십시오.

`setOfflineMode`를 `true`로 설정하여 호출하면 오프라인 모드를 활성화할 수 있습니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

`setOfflineMode`를 `false`로 설정한 상태로 호출하면 오프라인 모드를 비활성화할 수 있습니다. Adjust SDK를 다시 온라인 모드로 전환하면 저장된 정보가 모두 올바른 시간 정보와 함께 Adjust 서버로 전송됩니다.

**추적 사용 중지와 달리 이 설정은 세션 간에 기억되지 않습니다.** 따라서 앱을 오프라인 모드에서 종료한 경우에도 SDK는 항상 온라인 모드로 시작됩니다.

### <a id="af-disable-tracking"></a>추적 사용 중지

`setEnabled`를 `false` 파라미터로 설정한 상태로 호출하면 Adjust SDK에서 현재 장치의 모든 작업 추적을 중지할 수 있습니다. **이 설정은 세션 간에 기억됩니다.**

<table>
<tr>
<td>
<b>Native App SDK</b>
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

`isEnabled` 함수를 호출하여 Adjust SDK가 현재 사용 가능한지 확인할 수 있습니다. 파라미터가 `true`로 설정된 `setEnabled`를 호출하면 Adjust SDK를 언제든 활성화할 수 있습니다.


### <a id="af-event-buffering"></a>이벤트 버퍼링

앱이 이벤트 추적을 많이 사용하는 경우, 매 분마다 배치(batch) 하나씩만 보내도록 하기 위해 일부 HTTP 요청을 지연시키고자 할 경우가 있을 수 있습니다. config 인스턴스로 이벤트 버퍼링을 활성화할 수 있습니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

### <a id="af-background-tracking"></a>백그라운드 추적

Adjust SDK 기본값 행위는 앱이 백그라운드에 있을 동안에는 HTTP 요청 전송을 잠시 중지하는 것입니다. config 인스턴스에서 이를 변경할 수 있습니다.

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

### <a id="af-gdpr-forget-me"></a>GDPR(일반 개인정보 보호법) 상의 잊힐 권리

유럽연합(EU) 일반 개인정보 보호법 제 17조에 의거하여, 사용자가 잊힐 권리를 행사하였을 경우 Adjust에 이를 통보할 수 있습니다. 다음 메서드를 호출하면 Adjust SDK는 사용자가 잊힐 권리를 사용하기로 했음을 Adjust 백엔드에 전달합니다:

<table>
<tr>
<td>
<b>네이티브 앱 SDK</b>
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
<b>웹뷰 SDK</b>
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

이 정보를 받는 즉시 Adjust는 사용자의 데이터를 삭제하며 Adjust SDK는 해당 사용자 추적을 중단합니다. 향후 이 기기로부터 어떤 요청도 Adjust에 전송되지 않습니다.

테스트를 하는 경우라도 이 결정은 영구적으로 적용됩니다. **즉, 이후에 되돌릴 수 없습니다.**

## 테스트 및 문제 해결

### <a id="tt-session-failed"></a>“세션 실패(Ignoring too frequent session...)” 오류가 표시됩니다.

이 오류는 일반적으로 설치를 테스트할 때 발생합니다. 앱을 제거하고 다시 설치해도 새 설치를 촉발할 수 없습니다. 서버에서는 SDK가 로컬에서 집계한 세션 데이터를 유실했다고 판단하며 서버에 제공된 장치 관련 정보에 따라 오류 메시지를 무시합니다.

이 동작은 테스트 중 불편을 초래할 수도 있지만, sandbox 동작이 프로덕션 환경과 최대한 일치하도록 하기 위해 필요합니다.

앱에 편집자 수준의 액세스 권한(또는 더 높은 권한)이 있는 경우 모든 기기의 앱 세션 데이터를 Adjust 대시보드에서 [테스트 콘솔][testing_console]을 사용하여 재설정할 수 있습니다.

기기가 메모리에서 삭제되면 링크에서 `Forgot device`만 반환됩니다. 기기가 이미 메모리에서 삭제되었거나 값이 올바르지 않으면 `Advertising ID not found` 메시지가 반환됩니다.

기기를 제거하면 GDPR 잊혀질 권리 호출을 되돌릴 수 없게 됩니다.

현재 패키지에서 액세스 권한을 주는 경우 [개발자 API][dev_api]를 사용하여 기기를 검사하고 제거할 수도 있습니다.

### <a id="tt-broadcast-receiver"></a>브로드캐스트 리시버가 설치 참조 페이지를 제대로 캡처하고 있나요?

[설명서](#qs-gps-intent) 지침을 따랐다면 브로드캐스트 리시버는 설치 referrer를 Adjust SDK와 Adjust 서버로 보내도록 구성됩니다.

테스트 설치 참조 페이지를 수동으로 촉발시켜 이 구성을 테스트할 수 있습니다. `com.your.appid`를 앱 ID로 대체하고 Android Studio와 함께 제공되는 [adb](http://developer.android.com/tools/help/adb.html) 도구를 사용하여 다음 명령을 실행합니다.

```
adb shell am broadcast -a com.android.vending.INSTALL_REFERRER -n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver --es "referrer" "adjust_reftag%3Dabc1234%26tracking_id%3D123456789%26utm_source%3Dnetwork%26utm_medium%3Dbanner%26utm_campaign%3Dcampaign"
```

이미 다른 브로드캐스트 리시버를 INSTALL_REFERRER 인텐트용으로 사용 중이면서 이 [설명서][referrer]를 따른 경우, `com.adjust.sdk.AdjustReferrerReceiver`를 사용자의 브로드캐스트 리시버로 대체합니다.

`-n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver` 파라미터를 제거하여 기기의 모든 앱에서 `INSTALL_REFERRER` 인텐트를 수신하도록 할 수도 있습니다.

로그 레벨을 `verbose`로 설정하면 referrer 확인 시 로그가 보일 것입니다.

```
V/Adjust: Reading query string (adjust_reftag=abc1234&tracking_id=123456789&utm_source=network&utm_medium=banner&utm_campaign=campaign) from reftag
```

클릭 패키지를 SDK의 패키지 핸들러에 추가:

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
```

앱 시작 전에 이 테스트를 실시하면 전송할 패키지가 보이지 않습니다. 패키지는 앱이 시작된 후에 전송됩니다.

**중요**: 이 기능 테스트에 `adb` 툴 사용을 권장하지 않는다는 사실에 주의해 주십시오. (여러 개의 파라미터를 &로 구분해 놓은 경우) 참조자 내용 전체를 `adb`에서 테스트한다면, 브로드캐스트 수신자에 들어가게 하기 위해 해당 내용을 인코딩해야 합니다. 인코딩을 하지 않으면 `adb`에서 첫 번째 & 다음에 나오는 참조자 내용을 잘라 버리기 때문에 브로드캐스트 수신자에 올바르지 않은 내용이 전달됩니다.

인코딩되지 않은 참조자 값이 앱에서 어떻게 수신되는지 보려면 아래 Adjust의 예제 앱을 참조하십시오. 전달되는 내용 중 `MainActivity.java` 파일에 있는 `onFireIntentClick` 메서드 내 인텐트로 촉발되는 부분을 다르게 써 본 후 그 결과를 확인할 수 있습니다.

```java
public void onFireIntentClick(View v) {
    Intent intent = new Intent("com.android.vending.INSTALL_REFERRER");
    intent.setPackage("com.adjust.examples");
    intent.putExtra("referrer", "utm_source=test&utm_medium=test&utm_term=test&utm_content=test&utm_campaign=test");
    sendBroadcast(intent);
}
```

`putExtra` 메서드에서 두 번째 파라미터를 필요한 대로 바꿔 쓰시면 됩니다.

### <a id="tt-event-at-launch"></a>응용 프로그램 런칭 시 이벤트를 촉발할 수 있나요?

직관적으로 생각하는 것과는 다를 수 있습니다. 이유는 다음과 같습니다.

전역 `Application` 클래스의 `onCreate` 메서드는 응용 프로그램이 시작될 때뿐만 아니라, 시스템 또는 응용 프로그램 이벤트가 앱에 의해 캡처될 때도 호출됩니다.

Adjust SDK는 이 때 초기화가 준비되지만 실제로 시작되지는 않습니다. 작업 시작 시, 즉 사용자가 실제로 앱을 시작할 경우에만 실제로 시작됩니다.

따라서 이 때 이벤트를 촉발해도 원하는 작업이 수행되지 않습니다. 이런 호출은 사용자가 앱을 시작하지 않은 경우에도 외부 요인에 따라 결정되는 시간에 맞춰 Adjust SDK를 시작하고 이벤트를 보냅니다.

따라서 응용 프로그램 시작 시 이벤트를 촉발시키면 추적하는 설치 및 세션 수가 부정확해집니다.

설치 후에 이벤트를 촉발시키려면  [어트리뷰션 콜백](#af-attribution-callback)을 사용하십시오.

앱이 시작될 때 이벤트를 촉발시키려면 시작된 작업의 `onCreate` 메서드를 사용하십시오.

[dashboard]:  http://adjust.com
[adjust.com]: http://adjust.com

[en-readme]:  ../../README.md
[zh-readme]:  ../chinese/README.md
[ja-readme]:  ../japanese/README.md
[ko-readme]:  ../korean/README.md

[example-java]:                   ../../Adjust/example-app-java
[example-kotlin]:                 ../../Adjust/example-app-kotlin
[example-tv]:                     ../../Adjust/example-app-tv
[example-webbridge]:              ../../Adjust/example-app-webbridge

[maven]:                          http://maven.org
[referrer]:                       ../korean/multiple-receivers.md
[releases]:                       https://github.com/adjust/adjust_android_sdk/releases
[google-ad-id]:                   https://support.google.com/googleplay/android-developer/answer/6048248?hl=en
[event-tracking]:                 https://docs.adjust.com/ko/event-tracking
[callbacks-guide]:                https://docs.adjust.com/ko/callbacks
[new-referrer-api]:               https://developer.android.com/google/play/installreferrer/library.html
[special-partners]:               https://docs.adjust.com/ko/special-partners
[attribution-data]:               https://github.com/adjust/sdks/blob/master/doc/attribution-data.md
[android-dashboard]:              http://developer.android.com/about/dashboards/index.html
[currency-conversion]:            https://docs.adjust.com/ko/event-tracking/#part-7
[android-application]:            http://developer.android.com/reference/android/app/Application.html
[android-launch-modes]:           https://developer.android.com/guide/topics/manifest/activity-element.html
[google-play-services]:           http://developer.android.com/google/play-services/setup.html
[reattribution-with-deeplinks]:   https://docs.adjust.com/ko/deeplinking/#part-6-1
[android-purchase-verification]:  https://github.com/adjust/android_purchase_sdk/tree/master/doc/korean
[testing_console]:                https://docs.adjust.com/en/testing-console/#how-to-clear-your-advertising-id-from-adjust-between-tests
[dev_api]:                        https://docs.adjust.com/en/adjust-for-developers/

[sdk2sdk-mopub]:                  ../korean/sdk-to-sdk/mopub.md


## <a id="license"></a>라이선스

Adjust SDK는 MIT 라이선스에 따라 사용이 허가되었습니다.

Copyright (c) 2012-2019 Adjust GmbH, http://www.adjust.com

이로써 본 소프트웨어와 관련 문서 파일(이하 "소프트웨어")의 복사본을 받는 사람에게는 아래 조건에 따라 소프트웨어를 제한 없이 다룰 수 있는 권한이 무료로 부여됩니다.이 권한에는 소프트웨어를 사용, 복사, 수정, 병합, 출판, 배포 및/또는 판매하거나 2차 사용권을 부여할 권리와 소프트웨어를 제공 받은 사람이 소프트웨어를 사용, 복사, 수정, 병합, 출판, 배포 및/또는 판매하거나 2차 사용권을 부여하는 것을 허가할 수 있는 권리가 제한 없이 포함됩니다.

위 저작권 고지문과 본 권한 고지문은 소프트웨어의 모든 복사본이나 주요 부분에 포함되어야 합니다.

소프트웨어는 상품성, 특정 용도에 대한 적합성 및 비침해에 대한 보증 등을 비롯한 어떤 종류의 명시적이거나 암묵적인 보증 없이 "있는 그대로" 제공됩니다. 어떤 경우에도 저작자나 저작권 보유자는 소프트웨어와 소프트웨어의 사용 또는 기타 취급에서 비롯되거나 그에 기인하거나 그와 관련하여 발생하는 계약 이행 또는 불법 행위 등에 관한 배상 청구, 피해 또는 기타 채무에 대해 책임지지 않습니다.
