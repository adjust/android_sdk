package com.adjust.testapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.adjust.sdk.Adjust;
import com.adjust.test.TestLibrary;

public class MainActivity extends AppCompatActivity {
    public static TestLibrary testLibrary;
    private static final String baseIp = "192.168.1.127";
    public static final String baseUrl = "https://" + baseIp + ":8443";
    public static final String gdprUrl = "https://" + baseIp + ":8443";
    public static final String controlUrl = "ws://" + baseIp + ":1987";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if deferred deep link was received
        Intent intent = getIntent();
        Uri deeplinkData = intent.getData();
        if (deeplinkData != null) {
            Adjust.appWillOpenUrl(deeplinkData, getApplicationContext());
            return;
        }

        testLibrary = new TestLibrary(baseUrl, controlUrl, new CommandListener(this.getApplicationContext()));
        // testLibrary.doNotExitAfterEnd();

        startTestSession();
    }

    private void startTestSession() {
        testLibrary.addTest("Test_PurchaseVerification_android_after_install");
        testLibrary.addTest("Test_PurchaseVerification_android_before_install");
        // testLibrary.addTestDirectory("current/gdpr");

        testLibrary.startTestSession(Adjust.getSdkVersion());
    }
}
