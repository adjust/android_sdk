# アクティビティのライフサイクル

## Android 4.0.0 Ice Cream Sandwich以降

Gradleの`minSdkVersion`が`14`もしくはそれ以上の場合、アクティビティ毎に`Adjust.onResume`と
`Adjust.onPause`のコールを追加する必要はありません。[ガイド][guide]に従って設定してください。
これらのコールが既に追加されている場合は、削除する必要があります。

## Android 2.3 Gingerbread

Gradleの`minSdkVersion`が`9`から`13`までのいずれかの場合、`14`以上に更新すると以後の連携手順が
簡単になりますので、ご検討ください。Android公式[ダッシュボード][android-dashboard]にて
バージョンのマーケットシェアをご覧いただけます。

セッショントラッキングを正しく行うために、アクティビティの再開時と停止時に毎回
特定のAdjustメソッドをコールする必要があります。この設定が済んでいないと、SDKはセッションの開始や終了を
捉えられない場合があります。下記の手順に従って、アプリの**すべての**アクティビティにこの設定を加えてください。

1. アクティビティのソースファイルを開いてください。
2. ファイルの先頭に`import`の記述を追加してください。
3. アクティビティ内に`onResume`があればそこに、なければこのメソッドを作成して、
  `Adjust.onResume`のコールを追加してください。
4. アクティビティ内に`onPause`があればそこに、なければこのメソッドを作成して、
  `Adjust.onPause`のコールを追加してください。

これらの手順が済むと、アクティビティは次のようになるはずです。

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

![][activity]

これらの手順をアプリの**すべての**アクティビティで行ってください。今後新しいアクティビティを作成する際にも
忘れずに行ってください。コーディング方法によっては、すべてのアクティビティ共通のスーパークラスに
これを実装したほうがいい場合もあります。


[guide]:      /README.md
[android-dashboard]:    http://developer.android.com/about/dashboards/index.html

[activity]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/14_activity.png
