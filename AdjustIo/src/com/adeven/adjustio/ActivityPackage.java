// TODO: add header comments

package com.adeven.adjustio;

import java.io.Serializable;
import java.util.Map;

public class ActivityPackage implements Serializable {
    private static final long serialVersionUID = -35935556512024097L;

    // data
    protected String path;
    protected String userAgent;
    protected Map<String, String> parameters;

    // logs
    protected String kind;
    protected String suffix;

    public String toString() {
        return String.format("%s%s %s", kind, suffix, path);
    }

    protected String getParameterString() {
        if (parameters == null) return "Parameters: null";

        StringBuilder builder = new StringBuilder("Parameters:");

        for (Map.Entry<String, String> entity : parameters.entrySet()) {
            builder.append(String.format("\n\t%-16s %s", entity.getKey(), entity.getValue()));
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
