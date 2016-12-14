# Google広告ID

[Google広告ID][google_ad_id]が取得できる場合、adjustのAndroid SDKはデフォルトでこれを送信します。
アプリが[Google Playサービス][ensure]を利用していない場合のみ、adjustはそのデバイスのAndroid IDと
Macアドレスの取得を試みます。

adjustにはGoogle PlayサービスのためのMacアドレスやAndroid IDの使用を防止する対策がありますが、
このデバイス用機能へアクセスするソースファイルを削除することも可能です。削除するには以下の手順に従ってください。

1. こちらの[ガイド][get_sdk]の最初の手順に従ってadjustのAndroid SDKを入手してください。

2. `adjust/src/main/java/com/adjust/sdk/plugin`フォルダを見つけてください。
そこに`MacAddressUtil.java`ファイルと`AndroidIdUtil.java`ファイルがあります。

3. プロジェクトでそれらを使いたくなければ、それらの片方もしくは両方を削除してください。

[google_ad_id]:https://developer.android.com/google/play-services/id.html
[ensure]:http://developer.android.com/google/play-services/setup.html#ensure
[get_sdk]:https://github.com/adjust/android_sdk#1-get-the-sdk
