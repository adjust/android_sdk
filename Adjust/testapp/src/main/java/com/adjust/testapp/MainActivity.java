package com.adjust.testapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.adjust.sdk.Adjust;
import com.adjust.testlibrary.TestLibrary;

public class MainActivity extends AppCompatActivity {
    public static TestLibrary testLibrary;
    private CommandListener commandListener;
    public static final String TAG = "TestApp";
    public static final String baseUrl = "https://10.0.2.2:8443";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if deferred deeplink was received
        Intent intent = getIntent();
        Uri deeplinkData = intent.getData();
        if (deeplinkData != null) {
            Adjust.appWillOpenUrl(deeplinkData);
            return;
        }

        commandListener = new CommandListener(this.getApplicationContext());
        testLibrary = new TestLibrary(baseUrl, commandListener);
        startTestSession();
    }

    private void startTestSession() {
        //testLibrary.setTests("current/appSecret/Test_AppSecret_no_secret");
        //testLibrary.setTests("current/Test_Nothing");
        //testLibrary.setTests("current/attributionCallback/Test_AttributionCallback_no_ask_in");
        testLibrary.doNotExitAfterEnd();
        testLibrary.initTestSession("android4.12.0");
    }

    public void onStartTestSession(View v) {
        startTestSession();
    }
}
