## OAID 플러그인

OAID는 HMS(Huawei 모바일 서비스) 버전 2.6.2 이상이 설치된 기기에서 사용할 수 있는 새로운 광고 ID입니다. Google Play 서비스를 사용할 수 없는 시장에서 이 ID를 사용하여 Android 기기를 어트리뷰션 및 트래킹할 수 있습니다. 

OAID 플러그인을 사용하면 Adjust Android SDK가 기기의 OAID 값을 읽을 수 있을 *뿐만 아니라* 검색하는 다른 기기 ID도 기본적으로 읽을 수 있습니다. 

시작하려면 먼저 공식 [Android SDK README][readme]의 내용을 읽고 Adjust SDK를 앱에 연동 해야 합니다.

Adjust SDK가 OAID를 수집하고 트래킹할 수 있게 하려면 다음 단계를 따르세요.

### 앱에 OAID 플러그인 추가

Maven을 사용하고 있는 경우, 기존 Adjust SDK dependency 옆에 있는 `build.gradle` 파일에 다음의 OAID plugin dependency을 추가하세요.

```
implementation 'com.adjust.sdk:adjust-android:4.19.0'
implementation 'com.adjust.sdk:adjust-android-oaid:4.19.0'
```

Adjust OAID 플러그인을 JAR 파일로 추가할 수도 있습니다. Adjust [릴리스 페이지][releases]에서 다운로드하세요.

### Proguard 설정

Proguard를 사용 중이고 Google Play 스토어에 앱을 게시하지 않을 계획이라면 Google Play 서비스와 관련된 모든 규칙을 제거하고 [SDK README][readme proguard]에서 install referrer 라이브러리를 설치할 수 있습니다.

아래와 같이 모든 `com.adjust.sdk` 패키지 규칙을 적용 하세요.

```
-keep public class com.adjust.sdk.** { *; }
```

### 플러그인 사용

OAID 값을 읽으려면 SDK를 시작하기 전에 `AdjustOaid.readOaid()`를 호출하세요.

```java
AdjustOaid.readOaid();

// ...

Adjust.onCreate(config);
```

SDK가 OAID 값을 그만 읽게 하려면 AdjustOaid.doNotReadOaid()`를 호출하세요.


[readme]:    ../../korean/README.md
[releases]:  https://github.com/adjust/android_sdk/releases
[readme proguard]: ../../korean/README.md#qs-proguard
