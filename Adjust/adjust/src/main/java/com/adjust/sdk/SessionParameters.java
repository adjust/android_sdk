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
public class SessionParameters {
    Map<String, String> callbackParameters;
    Map<String, String> partnerParameters;

    public SessionParameters deepCopy() {
        SessionParameters newSessionParameters = new SessionParameters();
        if (this.callbackParameters != null) {
            newSessionParameters.callbackParameters = new HashMap<String, String>(this.callbackParameters);
        }
        if (this.partnerParameters != null) {
            newSessionParameters.partnerParameters = new HashMap<String, String>(this.partnerParameters);
        }
        return newSessionParameters;
    }
}
