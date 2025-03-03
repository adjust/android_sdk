package com.adjust.sdk;

import android.net.Uri;

public class AdjustDeeplink {
    Uri url;
    Uri referrer;

    public AdjustDeeplink(Uri url) {
        this.url = url;
    }

    public boolean isValid() {
        if (url == null || url.toString().isEmpty()) {
            return false;
        }
        return true;
    }

    public void setReferrer(Uri referrer) {
        this.referrer = referrer;
    }

    public Uri getUrl() {
        return url;
    }

    public Uri getReferrer() {
        return referrer;
    }
}
