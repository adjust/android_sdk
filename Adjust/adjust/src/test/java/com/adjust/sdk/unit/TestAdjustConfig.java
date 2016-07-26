package com.adjust.sdk.unit;

import android.content.*;

import com.adjust.sdk.*;

import org.junit.*;
import org.mockito.*;

import org.mockito.Mock;

import base.*;

/**
 * Created by abdullah on 7/24/16.
 */

public class TestAdjustConfig extends UnitTestBase {
    @Mock
    private Context context;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void returnConfig_sandbox() {
        String environment = "sandbox";
        String appToken = "123456789012";
        AdjustConfig adjustConfig = new AdjustConfig(context, appToken, environment);

        Assert.assertNotNull(adjustConfig);
        // TODO: 7/24/16 assert environment is sandbox
    }

    @Test
    public void returnConfig_production() {
        String environment = "production";
        String appToken = "123456789012";
        AdjustConfig adjustConfig = new AdjustConfig(context, appToken, environment);

        Assert.assertNotNull(adjustConfig);
        // TODO: 7/24/16 assert environment is production
    }

    @Test(expected = IllegalArgumentException.class)
    public void returnConfig_noContext() {
        String environment = "production";
        String appToken = "123456789012";
        AdjustConfig adjustConfig = new AdjustConfig(null, appToken, environment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void returnConfig_noAppToken() {
        String environment = "production";
        String appToken = null;
        AdjustConfig adjustConfig = new AdjustConfig(null, appToken, environment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void returnConfig_noEnvironment() {
        String environment = null;
        String appToken = "123456789012";
        AdjustConfig adjustConfig = new AdjustConfig(null, appToken, environment);
    }
}
