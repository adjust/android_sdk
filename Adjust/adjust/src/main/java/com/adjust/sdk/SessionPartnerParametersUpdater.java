package com.adjust.sdk;

import java.util.Map;

/**
 * Created by pfms on 01/04/16.
 */
// XXX TODO remove
public interface SessionPartnerParametersUpdater {
    Map<String, String> updateSessionPartnerParameters(Map<String, String> currentPartnerParameters);
}
