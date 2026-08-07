package com.adjust.sdk;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ObjectInputFilterStreamPersistedTest {
    @Test
    public void roundTrip_activityState() throws Exception {
        ActivityState original = new ActivityState();
        original.uuid = "uuid-1";
        original.enabled = false;
        original.isGdprForgotten = true;
        original.askingAttribution = true;
        original.eventCount = 12;
        original.sessionCount = 34;
        original.subsessionCount = 56;
        original.sessionLength = 1001L;
        original.timeSpent = 1002L;
        original.lastActivity = 1003L;
        original.lastInterval = 1004L;
        original.orderIds = new LinkedList<>();
        original.orderIds.add("order-1");
        original.orderIds.add("order-2");
        original.pushToken = "push-token";
        original.adid = "adid-1";
        original.clickTime = 2001L;
        original.installBegin = 2002L;
        original.installReferrer = "install-ref";
        original.googlePlayInstant = Boolean.TRUE;
        original.clickTimeServer = 2003L;
        original.installBeginServer = 2004L;
        original.installVersion = "1.2.3";
        original.clickTimeHuawei = 3001L;
        original.installBeginHuawei = 3002L;
        original.installReferrerHuawei = "hua-ref";
        original.installReferrerHuaweiAppGallery = "hua-app-gallery-ref";
        original.isThirdPartySharingDisabledForCoppa = true;
        original.clickTimeXiaomi = 4001L;
        original.installBeginXiaomi = 4002L;
        original.installReferrerXiaomi = "xiaomi-ref";
        original.clickTimeServerXiaomi = 4003L;
        original.installBeginServerXiaomi = 4004L;
        original.installVersionXiaomi = "x-1.0";
        original.clickTimeSamsung = 5001L;
        original.installBeginSamsung = 5002L;
        original.installReferrerSamsung = "samsung-ref";
        original.clickTimeVivo = 6001L;
        original.installBeginVivo = 6002L;
        original.installReferrerVivo = "vivo-ref";
        original.installVersionVivo = "v-1.0";
        original.installReferrerMeta = "meta-ref";
        original.clickTimeMeta = 7001L;
        original.isClickMeta = Boolean.FALSE;

        ActivityState restored = (ActivityState) deserialize(serialize(original));

        assertEquals(original.uuid, restored.uuid);
        assertEquals(original.enabled, restored.enabled);
        assertEquals(original.isGdprForgotten, restored.isGdprForgotten);
        assertEquals(original.askingAttribution, restored.askingAttribution);
        assertEquals(original.eventCount, restored.eventCount);
        assertEquals(original.sessionCount, restored.sessionCount);
        assertEquals(original.subsessionCount, restored.subsessionCount);
        assertEquals(original.sessionLength, restored.sessionLength);
        assertEquals(original.timeSpent, restored.timeSpent);
        assertEquals(original.lastActivity, restored.lastActivity);
        assertEquals(original.lastInterval, restored.lastInterval);
        assertEquals(original.orderIds, restored.orderIds);
        assertEquals(original.pushToken, restored.pushToken);
        assertEquals(original.adid, restored.adid);
        assertEquals(original.clickTime, restored.clickTime);
        assertEquals(original.installBegin, restored.installBegin);
        assertEquals(original.installReferrer, restored.installReferrer);
        assertEquals(original.googlePlayInstant, restored.googlePlayInstant);
        assertEquals(original.clickTimeServer, restored.clickTimeServer);
        assertEquals(original.installBeginServer, restored.installBeginServer);
        assertEquals(original.installVersion, restored.installVersion);
        assertEquals(original.clickTimeHuawei, restored.clickTimeHuawei);
        assertEquals(original.installBeginHuawei, restored.installBeginHuawei);
        assertEquals(original.installReferrerHuawei, restored.installReferrerHuawei);
        assertEquals(original.installReferrerHuaweiAppGallery, restored.installReferrerHuaweiAppGallery);
        assertEquals(original.isThirdPartySharingDisabledForCoppa, restored.isThirdPartySharingDisabledForCoppa);
        assertEquals(original.clickTimeXiaomi, restored.clickTimeXiaomi);
        assertEquals(original.installBeginXiaomi, restored.installBeginXiaomi);
        assertEquals(original.installReferrerXiaomi, restored.installReferrerXiaomi);
        assertEquals(original.clickTimeServerXiaomi, restored.clickTimeServerXiaomi);
        assertEquals(original.installBeginServerXiaomi, restored.installBeginServerXiaomi);
        assertEquals(original.installVersionXiaomi, restored.installVersionXiaomi);
        assertEquals(original.clickTimeSamsung, restored.clickTimeSamsung);
        assertEquals(original.installBeginSamsung, restored.installBeginSamsung);
        assertEquals(original.installReferrerSamsung, restored.installReferrerSamsung);
        assertEquals(original.clickTimeVivo, restored.clickTimeVivo);
        assertEquals(original.installBeginVivo, restored.installBeginVivo);
        assertEquals(original.installReferrerVivo, restored.installReferrerVivo);
        assertEquals(original.installVersionVivo, restored.installVersionVivo);
        assertEquals(original.installReferrerMeta, restored.installReferrerMeta);
        assertEquals(original.clickTimeMeta, restored.clickTimeMeta);
        assertEquals(original.isClickMeta, restored.isClickMeta);
    }

    @Test
    public void roundTrip_adjustAttribution() throws Exception {
        AdjustAttribution original = new AdjustAttribution();
        original.trackerToken = "token";
        original.trackerName = "name";
        original.network = "network";
        original.campaign = "campaign";
        original.adgroup = "adgroup";
        original.creative = "creative";
        original.clickLabel = "label";
        original.costType = "cpi";
        original.costAmount = 123.45d;
        original.costCurrency = "USD";
        original.fbInstallReferrer = "fb-ref";
        original.jsonResponse = "{\"a\":1}";

        AdjustAttribution restored = (AdjustAttribution) deserialize(serialize(original));

        assertEquals(original.trackerToken, restored.trackerToken);
        assertEquals(original.trackerName, restored.trackerName);
        assertEquals(original.network, restored.network);
        assertEquals(original.campaign, restored.campaign);
        assertEquals(original.adgroup, restored.adgroup);
        assertEquals(original.creative, restored.creative);
        assertEquals(original.clickLabel, restored.clickLabel);
        assertEquals(original.costType, restored.costType);
        assertEquals(original.costAmount, restored.costAmount);
        assertEquals(original.costCurrency, restored.costCurrency);
        assertEquals(original.fbInstallReferrer, restored.fbInstallReferrer);
        assertEquals(original.jsonResponse, restored.jsonResponse);
    }

    @Test
    public void roundTrip_eventMetadata() throws Exception {
        EventMetadata original = new EventMetadata();
        original.incrementSequenceForEvent("evt-a");
        original.incrementSequenceForEvent("evt-b");
        original.incrementSequenceForEvent("evt-b");

        EventMetadata restored = (EventMetadata) deserialize(serialize(original));

        assertEquals(original, restored);
    }

    @Test
    public void roundTrip_activityPackage() throws Exception {
        ActivityPackage original = new ActivityPackage(ActivityKind.EVENT);
        original.setPath("/event");
        original.setClientSdk("android5.5.1");
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("k1", "v1");
        parameters.put("k2", "v2");
        original.setParameters(parameters);
        original.setSuffix("_suffix");
        Map<String, String> callbackParameters = new LinkedHashMap<>();
        callbackParameters.put("cb1", "cv1");
        original.setCallbackParameters(callbackParameters);
        Map<String, String> partnerParameters = new LinkedHashMap<>();
        partnerParameters.put("pp1", "pv1");
        original.setPartnerParameters(partnerParameters);
        original.addError(400);
        original.addError(401);
        original.setWaitBeforeSendTimeSeconds(3.5d);

        ActivityPackage restored = (ActivityPackage) deserialize(serialize(original));

        assertEquals(original.getPath(), restored.getPath());
        assertEquals(original.getClientSdk(), restored.getClientSdk());
        assertEquals(original.getParameters(), restored.getParameters());
        assertEquals(original.getActivityKind(), restored.getActivityKind());
        assertEquals(original.getSuffix(), restored.getSuffix());
        assertEquals(original.getCallbackParameters(), restored.getCallbackParameters());
        assertEquals(original.getPartnerParameters(), restored.getPartnerParameters());
        // current ActivityPackage.readObject reads "retryCount" and drops legacy "errorCount"
        assertEquals(original.getRetryCount(), restored.getRetryCount());
        assertEquals(original.getFirstErrorCode(), restored.getFirstErrorCode());
        assertEquals(original.getLastErrorCode(), restored.getLastErrorCode());
        assertEquals(original.getWaitBeforeSendTimeSeconds(), restored.getWaitBeforeSendTimeSeconds(), 0.0d);
    }

    @Test
    public void roundTrip_packageQueue() throws Exception {
        ActivityPackage first = new ActivityPackage(ActivityKind.SESSION);
        first.setPath("/session");
        first.setClientSdk("android5.5.1");
        Map<String, String> firstParams = new LinkedHashMap<>();
        firstParams.put("k1", "v1");
        firstParams.put("k2", "v2");
        first.setParameters(firstParams);
        first.setSuffix("_first");
        Map<String, String> firstCallbackParams = new LinkedHashMap<>();
        firstCallbackParams.put("fcb1", "fcv1");
        first.setCallbackParameters(firstCallbackParams);
        Map<String, String> firstPartnerParams = new LinkedHashMap<>();
        firstPartnerParams.put("fpp1", "fpv1");
        first.setPartnerParameters(firstPartnerParams);
        first.addError(400);
        first.addError(401);
        first.setWaitBeforeSendTimeSeconds(1.5d);

        ActivityPackage second = new ActivityPackage(ActivityKind.EVENT);
        second.setPath("/event");
        second.setClientSdk("android5.5.1");
        Map<String, String> secondParams = new LinkedHashMap<>();
        secondParams.put("k2", "v2");
        secondParams.put("k3", "v3");
        second.setParameters(secondParams);
        second.setSuffix("_second");
        Map<String, String> secondCallbackParams = new LinkedHashMap<>();
        secondCallbackParams.put("scb1", "scv1");
        second.setCallbackParameters(secondCallbackParams);
        Map<String, String> secondPartnerParams = new LinkedHashMap<>();
        secondPartnerParams.put("spp1", "spv1");
        second.setPartnerParameters(secondPartnerParams);
        second.addError(402);
        second.addError(403);
        second.setWaitBeforeSendTimeSeconds(2.5d);

        List<ActivityPackage> original = new ArrayList<>();
        original.add(first);
        original.add(second);

        @SuppressWarnings("unchecked")
        List<ActivityPackage> restored = (List<ActivityPackage>) deserialize(serialize(original));

        assertEquals(2, restored.size());
        assertEquals(first.getPath(), restored.get(0).getPath());
        assertEquals(first.getClientSdk(), restored.get(0).getClientSdk());
        assertEquals(first.getParameters(), restored.get(0).getParameters());
        assertEquals(first.getActivityKind(), restored.get(0).getActivityKind());
        assertEquals(first.getSuffix(), restored.get(0).getSuffix());
        assertEquals(first.getCallbackParameters(), restored.get(0).getCallbackParameters());
        assertEquals(first.getPartnerParameters(), restored.get(0).getPartnerParameters());
        assertEquals(first.getRetryCount(), restored.get(0).getRetryCount());
        assertEquals(first.getFirstErrorCode(), restored.get(0).getFirstErrorCode());
        assertEquals(first.getLastErrorCode(), restored.get(0).getLastErrorCode());
        assertEquals(first.getWaitBeforeSendTimeSeconds(), restored.get(0).getWaitBeforeSendTimeSeconds(), 0.0d);

        assertEquals(second.getPath(), restored.get(1).getPath());
        assertEquals(second.getClientSdk(), restored.get(1).getClientSdk());
        assertEquals(second.getParameters(), restored.get(1).getParameters());
        assertEquals(second.getActivityKind(), restored.get(1).getActivityKind());
        assertEquals(second.getSuffix(), restored.get(1).getSuffix());
        assertEquals(second.getCallbackParameters(), restored.get(1).getCallbackParameters());
        assertEquals(second.getPartnerParameters(), restored.get(1).getPartnerParameters());
        assertEquals(second.getRetryCount(), restored.get(1).getRetryCount());
        assertEquals(second.getFirstErrorCode(), restored.get(1).getFirstErrorCode());
        assertEquals(second.getLastErrorCode(), restored.get(1).getLastErrorCode());
        assertEquals(second.getWaitBeforeSendTimeSeconds(), restored.get(1).getWaitBeforeSendTimeSeconds(), 0.0d);
    }

    @Test
    public void roundTrip_globalParametersMap() throws Exception {
        Map<String, String> original = new LinkedHashMap<>();
        original.put("k1", "v1");
        original.put("k2", "v2");

        @SuppressWarnings("unchecked")
        Map<String, String> restored = (Map<String, String>) deserialize(serialize(original));

        assertEquals(original, restored);
    }

    @Test
    public void blocks_activityState_withBoobytrappedOrderIds() throws Exception {
        ActivityState boobytrapped = new ActivityState();
        LinkedList<Object> orderIds = new LinkedList<>();
        orderIds.add("safe");
        orderIds.add(new UnfilteredClass());
        @SuppressWarnings("unchecked")
        LinkedList<String> raw = (LinkedList<String>) (LinkedList<?>) orderIds;
        boobytrapped.orderIds = raw;

        assertThrows(InvalidClassException.class, () -> deserialize(serialize(boobytrapped)));
    }

    @Test
    public void blocks_eventMetadata_withBoobytrappedEventSequence() throws Exception {
        EventMetadata boobytrapped = new EventMetadata();
        Map<String, Object> eventSequence = new HashMap<>();
        eventSequence.put("safe", 1);
        eventSequence.put("bad", new UnfilteredClass());
        Field field = EventMetadata.class.getDeclaredField("eventSequence");
        field.setAccessible(true);
        field.set(boobytrapped, eventSequence);

        assertThrows(InvalidClassException.class, () -> deserialize(serialize(boobytrapped)));
    }

    @Test
    public void blocks_activityPackage_withBoobytrappedParameters() throws Exception {
        ActivityPackage boobytrapped = new ActivityPackage(ActivityKind.EVENT);
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("safe", "value");
        parameters.put("bad", new UnfilteredClass());
        @SuppressWarnings("unchecked")
        Map<String, String> raw = (Map<String, String>) (Map<?, ?>) parameters;
        boobytrapped.setParameters(raw);

        assertThrows(InvalidClassException.class, () -> deserialize(serialize(boobytrapped)));
    }

    @Test
    public void blocks_packageQueue_withBoobytrappedElement() throws Exception {
        ActivityPackage safePackage = new ActivityPackage(ActivityKind.SESSION);
        safePackage.setPath("/safe");
        Map<String, String> safeParameters = new LinkedHashMap<>();
        safeParameters.put("safe_k", "safe_v");
        safePackage.setParameters(safeParameters);

        ActivityPackage boobytrappedPackage = new ActivityPackage(ActivityKind.EVENT);
        boobytrappedPackage.setPath("/boobytrapped");
        Map<String, Object> boobytrappedParameters = new LinkedHashMap<>();
        boobytrappedParameters.put("safe_k", "safe_v");
        boobytrappedParameters.put("bad_k", new UnfilteredClass());
        @SuppressWarnings("unchecked")
        Map<String, String> rawBoobytrappedParameters =
                (Map<String, String>) (Map<?, ?>) boobytrappedParameters;
        boobytrappedPackage.setParameters(rawBoobytrappedParameters);

        List<ActivityPackage> boobytrappedQueue = new ArrayList<>();
        boobytrappedQueue.add(safePackage);
        boobytrappedQueue.add(boobytrappedPackage);

        assertThrows(InvalidClassException.class, () -> deserialize(serialize(boobytrappedQueue)));
    }

    @Test
    public void blocks_globalParametersMap_withBoobytrappedValue() throws Exception {
        Map<String, Object> boobytrapped = new LinkedHashMap<>();
        boobytrapped.put("safe", "value");
        boobytrapped.put("bad", new UnfilteredClass());

        assertThrows(InvalidClassException.class, () -> deserialize(serialize(boobytrapped)));
    }

    private static Object deserialize(byte[] data) throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        try (ObjectInputStream objectInputStream = new ObjectInputFilterStream(input)) {
            return objectInputStream.readObject();
        }
    }

    private static byte[] serialize(Object obj) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(output)) {
            objectOutputStream.writeObject(obj);
        }
        return output.toByteArray();
    }
}
