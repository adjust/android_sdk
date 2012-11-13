## Summary

This is the Android SDK of AdjustIo. You ca read more about it at [adjust.io][].

## Basic Installation

These are the minimal steps required to integrate the AdjustIo SDK into your Android project. We are going to assume that you use Eclipse for your Android development.

### 1. Get the SDK
Download the latest version from our [tags page][tags]. Extract the archive in a folder of your liking.

### 2. Add it to your project
In the Eclipse menu select `File|New|Project...`.

![New Project][project]

From the Wizard expand the `Android` group and select `Android Project from Existing Code` and click `Next`.

![Android Project][android]

On the top of the next screen click the `Browse...` button and locate the folder you extracted in step 1. Select the AdjustIo subfolder and click `Open`. In the `Projects:` group make sure the AdjustIo project is selected. Also tick the option `Copy projects into workspace` and click `Finish`.

![Import Projects][import]

### 3. Integrate AdjustIo into your app
In the Package Explorer right click on your Android project and select `Properties`.

![Project Properties][properties]

In the left pane select `Android`. In the bottom right group `Library` click the `Add...` button. From the list select the AdjustIo library project and click `OK`. Save your changed project properties by clicking `OK` again.

![Add Library][library]

In the Package Explorer open the `AndroidManifest.xml` of your Android project. Add the `uses-permission` tag for `INTERNET` if it isn't present already.

    <uses-permission android:name="android.permission.INTERNET" />

![Add Permissions][permissions]

In the Package Explorer open the launch activity of your Android App. Add the `import` statement to the top of the source file. In the onCreate method of your activity call the method `appDidLaunch`. This tells AdjustIo about the launch of your Application.

    import com.adeven.adjustio.AdjustIo;
    ...
    AdjustIo.appDidLaunch(getApplication());

![Adjust Activity][activity]

### 4. Build your app
Build and run your Android app. In your LogCat viewer you can set the filter `tag:AdjustIo` to hide all other logs. After your app has launched you should see the following AdjustIo log: `Tracked session start.`

![AdjustIo log][log]

## Additional Features

Once you have integrated the AdjustIo SDK into you project, you can take advantage of the following features wherever you see fit.

### Add tracking of custom events.
You can tell AdjustIo about every event you consider to be of your interest. Suppose you want to track every tap on a button. We would give you an eventId, like `abc123`. In your button's onClick method you could then add the following code to track the click:

    AdjustIo.trackEvent("abc123");

You can also register a callback URL for that event and we will send a request to that URL whenever the event happens. Additianally you can put some key-value-pairs in a Map and pass it to the trackEvent method. In that case we will forward these named parameters to your callback URL. Suppose you registered the URL `http://www.adeven.com/callback` for your event and call the following lines:

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("key", "value");
    parameters.put("foo", "bar");
    AdjustIo.trackEvent("abc123", parameters);

In that case we would track the event and send a request to `http://www.adeven.com/callback?key=value&foo=bar`. In any case you need to import AdjustIo in any source file that makes use of the SDK.

### Add tracking of revenue

If your users can make revenue by clicking on advertisements you can track those revenues. If the click is worth one cent, you could make the following call to track that revenue:

    AdjustIo.trackRevenue(1.0f);

The parameter is supposed to be in Cents and will get rounded to one decimal point. If you want to differentiate between different kinds of revenue you can get different eventIds for each kind. In that case you would make a call like this:

    AdjustIo.trackRevenue(1.0f, "abc123");

You can also register a callback URL again and provide a map of named parameters, just like it worked with normal events.

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("key", "value");
    parameters.put("foo", "bar");
    AdjustIo.trackRevenue(1.0f, "abc123", parameters)

In any case, don't forget to import AdjustIo.


[adjust.io]: http://www.adjust.io
[tags]: https://github.com/adeven/adjust_android_sdk/tags
[project]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/project.png
[android]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/android.png
[import]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/import.png
[properties]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/properties.png
[library]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/library.png
[permissions]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/permissions2.png
[activity]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/activity.png
[log]: https://raw.github.com/adeven/adjust_sdk/master/Resources/android/log.png


## License

The adjust-sdk is licensed under the MIT License.

Copyright (c) 2012 adeven GmbH,
http://www.adeven.com

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
