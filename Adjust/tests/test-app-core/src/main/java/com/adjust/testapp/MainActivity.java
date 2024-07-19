package com.adjust.testapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustDeeplink;
import com.adjust.sdk.OnSdkVersionReadListener;
import com.adjust.test.TestLibrary;

public class MainActivity extends AppCompatActivity {
    public static TestLibrary testLibrary;
    private static final String baseIp = "10.0.2.2";
    public static final String baseUrl = "https://" + baseIp + ":8443";
    public static final String gdprUrl = "https://" + baseIp + ":8443";
    public static final String controlUrl = "ws://" + baseIp + ":1987";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if deferred deeplink was received
        Intent intent = getIntent();
        Uri deeplinkData = intent.getData();
        if (deeplinkData != null) {
            AdjustDeeplink adjustDeeplink = new AdjustDeeplink(deeplinkData);
            Adjust.processDeeplink(adjustDeeplink, getApplicationContext());
            return;
        }

        testLibrary = new TestLibrary(baseUrl, controlUrl, this.getApplicationContext(),
          new CommandListener(this.getApplicationContext()));
        // testLibrary.doNotExitAfterEnd();

        startTestSession();
    }

    private void startTestSession() {
        //testLibrary.addTestDirectory("app-secret");
        //testLibrary.addTest("Test_Event_Params");

        Adjust.getSdkVersion(new OnSdkVersionReadListener() {
            @Override
            public void onSdkVersionRead(String sdkVersion) {
                testLibrary.startTestSession(sdkVersion);
            }
        });

    }
}
