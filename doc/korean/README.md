## 요약

Adjust™의 Android SDK에 관한 문서입니다. Adjust™에 대한 자세한 정보는 [adjust.com]에서 확인하십시오.


제공되는 언어: [English][en-readme], [中文][zh-readme], [日本語][ja-readme], [한국어][ko-readme].

## 목차


### 빠른 시작

   * [앱 예시](#qs-example-apps)
   * [시작하기](#qs-getting-started)
      * [프로젝트에 SDK 추가](#qs-add-sdk)
      * [Google Play 서비스 추가](#qs-gps)
      * [권한 추가](#qs-permissions)
      * [Proguard 설정](#qs-proguard)
      * [설치 리퍼러](#qs-install-referrer)
         * [Google Play 리퍼러 API](#qs-gpr-api)
         * [Google Play Store intent](#qs-gps-intent)
         * [Huawei 리퍼러 API](#qs-huawei-referrer-api)
   * [앱에 SDK 연동](#qs-integrate-sdk)
      * [기본 설정](#qs-basic-setup)
         * [네이티브 앱 SDK](#qs-basic-setup-native)
         * [웹뷰 SDK](#qs-basic-setup-web)
      * [세션 트래킹](#qs-session-tracking)
         * [API 레벨 14 및 이상](#qs-session-tracking-api-14)
         * [API 레벨 9~13](#qs-session-tracking-api-9)
      * [SDK 서명](#qs-sdk-signature)
      * [Adjust 로깅(logging)](#qs-adjust-logging)
      * [앱 빌드하기](#qs-build-the-app)

## 딥링크

   * [딥링크 개요](#dl)
   * [표준 딥링크 시나리오](#dl-standard)
   * [디퍼드 딥링크 시나리오](#dl-deferred)
   * [딥링크를 통한 리어트리뷰션](#dl-reattribution)

### 이벤트 추적

   * [이벤트 추적](#et-tracking)
   * [매출 추적](#et-revenue)
   * [매출 중복 제거](#et-revenue-deduplication)
   * [인앱 구매 검증](#et-purchase-verification)

### 커스텀 파라미터

   * [커스텀 파라미터 개요](#cp)
   * [이벤트 파트너 파라미터](#cp-event-partner-parameters)
      * [이벤트 콜백 파라미터](#cp-event-callback-parameters)
      * [이벤트 파트너 파라미터](#cp-event-partner-parameters)
      * [이벤트 콜백 식별자](#cp-event-callback-id)
   * [세션 파라미터](#cp-session-parameters)
      * [세션 콜백 파라미터](#cp-session-callback-parameters)
      * [세션 파트너 파라미터](#cp-session-partner-parameters)
      * [지연 시작](#cp-delay-start)

### 부가 기능

   * [푸시 토큰(삭제 트래킹)](#af-push-token)
   * [어트리뷰션 콜백](#af-attribution-callback)
   * [광고 매출 트래킹](#af-ad-revenue)
   * [구독 트래킹](#af-subscriptions)
   * [세션 및 이벤트 콜백](#af-session-event-callbacks)
   * [유저 어트리뷰션](#af-user-attribution)
   * [기기 ID](#af-device-ids)
      * [Google Play 서비스 광고 식별자](#af-gps-adid)
      * [Amazon 광고 식별자](#af-amazon-adid)
      * [Adjust 기기 식별자](#af-adid)
   * [사전 설치 앱](#af-preinstalled-apps)
   * [오프라인 모드](#af-offline-mode)
   * [트래킹 비활성화](#af-disable-tracking)
   * [이벤트 버퍼링](#af-event-buffering)
   * [백그라운드 트래킹](#af-background-tracking)
   * [GDPR 잊혀질 권리(Right to be Forgotten)](#af-gdpr-forget-me)
   * [서드파티 공유](#af-third-party-sharing)
      * [서드파티 공유 비활성화](#af-disable-third-party-sharing)
      * [Enable third-party sharing](#af-enable-third-party-sharing)
   * [Consent measurement](#af-measurement-consent)

### 테스트 및 문제 해결

   * ["세션 실패(너무 빈번한 세션 거부 )" 오류가 발생한 경우](#tt-session-failed)
   * [브로드캐스트 리시버가 설치 리퍼러를 포착하는지 확인하고 싶은 경우](#tt-broadcast-receiver)
   * [앱 실행 시 이벤트 트리거 가능 여부](#tt-event-at-launch)

### 라이센스


## 빠른 시작

### <a id="qs-example-apps"></a>예시 앱

[`example-app-java`][example-java], [`example-app-kotlin`][example-kotlin],[`example-app-keyboard`][example-keyboard] 디렉터리에는 Android 예시 앱이 있으며, [`example-webbridge` directory][example-webbridge] 안에는 웹뷰를 사용하는 예시 앱, [`example-app-tv`][example-tv] 디렉터리 안에는 Android TV 예시 앱이 있습니다. Android 프로젝트를 열어 Adjust SDK가 어떻게 연동될 수 있는지 예시를 확인할 수 있습니다.

### <a id="qs-getting-started"></a>시작하기

다음은 Android 앱에 Adjust SDK를 연동하는 데 필요한 최소한의 단계입니다. 본 설명에서는 Android 앱 개발에 Android 스튜디오를 사용하고 있다고 가정하겠습니다. Adjust SDK 연동에 필요한 Android API의 최소 지원 레벨은 **9 (Gingerbread)** 입니다.

### <a id="qs-add-sdk"></a>프로젝트에 SDK 추가하기

Maven을 사용하는 경우, 다음을 `build.gradle` 파일에 추가하시기 바랍니다.

```gradle
implementation 'com.adjust.sdk:adjust-android:4.28.2'
implementation 'com.android.installreferrer:installreferrer:2.2'
```

앱의 웹뷰 안에서 Adjust SDK를 사용하고자 하는 경우, 다음의 추가적인 dependency를 추가하시기 바랍니다.

```gradle
implementation 'com.adjust.sdk:adjust-android-webbridge:4.28.2'
```

**참고**: 웹뷰 확장에 필요한 Android API의 최소 지원 레벨은 17(Jelley Bean)입니다.

Adjust SDK와 웹뷰 확장을 JAR 파일로 추가할 수도 있습니다. 이는 Adjust의 [releases page][releases]에서 다운로드하실 수 있습니다.

### <a id="qs-gps"></a>Google Play Services 추가하기

2014년 8월 1일 이후부터 Google Play Store의 모든 앱은 고유 기기 식별을 위해 반드시 [Google Advertising ID][google-ad-id]를 사용해야 합니다. Adjust SDK에서 Google 광고 ID를 활성화하려면, [Google Play Services][google-play-services]를 반드시 연동해야 합니다. 이 과정을 아직 완료하지 않았다면, Google Play Services 라이브러리에 dependency를 추가하시기 바랍니다. 이는 앱의 `build.gradle` 파일의 `dependencies` 블록에 다음의 dependency를 추가하면 됩니다.

```gradle
implementation 'com.google.android.gms:play-services-ads-identifier:17.0.0'
```

**참고**: Adjust SDK는 Google Play Services 라이브러리의 `play-services-analytics`의 그 어떤 버전과도 연결되어있지 않습니다. 라이브러리에서 최신 버전을 사용하거나 필요한 버전을 사용하시기 바랍니다.

### <a id="qs-permissions"></a>권한 추가하기

Adjust SDK는 다음의 권한을 필요로합니다. 아직 다음의 권한이 보이지 않는다면 이를 `AndroidManifest.xml` 파일에 추가하시기 바랍니다.

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

**Google Play Store를 타게팅하지 않는 경우**, 다음의 권한을 반드시 추가해야 합니다.

```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
```

### <a id="qs-proguard"></a>Proguard 설정

Proguard를 사용하는 경우, Proguard 파일에 다음 줄을 추가하세요.

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

**Google Play Store**에 앱을 게시하지 않는 경우, 다음의 `com.adjust.sdk` 패키지 규칙을 사용하시기 바랍니다.

```
-keep public class com.adjust.sdk.** { *; }
```

### <a id="qs-install-referrer"></a>설치 리퍼러

앱 설치를 소스에 올바르게 어트리뷰션하기 위해 Adjust는 **설치 리퍼러**에 관한 정보가 필요합니다. 이는 2가지 방식으로 가능합니다: **Google Play 리퍼러 API***를 사용하거나 **Google Play Store 인텐트**를 브로드캐스트 리시버로 수집.

**중요**: Google은 보다 안전하고 신뢰할 수 있는 방식으로 설치 리퍼러를 획득할 수 있도록 지원하고, 클릭 주입에 대응할 수 있도록 하기 위해 Google Play 리퍼러 API를 도입했습니다. Adjust는 앱에서 이를 반드시 지원하기를 **강력히 권고드립니다**. Google Play 스토어 인텐트는 상대적으로 덜 안전한 설치 리퍼러 획득 방식입니다. 당분간은 새로운 Google Play 리퍼러 API와 함께 사용 가능하지만, 향후 지원이 중단될 예정입니다.

#### <a id="qs-gpr-api"></a>Google Play 리퍼러 API

앱에서 Google Play 리퍼러 API를 지원하려면 [프로젝트에 SDK 추가하기](#qs-add-sdk) 부분을 참조하여 올바르게 수행하고, 다음의 줄을 `build.gradle` 파일에 추가하시기 바랍니다.

```
implementation 'com.android.installreferrer:installreferrer:2.2'
```

[Proguard 설정](#qs-proguard)의 설명을 올바르게 수행하시기 바랍니다. 언급된 모든 규칙을 비롯하여 특히 본 기능을 위해 필요한 규칙을 추가했는지 확인하시기 바랍니다.

```
-keep public class com.android.installreferrer.** { *; }
```

**Adjust SDK v4.12.0 이상 버전**을 사용 중인 경우 이 기능이 지원됩니다.

#### <a id="qs-gps-intent"></a>Google Play Store 인텐트

**참고**: Google은 [발표](https://android-developers.googleblog.com/2019/11/still-using-installbroadcast-switch-to.html)를 통해 2021년 3월 1일자로 리퍼러 정보 전달에 사용되는 `INSTALL_REFERRER` 인텐트 사용에 대한 지원을 중단한다고 발표했습니다. 만약 리퍼러 정보를 얻기 위해 해당 방식을 사용하고 있다면, [Google Play 리퍼러 API](#qs-gpr-api) 방식으로 전환하시기 바랍니다.

브로드캐스트 리시버를 통해 Google Play Store `INSTALL_REFERRER` 인텐트를 캡쳐할 수 있습니다. `INSTALL_REFERRER` 인텐트를 수신하기 위한 목적으로 **자체 브로드캐스트 리시버를 사용하고 있지 않은 경우**, `AndroidManifest.xml` 내 `application` 태그에 다음의 `receiver` 태그를 추가하시기 바랍니다.

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

Adjust는 본 브로드캐스트 리시버를 사용하여 설치 리퍼러를 조회하고 백엔드로 전송합니다.

`INSTALL_REFERRER` 인텐트에 다른 브로드캐스트 리시버를 사용하고 있다면, Adjust 브로드캐스트 리시버에 메시지를 적절하게 보낼 수 있도록 본 [리퍼러 가이드]를 참조하시기 바랍니다.

#### <a id="qs-huawei-referrer-api"></a>Huawei 리퍼러 API

Adjust SDK 4.21.1 버전부터는 Huawei 앱 갤러리 버전이 10.4 이상인 Huawei 기기에 설치 추적을 지원합니다. Huawei 리퍼러 API를 사용하기 위해 추가적인 연동 단계를 수행하지 않아도 됩니다.

### <a id="qs-integrate-sdk"></a>앱에 SDK 연동하기

우선 기본 세션 트래킹 설정에 관해 다루도록 하겠습니다.

### <a id="qs-basic-setup"></a>기본 설정

네이티브 앱에 SDK를 연동하는 경우 [네티이브 앱 SDK](#qs-basic-setup-native)를 참조하시기 바랍니다. 웹뷰 내 사용을 위해 SDK를 연동하는 경우 아래의 [웹뷰 SDK](#qs-basic-setup-web)를 참조하시기 바랍니다.

#### <a id="qs-basic-setup-native"></a>네이티브 앱 SDK

Adjust는 SDK 초기화를 위해 글로벌 Android [어플리케이션][android-application] 클래스를 사용하는 것을 추천합니다. 앱에 아직 없다면 다음의 단계를 수행하시기 바랍니다:

- `Application`을 확장하는 클래스를 생성합니다.
- `AndroidManifest.xml` 파일을 열고, `<application>` 요소를 찾습니다.
- `android:name` 어트리뷰션을 추가하고, 신규 어플리케이션 클래스의 이름으로 설정합니다.

    Adjust의 예시 앱에서는 `GlobalApplication`라고 명명된 `Application` 클래스를 사용합니다. 이에 따라 매니페스트 파일을 다음과 같이 구성합니다.
    ```xml
     <application
       android:name=".GlobalApplication"
       <!-- ... -->
     </application>
    ```

- `Application` 클래스에서, `onCreate` 메서드를 찾거나 생성합니다. 다음의 코드를 추가하여 Adjust SDK를 초기화합니다.

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

'{YourAppToken}'을 사용 중인 앱 토큰으로 교체한 다음, [Dashboard]에서 결과를 확인해 보세요.

이후 `environment`를 샌드박스나 기타 프로덕션 모드로 설정해야 합니다.

```java
String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
```

**중요:** 이 값은 앱을 테스트하는 상황에서만`AdjustConfig.ENVIRONMENT_SANDBOX`로 설정해야 합니다. 앱을 퍼블리시하기 전, 환경을 `AdjustConfig.ENVIRONMENT_PRODUCTION`으로 설정하시기 바랍니다. 앱 개발 및 테스트를 새로 시작하는 경우, 환경을 다시 `AdjustConfig.ENVIRONMENT_SANDBOX`로 설정하시기 바랍니다.

Adjust는 테스트 기기로 인해 발생하는 테스트 트래픽과 실제 트래픽을 구분하기 위해 이와 같이 각기 다른 환경을 사용하므로, 반드시 상황에 알맞은 환경을 설정하시기 바랍니다.

#### <a id="qs-basic-setup-web"></a>웹뷰 SDK

`WebView` 객체에 대한 레퍼런스를 획득한 이후:

- `webView.getSettings().setJavaScriptEnabled(true)`를 호출하여 웹뷰에서 자바스크립트를 활성화합니다.
- `AdjustBridgeInstance`의 기본값 인스턴스를 시작합니다. 이는 `AdjustBridge.registerAndGetInstance(getApplication(), webview)`를 호출하면 됩니다.
- 이는 또한 Adjust 브릿지를 자바스크립트 인터페이스로서 웹뷰에 등록하게 됩니다.
- 필요한 경우 `AdjustBridge.setWebView()`를 호출하여 새로운 `WebView`를 설정합니다.  
- `AdjustBridge.unregister()`를 호출하여 `AdjustBridgeInstance`와 `WebView`의 등록을 취소합니다.  

위 단계를 모두 완료하면 액티비티는 다음과 같아야 합니다.

```java
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) \{
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
    
    @Override
    protected void onDestroy() {
        AdjustBridge.unregister();

        super.onDestroy();
    }
}
```

본 단계를 완료하면 Adjust 브릿지가 앱에 성공적으로 추가됩니다. 이제 Javascript 브릿지가 활성화되어 Adjust의 네티이브 Android SDK와 페이지 간에 커뮤니케이션이 가능하며, 이는 웹뷰에 로딩됩니다.

asset 폴더에 있는 Adjust Javascript트 파일을 HTML 파일에 가져오시기 바랍니다. HTML 파일도 해당 폴더에 있는 경우 다음과 같이 가져오시기 바랍니다.

```html
<script type="text/javascript" src="adjust.js"></script>
<script type="text/javascript" src="adjust_event.js"></script>
<script type="text/javascript" src="adjust_third_party_sharing.js"></script>
<script type="text/javascript" src="adjust_config.js"></script>
```

Javascript 파일에 레퍼런스를 추가한 뒤, HTML 파일에서 이를 사용하여 Adjust SDK를 초기화하시기 바랍니다.

```js
let yourAppToken = '{YourAppToken}';
let environment = AdjustConfig.EnvironmentSandbox;
let adjustConfig = new AdjustConfig(yourAppToken, environment);

Adjust.onCreate(adjustConfig);
```

'{YourAppToken}'을 사용 중인 앱 토큰으로 교체한 다음, [Dashboard]에서 결과를 확인해 보세요.

이후 현재 상황이 테스트 상황인지 프로덕션 모드인지에 따라 `environment`를 이에 상응하는 값으로 설정합니다.

```js
let environment = AdjustConfig.EnvironmentSandbox;
let environment = AdjustConfig.EnvironmentProduction;
```

**중요:** 앱을 테스트하는 경우에만 값을 `AdjustConfig.EnvironmentSandbox`로 설정하시기 바랍니다. 앱을 게시하기 전, 환경을 `AdjustConfig.EnvironmentProduction`로 설정하시기 바랍니다. 앱 개발 및 테스트를 새로 시작한다면 `AdjustConfig.EnvironmentSandbox`로 다시 설정하시기 바랍니다.

테스트 기기로 인해 발생하는 테스트 트래픽과 실제 트래픽을 구분하기 위해 다른 환경모드를 사용하고 있음으로, 상황에 알맞은 설정을 적용하시기 바랍니다.

### <a id="qs-session-tracking"></a>세션 트래킹

**중요**: 본 단계는 **매우 중요합니다**. **앱에서 다음의 사항을 올바르게 수행**하시기 바랍니다. 본 단계를 올바르게 완료해야만 Adjust SDK가 앱에서의 세션을 트래킹할 수 있습니다.

#### <a id="qs-session-tracking-api-14"></a>API 레벨 14 이상

- `ActivityLifecycleCallbacks` 인터페이스를 시행하는 비공개 클래스를 추가합니다. 본 인터페이스에 액세스할 수 없다면 앱이 타게팅하는 Android API 레벨이 14 이하인 것입니다. 다음의 [설명](#qs-session-tracking-api-9)을 참조하여 각 액티비티를 수동으로 업데이트하시기 바랍니다. `Adjust.onResume`와 `Adjust.onPause` 콜이 앱의 액티비티에 각각 있다면, 이를 삭제해야 합니다.
- `onActivityResumed(Activity activity)` 메서드를 수정하고, `Adjust.onResume()`에 콜을 추가합니다. 그리고
`onActivityPaused(Activity activity)` 메서드를 수정하고, `Adjust.onPause()`에 콜을 추가합니다.
- `onCreate()` 메서드를 Adjust SDK에 추가하고, `registerActivityLifecycleCallbacks`를 생성된 `ActivityLifecycleCallbacks` 클래스의 인스턴스와 전송합니다.

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

#### <a id="qs-session-tracking-api-9"></a>API 9~13 레벨

gradle의 앱 `minSdkVersion`이 `9`에서 `13` 사이인 경우 연동 과정의 간소화를 위해 이를 최소 `14`로 업데이트하시기 바랍니다. 공식 Android [대시보드][android-dashboard]에서 주요 버전의 최신 시장 점유율 정보를 확인하시기 바랍니다.

세션 트래킹을 적절하게 수행하기 위해, 액티비티가 재개되거나 중단된 경우 Adjust SDK 메서드가 호출됩니다. (이렇게 하지 않으면 SDK가 세션 시작과 종료를 놓칠 수 있음.) 이를 위해 앱의 **각 액티비티마다** 다음의 단계를 수행하시기 바랍니다.

- 액티비티의 `onResume` 메서드에서 `Adjust.onResume()`를 호출합니다. 필요한 경우 본 메서드를 생성합니다.
- 액티비티의 `onPause` 메서드에서 `Adjust.onPause()`를 호출합니다. 필요한 경우 본 메서드를 생성합니다.

위 단계를 모두 완료하면 액티비티는 다음과 같아야 합니다.

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

앱에서 **각 액티비티에 대하여** 위 단계를 반복하시기 바랍니다. 새로운 액티비티 생성 시, 위 단계를 반드시 반복하시기 바랍니다. 코딩 스타일에 따라, 모든 액티비티에 대해 공통 슈퍼클래스에서 본 단계를 이행할 수도 있습니다.

### <a id="qs-sdk-signature"></a>SDK 서명

계정 관리자는 애드저스트 SDK 서명을 활성화해야 합니다. 이 기능의 사용에 관심이 있는 경우 Adjust 고객 지원팀(support@adjust.com)에 문의하시기 바랍니다.

SDK 서명이 이미 계정에서 활성화되어 있으며 Adjust 대시보드의 App Secret에 액세스할 수 있는 경우, 아래 방법을 사용하여 SDK 서명을 앱에 연동하세요.

앱 시크릿은 설정 인스턴스에서 `setAppSecret`를 호출하여 설정됩니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

### <a id="qs-adjust-logging"></a>Adjust 로그

다음 파라미터 중 하나를 통해 config 인스턴스에서 `setLogLevel`을 호출하여 테스트하는 동안 조회할 로그의 양을 늘리거나 줄일 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

로그 출력을 모두 비활성화하려면, 로그 레벨을 'suppress'로 설정하고, config 객체에 생성자를 사용하시기 바랍니다. (그러면 suppress 로그 수준의 지원 여부를 나타내는 부울 자료 파라미터가 열립니다.)

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

### <a id="qs-build-the-app"></a>앱 빌드하기

Android 앱을 빌드하고 실행합니다. `LogCat` 뷰어에서 필터를 `tag:Adjust`로 설정하여 그 외 모든 로그를 숨깁니다. 앱이 실행된 이후 다음의 Adjust 로그가 나타나야 합니다:`Install tracked`

## 딥링크

### <a id="dl"></a>딥링크 개요

Adjust 트래커 URL에 딥링크를 활성화한 경우, 딥링크 URL과 그 콘텐츠에 대한 정보를 받아볼 수 있습니다. 유저는 앱이 기기에 설치되었거나(표준 딥링크 시나리오) 앱이 설치되지 않은 경우(디퍼드 딥링크 시나리오)에 관계없이 URL과 모두 상호작용할 수 있습니다. 표준 딥링크 시나리오에서 Android 플랫폼은 딥링크 콘텐츠에 관한 정보를 수신할 수 있는 가능성을 네이티브로 제공합니다. Android 플랫폼은 디퍼드 딥링크 시나리오를 자동으로 지원하지 않습니다. 이 경우, Adjust SDK는 딥링크 콘텐츠의 정보를 얻기 위해 필요한 메커니즘을 제공합니다.

### <a id="dl-standard"></a>표준 딥링크 시나리오

이미 앱을 설치한 유저에 대하여 `deep_link` 파라미터가 포함된 Adjust 트래커 URL과 인게이지한 이후 앱이 실행되게 하고 싶다면, 앱에서 딥링크를 활성화하시기 바랍니다. 이는 원하는 **고유의 스킴 이름***을 선택하여 수행할 수 있습니다. `AndroidManifest.xml` 파일에서 유저가 트래커 URL을 클릭한 이후 앱이 열릴 때 실행시키고 싶은 활동에 이를 부여합니다. 매니페스트 파일에 `intent-filter` 섹션을 원하는 활동 정의에 추가하고, 원하는 스킴 이름과 함께 `android:scheme` 속성 값을 부여합니다.

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

트래커 URL이 클릭 된 이후 앱이 실행되도록 하고 싶다면, Adjust 트래커 URL의 `deep_link` 파라미터에서 부여한 스킴 이름을 사용합니다. 딥링크에 별도의 정보가 추가되지 않은 트래커 URL은 아래와 같이 나타납니다.

```
https://app.adjust.com/abc123?deep_link=adjustExample%3A%2F%2F
```

URL에서 `deep_link` 파라미터를 **반드시 url 인코딩**해야 한다는 점을 기억하시기 바랍니다.

앱이 위와 같이 설정되고 나면 유저가 트래커 URL을 클릭할 때 앱이 `MainActivity` 인텐트와 함께 실행되게 됩니다. `MainActivity` 클래스에서는 자동으로 `deep_link` 파라미터 콘텐츠에 관한 정보를 수신할 수 있습니다. 콘텐츠를 수신하고 나면 이는 (URL에서는 인코딩되었을지라도) 인코딩되지 **않습니다**.

`AndroidManifest.xml` 파일 내에서의 `android:launchMode` 활동 설정은 활동 파일 내에서 `deep_link` 파라미터의 콘텐츠 전송 위치를 결정할 것입니다. `android:launchMode` 속성의 가능한 값에 대한 자세한 정보는 Android의 [공식 문서][android-launch-modes]를 참조하시기 바랍니다.

원하는 활동 내의 딥링크 콘텐츠 정보는 `Intent` 객체를 통해 `onCreate` 또는 `onNewIntent` 메서드로 전달됩니다. 앱을 실행하고 이러한 메서드 중 하나를 트리거하면, 클릭 URL 안의 `deep_link` 파라미터에서 전송된 실제 딥링크를 수신할 수 있습니다. 이 정보를 사용하여 앱에서 추가적인 로직을 수행할 수 있습니다.

딥링크 콘텐츠는 위의 두 가지 메서드 중 하나를 사용하여 다음과 같이 내보내기 할 수 있습니다.

```java
@Override
protected void onCreate(Bundle savedInstanceState) \{
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Intent intent = getIntent();
    Uri data = intent.getData();
    // data.toString() -> deep_link 파라미터 값입니다.
}
```

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    Uri data = intent.getData();
    // data.toString() -> deep_link 파라미터 값입니다.
}
```

### <a id="dl-deferred"></a>디퍼드 딥링크 시나리오

디퍼드 딥링크 시나리오는 유저가 `deep_link` 파라미터를 포함한 Adjust의 트래커 URL을 클릭하였으나 클릭한 시점에 기기에 앱이 설치되어있지 않은 경우에 발생합니다. 이 경우 유저가 URL을 클릭하면 앱을 설치할 수 있도록 Play Store로 리디렉션됩니다. 앱을 최초 실행 시 `deep_link` 파라미터 콘텐츠가 앱으로 전달됩니다.

Adjust SDK는 기본값 설정에 따라 디퍼드 딥링크를 엽니다. 따라서 별도의 설정이 필요 없습니다.

#### 디퍼드 딥링크 콜백

Adjust SDk가 디퍼드 딥링크를 여는 것을 통제하고 싶다면 config 객체에서 콜백 메서드를 사용할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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

Adjust SDK가 백엔드로부터 딥링크 정보를 수신하면, SDK는 이 내용을 수신기를 통해 전송하고 사용자로부터 `boolean` 값을 반환받을 것을 기대합니다. 반환 값은 Adjust SDK가 딥링크에서 스킴 이름이 부여된 활동을 실행해야 할지 말아야 할지에 대한 사용자의 결정을 보여줍니다. (표준 딥링크 시나리오와 유사)

즉, 반환 값이 `true`인 경우 Adjust는 해당 활동을 실행하고, [표준 딥링크 시나리오](#dl-standard) 챕터에서 명시된 시나리오를 트리거합니다. SDK가 해당 활동을 실행하는 것을 원하지 않는다면 수신기에서 `false` 값을 반환하고, (딥링크 콘텐츠에 따라) 앱에서 다음에 무엇을 해야 할지 결정하시기 바랍니다.
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
adjustConfig.setDeferredDeeplinkCallback(function (deeplink) {});

Adjust.onCreate(adjustConfig);
```

이 디퍼드 딥링크 시나리오에서는 cofig 객체에서 설정할 수 있는 추가적인 설정이 있습니다. Adjust SDK가 디퍼드 딥링크 정보를 획득하면, SDK가 이 URL을 열도록 할지 선택할 수 있습니다. cofig 객체에서 `setOpenDeferredDeeplink` 메서드를 호출하여 이 옵션을 설정할 수 있습니다.

```js
// ...

function deferredDeeplinkCallback(deeplink) {}

let adjustConfig = new AdjustConfig(yourAppToken, environment);
adjustConfig.setOpenDeferredDeeplink(true);
adjustConfig.setDeferredDeeplinkCallback(deferredDeeplinkCallback);

Adjust.start(adjustConfig);

```

콜백을 설정하지 않으면 **Adjust는 기본값 설정에 따라 항상 URL 실행을 시도할 것입니다.**
</td>
</tr>
<table>

### <a id="dl-reattribution"></a>딥링크를 통한 리어트리뷰션

Adjust는 딥링크를 통해 리인게이지먼트 캠페인을 집행할 수 있도록 지원합니다. 자세한 정보는 Adjust의 [공식 문서][reattribution-with-deeplinks]를 참조하시기 바랍니다.

이 기능을 사용하려면, 앱에서 Adjust SDK로 추가적인 콜을 수행하여 Adjust가 적절하게 유저를 리어트리뷰션할 수 있도록 해야 합니다.

앱에서 딥링크 콘텐츠를 수신했으면 `Adjust.appWillOpenUrl(Uri, Context)` 메서드에 콜을 추가합니다. 이 콜을 수행함으로써 Adjust SDK는 Adjust 백엔드로 정보를 전송하여 딥링크 내에 새로운 어트리뷰션 정보가 있는지 확인할 수 있습니다. 딥링크 콘텐츠를 포함하는 Adjust 트래커 URL의 클릭에 유저가 리어트리뷰션된 경우, 앱에서는 해당 유저에 대해 새로운 어트리뷰션 정보와 함께 [어트리뷰션 콜백](#af-attribution-callback)이 트리거 됩니다.

`Adjust.appWillOpenUrl(Uri, Context)`는 다음과 같이 나타납니다.

```java
@Override
protected void onCreate(Bundle savedInstanceState) \{
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

**참고**: `Adjust.appWillOpenUrl(Uri)` 메서드는 Android SDK v4.14.0부터 **지원 중단**되었습니다. 따라서, `Adjust.appWillOpenUrl(Uri, Context)` 메서드를 대신 사용하시기 바랍니다.

**웹뷰 참고**: 웹뷰에서도 본 콜을 자바스크립트의 `Adjust.appWillOpenUrl` 기능을 통해 다음과 같이 수행할 수 있습니다.

```js
Adjust.appWillOpenUrl(deeplinkUrl);
```

## 이벤트 추적

### <a id="et-tracking"></a>이벤트 트래킹

Adjust를 사용하여 앱의 모든 이벤트를 트래킹할 수 있습니다. 버튼을 탭하는 행동을 모두 트래킹하고 싶으시다면, [대시보드]에서 새로운 이벤트 토큰을 생성해야 합니다. 이벤트 토큰이 abc123`이라고 가정해 보겠습니다. 버튼의 `onClick` 메서드에 다음의 라인을 추가하여 클릭을 트래킹할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

### <a id="et-revenue"></a>매출 트래킹

유저가 광고를 탭하거나 인앱 구매를 통해 매출을 창출할 수 있는 경우, 이벤트를 통해 해당 매출을 트래킹할 수 있습니다. 광고를 한번 누르는 행위에 €0.01의 매출 금액이 발생한다고 가정해 보겠습니다. 매출 이벤트를 다음과 같이 추적할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
adjustEvent.setRevenue(0.01, 'EUR');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<table>

이는 콜백 파라미터와도 함께 사용할 수 있습니다.

사용자가 통화 토큰을 설정하면, Adjust는 사용자의 선택에 따라 발생 매출을 선택한 보고 매출로 자동 전환합니다. 자세한 [통화 변환][currency-conversion] 정보는 여기에서 확인하시기 바랍니다.

인앱 결제를 트래킹하려는 경우, 구매가 완료되고 아이템이 결제되었을 때만 `trackEvent`를 호출하시기 바랍니다. 이렇게 하면 실제로 발생하지 않은 매출을 추적하는 것을 방지할 수 있습니다.

매출과 이벤트 트래킹에 대한 자세한 정보는 Adjust의 [이벤트 트래킹 가이드][event-tracking]를 참조하시기 바랍니다.

### <a id="et-revenue-deduplication"></a>매출 중복 제거

중복되는 매출 트래킹하는 것을 방지하기 위해 주문 ID를 선택적으로 추가할 수 있습니다. 마지막 10개의 주문 ID가 보관되며, 중복되는 주문 ID가 있는 매출 이벤트는 건너뛰게 됩니다. 이러한 방식은 인앱 결제 추적에 특히 유용합니다. 아래 예시를 참조하시기 바랍니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
adjustEvent.setRevenue(0.01, 'EUR');
adjustEvent.setOrderId('{OrderId}');
Adjust.trackEvent(event);
```
</td>
</tr>
<table>

### <a id="et-purchase-verification"></a>인앱 결제 검증

서버측 영수증 검증 도구인 [Adjust의 결제 검증][android-purchase-verification]을 사용하여 앱에서 이루어진 인앱 결제를 검증할 수 있습니다. 링크를 클릭하여 자세한 내용을 확인하시기 바랍니다.   

## 맞춤 파라미터

### <a id="cp"></a>맞춤 파라미터 개요

Adjust SDK가 기본적으로 수집하는 데이터 포인트 외에도 Adjust SDK를 사용하여 이벤트나 세션에 필요한 만큼의 맞춤 값을 추적하고 추가할 수 있습니다(사용자 ID, 제품 ID 등). 맞춤 파라미터는 원시 데이터로만 제공되며 Adjust 대시보드에 표시되지 **않습니다**.

개인 내부용으로 수집하는 값의 경우 **콜백 파라미터**를 사용하고, 외부 파트너와 공유하는 값은 **파트너 파라미터**를 사용해야 합니다. 만약에 내부와 외부 모두를 위해 값(예: 구매 ID)이 트래킹되는 경우, 콜백과 파트너 파라미터 모두를 사용하여 트래킹하는 것을 권고합니다.


### <a id="cp-event-parameters"></a>이벤트 파라미터

### <a id="cp-event-callback-parameters"></a>이벤트 콜백 파라미터

[대시보드]에서 이벤트를 위한 콜백 URL을 등록할 수 있습니다. Adjust는 이벤트가 트래킹될 때마다 해당 URL에 GET 요청을 보냅니다. 이벤트를 트래킹하기 전에 이벤트에서 'addCallbackParameter'를 호출하여 해당 이벤트에 콜백 파라미터를 추가 할 수 있습니다. 이후 Adjust는 해당 파라미터를 사용자의 콜백 URL에 추가합니다.

예를 들어, 사용자가 이벤트를 위해 `http://www.example.com/callback` URL을 등록했으며 다음과 같은 이벤트를 추적한다고 가정해 보겠습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
adjustEvent.addCallbackParameter('key', 'value');
adjustEvent.addCallbackParameter('foo', 'bar');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<table>

이 경우, Adjust가 이벤트를 추적하여 다음으로 요청을 전송합니다.

```
http://www.example.com/callback?key=value&foo=bar
```

Adjust는 파라미터 값으로 사용될 수 있는 `{gps_adid}` 등 다양한 플레이스홀더를 지원합니다. 결과 콜백에서 Adjust는 `{gps_adid}` 플레이스홀더를 현재 기기의 Google Play Services ID로 교체합니다. Adjust는 사용자의 맞춤 파라미터를 저장하지 않으며, 콜백에 **추가**만 합니다. 이벤트 콜백을 등록하지 않으면 Adjust가 맞춤 파라미터를 읽을 수 없습니다.

Adjust [콜백 가이드][callbacks-guide]에서 URL 콜백에 대해 자세히 알아보시기 바랍니다.

### <a id="cp-event-partner-parameters"></a>이벤트 파트너 파라미터

Adjust 대시보드에서 파라미터가 활성화되면, 이를 네트워크 파트너에게 전송할 수 있습니다.

이는 위에서 설명된 콜백 파라미터와 같은 원리로 작동됩니다. 이벤트 인스턴스에 `addPartnerParameter` 메서드를 호출하여 추가할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
adjustEvent.addPartnerParameter('key', 'value');
adjustEvent.addPartnerParameter('foo', 'bar');
Adjust.trackEvent(adjustEvent);
```
</td>
</tr>
<table>

[특별 파트너 가이드][special-partners]에서 특별 파트너와 연동 방법에 대한 자세한 내용을 알아보실 수 있습니다.

### <a id="cp-event-callback-id"></a>이벤트 콜백 ID

트래킹하고자 하는 각 이벤트에 맞춤 문자열 ID를 추가할 수도 있습니다. 이 ID는 이후에 이벤트 성공 및/또는 이벤트 실패 콜백에서 보고되며, 이를 통해 성공적으로 트래킹된 이벤트와 그렇지 않은 이벤트를 확인할 수 있습니다. 이벤트 인스턴스에 `setCallbackId` 메서드를 호출하여 이 ID를 설정할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

### <a id="cp-session-parameters"></a>세션 파라미터

세션 파라미터는 로컬에 저장되며, Adjust SDK의 모든 **이벤트**와 **세션**에 전송됩니다. 이러한 파라미터를 추가할 때마다 Adjust가 해당 파라미터를 저장하므로, 다시 추가할 필요가 없습니다. 동일한 파라미터를 다시 추가해도 아무 일도 일어나지 않습니다.

이러한 세션 파라미터는 설치 중에도 전송될 수 있도록 Adjust SDK가 실행되기 전에 호출될 수 있습니다. 설치 시에 파라미터를 전송해야 하지만, 필요한 값을 실행 이후에만 확보할 수 있는 경우, 이러한 동작이 가능하게 하려면 Adjust SDK의 첫 실행을 지연시키면 됩니다.

### <a id="cp-session-callback-parameters"></a>세션 콜백 파라미터

Adjust SDK의 모든 이벤트 또는 세션에서 전송될 [이벤트](#event-callback-parameters)를 위해 모든 콜백 파라미터를 저장할 수 있습니다.

세션 콜백 파라미터의 인터페이스는 이벤트 콜백 파라미터와 유사합니다. 이벤트에 키와 값을 추가하는 대신, `Adjust.addSessionCallbackParameter(String key, String value)`메서드로의 호출을 통해 추가하시기 바랍니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

세션 콜백 파라미터는 이벤트에 추가한 콜백 파라미터와 병합됩니다. 이벤트에 추가된 콜백 파라미터는 세션 콜백 파라미터를 우선합니다. 즉, 세션에서 추가된 것과 동일한 키로 콜백 파라미터를 이벤트에 추가하면 이벤트에 추가된 콜백 파라미터의 값이 우선시됩니다.

원하는 키를 `Adjust.removeSessionCallbackParameter(String key)` 메서드에 전달하여 특정 세션 콜백 파라미터를 삭제할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

세션 콜백 파라미터의 모든 키와 이에 상응하는 값을 삭제하고 싶다면 `Adjust.resetSessionCallbackParameters()` 메서드를 통해 리셋할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

### <a id="cp-session-partner-parameters"></a>세션 파트너 파라미터

[세션 콜백 파라미터](#session-callback-parameters)가 Adjust SDK의 모든 이벤트 및 세션에서 전송되는 것과 마찬가지로, 세션 파트너 파라미터도 있습니다.

이러한 세션 파트너 파라미터는 Adjust [대시보드]에서 연동이 활성화된 모든 네트워크 파트너에 전송됩니다.

세션 파트너 파라미터 인터페이스는 이벤트 파트너 파라미터 인터페이스와 유사합니다. 이벤트에 키와 값을 추가하는 대신에, `Adjust.addSessionPartnerParameter(String key, String value)` 메서드를 호출하여 추가할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
Adjust.addSessionPartnerParameter('foo','bar');
```
</td>
</tr>
<table>

세션 파트너 파라미터는 이벤트에 추가된 파트너 파라미터와 병합됩니다. 이벤트에 추가된 파트너 파라미터는 세션 파트너 파라미터보다 높은 우선순위를 가집니다. 세션에서 추가된 것과 동일한 키로 파트너 파라미터를 이벤트에 추가하면, 이벤트에 추가된 파트너 파라미터의 값이 우선시됩니다.

원하는 키를 `Adjust.removeSessionPartnerParameter(String key)`에 전달하여 특정 세션 파트너 파라미터를 제거할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

세션 파트너 파라미터의 모든 키와 이에 상응하는 값을 삭제하고 싶다면 `Adjust.resetSessionPartnerParameters()` 메서드를 통해 리셋할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

### <a id="cp-delay-start"></a>시작 지연

Adjust SDK의 시작을 지연시키면, 앱이 고유 ID와 같이 설치 시 전송될 세션 파라미터를 획득할 시간을 확보할 수 있습니다.

Config 인스턴스의 `setDelayStart` 메서드로 초기 지연을 초 단위로 설정하시기 바랍니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

본 예는 Adjust SDK가 초기 설치 세션과 발생한 모든 이벤트를 전송하는 것을 5.5초 지연시킵니다. 지연 설정한 시간이 만료되면(또는 도중에 `Adjust.sendFirstPackages()`를 호출한 경우), 모든 세션 파라미터는 지연된 설치 세션과 이벤트에 추가되며, Adjust SDK가 다시 원래대로 작업을 재개합니다.

**Adjust SDK의 최대 시작 지연 시간은 10초입니다.**


## 부가 기능

Adjust SDK를 프로젝트에 연동하면 다음의 기능을 활용할 수 있습니다.

### <a id="af-push-token"></a>푸시 토큰(설치 삭제 트래킹)

푸시 토큰은 Audience Builder 및 클라이언트 콜백에 사용되며 삭제 및 재설치 추적 기능에 필요합니다.

푸시 알림 토큰을 Adjust에 전송하려면, 토큰을 획득한 이후(또는 값이 변경된 이후) 다음의 콜을 Adjust에 추가하시기 바랍니다.

<table>
<tr>
<td>
<b>Native SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.setPushToken(pushNotificationsToken, context);
```

`context`가 추가된 업데이트된 서명은 SDK가 더 다양한 시나리오에 대응할 수 있도록 하여 푸시 토큰이 전송되도록 보장할 수 있습니다. 따라서 위의 서명 메서드 사용을 권고합니다.

그러나 Adjust는 `context`가 포함되지 않은 이전의 동일한 서명 메서드도 여전히 지원합니다.

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
<table>

### <a id="af-attribution-callback"></a>어트리뷰션 콜백

수신기를 등록하여 트래커 어트리뷰션의 변경 사항에 대한 알림을 받을 수 있습니다. 어트리뷰션에 고려하는 소스가 다양하기 때문에 Adjust는 이 정보를 동시에 전송할 수 없습니다.

자세한 정보는 Adjust의 [어트리뷰션 데이터 정책][attribution-data]을 확인하시기 바랍니다.

SDK를 시작하기 전에 config 인스턴스와 함께 어트리뷰션 콜백을 추가하시기 바랍니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

수신기 함수는 SDK가 최종 어트리뷰션 데이터를 수신한 이후 호출됩니다. 수신기 함수 내에서 `attribution` 파라미터에 액세스할 수 있습니다. 그 속성에 대한 요약 정보는 다음과 같습니다.

- `trackerToken`: 현재 어트리뷰션의 트래커 토큰 문자열
- `trackerName`: 현재 어트리뷰션의 트래커 이름 문자열
- `network`: 현재 어트리뷰션의 네트워크 그룹화 수준 문자열
- `campaign`: 현재 어트리뷰션의 캠페인 그룹화 수준 문자열
- `adgroup`: 현재 어트리뷰션의 광고 그룹 그룹화 수준 문자열
- `creative`: 현재 어트리뷰션의 크리에이티브 그룹화 수준 문자열
- `clickLabel`: 현재 어트리뷰션의 클릭 레이블 문자열
- `adid`: Adjust의 기기 식별자 문자열
- `costType`: 비용 유형 문자열
- `costAmount`: 비용 금액
- `costCurrency`: 비용 통화 문자열

**참고**: 비용 데이터인 `costType`와 `costAmount`, `costCurrency`는 `setNeedsCost` 메서드를 호출하여 `AdjustConfig`에서 설정된 경우에만 이용 가능합니다. 설정이 되지 않았거나 또는 설정이 되었으나 어트리뷰션의 일부가 아닌 경우에는 필드의 값이 `null`로 나타납니다. 본 기능은 DK v4.25.0 이상 버전에서만 이용 가능합니다.

### <a id="af-subscriptions"></a>구독 트래킹

**참고**: 이 기능은 SDK 4.22.0 버전 이상에서만 사용할 수 있습니다.

Adjust SDK에서 Play Store 구독을 트래킹하고 유효성을 검증할 수 있습니다. 구독이 결제되면 다음을 Adjust SDK로 호출하시기 바랍니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

구독 트래킹 파라미터:

- [가격](https://developer.android.com/reference/com/android/billingclient/api/SkuDetails#getpriceamountmicros)
- [통화](https://developer.android.com/reference/com/android/billingclient/api/SkuDetails#getpricecurrencycode)
- [sku](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getsku)
- [주문 ID](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getorderid)
- [서명](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getsignature)
- [구매 토큰](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getpurchasetoken)
- [구매 시간](https://developer.android.com/reference/com/android/billingclient/api/Purchase#getpurchasetime)

이벤트 추적과 마찬가지로 콜백 및 파트너 파라미터를 구독 객체에 연결할 수 있습니다.

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
subscription.addCallbackParameter("key", "value");
subscription.addCallbackParameter("foo", "bar");

// add partner parameters
subscription.addPartnerParameter("key", "value");
subscription.addPartnerParameter("foo", "bar");

Adjust.trackPlayStoreSubscription(subscription);
```

### <a id="af-ad-revenue"></a>광고 매출 트래킹

**참고**: 이 기능은 네이티브 SDK v4.18.0 이상에서만 사용할 수 있습니다.

다음 메서드를 호출하여 Adjust SDK로 광고 매출 정보를 트래킹할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

```java
Adjust.trackAdRevenue(source, payload);
```
</td>
</tr>
<table>

전달해야 하는 메서드 파라미터는 다음과 같습니다.

- `source` - 광고 매출 정보의 소스를 나타내는 `String` 객체
- `payload` - 광고 매출 JSON을 포함하는 `JSONObject` 객체

애드저스트는 현재 다음의 `source` 파라미터 값을 지원합니다.

- `AD_REVENUE_MOPUB` - MoPub 미디에이션 플랫폼을 나타냄(자세한 정보는 [연동 가이드][sdk2sdk-mopub] 확인)

### <a id="af-session-event-callbacks"></a>세션 이벤트 콜백

이벤트나 세션이 트래킹될 때 수신기에 알림을 보내도록 설정할 수 있습니다. 수신기는 성공적인 이벤트를 트래킹하기 위한 수신기, 실패한 이벤트를 트래킹하기 위한 수신기, 성공한 세션을 트래킹하기 위한 수신기, 실패한 세션을 트래킹하기 위한 수신기로 총 4가지가 있습니다. config 객체를 생성한 이후 다음과 같이 필요한 만큼의 수신기를 추가하시기 바랍니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

수신기 함수는 SDK가 서버로 패키지를 전송하려고 시도한 이후 호출됩니다. 수신기 내에서는 해당 수신기에 특화된 응답 데이터 객체에 액세스할 수 있습니다. 성공적인 세션 응답 데이터 객체 필드를 짧게 요약하면 다음과 같습니다.

- `message`: 서버로부터의 메시지 문자열(또는 SDK에 의해 로그된 오류)
- `timestamp`: 서버로부터의 타임스탬프 문자열
- `adid`: Adjust가 제공한 고유의 문자열 기기 식별자
- `jsonResponse`: 서버로부터의 응답을 포함하는 JSON 객체

두 이벤트 응답 데이터 개체는 다음을 포함합니다.

- `eventToken`: 이벤트 토큰 문자열(트래킹된 패키지가 이벤트인 경우)
- `callbackId`: 이벤트 객체에 설정된 맞춤 정의된 [콜백 ID](#cp-event-callback-id) 문자열

두 이벤트 및 세션 실패 개체는 다음을 포함합니다.

- `willRetry`: 이후 해당 패키지의 재전송 시도 여부를 명시하는 부울 방식

### <a id="af-user-attribution"></a>유저 어트리뷰션

[어트리뷰션 콜백 세션](#af-attribution-callback)에서 설명된 바와 같이, 본 콜백은 어트리뷰션 정보가 바뀔 때마다 트리거됩니다. 필요할 때마다`Adjust` 인스턴스의 다음 메서드를 호출하여 유저의 현재 어트리뷰션 정보에 액세스할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

**참고**: 본 호출은 Adjust SDK v4.11.0 이상 버전에서만 이용할 수 있습니다.

**참고**: 현재 어트리뷰션 정보는 Adjust 백엔드에서 앱 설치를 트래킹하고, 어트리뷰션 콜백을 실행한 이후에만 이용 가능합니다. SDK가 초기화되고 어트리뷰션 콜백이 실행되기 전에는 유저의 어트리뷰션 값에 액세스할 수 **없습니다.**

### <a id="af-device-ids"></a>기기 ID

Adjust SDK를 통해 기기 식별자를 획득할 수 있습니다.

### <a id="af-gps-adid"></a>Google Play Services 광고 ID

특정 서비스(예: Google Analytics)는 중복 보고를 방지하기 위해 광고 및 클라이언트 ID 통합을 요청합니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
</td>
</tr>
<tr>
<td>

Google Advertising ID가 필요한 경우에는 제약이 있습니다. 이는 백그라운드 스레드에서만 읽을 수 있습니다. context를 포함한 `getGoogleAdId` 함수와 `OnDeviceIdsRead` 인스턴스를 호출하면, 어떤 상황에서든 작동할 것입니다.

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

해당 기기의 Google Advertising 기기 식별자 정보를 얻으려면, `Adjust.getGoogleAdId`로 콜백 함수를 전송하여, 다음과 같이 인수에서 Google Advertising ID를 받도록 할 수 있습니다.

```js
Adjust.getGoogleAdId(function(googleAdId) {
    // ...
});
```
</td>
</tr>
<table>

### <a id="af-amazon-adid"></a>Amazon 광고 ID

Amazon 광고 ID가 필요한 경우, 다음의 메서드를 `Adjust`에서 호출합니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

### <a id="af-adid"></a>Adjust 기기 식별자

앱이 설치된 각 기기에 대해 Adjust의 백엔드는 고유한 **Adjust 기기 식별자(adid)** 를 생성합니다. 이 식별자를 획득하려면 다음의 메서드를 `Adjust` 인스턴스에서 호출하시기 바랍니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

**참고**: 본 호출은 Adjust SDK v4.11.0 이상 버전에서만 이용할 수 있습니다.

**참고**: **adid**에 관한 정보는 Adjust의 백엔드가 앱 설치를 트래킹한 이후에만 이용 가능합니다. 따라서 SDK가 초기화되고 앱 설치가 성공적으로 추적되기 전까지는 **adid** 값에 액세스할 수 **없습니다.**

### <a id="af-preinstalled-apps"></a>사전 설치된 앱

애드저스트 SDK를 사용하여 기기 제조 과정에서 앱이 사전 설치되었던 유저를 파악할 수 있습니다. 시스템 페이로드 또는 기본 트래커를 사용하는 두 가지 방법으로 사전 설치 여부를 확인할 수 있습니다. 

일반적으로 Adjust는 시스템 실 데이터 솔루션을 권고합니다. 그러나 트래커가 필요한 경우도 있습니다. Adjust의 사전설치 파트너와 연동에 관한 자세한 정보는 [Adjust 헬프 센터](https://help.adjust.com/ko/article/pre-install-tracking)에서 확인하시기 바랍니다. 어떠한 솔루션을 사용해야 할지 모르겠다면 integration@adjust.com으로 연락주시기 바랍니다.

#### 시스템 실 데이터 사용

본 솔루션은 SDK **SDK v4.23.0 이상에서만** 지원됩니다.

Adjust SDK가 사전 설치된 앱을 인식할 수 있도록 하려면, cofig 객체를 생성한 후 `true` 파라미터와 함께 `setPreinstallTrackingEnabled`를 호출하시기 바랍니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

#### 기본값 트래커 사용

- [대시보드]에서 새 트래커를 생성합니다.
- 앱 델리게이트를 열고 config의 기본 트래커를 설정합니다.

  <table>
  <tr>
  <td>
  <b>Native App SDK</b>
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
  <table>

- {TrackerToken}을 1단계에서 생성한 트래커 토큰으로 교체합니다. 대시보드에는 트래커 URL이 표시됩니다(`http://app.adjust.com/` 포함). 소스 코드에서는 전체 URL이 아닌 6~7자의 토큰만 지정해야 합니다.

- 앱을 빌드하고 실행합니다. LogCat에서 다음과 같은 라인이 나타나야 합니다.

  ```
  Default tracker: 'abc123'
  ```

### <a id="af-offline-mode"></a>오프라인 모드

Adjust 서버에 대한 전송을 일시적으로 중지(트래킹된 데이터는 이후에 전송되도록 보유)하려면 Adjust SDK를 오프라인 모드로 설정할 수 있습니다. 오프라인 모드에서는 모든 정보가 파일로 저장됩니다. 오프라인 모드에서 너무 많은 이벤트를 트리거하지 않도록 주의해야 합니다.

오프라인 모드를 활성화하려면 `setOfflineMode`를 호출하고 파라미터를 `true`로 설정합니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

반대로`setOfflineMode`를 `false`와 호출하여 오프라인 모드를 취소할 수 있습니다. Adjust SDK가 다시 온라인 모드가 되면, 저장된 모든 정보가 정확한 시간 정보와 함께 Adjust 서버로 전송됩니다.

트래킹 비활성화와는 다르게 이 설정은 **세션 간에 유지되지 않습니다.** 즉, 앱이 오프라인 모드에서 종료되었더라도, Adjust SDK는 항상 온라인 모드로 시작됩니다.


### <a id="af-disable-tracking"></a>트래킹 비활성화

`false` 파라미터와 함께`setEnabled`를 호출하여 Adjust SDK가 현재 기기의 모든 활동을 트래킹하는 것을 비활성화 할 수 있습니다. (주의: 트래킹 중단을 원한때만 사용하시기 바랍니다.) **이 설정은 세션 간에 유지됩니다**.

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
<table>

`isEnabled`함수를 호출하여 Adjust SDK가 현재 활성화되어있는지 확인할 수 있습니다. 활성화된 파라미터를`true`로 설정하여 `setEnabled`를 호출하고 언제든지 Adjust SDK를 활성화할 수 있습니다.

### <a id="af-event-buffering"></a>이벤트 버퍼링

앱이 이벤트 트래킹을 많이 사용하는 경우, 일부 네트워크 요청을 연기하여 네트워크 요청을 1분에 한 번씩 일괄적으로 보낼 수 있습니다. 이벤트 버퍼링은 cofig 인스턴스에서 활성화할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

### <a id="af-background-tracking"></a>백그라운드 트래킹

Adjust SDK는 기본적으로 앱이 백그라운드에서 작동하는 동안 네트워크 요청 전송을 일시 중지하도록 설정되어 있습니다. 이는 cofig 인스턴스에서 변경할 수 있습니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

### <a id="af-gdpr-forget-me"></a>GDPR 잊혀질 권리

EU의 개인정보보호법(GDPR) 제 17조에 따라, 사용자는 잊혀질 권리(Right to be Forgotten)를 행사했음을 Adjust에 알릴 수 있습니다. 다음 메서드를 호출하면 Adjust SDK가 잊혀질 권리에 대한 사용자의 선택과 관련된 정보를 Adjust 백엔드에 보냅니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

이 정보를 수신한 후 Adjust는 해당 사용자의 데이터를 삭제하며 Adjust SDK는 해당 사용자에 대한 추적을 중지합니다. 이 기기로부터의 요청은 향후 Adjust에 전송되지 않습니다.

이러한 설정은 테스트에서 적용하더라도 영구적으로 유지되며, **취소할 수 없습니다.**

## <a id="af-third-party-sharing"></a>특정 유저에 대한 서드파티 공유

유저가 서드파티 파트너와의 데이터 공유를 비활성화, 활성화 및 재활성화할 때 Adjust에 이를 고지할 수 있습니다.

### <a id="af-disable-third-party-sharing"></a>특정 유저에 대한 서드파티 공유 비활성화

다음 메서드를 호출하여 Adjust SDK가 데이터 공유 비활성화에 대한 사용자의 선택과 관련된 정보를 Adjust 백엔드에 보냅니다:

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

이 정보를 수신하면 Adjust는 특정 사용자의 데이터를 파트너와 공유하는 것을 차단하고 Adjust SDK는 계속 정상적으로 작동합니다.

### <a id="af-enable-third-party-sharing"></a>특정 유저에 대한 서드파티 공유 활성화 및 재활성화

다음 메서드를 호출하여 Adjust SDK가 데이터 공유에 대한 유저의 선택과 변경 내용을 Adjust 백엔드에 보내도록 하시기 바랍니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

Adjust는 정보 수신 후 해당 유저에 대한 파트너와의 데이터 공유 상태를 변경합니다. Adjust SDK는 계속해서 정상적으로 작동합니다.

Adjust SDK가 Adjust 백엔드로 상세한 옵션을 전송하도록 하려면 다음의 메서드를 호출합니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

### <a id="af-measurement-consent"></a>특정 유저에 대한 동의 측정

Adjust 대시보드에서 데이터 프라이버시 설정을 활성화 또는 비활성화하려면(동의 만료 기간 및 유저 데이터 보유 기간 포함) 다음의 메서드를 도입해야 합니다.

다음의 메서드를 호출하여 Adjust SDK가 데이터 프라이버시 설정을 Adjust 백엔드로 보내도록 하시기 바랍니다.

<table>
<tr>
<td>
<b>Native App SDK</b>
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
<table>

Adjust가 이 정보를 수신하면 동의 측정을 활성화 또는 비활성화합니다. Adjust SDK는 계속해서 정상적으로 작동합니다.

## 테스트 및 문제 해결

### <a id="tt-session-failed"></a>"세션 실패(너무 빈번한 세션 거부) " 오류가 발생한 경우

본 오류는 일반적으로 설치 테스트 시 발생합니다. 앱의 설치 삭제와 재설치만으로는 새 설치가 트리거되지 않습니다. 서버는 SDK가 로컬에 집계된 세션 데이터를 소실했다고 판단하여, 해당 기기에 대해 서버에서 이용 가능한 정보를 기반으로 오류 메시지를 무시할 것입니다.

이러한 행동은 테스트 시 번거로울 수 있으나, 샌드박스의 행동이 프로덕션 행동과 최대한 일치하도록 하기 위해 반드시 필요합니다.

앱에 대해 편집자(또는 그 이상)의 권한이 있는 경우, Adjust 대시보드에서 Adjust의 [테스트용 콘솔][testing_console] 기능을 사용하여 직접 모든 기기에 대한 앱의 세션 데이터를 삭제할 수 있습니다. 

기기의 정보가 올바르게 삭제되면 테스트용 콘솔은 `Forgot device` 값을 반환합니다. 기기가 이미 삭제된 경우(또는 값이 정확하지 않은 경우) 링크는 `Advertising ID not found` 값을 반환합니다.

기기의 삭제는 GDPR 잊혀질 권리의 호출을 뒤바꾸지 않습니다.

현재 패키지에서 액세스가 있는 경우, Adjust의 [개발자 API][dev_api]를 사용해 기기를 조사하고 삭제할 수 있습니다.

### <a id="tt-broadcast-receiver"></a>브로드캐스트 리시버가 설치 리퍼러를 포착하는지 확인하고 싶은 경우

[가이드](#qs-gps-intent)를 그대로 수행했다면, 브로드캐스트 리시버가 Adjust의 SDK와 서버에 설치 리퍼러를 전송하도록 설정되었을 것입니다.

테스트용 설치 리퍼러를 수동으로 실행하여 이를 테스트해볼 수 있습니다. `com.your.appid`를 앱 ID로 교체하고, Android Studio의 [adb](http://developer.android.com/tools/help/adb.html) 툴을 사용하여 다음의 명령어를 실행하시기 바랍니다.

```
adb shell am broadcast -a com.android.vending.INSTALL_REFERRER -n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver --es "referrer" "adjust_reftag%3Dabc1234%26tracking_id%3D123456789%26utm_source%3Dnetwork%26utm_medium%3Dbanner%26utm_campaign%3Dcampaign"
```

`INSTALL_REFERRER` 인텐트에 대해 이미 다른 브로드캐스트 리시버를 사용하고 본 [리퍼러 가이드]를 수행한 경우, `com.adjust.sdk.AdjustReferrerReceiver`를 브로드캐스트 리시버로 교체하시기 바랍니다.

`-n com.your.appid/com.adjust.sdk.AdjustReferrerReceiver` 파라미터를 삭제하여 기기의 모든 앱이 `INSTALL_REFERRER` 인텐트를 수신하도록 설정할 수 있습니다.

로그 레벨을 `verbose`로 설정했다면 리퍼러를 읽을 때 로그를 다음과 같이 볼 수 있습니다.

```
V/Adjust: Referrer to parse (adjust_reftag=abc1234&tracking_id=123456789&utm_source=network&utm_medium=banner&utm_campaign=campaign) from reftag
```

그리고 SDK 패키지 핸들러에 추가된 클릭 패키지를 볼 수 있습니다.

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

앱을 실행하기 전에 이 테스트를 수행했다면, 패키지가 전송되지 않을 것입니다. 패키지는 앱이 실행된 경우에만 전송됩니다.

**중요** 본 기능을 테스트하기 위해 `adb` 툴을 사용하는 것은 권고되지 **않습니다.** 전체 리퍼러 콘텐츠(`&`로 구분된 여러 파라미터가 있는 경우)를 `adb` 툴로 테스트하려면 브로드캐스트 리시버에 도달하도록 이를 인코딩해야 합니다. 인코딩을 하지 않으면 `adb`는 첫 `&` 이후의 리퍼러를 삭제하여, 브로드캐스트 리시버에 잘못된 콘텐츠가 전송됩니다.

앱이 인코딩되지 않은 리퍼러 값을 어떻게 수신하는지 확인하려면, Adjust의 예시 앱을 사용하여 전송되는 값을 변경함으로써 `MainActivity.java` 파일 내에 `onFireIntentClick` 메서드 안에 있는 인텐트를 전송하도록 할 수 있습니다.

```java
public void onFireIntentClick(View v) {
    Intent intent = new Intent("com.android.vending.INSTALL_REFERRER");
    intent.setPackage("com.adjust.examples");
    intent.putExtra("referrer", "utm_source=test&utm_medium=test&utm_term=test&utm_content=test&utm_campaign=test");
    sendBroadcast(intent);
}
```

`putExtra` 메서드의 두 번째 파라미터는 원하는 콘텐츠로 변경할 수 있습니다.

### <a id="tt-event-at-launch"></a>앱 실행 시 이벤트 트리거 가능 여부

앱 실행 시 이벤트를 트리거하면 예상과는 다른 결과가 나올 수 있습니다. 그 이유는 다음과 같습니다.

글로벌 `Application` 클래스의 `onCreate` 메서드는 앱 실행 시 호출될 뿐만 아니라, 시스템이나 앱 이벤트가 앱에 의해 포착된 경우에도 호출됩니다.

Adjust SDK는 이 시점에서 초기화에 준비되어있으나 실제로 시작된 것은 아닙니다. 이는 실제 활동(유저가 실제로 앱을 실행함)이 일어난 경우에만 발생합니다.

앱 실행 시 이벤트를 트리거하는 것은 실제 유저에 의해 앱이 실행되지 않았음에도 Adjust SDk를 시작하여, 앱의 외부 요소에 따라 결정되는 시점에 이벤트를 전송하도록 합니다.

따라서 앱 실행 시 이벤트를 트리거하는 것은 트래킹된 설치와 세션 숫자의 정확도에 영향을 줍니다.

설치 이후에 이벤트를 트리거하고 싶다면 [어트리뷰션 콜백](#af-attribution-callback)을 사용하시기 바랍니다.

앱이 실행될 때 이벤트를 트리거하고 싶다면, 해당 활동에 대해 `onCreate` 메서드를 사용하시기 바랍니다.

[dashboard]:  http://adjust.com/ko
[adjust.com]: http://adjust.com/ko

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
[referrer]:                       doc/english/misc/multiple-receivers.md
[리퍼러 가이드]:                     https://github.com/adjust/android_sdk/blob/master/doc/english/misc/multiple-receivers.md
[releases]:                       https://github.com/adjust/android_sdk/releases
[google-ad-id]:                   https://support.google.com/googleplay/android-developer/answer/6048248?hl=en
[event-tracking]:                 https://docs.adjust.com/ko/event-tracking
[callbacks-guide]:                https://docs.adjust.com/ko/callbacks
[new-referrer-api]:               https://developer.android.com/google/play/installreferrer/library.html
[special-partners]:               https://docs.adjust.com/ko/special-partners
[attribution-data]:               https://github.com/adjust/sdks/blob/master/doc/attribution-data.md
[android-dashboard]:              http://developer.android.com/about/dashboards/index.html
[대시보드]:                         http://developer.android.com/about/dashboards/index.html
[currency-conversion]:            https://docs.adjust.com/ko/event-tracking/#tracking-purchases-in-different-currencies
[android-application]:            http://developer.android.com/reference/android/app/Application.html
[android-launch-modes]:           https://developer.android.com/guide/topics/manifest/activity-element.html
[google-play-services]:           http://developer.android.com/google/play-services/setup.html
[reattribution-with-deeplinks]:   https://docs.adjust.com/ko/deeplinking/#manually-appending-attribution-data-to-a-deep-link
[android-purchase-verification]:  https://github.com/adjust/android_purchase_sdk
[testing_console]: https://docs.adjust.com/en/testing-console/#how-to-clear-your-advertising-id-from-adjust-between-tests
[dev_api]: https://docs.adjust.com/en/adjust-for-developers/

[sdk2sdk-mopub]:    ../../doc/korean/sdk-to-sdk/mopub.md

## <a id="license"></a>라이선스

Adjust SDK는 MIT 라이선스에 따라 사용이 허가되었습니다.

Copyright (c) 2012-2021 Adjust GmbH, http://www.adjust.com

이로써 본 소프트웨어와 관련 문서 파일(이하 "소프트웨어")의 복사본을 받는 사람에게는 아래 조건에 따라 소프트웨어를 제한 없이 다룰 수 있는 권한이 무료로 부여됩니다.이 권한에는 소프트웨어를 사용, 복사, 수정, 병합, 출판, 배포 및/또는 판매하거나 2차 사용권을 부여할 권리와 소프트웨어를 제공 받은 사람이 소프트웨어를 사용, 복사, 수정, 병합, 출판, 배포 및/또는 판매하거나 2차 사용권을 부여하는 것을 허가할 수 있는 권리가 제한 없이 포함됩니다.

위 저작권 고지문과 본 권한 고지문은 소프트웨어의 모든 복사본이나 주요 부분에 포함되어야 합니다.

소프트웨어는 상품성, 특정 용도에 대한 적합성 및 비침해에 대한 보증 등을 비롯한 어떤 종류의 명시적이거나 암묵적인 보증 없이 "있는 그대로" 제공됩니다. 어떤 경우에도 저작자나 저작권 보유자는 소프트웨어와 소프트웨어의 사용 또는 기타 취급에서 비롯되거나 그에 기인하거나 그와 관련하여 발생하는 계약 이행 또는 불법 행위 등에 관한 배상 청구, 피해 또는 기타 채무에 대해 책임지지 않습니다.

