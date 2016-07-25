package com.adjust.sdk;

import android.support.test.rule.*;

import org.junit.*;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

public class TestUnitTestActivity {

    @Rule
    public ActivityTestRule<UnitTestActivity> activity = new ActivityTestRule<>(UnitTestActivity.class);

    @Test
    public void shouldDisplayMainScreenWithCorrectTitle() {
        onView(withText("AndroidGradleTemplate")).check(matches(isDisplayed()));
    }
}
