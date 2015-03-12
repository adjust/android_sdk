## Using Adjust for pre-installed apps

If you want to use the Adjust SDK to recognize users that found your app
pre-installed on their device, follow these steps.

1. Integrate Adjust as described in our [README].
2. Create a new tracker in your [dashboard].
3. Open your Application class and add set the default tracker of your
   `AdjustConfig`:

    ```java
    djustConfig config = new AdjustConfig(this, appToken, environment);
    config.setDefaultTracker("{TrackerToken}");
    Adjust.onCreate(config);
    ```

    Replace `{TrackerToken}` with the tracker token you created in step 2.
    Please note that the dashboard displays a tracker URL (including
    `http://app.adjust.io/`). In your source code, you should specify only the
    six-character token and not the entire URL.

4. Build and run your app. You should see a line like the following in LogCat:

    ```
    Default tracker: 'abc123'
    ```

[README]: ../README.md
[dashboard]: http://adjust.com
