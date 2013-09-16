## Migrate to AdjustIo SDK for Android v2.1.0

0. In order to save some time later on you might want to check your current SDK
   Version. You can find that value in a constant named `CLIENT_SDK` (or
   `CLIENTSDK`) in `com.adeven.adjustio/Util.java`. It should look like
   `android1.6` or similar.

1. Delete the old `AdjustIo` project from your `Package Explorer`. Download
   version v2.1.0 and create a new `Android Project from Existing Code` as
   described in the [README].

    ![][import]

2.  Add AdjustIo settings to your `AndroidManifest.xml`. Add the following
    `meta-data` tags inside the `application` tag.

    ```xml
    <meta-data android:name="AdjustIoAppToken"    android:value="{YourAppToken}" />
    <meta-data android:name="AdjustIoLogLevel"    android:value="info" />
    <meta-data android:name="AdjustIoEnvironment" android:value="sandbox" /> <!-- TODO: change to 'production' -->
    ```

    ![][settings]

    Replace `{YourAppToken}` with your App Token. You can find it in your
    [dashboard].

    The log level is now set globally by `AdjustIoLogLevel`. Possible values:

   - `verbose` - enable all logging
   - `debug` - enable more logging
   - `info` - the default
   - `warn` - disable info logging
   - `error` - disable warnings as well
   - `assert` - disable errors as well

    Depending on whether or not you build your app for testing or for
    production you must adjust the `AdjustIoEnvironment` setting:

   - `sandbox` - for testing
   - `production` - before publishing

    **Important:** This value should be set to `sandbox` if and only if you or
    someone else is testing your app. Make sure to set the environment to
    `production` just before you publish the app. Set it back to `sandbox` when
    you start testing it again.

    We use this environment to distinguish between real traffic and artificial
    traffic from test devices. It is very important that you keep this value
    meaningful at all times! Especially if you are tracking revenue.

## Additional steps if you come from v2.0.x

1. Every activity of your app should call `AdjustIo.onResume` in its own
   `onResume` method. Remove the `appToken` parameter in all those calls.
   Afterwards it should look like this:

    ```java
    protected void onResume() {
        super.onResume();
        AdjustIo.onResume(this);
    }
    ```

2. Remove all calls to `AdjustIo.setLogLevel`.

## Additional steps if you come from v1.x

3. We no longer use the `AdjustIo.appDidLaunch()` method for initialization.
   Delete the call in your launch activity's `onCreate` method.

4. Instead, to provide proper session tracking, it is required to call certain
   new AdjustIo methods every time any Activity resumes or pauses. Otherwise
   the SDK might miss a session start or session end. In order to do so you
   should follow these steps for **each** Activity of your app:

   - Open the source file of your Activity.
   - Add the `import` statement at the top of the file.
   - In your Activity's `onResume` method call `AdjustIo.onResume`. Create the
     method if needed.
   - In your Activity's `orPause` method call `AdjustIo.onPause`. Create the
     method if needed.

    After these steps your activity should look like this:

    ```java
    import com.adeven.adjustio.AdjustIo;
    // ...
    public class YourActivity extends Activity {
        protected void onResume() {
            super.onResume();
            AdjustIo.onResume(this);
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

5. The `amount` parameter of the `trackRevenue` methods is now of type
   `double`, so you can drop the `f` suffixes in number literals (`12.3f`
   becomes `12.3`).

[README]: ../README.md
[import]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/import.png
[activity]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/activity3.png
[dashboard]: http://adjust.io
[settings]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/settings.png

