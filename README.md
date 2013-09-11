<!-- TODO: Explain how to configure AdjustIo in the AndroidManifest.xml -->
## Summary

This is the Android SDK of AdjustIo. You ca read more about AdjustIo at
[adjust.io].

## Basic Installation

These are the minimal steps required to integrate the AdjustIo SDK into your
Android project. We are going to assume that you use Eclipse for your Android
development.

### 1. Get the SDK

Download the latest version from our [releases page][releases]. Extract the
archive in a folder of your choice.

### 2. Create the AdjustIo project

In the Eclipse menu select `File|New|Project...`.

![][project]

From the Wizard expand the `Android` group and select `Android Project from
Existing Code` and click `Next`.

![][android]

On the top of the next screen click the `Browse...` button and locate the
folder you extracted in step 1. Select the AdjustIo subfolder and click `Open`.
In the `Projects:` group make sure the AdjustIo project is selected. Also tick
the option `Copy projects into workspace` and click `Finish`.

![][import]

### 3. Add the AdjustIo library to your project

In the Package Explorer right click on your Android project and select
`Properties`.

![][properties]

In the left pane select `Android`. In the bottom right group `Library` click
the `Add...` button. From the list select the AdjustIo library project and
click `OK`. Save your changed project properties by clicking `OK` again.

![][library]

### 4. Add permissions

In the Package Explorer open the `AndroidManifest.xml` of your Android project.
Add the `uses-permission` tags for `INTERNET` and `ACCESS_WIFI_STATE` if they
aren't present already.

```java
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

![][permissions]

### 5. Integrate AdjustIo into your app

To provide proper session tracking it is required to call certain AdjustIo
methods every time any Activity resumes or pauses. Otherwise the SDK might miss
a session start or session end. In order to do so you should follow these steps
for **each** Activity of your app:

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

Repeat these steps for **every** Activity of your app. Don't forget these steps
when you create new Activities in the future. Depending on your coding style
you might want to implement this in a common superclass of all your Activities.

### 6. Build your app

Build and run your Android app. In your LogCat viewer you can set the filter
`tag:AdjustIo` to hide all other logs. After your app has launched you should
see the following AdjustIo log: `Tracked session start`

![][log]

### 7. Adjust Logging

You can increase or decrease the amount of logs you see by calling
`setLogLevel` with one of the following parameters. Make sure to import
`android.util.Log`.

```java
AdjustIo.setLogLevel(Log.VERBOSE); // enable all logging
AdjustIo.setLogLevel(Log.DEBUG);   // enable more logging
AdjustIo.setLogLevel(Log.INFO);    // the default
AdjustIo.setLogLevel(Log.WARN);    // disable info logging
AdjustIo.setLogLevel(Log.ERROR);   // disable warnings as well
AdjustIo.setLogLevel(Log.ASSERT);  // disable errors as well
```

## Additional Features

Once you have integrated the AdjustIo SDK into your project, you can take
advantage of the following features.

### Add tracking of custom events.

You can tell AdjustIo about every event you want. Suppose you want to track
every tap on a button. You would have to create a new Event Token in your
[dashboard]. Let's say that Event Token is `abc123`. In your button's `onClick`
method you could then add the following line to track the click:

```java
AdjustIo.trackEvent("abc123");
```

You can also register a callback URL for that event in your [dashboard] and we
will send a GET request to that URL whenever the event gets tracked. In that
case you can also put some key-value-pairs in a dictionary and pass it to the
`trackEvent` method. We will then append these named parameters to your
callback URL.

For example, suppose you have registered the URL
`http://www.adeven.com/callback` for your event with Event Token `abc123` and
execute the following lines:

```java
Map<String, String> parameters = new HashMap<String, String>();
parameters.put("key", "value");
parameters.put("foo", "bar");
AdjustIo.trackEvent("abc123", parameters);
```

In that case we would track the event and send a request to:

    http://www.adeven.com/callback?key=value&foo=bar

It should be mentioned that we support a variety of placeholders like
`{android_id}` that can be used as parameter values. In the resulting callback
this placeholder would be replaced with the AndroidID of the current device.
Also note that we don't store any of your custom parameters, but only append
them to your callbacks. If you haven't registered a callback for an event,
these parameters won't even be read.

### Add tracking of revenue

If your users can generate revenue by clicking on advertisements or making
purchases you can track those revenues. If, for example, a click is worth one
cent, you could make the following call to track that revenue:

```java
AdjustIo.trackRevenue(1.0);
```

The parameter is supposed to be in cents and will get rounded to one decimal
point. If you want to differentiate between different kinds of revenue you can
get different Event Tokens for each kind. Again, you need to create those Event
Tokens in your [dashboard]. In that case you would make a call like this:

```java
AdjustIo.trackRevenue(1.0, "abc123");
```

Again, you can register a callback and provide a dictionary of named
parameters, just like it worked with normal events.

```java
Map<String, String> parameters = new HashMap<String, String>();
parameters.put("key", "value");
parameters.put("foo", "bar");
AdjustIo.trackRevenue(1.0, "abc123", parameters);
```

[adjust.io]: http://adjust.io
[dashboard]: http://adjust.io
[releases]: https://github.com/adeven/adjust_android_sdk/releases
[project]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/project.png
[android]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/android.png
[import]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/import.png
[properties]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/properties.png
[library]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/library.png
[permissions]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/permissions.png
[activity]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/activity2.png
[log]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/log2.png


## License

The adjust-sdk is licensed under the MIT License.

Copyright (c) 2012 adeven GmbH,
http://www.adeven.com

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
