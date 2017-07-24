package com.adjust.sdk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pfms on 29/07/2016.
 */
public class SessionParameters implements Serializable, Cloneable {
    Map<String, String> callbackParameters;
    Map<String, String> partnerParameters;

    String externalDeviceId;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("externalDeviceId", String.class),
    };

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        SessionParameters otherSessionParameters = (SessionParameters) other;

        if (!Util.equalString(externalDeviceId, otherSessionParameters.externalDeviceId))   return false;
        if (!Util.equalObject(callbackParameters, otherSessionParameters.callbackParameters)) return false;
        if (!Util.equalObject(partnerParameters, otherSessionParameters.partnerParameters)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + Util.hashString(externalDeviceId);
        hashCode = 37 * hashCode + Util.hashObject(callbackParameters);
        hashCode = 37 * hashCode + Util.hashObject(partnerParameters);
        return hashCode;
    }

    public SessionParameters deepCopy() {
        SessionParameters newSessionParameters = new SessionParameters();
        if (this.callbackParameters != null) {
            newSessionParameters.callbackParameters = new HashMap<String, String>(this.callbackParameters);
        }
        if (this.partnerParameters != null) {
            newSessionParameters.partnerParameters = new HashMap<String, String>(this.partnerParameters);
        }
        newSessionParameters.externalDeviceId = this.externalDeviceId;
        return newSessionParameters;
    }

    public String toStringSerialized() {
        return String.format("externalDeviceId %s", externalDeviceId);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();

        externalDeviceId = Util.readStringField(fields, "externalDeviceId", null);
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }
}
