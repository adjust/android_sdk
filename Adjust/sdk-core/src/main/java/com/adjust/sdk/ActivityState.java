//
//  ActivityState.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Calendar;
import java.util.LinkedList;

public class ActivityState implements Serializable, Cloneable {
    private static final long serialVersionUID = 9039439291143138148L;
    private static final int EVENT_DEDUPLICATION_IDS_MAX_SIZE = 10;
    private transient ILogger logger;
    @SuppressWarnings("unchecked")
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("uuid", String.class),
            new ObjectStreamField("enabled", boolean.class),
            new ObjectStreamField("isGdprForgotten", boolean.class),
            new ObjectStreamField("askingAttribution", boolean.class),
            new ObjectStreamField("eventCount", int.class),
            new ObjectStreamField("sessionCount", int.class),
            new ObjectStreamField("subsessionCount", int.class),
            new ObjectStreamField("sessionLength", long.class),
            new ObjectStreamField("timeSpent", long.class),
            new ObjectStreamField("lastActivity", long.class),
            new ObjectStreamField("lastInterval", long.class),
            new ObjectStreamField("orderIds", (Class<LinkedList<String>>)(Class) LinkedList.class),
            new ObjectStreamField("pushToken", String.class),
            new ObjectStreamField("adid", String.class),
            new ObjectStreamField("clickTime", long.class),
            new ObjectStreamField("installBegin", long.class),
            new ObjectStreamField("installReferrer", String.class),
            new ObjectStreamField("googlePlayInstant", Boolean.class),
            new ObjectStreamField("clickTimeServer", long.class),
            new ObjectStreamField("installBeginServer", long.class),
            new ObjectStreamField("installVersion", String.class),
            new ObjectStreamField("clickTimeHuawei", long.class),
            new ObjectStreamField("installBeginHuawei", long.class),
            new ObjectStreamField("installReferrerHuawei", String.class),
            new ObjectStreamField("installReferrerHuaweiAppGallery", String.class),
            new ObjectStreamField("isThirdPartySharingDisabledForCoppa", boolean.class),
            new ObjectStreamField("clickTimeXiaomi", long.class),
            new ObjectStreamField("installBeginXiaomi", long.class),
            new ObjectStreamField("installReferrerXiaomi", String.class),
            new ObjectStreamField("clickTimeServerXiaomi", long.class),
            new ObjectStreamField("installBeginServerXiaomi", long.class),
            new ObjectStreamField("installVersionXiaomi", String.class),
            new ObjectStreamField("clickTimeSamsung", long.class),
            new ObjectStreamField("installBeginSamsung", long.class),
            new ObjectStreamField("installReferrerSamsung", String.class),
            new ObjectStreamField("clickTimeVivo", long.class),
            new ObjectStreamField("installBeginVivo", long.class),
            new ObjectStreamField("installReferrerVivo", String.class),
            new ObjectStreamField("installVersionVivo", String.class),
            new ObjectStreamField("installReferrerMeta", String.class),
            new ObjectStreamField("clickTimeMeta", long.class),
            new ObjectStreamField("isClickMeta", Boolean.class),
    };

    // persistent data
    protected String uuid;
    protected boolean enabled;
    protected boolean isGdprForgotten;
    protected boolean isThirdPartySharingDisabledForCoppa;
    protected boolean askingAttribution;

    // global counters
    protected int eventCount;
    protected int sessionCount;

    // session attributes
    protected int subsessionCount;
    protected long sessionLength;   // all durations in milliseconds
    protected long timeSpent;
    protected long lastActivity;    // all times in milliseconds since 1970

    protected long lastInterval;

    protected LinkedList<String> orderIds;
    protected int eventDeduplicationIdsMaxSize;

    protected String pushToken;
    protected String adid;

    protected long clickTime;
    protected long installBegin;
    protected String installReferrer;
    protected Boolean googlePlayInstant;
    protected long clickTimeServer;
    protected long installBeginServer;
    protected String installVersion;

    protected long clickTimeHuawei;
    protected long installBeginHuawei;
    protected String installReferrerHuawei;
    protected String installReferrerHuaweiAppGallery;

    protected long clickTimeXiaomi;
    protected long installBeginXiaomi;
    protected String installReferrerXiaomi;
    protected long clickTimeServerXiaomi;
    protected long installBeginServerXiaomi;
    protected String installVersionXiaomi;

    protected long clickTimeSamsung;
    protected long installBeginSamsung;
    protected String installReferrerSamsung;

    protected long clickTimeVivo;
    protected long installBeginVivo;
    protected String installReferrerVivo;
    protected String installVersionVivo;

    protected String installReferrerMeta;
    protected long clickTimeMeta;
    protected Boolean isClickMeta;

    protected ActivityState() {
        logger = AdjustFactory.getLogger();
        // create UUID for new devices
        uuid = Util.createUuid();
        enabled = true;
        isGdprForgotten = false;
        isThirdPartySharingDisabledForCoppa = false;
        askingAttribution = false;
        eventCount = 0; // no events yet
        sessionCount = 0; // the first session just started
        subsessionCount = -1; // we don't know how many subsessions this first  session will have
        sessionLength = -1; // same for session length and time spent
        timeSpent = -1; // this information will be collected and attached to the next session
        lastActivity = -1;
        lastInterval = -1;
        orderIds = null;
        pushToken = null;
        adid = null;
        clickTime = 0;
        installBegin = 0;
        installReferrer = null;
        googlePlayInstant = null;
        clickTimeServer = 0;
        installBeginServer = 0;
        installVersion = null;
        clickTimeHuawei = 0;
        installBeginHuawei = 0;
        installReferrerHuawei = null;
        installReferrerHuaweiAppGallery = null;
        clickTimeXiaomi = 0;
        installBeginXiaomi = 0;
        installReferrerXiaomi = null;
        clickTimeServerXiaomi = 0;
        installBeginServerXiaomi = 0;
        installVersionXiaomi = null;
        clickTimeSamsung = 0;
        installBeginSamsung = 0;
        installReferrerSamsung = null;
        clickTimeVivo = 0;
        installBeginVivo = 0;
        installReferrerVivo = null;
        installVersionVivo = null;
        installReferrerMeta = null;
        clickTimeMeta = 0;
        isClickMeta = null;
        eventDeduplicationIdsMaxSize = EVENT_DEDUPLICATION_IDS_MAX_SIZE;
    }

    protected void resetSessionAttributes(long now) {
        subsessionCount = 1; // first subsession
        sessionLength = 0; // no session length yet
        timeSpent = 0; // no time spent yet
        lastActivity = now;
        lastInterval = -1;
    }

    protected void addDeduplicationId(String deduplicationId) {
        if (eventDeduplicationIdsMaxSize == 0) {
            return;
        }

        if (orderIds == null) {
            orderIds = new LinkedList<String>();
        } else {
            while (orderIds.size() >= eventDeduplicationIdsMaxSize) {
                orderIds.removeLast();
            }
        }
        orderIds.addFirst(deduplicationId);
    }

    protected boolean eventDeduplicationIdExists(String deduplicationId) {
        if (orderIds == null) {
            return false;
        }
        return orderIds.contains(deduplicationId);
    }

    protected void setEventDeduplicationIdsMaxSize(Integer size) {
        if (size != null && size >= 0) {
            eventDeduplicationIdsMaxSize = size;
        }
    }

    @Override
    public String toString() {
        return Util.formatString("ec:%d sc:%d ssc:%d sl:%.1f ts:%.1f la:%s uuid:%s",
                eventCount, sessionCount, subsessionCount,
                sessionLength / 1000.0, timeSpent / 1000.0,
                stamp(lastActivity), uuid);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        ActivityState otherActivityState = (ActivityState) other;

        if (!Util.equalString(uuid, otherActivityState.uuid)) return false;
        if (!Util.equalBoolean(enabled, otherActivityState.enabled)) return false;
        if (!Util.equalBoolean(isGdprForgotten, otherActivityState.isGdprForgotten)) return false;
        if (!Util.equalBoolean(isThirdPartySharingDisabledForCoppa, otherActivityState.isThirdPartySharingDisabledForCoppa)) return false;
        if (!Util.equalBoolean(askingAttribution, otherActivityState.askingAttribution)) return false;
        if (!Util.equalInt(eventCount, otherActivityState.eventCount)) return false;
        if (!Util.equalInt(sessionCount, otherActivityState.sessionCount)) return false;
        if (!Util.equalInt(subsessionCount, otherActivityState.subsessionCount)) return false;
        if (!Util.equalLong(sessionLength, otherActivityState.sessionLength)) return false;
        if (!Util.equalLong(timeSpent, otherActivityState.timeSpent)) return false;
        if (!Util.equalLong(lastInterval, otherActivityState.lastInterval)) return false;
        if (!Util.equalObject(orderIds, otherActivityState.orderIds)) return false;
        if (!Util.equalString(pushToken, otherActivityState.pushToken)) return false;
        if (!Util.equalString(adid, otherActivityState.adid)) return false;
        if (!Util.equalLong(clickTime, otherActivityState.clickTime)) return false;
        if (!Util.equalLong(installBegin, otherActivityState.installBegin)) return false;
        if (!Util.equalString(installReferrer, otherActivityState.installReferrer)) return false;
        if (!Util.equalBoolean(googlePlayInstant, otherActivityState.googlePlayInstant)) return false;
        if (!Util.equalLong(clickTimeServer, otherActivityState.clickTimeServer)) return false;
        if (!Util.equalLong(installBeginServer, otherActivityState.installBeginServer)) return false;
        if (!Util.equalString(installVersion, otherActivityState.installVersion)) return false;
        if (!Util.equalLong(clickTimeHuawei, otherActivityState.clickTimeHuawei)) return false;
        if (!Util.equalLong(installBeginHuawei, otherActivityState.installBeginHuawei)) return false;
        if (!Util.equalString(installReferrerHuawei, otherActivityState.installReferrerHuawei)) return false;
        if (!Util.equalString(installReferrerHuaweiAppGallery, otherActivityState.installReferrerHuaweiAppGallery)) return false;
        if (!Util.equalLong(clickTimeXiaomi, otherActivityState.clickTimeXiaomi)) return false;
        if (!Util.equalLong(installBeginXiaomi, otherActivityState.installBeginXiaomi)) return false;
        if (!Util.equalString(installReferrerXiaomi, otherActivityState.installReferrerXiaomi)) return false;
        if (!Util.equalLong(clickTimeServerXiaomi, otherActivityState.clickTimeServerXiaomi)) return false;
        if (!Util.equalLong(installBeginServerXiaomi, otherActivityState.installBeginServerXiaomi)) return false;
        if (!Util.equalString(installVersionXiaomi, otherActivityState.installVersionXiaomi)) return false;
        if (!Util.equalLong(clickTimeSamsung, otherActivityState.clickTimeSamsung)) return false;
        if (!Util.equalLong(installBeginSamsung, otherActivityState.installBeginSamsung)) return false;
        if (!Util.equalString(installReferrerSamsung, otherActivityState.installReferrerSamsung)) return false;
        if (!Util.equalLong(clickTimeVivo, otherActivityState.clickTimeVivo)) return false;
        if (!Util.equalLong(installBeginVivo, otherActivityState.installBeginVivo)) return false;
        if (!Util.equalString(installReferrerVivo, otherActivityState.installReferrerVivo)) return false;
        if (!Util.equalString(installVersionVivo, otherActivityState.installVersionVivo)) return false;
        if (!Util.equalString(installReferrerMeta, otherActivityState.installReferrerMeta)) return false;
        if (!Util.equalLong(clickTimeMeta, otherActivityState.clickTimeMeta)) return false;
        if (!Util.equalBoolean(isClickMeta, otherActivityState.isClickMeta)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode = Util.hashString(uuid, hashCode);
        hashCode = Util.hashBoolean(enabled, hashCode);
        hashCode = Util.hashBoolean(isGdprForgotten, hashCode);
        hashCode = Util.hashBoolean(isThirdPartySharingDisabledForCoppa, hashCode);
        hashCode = Util.hashBoolean(askingAttribution, hashCode);
        hashCode = 37 * hashCode + eventCount;
        hashCode = 37 * hashCode + sessionCount;
        hashCode = 37 * hashCode + subsessionCount;
        hashCode = Util.hashLong(sessionLength, hashCode);
        hashCode = Util.hashLong(timeSpent, hashCode);
        hashCode = Util.hashLong(lastInterval, hashCode);
        hashCode = Util.hashObject(orderIds, hashCode);
        hashCode = Util.hashString(pushToken, hashCode);
        hashCode = Util.hashString(adid, hashCode);
        hashCode = Util.hashLong(clickTime, hashCode);
        hashCode = Util.hashLong(installBegin, hashCode);
        hashCode = Util.hashString(installReferrer, hashCode);
        hashCode = Util.hashBoolean(googlePlayInstant, hashCode);
        hashCode = Util.hashLong(clickTimeServer, hashCode);
        hashCode = Util.hashLong(installBeginServer, hashCode);
        hashCode = Util.hashString(installVersion, hashCode);
        hashCode = Util.hashLong(clickTimeHuawei, hashCode);
        hashCode = Util.hashLong(installBeginHuawei, hashCode);
        hashCode = Util.hashString(installReferrerHuawei, hashCode);
        hashCode = Util.hashString(installReferrerHuaweiAppGallery, hashCode);
        hashCode = Util.hashLong(clickTimeXiaomi, hashCode);
        hashCode = Util.hashLong(installBeginXiaomi, hashCode);
        hashCode = Util.hashString(installReferrerXiaomi, hashCode);
        hashCode = Util.hashLong(clickTimeServerXiaomi, hashCode);
        hashCode = Util.hashLong(installBeginServerXiaomi, hashCode);
        hashCode = Util.hashString(installVersionXiaomi, hashCode);
        hashCode = Util.hashLong(clickTimeSamsung, hashCode);
        hashCode = Util.hashLong(installBeginSamsung, hashCode);
        hashCode = Util.hashString(installReferrerSamsung, hashCode);
        hashCode = Util.hashLong(clickTimeVivo, hashCode);
        hashCode = Util.hashLong(installBeginVivo, hashCode);
        hashCode = Util.hashString(installReferrerVivo, hashCode);
        hashCode = Util.hashString(installVersionVivo, hashCode);
        hashCode = Util.hashString(installReferrerMeta, hashCode);
        hashCode = Util.hashLong(clickTimeMeta, hashCode);
        hashCode = Util.hashBoolean(isClickMeta, hashCode);
        return hashCode;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        GetField fields = stream.readFields();

        eventCount = Util.readIntField(fields, "eventCount", 0);
        sessionCount = Util.readIntField(fields, "sessionCount", 0);
        subsessionCount = Util.readIntField(fields, "subsessionCount", -1);
        sessionLength = Util.readLongField(fields, "sessionLength", -1l);
        timeSpent = Util.readLongField(fields, "timeSpent", -1l);
        lastActivity = Util.readLongField(fields, "lastActivity", -1l);
        lastInterval = Util.readLongField(fields, "lastInterval", -1l);

        // new fields
        uuid = Util.readStringField(fields, "uuid", null);
        enabled = Util.readBooleanField(fields, "enabled", true);
        isGdprForgotten = Util.readBooleanField(fields, "isGdprForgotten", false);
        isThirdPartySharingDisabledForCoppa = Util.readBooleanField(fields, "isThirdPartySharingDisabledForCoppa", false);
        askingAttribution = Util.readBooleanField(fields, "askingAttribution", false);

        orderIds = Util.readObjectField(fields, "orderIds", null);
        pushToken = Util.readStringField(fields, "pushToken", null);
        adid = Util.readStringField(fields, "adid", null);

        clickTime = Util.readLongField(fields, "clickTime", -1l);
        installBegin = Util.readLongField(fields, "installBegin", -1l);
        installReferrer = Util.readStringField(fields, "installReferrer", null);
        googlePlayInstant = Util.readObjectField(fields, "googlePlayInstant", null);
        clickTimeServer = Util.readLongField(fields, "clickTimeServer", -1l);
        installBeginServer = Util.readLongField(fields, "installBeginServer", -1l);
        installVersion = Util.readStringField(fields, "installVersion", null);

        clickTimeHuawei = Util.readLongField(fields, "clickTimeHuawei", -1l);
        installBeginHuawei = Util.readLongField(fields, "installBeginHuawei", -1l);
        installReferrerHuawei = Util.readStringField(fields, "installReferrerHuawei", null);
        installReferrerHuaweiAppGallery = Util.readStringField(fields, "installReferrerHuaweiAppGallery", null);

        clickTimeXiaomi = Util.readLongField(fields, "clickTimeXiaomi", -1l);
        installBeginXiaomi = Util.readLongField(fields, "installBeginXiaomi", -1l);
        installReferrerXiaomi = Util.readStringField(fields, "installReferrerXiaomi", null);
        clickTimeServerXiaomi = Util.readLongField(fields, "clickTimeServerXiaomi", -1l);
        installBeginServerXiaomi = Util.readLongField(fields, "installBeginServerXiaomi", -1l);
        installVersionXiaomi = Util.readStringField(fields, "installVersionXiaomi", null);

        clickTimeSamsung = Util.readLongField(fields, "clickTimeSamsung", -1l);
        installBeginSamsung = Util.readLongField(fields, "installBeginSamsung", -1l);
        installReferrerSamsung = Util.readStringField(fields, "installReferrerSamsung", null);

        clickTimeVivo = Util.readLongField(fields, "clickTimeVivo", -1l);
        installBeginVivo = Util.readLongField(fields, "installBeginVivo", -1l);
        installReferrerVivo = Util.readStringField(fields, "installReferrerVivo", null);
        installVersionVivo = Util.readStringField(fields, "installVersionVivo", null);

        installReferrerMeta = Util.readStringField(fields, "installReferrerMeta", null);
        clickTimeMeta = Util.readLongField(fields, "clickTimeMeta", -1l);
        isClickMeta = Util.readObjectField(fields, "isClickMeta", null);

        // create UUID for migrating devices
        if (uuid == null) {
            uuid = Util.createUuid();
        }

        eventDeduplicationIdsMaxSize = EVENT_DEDUPLICATION_IDS_MAX_SIZE;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
     }

    private static String stamp(long dateMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);
        return Util.formatString("%02d:%02d:%02d",
                calendar.HOUR_OF_DAY,
                calendar.MINUTE,
                calendar.SECOND);
    }
}
