package com.adjust.sdk.meta;

public interface OnMetaInstallReferrerReadListener {
    void onInstallReferrerDetailsRead(MetaInstallReferrerDetails referrerDetails);
    void onFail(String message);
}
