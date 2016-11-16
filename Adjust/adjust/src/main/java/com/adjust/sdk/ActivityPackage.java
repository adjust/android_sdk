//
//  ActivityPackage.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ActivityPackage implements Serializable {
    private static final long serialVersionUID = -35935556512024097L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("path", String.class),
            new ObjectStreamField("clientSdk", String.class),
            new ObjectStreamField("parameters", (Class<Map<String, String>>) (Class) Map.class),
            new ObjectStreamField("activityKind", ActivityKind.class),
            new ObjectStreamField("suffix", String.class),
            new ObjectStreamField("callbackParameters", (Class<Map<String, String>>) (Class) Map.class),
            new ObjectStreamField("partnerParameters", (Class<Map<String, String>>) (Class) Map.class),
    };

    private transient int hashCode;

    // data
    private String path;
    private String clientSdk;
    private Map<String, String> parameters;

    // logs
    private ActivityKind activityKind = ActivityKind.UNKNOWN;
    private String suffix;

    // delay
    private Map<String, String> callbackParameters;
    private Map<String, String> partnerParameters;

    private int retries;

    public final String getPath() {
        return path;
    }

    public final void setPath(final String path) {
        this.path = path;
    }

    public final String getClientSdk() {
        return clientSdk;
    }

    public final void setClientSdk(final String clientSdk) {
        this.clientSdk = clientSdk;
    }

    public final Map<String, String> getParameters() {
        return parameters;
    }

    public final void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public final void setCallbackParameters(final Map<String, String> callbackParameters) {
        this.callbackParameters = callbackParameters;
    }

    public final void setPartnerParameters(final Map<String, String> partnerParameters) {
        this.partnerParameters = partnerParameters;
    }

    public final ActivityKind getActivityKind() {
        return activityKind;
    }

    public final String getSuffix() {
        return suffix;
    }

    public final void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    public final int getRetries() {
        return retries;
    }

    public final int increaseRetries() {
        retries++;
        return retries;
    }

    public final Map<String, String> getCallbackParameters() {
        return callbackParameters;
    }

    public final Map<String, String> getPartnerParameters() {
        return partnerParameters;
    }

    public ActivityPackage(final ActivityKind activityKind) {
        this.activityKind = activityKind;
    }

    public final String toString() {
        return String.format(Locale.US, "%s%s", activityKind.toString(), suffix);
    }

    public final String getExtendedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.US, "Path:      %s%n", path));
        builder.append(String.format(Locale.US, "ClientSdk: %s%n", clientSdk));

        if (parameters != null) {
            builder.append("Parameters:");
            SortedMap<String, String> sortedParameters = new TreeMap<String, String>(parameters);
            for (Map.Entry<String, String> entry : sortedParameters.entrySet()) {
                builder.append(String.format(Locale.US, "%n\t%-16s %s", entry.getKey(), entry.getValue()));
            }
        }
        return builder.toString();
    }

    protected final String getFailureMessage() {
        return String.format(Locale.US, "Failed to track %s%s", activityKind.toString(), suffix);
    }

    private void writeObject(final ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    private void readObject(final ObjectInputStream stream) throws ClassNotFoundException, IOException {
        ObjectInputStream.GetField fields = stream.readFields();

        path = Util.readStringField(fields, "path", null);
        clientSdk = Util.readStringField(fields, "clientSdk", null);
        parameters = Util.readObjectField(fields, "parameters", null);
        activityKind = Util.readObjectField(fields, "activityKind", ActivityKind.UNKNOWN);
        suffix = Util.readStringField(fields, "suffix", null);
        callbackParameters = Util.readObjectField(fields, "callbackParameters", null);
        partnerParameters = Util.readObjectField(fields, "partnerParameters", null);
    }

    @Override
    public final boolean equals(final Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        ActivityPackage otherActivityPackage = (ActivityPackage) other;

        if (!Util.equalString(path, otherActivityPackage.path)) return false;
        if (!Util.equalString(clientSdk, otherActivityPackage.clientSdk)) return false;
        if (!Util.equalObject(parameters, otherActivityPackage.parameters)) return false;
        if (!Util.equalEnum(activityKind, otherActivityPackage.activityKind)) return false;
        if (!Util.equalString(suffix, otherActivityPackage.suffix)) return false;
        if (!Util.equalObject(callbackParameters, otherActivityPackage.callbackParameters))
            return false;
        if (!Util.equalObject(partnerParameters, otherActivityPackage.partnerParameters))
            return false;

        return true;
    }

    @Override
    public final int hashCode() {
        if (hashCode == 0) {
            hashCode = 17;
            hashCode = 37 * hashCode + Util.hashString(path);
            hashCode = 37 * hashCode + Util.hashString(clientSdk);
            hashCode = 37 * hashCode + Util.hashObject(parameters);
            hashCode = 37 * hashCode + Util.hashEnum(activityKind);
            hashCode = 37 * hashCode + Util.hashString(suffix);
            hashCode = 37 * hashCode + Util.hashObject(callbackParameters);
            hashCode = 37 * hashCode + Util.hashObject(partnerParameters);
        }
        return hashCode;
    }
}
