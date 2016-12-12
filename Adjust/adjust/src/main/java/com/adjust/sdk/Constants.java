//
//  Constants.java
//  Adjust
//
//  Created by keyboardsurfer on 2013-11-08.
//  Copyright (c) 2012-2014 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import java.util.Arrays;
import java.util.List;

import static android.R.attr.data;

/**
 * @author keyboardsurfer
 * @since 8.11.13
 */
public interface Constants {
    int ONE_SECOND = 1000;
    int ONE_MINUTE = 60 * ONE_SECOND;
    int THIRTY_MINUTES = 30 * ONE_MINUTE;
    int ONE_HOUR = 2 * THIRTY_MINUTES;

    int CONNECTION_TIMEOUT = Constants.ONE_MINUTE;
    int SOCKET_TIMEOUT = Constants.ONE_MINUTE;
    int MAX_WAIT_INTERVAL = Constants.ONE_MINUTE;

    String BASE_URL = "https://app.adjust.com";
//    String BASE_URL = "http://172.16.150.242:8080/";
    String SCHEME = "https";
    String AUTHORITY = "app.adjust.com";
    String CLIENT_SDK = "android4.10.4";
    String LOGTAG = "Adjust";
    String REFTAG = "reftag";
    String DEEPLINK = "deeplink";
    String PUSH = "push";
    String THREAD_PREFIX = "Adjust-";

    String ACTIVITY_STATE_FILENAME = "AdjustIoActivityState";
    String ATTRIBUTION_FILENAME = "AdjustAttribution";
    String SESSION_CALLBACK_PARAMETERS_FILENAME = "AdjustSessionCallbackParameters";
    String SESSION_PARTNER_PARAMETERS_FILENAME = "AdjustSessionPartnerParameters";

    String MALFORMED = "malformed";
    String SMALL = "small";
    String NORMAL = "normal";
    String LONG = "long";
    String LARGE = "large";
    String XLARGE = "xlarge";
    String LOW = "low";
    String MEDIUM = "medium";
    String HIGH = "high";
    String REFERRER = "referrer";

    String ENCODING = "UTF-8";
    String MD5 = "MD5";
    String SHA1 = "SHA-1";

    String CALLBACK_PARAMETERS = "callback_params";
    String PARTNER_PARAMETERS = "partner_params";

    String STATE_SDK_ENABLED = "sdk_enabled";
    String STATE_SDK_OFFLINE = "sdk_offline";
    String STATE_BACKGROUND_ENABLED = "background_enabled";
    String STATE_DELAY_START = "delay_start";
    String STATE_TO_UPDATE_PACKAGES = "to_update_packages";
    String STATE_PUSH_TOKEN = "push_token";
    String STATE_REFERRER = "referrer";
    String STATE_CALLBACK_PARAMETERS = "callback_parameters";
    String STATE_PARTNER_PARAMETERS = "partner_parameters";
    String STATE_DEFAULT_TRACKER = "default_tracker";
    String STATE_IS_ATTRIBUTION_CALLBACK_IMPLEMENNTED = "is_attribution_callback_implemented";
    String STATE_IS_EVENT_TRACKING_SUCCEEDED_CALLBACK_IMPLEMENTED = "is_event_tracking_succeeded_callback_implemented";
    String STATE_IS_EVENT_TRACKING_FAILED_CALLBACK_IMPLEMENTED = "is_event_tracking_failed_callback_implemented";
    String STATE_IS_SESSION_TRACKING_SUCCEEDED_CALLBACK_IMPLEMENTED = "is_session_tracking_succeeded_callback_implemented";
    String STATE_IS_SESSION_TRACKING_FAILED_CALLBACK_IMPLEMENTED = "is_session_tracking_failed_callback_implemented";
    String STATE_IS_DEFERRED_DEEPLINK_CALLBACK_IMPLEMENTED = "is_deferred_deeplink_callback_implemented";
    String STATE_ALLOW_SUPPRESS_LOG_LEVEL = "allow_suppress_log_level";
    String STATE_USER_AGENT = "user_agent";
    String STATE_APP_TOKEN = "app_token";
    String STATE_ENVIRONMENT = "environment";
    String STATE_PROCESS_NAME = "process_name";
    String STATE_SDK_PREFIX = "sdk_prefix";

    // List of known plugins, possibly not active
    List<String> PLUGINS = Arrays.asList();
}
