package com.adjust.example;

import android.net.*;
import android.support.test.filters.*;
import android.support.test.rule.*;
import android.support.test.runner.*;
import android.util.*;

import com.adjust.sdk.*;

import org.junit.*;
import org.junit.runner.*;

import dalvik.annotation.*;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by ab on 01/12/2016.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HelloWorldEspressoTest {
    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void foo() {
//        initAdjust();

//        AdjustAnalyzer.reportState("foo() 1");

//        onView(withId(R.id.btnEnableDisableOfflineMode))
//                .perform(click());

//        AdjustAnalyzer.reportState("foo() 2");

        onView(withId(R.id.btnEnableDisableSDK))
                .perform(click());

//        AdjustAnalyzer.reportState("foo() 3");

        onView(withId(R.id.btnEnableDisableSDK))
                .perform(click());

//        AdjustAnalyzer.reportState("foo() 4");

//        AdjustAnalyzer.shutdownAnalyzer();
    }

    private void initAdjust() {
        // Configure adjust SDK.
        String appToken = "2fm9gkqubvpc";
        String environment = AdjustConfig.ENVIRONMENT_SANDBOX;

        AdjustConfig config = new AdjustConfig(mainActivityActivityTestRule.getActivity(), appToken, environment);

        // Change the log level.
        config.setLogLevel(LogLevel.VERBOSE);

        // Set attribution delegate.
        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                Log.d("example", "Attribution callback called!");
                Log.d("example", "Attribution: " + attribution.toString());
            }
        });

        // Set event success tracking delegate.
        config.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
            @Override
            public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
                Log.d("example", "Event success callback called!");
                Log.d("example", "Event success data: " + eventSuccessResponseData.toString());
            }
        });

        // Set event failure tracking delegate.
        config.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
            @Override
            public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
                Log.d("example", "Event failure callback called!");
                Log.d("example", "Event failure data: " + eventFailureResponseData.toString());
            }
        });

        // Set session success tracking delegate.
        config.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
            @Override
            public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
                Log.d("example", "Session success callback called!");
                Log.d("example", "Session success data: " + sessionSuccessResponseData.toString());
            }
        });

        // Set session failure tracking delegate.
        config.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
            @Override
            public void onFinishedSessionTrackingFailed(AdjustSessionFailure sessionFailureResponseData) {
                Log.d("example", "Session failure callback called!");
                Log.d("example", "Session failure data: " + sessionFailureResponseData.toString());
            }
        });

        // Evaluate deferred deep link to be launched.
        config.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
            @Override
            public boolean launchReceivedDeeplink(Uri deeplink) {
                Log.d("example", "Deferred deep link callback called!");
                Log.d("example", "Deep link URL: " + deeplink);

                return true;
            }
        });

        // Set default tracker.
        // config.setDefaultTracker("{YourDefaultTracker}");

        // Set process name.
        // config.setProcessName("com.adjust.examples");

        // Allow to send in the background.
        config.setSendInBackground(true);

        // Enable event buffering.
        // config.setEventBufferingEnabled(true);

        // Delay first session.
        // config.setDelayStart(7);

        // Add session callback parameters.
        Adjust.addSessionCallbackParameter("sc_foo", "sc_bar");
        Adjust.addSessionCallbackParameter("sc_key", "sc_value");

        // Add session partner parameters.
        Adjust.addSessionPartnerParameter("sp_foo", "sp_bar");
        Adjust.addSessionPartnerParameter("sp_key", "sp_value");

        // Remove session callback parameters.
        Adjust.removeSessionCallbackParameter("sc_foo");

        // Remove session partner parameters.
        Adjust.removeSessionPartnerParameter("sp_key");

        // Remove all session callback parameters.
        Adjust.resetSessionCallbackParameters();

        // Remove all session partner parameters.
        Adjust.resetSessionPartnerParameters();

        // Initialise the adjust SDK.
        AdjustAnalyzer.connectToAnalyzer("172.16.150.242", "9999", config);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Adjust.onCreate(config);

        // Abort delay for the first session introduced with setDelayStart method.
        // Adjust.sendFirstPackages();

        // Register onResume and onPause events of all activities
        // for applications with minSdkVersion >= 14.

        // Put the SDK in offline mode.
        // Adjust.setOfflineMode(true);

        // Disable the SDK
        // Adjust.setEnabled(false);

        // Send push notification token.
        // Adjust.setPushToken("token");
    }
}
