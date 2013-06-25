package com.adeven.adjustio;

import java.util.Map;

public class PackageBuilder {
    // TODO: move somewhere else?
    public float amountInCents;
    public String eventToken;
    public Map<String, String> parameters;

    private String path;
    private String successMessage;
    private String failureMessage;
    private String userAgent;
    private String parameterString;

    PackageBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    PackageBuilder setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        Logger.verbose(path, "userAgent", userAgent);
        return this;
    }

    PackageBuilder setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
        Logger.verbose(path, "successMessage", successMessage);
        return this;
    }

    PackageBuilder setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
        Logger.verbose(path, "failureMessage", failureMessage);
        return this;
    }

    PackageBuilder addTrackingParameter(String key, String value) {
        if (value == null || value == "") {
            return this;
        }

        if (parameterString == null || parameterString == "") {
            parameterString = key + "=" + value;
        } else {
            parameterString += "&" + key + "=" + value;
        }

        Logger.verbose(path, key, value); // TODO: remove these logs here?
        return this;
    }

    TrackingPackage build() {
        TrackingPackage trackingPackage = new TrackingPackage(path,
                successMessage, failureMessage, userAgent, parameterString);
        return trackingPackage;
    }

    public void setSessionCount(int sessionCount) {
        // TODO Auto-generated method stub

    }

    public void setSubsessionCount(int subsessionCount) {
        // TODO Auto-generated method stub

    }

    public void setSessionLength(double sessionLength) {
        // TODO Auto-generated method stub

    }

    public void setCreatedAt(long createdAt) {
        // TODO Auto-generated method stub

    }

    public void setTimeSpent(double timeSpent) {
        // TODO Auto-generated method stub

    }
}
