## Migrate to AdjustIo SDK for Android v2.0

1. Delete the old `AdjustIo` project from your `Package Explorer`. Download
   version v2.0 and create a new `Android Project from Existing Code` as
   described in the [README].

    ![][import]

2. We no longer use the `AdjustIo.appDidLaunch()` method for initialization.
   Delete the call in your launch activity's `onCreate` method.

3. Instead, to provide proper session tracking, it is required to call certain
   new AdjustIo methods every time any Activity resumes or pauses. Otherwise
   the SDK might miss a session start or session end. In order to do so you
   should follow these steps for **each** Activity of your app:

   - Open the source file of your Activity.
   - Add the `import` statement at the top of the file.
   - In your Activity's `onResume` method call `AdjustIo.onResume`. Create the
     method if needed.
   - Replace `{YourAppToken}` with your App Token. You can find in your
     [dashboard].
   - In your Activity's `orPause` method call `AdjustIo.onPause`. Create the
     method if needed.

    After these steps your activity should look like this:

    ```java
    import com.adeven.adjustio.AdjustIo;
    // ...
    public class YourActivity extends Activity {
        protected void onResume() {
            super.onResume();
            AdjustIo.onResume("{YourAppToken}", this);
        }
        protected void onPause() {
            super.onPause();
            AdjustIo.onPause();
        }
        // ...
    }
    ```

    ![][activity]

    Repeat these steps for **every** Activity of your app. Don't forget these
    steps when you create new Activities in the future. Depending on your
    coding style you might want to implement this in a common superclass of all
    your Activities.

4. The `amount` parameter of the `trackRevenue` methods is now of type
   `double`, so you can drop the `f` suffixes in number literals (`12.3f`
   becomes `12.3`).

[README]: ../README.md
[import]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/import.png
[activity]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/activity2.png
[dashboard]: http://adjust.io

