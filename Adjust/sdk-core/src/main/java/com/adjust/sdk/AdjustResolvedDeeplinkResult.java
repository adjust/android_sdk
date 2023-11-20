package com.adjust.sdk;

public class AdjustResolvedDeeplinkResult {
    private final String resolvedLink;

    public AdjustResolvedDeeplinkResult(String resolvedLink) {
        this.resolvedLink = resolvedLink;
    }

    public String getResolvedLink() {
        return resolvedLink;
    }
}
