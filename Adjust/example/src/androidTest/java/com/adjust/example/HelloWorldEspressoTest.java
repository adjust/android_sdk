package com.adjust.example;

import android.content.*;
import android.net.*;
import android.support.test.*;
import android.support.test.filters.*;
import android.support.test.rule.*;
import android.support.test.runner.*;
import android.util.*;

import com.adjust.sdk.*;

import org.junit.*;
import org.junit.runner.*;

import dalvik.annotation.*;

import static android.support.test.espresso.Espresso.getIdlingResources;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;

/**
 * Created by ab on 01/12/2016.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HelloWorldEspressoTest {
    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.adjust.example", appContext.getPackageName());
    }

    @Test
    public void foo() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AdjustAnalyzer.reportState("foo() 1");

        onView(withId(R.id.btnEnableDisableOfflineMode))
                .perform(click());

        AdjustAnalyzer.reportState("foo() 2");

        onView(withId(R.id.btnEnableDisableSDK))
                .perform(click());

        AdjustAnalyzer.reportState("foo() 3");

        onView(withId(R.id.btnEnableDisableSDK))
                .perform(click());

        AdjustAnalyzer.reportState("foo() 4");

        Adjust.addSessionCallbackParameter("sc_foo", "sc_bar");
        Adjust.addSessionCallbackParameter("sc_key", "sc_value");

        // Add session partner parameters.
        Adjust.addSessionPartnerParameter("sp_foo", "sp_bar");
        Adjust.addSessionPartnerParameter("sp_key", "sp_value");

        // Remove session callback parameters.
//        Adjust.removeSessionCallbackParameter("sc_foo");

        // Remove session partner parameters.
//        Adjust.removeSessionPartnerParameter("sp_key");

        AdjustAnalyzer.reportState("foo() 6");

        // Remove all session callback parameters.
//        Adjust.resetSessionCallbackParameters();

        // Remove all session partner parameters.
//        Adjust.resetSessionPartnerParameters();

        AdjustAnalyzer.reportState("foo() 7");
    }
}
