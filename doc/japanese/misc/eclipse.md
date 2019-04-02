## Eclipseを使ったadjustとの連携

SDKバージョン4.0.0以降、adjustではAndroid Studioでの開発をお勧めしています。
Eclipseを使ってadjust SDKを連携するには、以下の手順に従ってください。

## 基本的なインストール

EclipseプロジェクトでコンパイルしたJarをリンクしてadjust SDKを連携させる方法が最も簡単です。

### 1. Jarの取得

[リリースページ][releases]から最新のJarを入手してください。もしくは、[Mavenレポジトリ][maven]から
[`com.adjust.sdk`][maven_search]を検索してダウンロードしてください。

### 2. プロジェクトへadjustライブラリを追加

Jarファイルをダウンロードしたら、プロジェクトの`libs`フォルダにドラッグしてください。
これでadjust SDKがアプリで利用できるようになります。

### 3. Google Playサービスの追加

2014年8月1日以降、Google Playストア内のアプリはデバイスの特定のために[Google広告ID][google_ad_id]を
使うことが必須とされています。adjust SDKでGoogle広告IDを使うためには、
[Google Playサービス][google_play_services]を連携させる必要があります。
Google Playサービスの連携がお済みでない場合は、以下の手順に進んでください。

1. 以下のライブラリのプロジェクトをコピーし、

    ```
    <android-sdk>/extras/google/google_play_services/libproject/google-play-services_lib/
    ```

    Androidプロジェクトの開発に使っている場所にペーストしてください。

2. Eclipseワークスペースにライブラリプロジェクトをインポートしてください。
   `File > Import`と進み、`Android > Existing Android Code into Workspace`を選択、
   ライブラリプロジェクトのコピーをインポートしてください。

3. アプリプロジェクト内で、Google Playサービスのライブラリプロジェクトへの参照を加えてください。
   詳しくは[Referencing a Library Project for Eclipse][eclipse_library]をご確認ください。

     必ず開発用のワークスペースにコピーしたライブラリを参照してください。
     Android SDKディレクトリから直接ライブラリを参照しないでくささい。

4. Google Playサービスのライブラリをアプリプロジェクトにdependencyとして追加したら、
   アプリのマニフェストファイルを開き、以下のタグを[<application>][application]エレメントの子要素として
   追加してください。
   

    ```xml
    <meta-data android:name="com.google.android.gms.version"
          android:value="@integer/google_play_services_version" />
    ```

### 4. 以降の手順

adjustの[ガイド][guide_permissions]の`5. パーミッションの追加`項目より設定を進めてください。

[releases]:             https://github.com/adjust/adjust_android_sdk/releases
[google_ad_id]:         https://developer.android.com/google/play-services/id.html
[maven]:                http://maven.org
[maven_search]:         http://search.maven.org/#search%7Cga%7C1%7Ccom.adjust.sdk
[application]:          http://developer.android.com/guide/topics/manifest/application-element.html
[eclipse_library]:      http://developer.android.com/tools/projects/projects-eclipse.html#ReferencingLibraryProject
[guide_permissions]:    https://github.com/adjust/android_sdk#5-add-permissions
[google_play_services]: http://developer.android.com/google/play-services/setup.html
