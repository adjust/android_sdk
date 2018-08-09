package com.example.testappwebbridge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.adjust.sdk.bridge.AdjustBridge;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WebView webView = (WebView) findViewById(R.id.webView1);
        AdjustBridge.setApplicationContext(getApplication());
        AdjustBridge.setWebView(webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(AdjustBridge.getDefaultInstance(), "AdjustBridge");

        TestLibraryBridge testLibraryBridge = new TestLibraryBridge(webView);
        webView.addJavascriptInterface(testLibraryBridge, "TestLibraryBridge");

        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String message = String.format("%s - %d: %s", consoleMessage.sourceId(), consoleMessage.lineNumber(), consoleMessage.message());
                Log.d("testappwebbridge", message);
                return super.onConsoleMessage(consoleMessage);
            }
        });

        try {
            webView.loadUrl("file:///android_asset/AdjustTestApp-WebView.html");
        } catch (Exception e) {
            Log.d("testappwebbridge", "loadUrl, exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}