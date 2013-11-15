//
//  ActivityPackage.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import java.io.Serializable;
import java.util.Map;

public class ActivityPackage implements Serializable {
    private static final long serialVersionUID = -35935556512024097L;

    public enum PackageType {
        EVENT("/event", "event"), REVENUE("/revenue", "revenue"), SESSION_START("/startup", "session start");
        private final String path;
        private final String kind;

        PackageType(final String path, final String kind) {
            this.path = path;
            this.kind = kind;
        }

        public String getPath() {
            return path;
        }

        public String getKind() {
            return kind;
        }
    }

    // data
    private PackageType         type;
    private String              userAgent;
    private String              clientSdk;
    private Map<String, String> parameters;

    // logs
    private String suffix;

    public void setType(PackageType type) {
        this.type = type;
    }

    public String getPath() {
        return type.getPath();
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getClientSdk() {
        return clientSdk;
    }

    public void setClientSdk(String clientSdk) {
        this.clientSdk = clientSdk;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String toString() {
        return String.format("%s%s", type.getKind(), suffix);
    }

    protected String getExtendedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Path:      %s\n", type.getPath()));
        builder.append(String.format("UserAgent: %s\n", userAgent));
        builder.append(String.format("ClientSdk: %s\n", clientSdk));

        if (parameters != null) {
            builder.append("Parameters:");
            for (Map.Entry<String, String> entity : parameters.entrySet()) {
                builder.append(String.format("\n\t%-16s %s", entity.getKey(), entity.getValue()));
            }
        }
        return builder.toString();
    }

    protected String getSuccessMessage() {
        return String.format("Tracked %s%s", type.getKind(), suffix);
    }

    protected String getFailureMessage() {
        return String.format("Failed to track %s%s", type.getKind(), suffix);
    }
}
