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
public class SessionParameters implements Serializable {
    private static final long serialVersionUID = 6492422652370701336L;
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("externalDeviceId", String.class),
    };
    String externalDeviceId;
    Map<String, String> callbackParameters;
    Map<String, String> partnerParameters;

    public SessionParameters deepCopy() {
        SessionParameters newSessionParameters = new SessionParameters();
        newSessionParameters.externalDeviceId = this.externalDeviceId;
        if (this.callbackParameters != null) {
            newSessionParameters.callbackParameters = new HashMap<String, String>(this.callbackParameters);
        }
        if (this.partnerParameters != null) {
            newSessionParameters.partnerParameters = new HashMap<String, String>(this.partnerParameters);
        }
        return newSessionParameters;
    }

    @Override
    public String toString() {
        return "External Device Id: " + externalDeviceId;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();

        externalDeviceId = Util.readStringField(fields, "externalDeviceId", null);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }
}
