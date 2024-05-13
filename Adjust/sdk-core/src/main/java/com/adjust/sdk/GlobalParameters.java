package com.adjust.sdk;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pfms on 29/07/2016.
 */
public class GlobalParameters {
    Map<String, String> callbackParameters;
    Map<String, String> partnerParameters;

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        GlobalParameters otherGlobalParameters = (GlobalParameters) other;

        if (!Util.equalObject(callbackParameters, otherGlobalParameters.callbackParameters)) return false;
        if (!Util.equalObject(partnerParameters, otherGlobalParameters.partnerParameters)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode = Util.hashObject(callbackParameters, hashCode);
        hashCode = Util.hashObject(partnerParameters, hashCode);
        return hashCode;
    }

    public GlobalParameters deepCopy() {
        GlobalParameters newGlobalParameters = new GlobalParameters();
        if (this.callbackParameters != null) {
            newGlobalParameters.callbackParameters = new HashMap<String, String>(this.callbackParameters);
        }
        if (this.partnerParameters != null) {
            newGlobalParameters.partnerParameters = new HashMap<String, String>(this.partnerParameters);
        }
        return newGlobalParameters;
    }
}
