# Activities lifecycle.

## Android 4.0.0 Ice Cream Sandwich or superior

If your app `minSdkVersion` in gradle is `14` or superior, you don't need add `Adjust.onResume` 
and `Adjust.onPause` calls on each Activity of your app, just follow the [guide][guide].
If you had them before, you should remove them.

## Android 2.3 Gingerbread

If your app `minSdkVersion` in gradle is between `9` and `13`, consider updating it 
to at least `14` to simplify the integration process in the long term. Consult the official
Android [dashboard][android-dashboard] to know the latest market share of the major versions.

To provide proper session tracking it is required to call certain Adjust
methods every time any Activity resumes or pauses. Otherwise the SDK might miss
a session start or session end. In order to do so you should follow these steps
for **each** Activity of your app:

1. Open the source file of your Activity.
2. Add the `import` statement at the top of the file.
3. In your Activity's `onResume` method call `Adjust.onResume`. Create the
  method if needed.
4. In your Activity's `onPause` method call `Adjust.onPause`. Create the method
  if needed.

After these steps your activity should look like this:

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

Repeat these steps for **every** Activity of your app. Don't forget these steps
when you create new Activities in the future. Depending on your coding style
you might want to implement this in a common superclass of all your Activities.


[guide]:      /README.md
[android-dashboard]:    http://developer.android.com/about/dashboards/index.html

[activity]: https://raw.github.com/adjust/sdks/master/Resources/android/v4/14_activity.png
