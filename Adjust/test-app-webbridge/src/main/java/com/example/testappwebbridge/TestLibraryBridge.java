package com.example.testappwebbridge;

import android.app.Application;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.AdjustTestOptions;
import com.adjust.sdk.webbridge.AdjustBridgeUtil;
import com.adjust.test.ICommandRawJsonListener;
import com.adjust.test.TestLibrary;
import com.adjust.test_options.TestConnectionOptions;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TestLibraryBridge {
    public static WebView webView;
    private static Application application;
    private static TestLibrary testLibrary;
    private static int order;
    private static final String baseIp = "10.0.2.2";
    private static final String baseUrl = "https://" + baseIp + ":8443";
    public static final String controlUrl = "ws://" + baseIp + ":1987";

    public TestLibraryBridge(Application application, final WebView webView) {
        TestLibraryBridge.application = application;
        TestLibraryBridge.webView = webView;
        initTestLibrary();
    }

    @JavascriptInterface
    public static void startTestSession(String clientSdk) {
        order = 0;
        testLibrary.startTestSession(clientSdk);
    }

    private static void initTestLibrary() {
        TestLibraryBridge.testLibrary = new TestLibrary(baseUrl, controlUrl, application, new ICommandRawJsonListener() {
            @Override
            public void executeCommand(String json) {
                Log.d("testappwebbridge", String.format("execRawTestCommandCallback %s, %d", json, order));
                execRawTestCommandCallback(webView, "TestLibrary.adjust_commandRawJsonListenerCallback", order, json);
                order++;
            }
        });
    }

    @JavascriptInterface
    public static void addTestDirectory(String testDir) {
        Log.d("testappwebbridge", String.format("addTestDirectory: %s", testDir));
        testLibrary.addTestDirectory(testDir);
    }

    @JavascriptInterface
    public static void addTest(String testName) {
        Log.d("testappwebbridge", String.format("addTest %s", testName));
        testLibrary.addTest(testName);
    }

    @JavascriptInterface
    public static void addInfoToSend(String key, String value) {
        testLibrary.addInfoToSend(key, value);
    }

    @JavascriptInterface
    public static void sendInfoToServer(String basePath) {
        testLibrary.sendInfoToServer(basePath);
    }

    @JavascriptInterface
    public static void setTestOptions(final String testOptionsString) {
        AdjustFactory.getLogger().verbose("TestLibraryBridge setTestOptions: %s", testOptionsString);

        try {
            AdjustTestOptions adjustTestOptions = new AdjustTestOptions();
            JSONObject jsonAdjustTestOptions = new JSONObject(testOptionsString);

            Object baseUrlField = jsonAdjustTestOptions.get("baseUrl");
            Object gdprUrlField = jsonAdjustTestOptions.get("gdprUrl");
            Object subscriptionUrlField = jsonAdjustTestOptions.get("subscriptionUrl");
            Object basePathField = jsonAdjustTestOptions.get("basePath");
            Object gdprPathField = jsonAdjustTestOptions.get("gdprPath");
            Object subscriptionPathField = jsonAdjustTestOptions.get("subscriptionPath");
            Object useTestConnectionOptionsField = jsonAdjustTestOptions.get("useTestConnectionOptions");
            Object timerIntervalInMillisecondsField = jsonAdjustTestOptions.get("timerIntervalInMilliseconds");
            Object timerStartInMillisecondsField = jsonAdjustTestOptions.get("timerStartInMilliseconds");
            Object sessionIntervalInMillisecondsField = jsonAdjustTestOptions.get("sessionIntervalInMilliseconds");
            Object subsessionIntervalInMillisecondsField = jsonAdjustTestOptions.get("subsessionIntervalInMilliseconds");
            Object teardownField = jsonAdjustTestOptions.get("teardown");
            Object tryInstallReferrerField = jsonAdjustTestOptions.get("tryInstallReferrer");
            Object noBackoffWaitField = jsonAdjustTestOptions.get("noBackoffWait");
            Object hasContextField = jsonAdjustTestOptions.get("hasContext");

            String gdprUrl = AdjustBridgeUtil.fieldToString(gdprUrlField);
            if (gdprUrl != null) {
                adjustTestOptions.gdprUrl = gdprUrl;
            }

            String baseUrl = AdjustBridgeUtil.fieldToString(baseUrlField);
            if (baseUrl != null) {
                adjustTestOptions.baseUrl = baseUrl;
            }

            String subscriptionUrl = AdjustBridgeUtil.fieldToString(subscriptionUrlField);
            if (subscriptionUrl != null) {
                adjustTestOptions.subscriptionUrl = subscriptionUrl;
            }

            String basePath = AdjustBridgeUtil.fieldToString(basePathField);
            if (basePath != null) {
                adjustTestOptions.basePath = basePath;
            }

            String gdprPath = AdjustBridgeUtil.fieldToString(gdprPathField);
            if (gdprPath != null) {
                adjustTestOptions.gdprPath = gdprPath;
            }

            String subscriptionPath = AdjustBridgeUtil.fieldToString(subscriptionPathField);
            if (subscriptionPath != null) {
                adjustTestOptions.subscriptionPath = subscriptionPath;
            }

            Long timerIntervalInMilliseconds = AdjustBridgeUtil.fieldToLong(timerIntervalInMillisecondsField);
            if (timerIntervalInMilliseconds != null) {
                adjustTestOptions.timerIntervalInMilliseconds = timerIntervalInMilliseconds;
            }

            Long timerStartInMilliseconds = AdjustBridgeUtil.fieldToLong(timerStartInMillisecondsField);
            if (timerStartInMilliseconds != null) {
                adjustTestOptions.timerStartInMilliseconds = timerStartInMilliseconds;
            }

            Long sessionIntervalInMilliseconds = AdjustBridgeUtil.fieldToLong(sessionIntervalInMillisecondsField);
            if (sessionIntervalInMilliseconds != null) {
                adjustTestOptions.sessionIntervalInMilliseconds = sessionIntervalInMilliseconds;
            }

            Long subsessionIntervalInMilliseconds = AdjustBridgeUtil.fieldToLong(subsessionIntervalInMillisecondsField);
            if (subsessionIntervalInMilliseconds != null) {
                adjustTestOptions.subsessionIntervalInMilliseconds = subsessionIntervalInMilliseconds;
            }

            Boolean teardown = AdjustBridgeUtil.fieldToBoolean(teardownField);
            if (teardown != null) {
                adjustTestOptions.teardown = teardown;
            }

            Boolean tryInstallReferrer = AdjustBridgeUtil.fieldToBoolean(tryInstallReferrerField);
            if (tryInstallReferrer != null) {
                adjustTestOptions.tryInstallReferrer = tryInstallReferrer;
            }

            Boolean noBackoffWait = AdjustBridgeUtil.fieldToBoolean(noBackoffWaitField);
            if (noBackoffWait != null) {
                adjustTestOptions.noBackoffWait = noBackoffWait;
            }

            Boolean hasContext = AdjustBridgeUtil.fieldToBoolean(hasContextField);
            if (hasContext != null && hasContext.booleanValue()) {
                adjustTestOptions.context = application.getApplicationContext();
            }

            Adjust.setTestOptions(adjustTestOptions);

            Boolean useTestConnectionOptions = AdjustBridgeUtil.fieldToBoolean(useTestConnectionOptionsField);
            if (useTestConnectionOptions != null) {
                TestConnectionOptions.setTestConnectionOptions();
            }
        } catch (Exception e) {
            AdjustFactory.getLogger().error("AdjustBridgeInstance setTestOptions: %s", e.getMessage());
        }
    }

    private static void execRawTestCommandCallback(final WebView webView, final String commandName, final int order, final String json) {
        if (webView == null) {
            return;
        }

        String command = null;
        try {
            // need to URL encode, because webView.loadUrl will Url decode it
            command = "javascript:" + commandName + "(" + order + ", " + URLEncoder.encode(json,"UTF-8") + ");";
        } catch (UnsupportedEncodingException e) {
            Log.d("testappwebbridge", "execRawTestCommandCallback: " + e.getMessage());
            // fallback to no url encode
            command = "javascript:" + commandName + "(" + order + ", " + json + ");";
        }

        final String finalCommand = command;
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(finalCommand);
            }
        });
    }
}
