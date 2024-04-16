package com.adjust.sdk.vivo;

import com.adjust.sdk.ReferrerDetails;

public interface OnVivoInstallReferrerReadListener {
    void onVivoInstallReferrerRead(VivoInstallReferrerDetails referrerDetails);
    void onFail(String message);
}
