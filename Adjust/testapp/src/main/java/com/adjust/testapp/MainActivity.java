package com.adjust.testapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.adjust.sdk.Adjust;
import com.adjust.testlibrary.TestLibrary;

public class MainActivity extends AppCompatActivity {
    public static TestLibrary testLibrary;
    public static final String baseUrl = "https://10.0.2.2:8443";
    public static final String gdprUrl = "https://10.0.2.2:8443";

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

        testLibrary = new TestLibrary(baseUrl, new CommandListener(this.getApplicationContext()));
        // testLibrary.doNotExitAfterEnd();

        startTestSession();
    }

    private void startTestSession() {
        // testLibrary.addTestDirectory("current/gdpr");
        // testLibrary.addTest("current/gdpr/Test_GdprForgetMe_after_install_kill_before_install");

        testLibrary.startTestSession("android4.13.0");
    }
}
