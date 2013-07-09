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

    // data
    protected String path;
    protected String userAgent;
    protected String clientSdk;
    protected Map<String, String> parameters;

    // logs
    protected String kind;
    protected String suffix;

    public String toString() {
        return String.format("%s%s", kind, suffix);
    }

    protected String getExtendedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Path:      %s\n", path));
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
        return String.format("Tracked %s%s", kind, suffix);
    }

    protected String getFailureMessage() {
        return String.format("Failed to track %s%s", kind, suffix);
    }
}
