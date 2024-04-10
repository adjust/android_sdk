package com.adjust.sdk.vivo;

import com.adjust.sdk.ReferrerDetails;

public interface OnVivoInstallReferrerReadListener {
    void onVivoInstallReferrerRead(ReferrerDetails referrerDetails);
    void onFailure(String message);
}
