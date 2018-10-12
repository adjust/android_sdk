package com.example.testappwebbridge;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.adjust.test.ICommandRawJsonListener;
import com.adjust.test.TestLibrary;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TestLibraryBridge {
    public static WebView webView;
    private static TestLibrary testLibrary;
    private static int order;
    private static final String baseUrl = "https://10.0.2.2:8443";

    public TestLibraryBridge(final WebView webView) {
        TestLibraryBridge.webView = webView;
        initTestLibrary();
    }

    @JavascriptInterface
    public static void startTestSession(String clientSdk) {
        order = 0;
        testLibrary.startTestSession(clientSdk);
    }

    private static void initTestLibrary() {
        TestLibraryBridge.testLibrary = new TestLibrary(baseUrl, new ICommandRawJsonListener() {
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
