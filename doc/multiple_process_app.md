# Multiple process app

Android application can consist of one or more processes. You can choose to run your services or activities in
a process different than the main one. You can make this happen by setting ```android:process``` property
inside your process or activity definition in Android manifest file.

```xml
<activity
    android:name=".YourActivity"
    android:process=":YourProcessName">
</activity>
```

```xml
<activity
    android:name=".YourService"
    android:process=":YourProcessName">
</activity>
```

By defining your services or activities like this, you are forcing them to run in different process than the
main one. 

You can also change the name of your main process by changing ```android:process``` property of your ```application```
in Android manifest file.

```xml
<application
  android:name=".YourApp"
  android:icon="@drawable/ic_launcher"
  android:label="@string/app_name"
  android:theme="@style/AppTheme"
  android:process=":YourMainProcessName">
</application>
```

If you just leave this property out of your ```application``` definition in Android manifest file, main process will
have the same name as your app package. If your app package name is ```com.example.myapp```, that will also be the 
name of your main process. In that case, ```YourActivity``` and ```YourService``` will run in process with name ```com.example.myapp:YourProcessName```.

Adjust SDK at this moment __does not support__ tracking from more than one process in app. If you are using
more processes in your app, you should set main process name in ```AdjustConfig``` object.

```java
String appToken = "{YourAppToken}";
String environment = AdjustConfig.ENVIRONMENT_SANDBOX;  // or AdjustConfig.ENVIRONMENT_SANDBOX
AdjustConfig config = new AdjustConfig(this, appToken, environment);

config.setMainProcessName("com.example.myapp");

Adjust.onCreate(config);
```

By doing this, you will give information to our SDK about your main process name and our SDK will not initialize
in any other process where you maybe try to use it. If you try to use Adjust SDK from some other process, you will
get following log messages:

```
05-06 13:20:02.880    3442-3442/com.example.myapp:YourProcessName E/Adjust﹕ You can't initialize Adjust in process which is not the main one
05-06 13:20:02.885    3442-3442/com.example.myapp:YourProcessName E/Adjust﹕ AdjustConfig not initialized correctly
```

If you do not set your main process name in ```AdjustConfig``` object and try to use SDK in other processes, you will 
initialize more different Adjust SDK instances, because different processes in Android do not share same memory space.
This can lead to some unpredicted situations, so we strongly advise you always to set your main process name if you 
have multiple process app or simply not to use Adjust SDK in more than one process in your app.
