package com.adjust.sdk;

import org.json.JSONObject;

/**
 * Adjust SDK
 * Created by Pedro Silva (@nonelse) on 4th January 2016.
 * Copyright © 2016-2018 Adjust GmbH. All rights reserved.
 */
public class AdjustEventFailure {
    public boolean willRetry;
    public String adid;
    public String message;
    public String timestamp;
    public String eventToken;
    public JSONObject jsonResponse;

    @Override
    public String toString() {
        return Util.formatString("Event Failure msg:%s time:%s adid:%s event:%s retry:%b json:%s",
                message,
                timestamp,
                adid,
                eventToken,
                willRetry,
                jsonResponse);
    }
}
