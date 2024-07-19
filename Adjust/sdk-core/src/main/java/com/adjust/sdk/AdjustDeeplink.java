package com.adjust.sdk;

import android.net.Uri;

public class AdjustDeeplink {
    Uri url;

    public AdjustDeeplink(Uri url) {
        this.url = url;
    }

    public boolean isValid() {
        if (url == null || url.toString().isEmpty()) {
            return false;
        }
        return true;
    }

    public Uri getUrl() {
        return url;
    }
}
