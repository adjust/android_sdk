//
//  TrackingPackage.java
//  AdjustIo
//
//  Created by Benjamin Weiss on 17.4.13
//  Copyright (c) 2012 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import java.io.Serializable;
import java.util.Map;

/**
 * Holds information of one tracking package.
 *
 * @author keyboardsurfer
 * @since 17.4.13
 */
public class TrackingPackage implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -5435782033488813179L;
    final String path;
    final String successMessage;
    final String failureMessage;
    final String userAgent;
    final String parameters;

    public TrackingPackage(String path, String successMessage, String failureMessage, String userAgent, String parameters) {
        this.path = path;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.userAgent = userAgent;
        this.parameters = parameters;
    }

    /**
     * A builder to enable chained building of a TrackingPackage.
     */
    static class Builder {
        public float amountInCents;
        public String eventToken;
        public Map<String, String> parameters;

        private String path;
        private String successMessage;
        private String failureMessage;
        private String userAgent;
        private String parameterString;

        Builder setPath(String path) {
            this.path = path;
            return this;
        }

        Builder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            Logger.verbose(path, "userAgent", userAgent);
            return this;
        }

        Builder setSuccessMessage(String successMessage) {
            this.successMessage = successMessage;
            Logger.verbose(path, "successMessage", successMessage);
            return this;
        }

        Builder setFailureMessage(String failureMessage) {
            this.failureMessage = failureMessage;
            Logger.verbose(path, "failureMessage", failureMessage);
            return this;
        }

        Builder addTrackingParameter(String key, String value) {
            if (value == null || value == "" ) {
                return this;
            }

            if (parameterString == null || parameterString == "") {
                parameterString = key + "=" + value;
            } else {
                parameterString += "&" + key + "=" + value;
            }

            Logger.verbose(path, key, value);   // TODO: remove these logs here?
            return this;
        }

        TrackingPackage build() {
            TrackingPackage trackingPackage = new TrackingPackage(path, successMessage, failureMessage, userAgent, parameterString);
            return trackingPackage;
        }
    }
}
