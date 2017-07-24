package com.adjust.sdk;

import com.adjust.sdk.ActivityKind;
import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import junit.framework.Assert;

import java.util.Map;

/**
 * Created by pfms on 09/01/15.
 */
public class TestActivityPackage {

    private ActivityPackage activityPackage;
    private Map<String, String> parameters;
    public String appToken;
    public String environment;
    public String clientSdk;
    public Boolean deviceKnow;
    public boolean playServices;
    public boolean eventBufferingEnabled;
    public String pushToken;
    // session
    public Integer sessionCount;
    public String defaultTracker;
    public Integer subsessionCount;
    // event
    public String eventToken;
    public String eventCount;
    public String suffix;
    public String revenueString;
    public String currency;
    public String callbackParams;
    public String partnerParams;
    public Map<String, String> savedCallbackParameters;
    public Map<String, String> savedPartnerParameters;
    // click
    public String reftag;
    public String otherParameters;
    public AdjustAttribution attribution;
    public String referrer;
    public String deeplink;

    public TestActivityPackage(ActivityPackage activityPackage) {
        this.activityPackage = activityPackage;
        parameters = activityPackage.getParameters();

        // default values
        appToken = "123456789012";
        environment = "sandbox";
        clientSdk = "android4.12.0";
        suffix = "";
        attribution = new AdjustAttribution();
        playServices = true;
    }

    public void testSessionPackage(int sessionCount) {
        // set the session count
        this.sessionCount = sessionCount;

        // test default package attributes
        testDefaultAttributes("/session", ActivityKind.SESSION, "session");

        // check default parameters
        testDefaultParameters();

        // session parameters
        // last_interval
        if (sessionCount == 1) {
            assertParameterNull("last_interval");
        } else {
            assertParameterNotNull("last_interval");
        }
        // default_tracker
        assertParameterEquals("default_tracker", defaultTracker);
        // callback_params
        assertJsonParameterEquals("callback_params", callbackParams);
        // partner_params
        assertJsonParameterEquals("partner_params", partnerParams);
    }

    public void testEventPackage(String eventToken) {
        // set the event token
        this.eventToken = eventToken;

        // test default package attributes
        testDefaultAttributes("/event", ActivityKind.EVENT, "event");

        // check default parameters
        testDefaultParameters();

        // event parameters
        // event_count
        if (eventCount == null) {
            assertParameterNotNull("event_count");
        } else {
            assertParameterEquals("event_count", eventCount);
        }
        // event_token
        assertParameterEquals("event_token", eventToken);
        // revenue and currency must come together
        if (parameters.get("revenue") != null &&
                parameters.get("currency") == null) {
            assertFail();
        }
        if (parameters.get("revenue") == null &&
                parameters.get("currency") != null) {
            assertFail();
        }
        // revenue
        assertParameterEquals("revenue", revenueString);
        // currency
        assertParameterEquals("currency", currency);
        // callback_params
        assertJsonParameterEquals("callback_params", callbackParams);
        // partner_params
        assertJsonParameterEquals("partner_params", partnerParams);

        // saved callback parameters
        if (savedCallbackParameters == null) {
            Assert.assertNull(activityPackage.getExtendedString(),
                    activityPackage.getCallbackParameters());
        } else {
            Assert.assertTrue(activityPackage.getExtendedString(),
                    savedCallbackParameters.equals(activityPackage.getCallbackParameters()));
        }
        // saved partner parameters
        if (savedPartnerParameters == null) {
            Assert.assertNull(activityPackage.getExtendedString(),
                    activityPackage.getPartnerParameters());
        } else {
            Assert.assertTrue(activityPackage.getExtendedString(),
                    savedPartnerParameters.equals(activityPackage.getPartnerParameters()));
        }
    }

    public void testClickPackage(String source) {
        testClickPackage(source, true);
    }

    public void testClickPackage(String source, boolean hasActivityState) {
        // test default package attributes
        testDefaultAttributes("/sdk_click", ActivityKind.CLICK, "click");

        // check device ids parameters
        testDefaultParameters(hasActivityState);

        // click parameters
        // source
        assertParameterEquals("source", source);

        // referrer
        assertParameterEquals(Constants.REFTAG, reftag);

        if (source == Constants.REFTAG) {
            assertParameterEquals("referrer", referrer);
            assertParameterNull("deeplink");
        } else if (source == Constants.DEEPLINK) {
            assertParameterEquals("deeplink", deeplink);
            assertParameterNull("referrer");
        } else {
            assertFail();
        }

        // params
        assertJsonParameterEquals("params", otherParameters);

        // click_time
        // TODO add string click time to compare
        assertParameterNotNull("click_time");

        // attributions
        if (attribution != null) {
            // tracker
            assertParameterEquals("tracker", attribution.trackerName);
            // campaign
            assertParameterEquals("campaign", attribution.campaign);
            // adgroup
            assertParameterEquals("adgroup", attribution.adgroup);
            // creative
            assertParameterEquals("creative", attribution.creative);
        }
    }

    public void testInfoPackage(String source) {
        // test default package attributes
        testDefaultAttributes("/sdk_info", ActivityKind.INFO, "info");

        // check device ids parameters
        testDeviceIdsParameters();

        // click parameters
        // source
        assertParameterEquals("source", source);

        if (source == Constants.PUSH) {
        } else {
            assertFail();
        }
    }

    public void testAttributionPackage() {
        // test default package attributes
        testDefaultAttributes("attribution", ActivityKind.ATTRIBUTION, "attribution");

        testDeviceIdsParameters();
    }

    public static void testQueryStringRequest(String queryStringRequest, Integer queueSize) {
        String[] queryPairs = queryStringRequest.split("&");
        boolean wasSentAtFound = false;
        boolean wasQueueSizeFound = false;
        int queueSizeFound = -1;

        for (String pair : queryPairs) {
            String[] pairComponents = pair.split("=");
            String key = pairComponents[0];
            String value = pairComponents[1];
            if (key.equals("sent_at")) {
                wasSentAtFound = true;
            }
            if (key.equals("queue_size")) {
                wasQueueSizeFound = true;
                queueSizeFound = Integer.parseInt(value);
            }
        }
        if (!wasSentAtFound) {
            Assert.fail(queryStringRequest);
        }

        if (queueSize == null) {
            Assert.assertFalse(queryStringRequest, wasQueueSizeFound);
            return;
        }

        if (!wasQueueSizeFound) {
            Assert.fail(queryStringRequest);
        } else {
            Assert.assertEquals(queryStringRequest,
                    queueSize.intValue(), queueSizeFound);
        }
    }

    private void testDefaultAttributes(String path, ActivityKind activityKind, String activityKindString) {
        // check the Sdk version is being tested
        assertEquals(activityPackage.getClientSdk(), clientSdk);
        // check the path
        assertEquals(activityPackage.getPath(), path);
        // test activity kind
        // check the activity kind
        assertEquals(activityPackage.getActivityKind(), activityKind);
        // the conversion from activity kind to String
        assertEquals(activityPackage.getActivityKind().toString(), activityKindString);
        // the conversion from String to activity kind
        assertEquals(activityPackage.getActivityKind(), ActivityKind.fromString(activityKindString));
        // test suffix
        assertEquals(activityPackage.getSuffix(), suffix);
    }

    private void testDefaultParameters() {
        testDefaultParameters(true);
    }

    private void testDefaultParameters(boolean hasActivityState) {
        testDeviceInfo();
        testConfig();
        if (hasActivityState) {
            testActivityState();
        } else {
            testWithoutActivityState();
        }
        testCreatedAt();
    }

    private void testDeviceIdsParameters() {
        testDeviceInfoIds();
        testConfig();
        testCreatedAt();
    }

    private void testCreatedAt() {
        // created_at
        assertParameterNotNull("created_at");
    }

    private void testDeviceInfo() {
        testDeviceInfoIds();
        // fb_id
        //assertParameterNotNull("fb_id");
        // package_name
        assertParameterNotNull("package_name");
        // app_version
        // device_type
        assertParameterNotNull("device_type");
        // device_name
        assertParameterNotNull("device_name");
        // device_manufacturer
        assertParameterNotNull("device_manufacturer");
        // os_name
        assertParameterEquals("os_name", "android");
        // os_version
        assertParameterNotNull("os_version");
        // language
        assertParameterNotNull("language");
        // country
        assertParameterNotNull("country");
        // screen_size
        assertParameterNotNull("screen_size");
        // screen_format
        assertParameterNotNull("screen_format");
        // screen_density
        assertParameterNotNull("screen_density");
        // display_width
        assertParameterNotNull("display_width");
        // display_height
        assertParameterNotNull("display_height");
        // hardware_name
        assertParameterNotNull("hardware_name");
        // cpu_type
        assertParameterNotNull("cpu_type");
        // os_build
        assertParameterNotNull("os_build");
        // vm_isa
        assertParameterNotNull("vm_isa");
    }

    private void testDeviceInfoIds() {
        // play services
        if (playServices) {
            // mac_sha1
            assertParameterNull("mac_sha1");
            // mac_md5
            assertParameterNull("mac_md5");
            // android_id
            assertParameterNull("android_id");
        } else {
            // mac_sha1
            assertParameterNotNull("mac_sha1");
            // mac_md5
            assertParameterNotNull("mac_md5");
            // android_id
            assertParameterNotNull("android_id");
        }
    }

    private void testConfig() {
        // app_token
        assertParameterEquals("app_token", appToken);
        // environment
        assertParameterEquals("environment", environment);
        // device_known
        testParameterBoolean("device_known", deviceKnow);
        // needs_attribution_data
        testParameterBoolean("needs_response_details", true);
        // play services
        if (playServices) {
            // gps_adid
            assertParameterNotNull("gps_adid");
            // tracking_enabled
            assertParameterNotNull("tracking_enabled");
        } else {
            // gps_adid
            assertParameterNull("gps_adid");
            // tracking_enabled
            assertParameterNull("tracking_enabled");
        }
        // event_buffering_enabled
        testParameterBoolean("event_buffering_enabled", eventBufferingEnabled);
        // push_token
        assertParameterEquals("push_token", pushToken);
    }

    private void testWithoutActivityState() {
        // android_uuid
        assertParameterNull("android_uuid");
        // session_count
        assertParameterNull("session_count");
        // subsession_count
        assertParameterNull("subsession_count");
        // session_length
        assertParameterNull("session_length");
        // time_spent
        assertParameterNull("time_spent");
    }

    private void testActivityState() {
        testActivityStateIds();
        // session_count
        if (sessionCount == null) {
            assertParameterNotNull("session_count");
        } else {
            assertParameterEquals("session_count", sessionCount);
        }
        // first session
        if (sessionCount != null && sessionCount == 1) {
            // subsession_count
            assertParameterNull("subsession_count");
            // session_length
            assertParameterNull("session_length");
            // time_spent
            assertParameterNull("time_spent");
        } else {
            // subsession_count
            if (subsessionCount == null)
                assertParameterNotNull("subsession_count");
            else
                assertParameterEquals("subsession_count", subsessionCount);
            // session_length
            assertParameterNotNull("session_length");
            // time_spent
            assertParameterNotNull("time_spent");
        }
    }

    private void testActivityStateIds() {
        // android_uuid
        assertParameterNotNull("android_uuid");
    }

    private void assertParameterNotNull(String parameterName) {
        Assert.assertNotNull(activityPackage.getExtendedString(),
                parameters.get(parameterName));
    }

    private void assertParameterNull(String parameterName) {
        Assert.assertNull(activityPackage.getExtendedString(),
                parameters.get(parameterName));
    }

    private void assertParameterEquals(String parameterName, String value) {
        if (value == null) {
            assertParameterNull(parameterName);
            return;
        }

        Assert.assertEquals(activityPackage.getExtendedString(),
                value, parameters.get(parameterName));
    }

    private void assertJsonParameterEquals(String parameterName, String value) {
        if (value == null) {
            assertParameterNull(parameterName);
            return;
        }

        JsonParser parser = new JsonParser();
        JsonElement e1 = parser.parse(value);
        JsonElement e2 = parser.parse(parameters.get(parameterName));

        assertEquals(e1, e2);
    }

    private void assertParameterEquals(String parameterName, int value) {
        Assert.assertEquals(activityPackage.getExtendedString(),
                value, Integer.parseInt(parameters.get(parameterName)));
    }


    private void assertEquals(String field, String value) {
        Assert.assertEquals(activityPackage.getExtendedString(),
                value, field);
    }

    private void assertEquals(Object field, Object value) {
        Assert.assertEquals(activityPackage.getExtendedString(),
                value, field);
    }

    private void assertFail() {
        Assert.fail(activityPackage.getExtendedString());
    }

    private void testParameterBoolean(String parameterName, Boolean value) {
        if (value == null) {
            assertParameterNull(parameterName);
        } else if (value) {
            assertParameterEquals(parameterName, "1");
        } else {
            assertParameterEquals(parameterName, "0");
        }
    }
}
