package com.example.testappwebbridge;

import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.webbridge.AdjustBridge;

public class MainActivity extends AppCompatActivity {
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView1);
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

        // Check if deferred deep link was received
        Intent intent = getIntent();
        Uri deeplinkData = intent.getData();
        if (deeplinkData != null) {
            Adjust.appWillOpenUrl(deeplinkData, getApplicationContext());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri deeplink = intent.getData();
        if (deeplink != null) {
            Adjust.appWillOpenUrl(deeplink, getApplicationContext());
        }
    }
}
