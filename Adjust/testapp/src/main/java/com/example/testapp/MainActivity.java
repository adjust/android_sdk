package com.example.testapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.Constants;
import com.adjust.sdk.UtilNetworking;
import com.adjust.testlibrary.TestLibrary;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.example.testapp.CommandListener.debug;

public class MainActivity extends AppCompatActivity {
    public static TestLibrary testLibrary;
    private CommandListener commandListener;
    public static final String TAG = "TestApp";

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
        String baseUrl = "https://10.0.2.2:8443";
        AdjustFactory.setTestingMode(baseUrl);
        testLibrary = new TestLibrary(baseUrl, commandListener);
        startTestSession();
    }

    private void startTestSession() {
//        testLibrary.setTests("current/sessionCount/Test_SessionCount");

        testLibrary.setTests("current/referrer/Test_ReftagReferrer_before_install;" +
                "current/referrer/Test_ReftagReferrer_before_install_kill_in_between;" +
                "current/referrer/Test_ReftagReferrer_before_install_uninstall_in_between;" +
                "current/referrer/Test_ReftagReferrer_after_install;" +
                "current/referrer/Test_ReftagReferrer_between_create_and_resume;" +
                "current/referrer/Test_ReftagReferrer_upon_new_session;" +
                "current/referrer/Test_ReftagReferrer_upon_new_subsession;" +
                "current/sessionParams/Test_SessionParams_add;" +
                "current/sessionParams/Test_SessionParams_overwrite;" +
                "current/sessionParams/Test_SessionParams_remove;" +
                "current/sessionParams/Test_SessionParams_reset;" +
                "current/appSecret/Test_AppSecret_no_secret;" +
                "current/appSecret/Test_AppSecret_with_secret;" +
                "current/attributionCallback/Test_AttributionCallback_ask_in_multiple;" +
                "current/attributionCallback/Test_AttributionCallback_ask_in_once;" +
                "current/attributionCallback/Test_AttributionCallback_no_ask_in;" +
                "current/defaultTracker/Test_DefaultTracker_empty_string;" +
                "current/defaultTracker/Test_DefaultTracker_null_string;" +
                "current/defaultTracker/Test_DefaultTracker_valid_string;" +
                "current/delayStart/Test_DelayStart_delay_0;" +
                "current/delayStart/Test_DelayStart_delay_15;" +
                "current/delayStart/Test_DelayStart_delay_5;" +
                "current/delayStart/Test_DelayStart_delay_5_call_sendFirstPackages;" +
                "current/delayStart/Test_DelayStart_delay_negative;" +
                "current/subsessionCount/Test_SubsessionCount;" +
                "current/userAgent/Test_UserAgent_empty;" +
                "current/userAgent/Test_UserAgent_not_empty;" +
                "current/disableEnable/Disable_pushToken_enable;" +
                "current/disableEnable/Disable_referrer_enable;" +
                "current/disableEnable/Disable_restart_track;" +
                "current/disableEnable/Disable_start_enable;" +
                "current/disableEnable/Disable_track_Enable_track;" +
                "current/sdkPrefix/Test_SdkPrefix_with_value;" +
                "current/sdkPrefix/Test_SdkPrefix_empty_value;" +
                "current/sdkPrefix/Test_SdkPrefix_null_value;" +
                "current/sdkInfo/Test_PushToken_after_install;" +
                "current/sdkInfo/Test_PushToken_before_install;" +
                "current/sdkInfo/Test_PushToken_before_install_kill_in_between;" +
                "current/sdkInfo/Test_PushToken_between_create_and_resume;" +
                "current/sdkInfo/Test_PushToken_multiple_tokens;" +
                "current/sessionCount/Test_SessionCount");
        // testLibrary.doNotExitAfterEnd();
        testLibrary.initTestSession("android4.12.0");
    }

    public void onStartTestSession(View v) {
        startTestSession();
    }
}
