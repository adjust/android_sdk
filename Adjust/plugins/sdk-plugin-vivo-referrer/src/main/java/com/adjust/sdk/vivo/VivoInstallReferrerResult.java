package com.adjust.sdk.vivo;

public class VivoInstallReferrerResult {
    public String error;
    public VivoInstallReferrerDetails vivoInstallReferrerDetails;

    public VivoInstallReferrerResult(VivoInstallReferrerDetails vivoInstallReferrerDetails) {
        this.vivoInstallReferrerDetails = vivoInstallReferrerDetails;
    }

    public VivoInstallReferrerResult(String error) {
        this.error = error;
    }
}
