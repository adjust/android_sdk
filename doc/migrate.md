## Migrate your adjust SDK for Android to 3.3.5 from v2.1.x

We renamed the main class `com.adeven.adjustio.AdjustIo` to
`com.adjust.sdk.Adjust`. Follow these steps to update all adjust SDK calls.

1. Right click on the old `AdjustIo` project in the `Package Explorer` and
   select `Delete`. Check the box `Delete project contents on disk` and confirm
   `OK`.

2. From the Eclipse menu select `Search → File...`, select the tab `File
   Search`, enter the search text `AdjustIo` and check the box `Case
   sensitive`. Make sure the file name pattern is `*` and the scope is
   `Workspace`.

   ![][search]

3. Click `Replace...`, enter the replacement text `Adjust` and
   click `Preview >`. All adjust calls in java files and all adjust settings in
   manifest files should be replaced. After you reviewed these changes confirm
   with `OK`.

   ![][replace]

4. In the same fashion, replace `adeven.adjustio` with `adjust.sdk` in all
   manifest files to update the package name of the `ReferrerReceiver`.

5. Download version v3.3.5 and create a new Android project from the `Adjust` folder.

    ![][import]

6. Open the Android properties of your apps and make sure that the new `Adjust`
   library is selected.

7. For each of your apps, right click on it in the `Package Explorer` and
   select `Source → Organize Imports`.

8. Build your project to confirm that everything is properly connected again.

The adjust SDK v3.3.5 added delegate notifications. Check out the [README] for
details.


## Additional steps if you come from v2.0.x

Add adjust settings to your `AndroidManifest.xml`. Add the following
`meta-data` tags inside the `application` tag.

```xml
<meta-data android:name="AdjustAppToken"    android:value="{YourAppToken}" />
<meta-data android:name="AdjustLogLevel"    android:value="info" />
<meta-data android:name="AdjustEnvironment" android:value="sandbox" /> <!-- TODO: change to 'production' -->
```

![][settings]

Replace `{YourAppToken}` with your App Token. You can find it in your
[dashboard].

The log level is now set globally by `AdjustLogLevel`. Possible values:

- `verbose` - enable all logging
- `debug` - enable more logging
- `info` - the default
- `warn` - disable info logging
- `error` - disable warnings as well
- `assert` - disable errors as well

Depending on whether or not you build your app for testing or for
production you must adjust the `AdjustEnvironment` setting:

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

1. Every activity of your app should call `Adjust.onResume` in its own
   `onResume` method. Remove the `appToken` parameter in all those calls.
   Afterwards it should look like this:

    ```java
    protected void onResume() {
        super.onResume();
        Adjust.onResume(this);
    }
    ```

2. Remove all calls to `Adjust.setLogLevel`.

## Additional steps if you come from v1.x

3. We no longer use the `Adjust.appDidLaunch()` method for initialization.
   Delete the call in your launch activity's `onCreate` method.

4. Instead, to provide proper session tracking, it is required to call certain
   new adjust methods every time any Activity resumes or pauses. Otherwise
   the SDK might miss a session start or session end. In order to do so you
   should follow these steps for **each** Activity of your app:

   - Open the source file of your Activity.
   - Add the `import` statement at the top of the file.
   - In your Activity's `onResume` method call `Adjust.onResume`. Create the
     method if needed.
   - In your Activity's `orPause` method call `Adjust.onPause`. Create the
     method if needed.

    After these steps your activity should look like this:

    ```java
    import com.adjust.sdk.Adjust;
    // ...
    public class YourActivity extends Activity {
        protected void onResume() {
            super.onResume();
            Adjust.onResume(this);
        }
        protected void onPause() {
            super.onPause();
            Adjust.onPause();
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
[dashboard]: http://adjust.com
[search]: https://raw.github.com/adjust/adjust_sdk/master/Resources/android/search.png
[replace]: https://raw.github.com/adjust/adjust_sdk/master/Resources/android/replace.png
[import]: https://raw.github.com/adjust/adjust_sdk/master/Resources/android/import2.png
[activity]: https://raw.github.com/adjust/adjust_sdk/master/Resources/android/activity4.png
[settings]: https://raw.github.com/adjust/adjust_sdk/master/Resources/android/settings.png

