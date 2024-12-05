//
//  Constants.java
//  Adjust
//
//  Created by keyboardsurfer on 2013-11-08.
//  Copyright (c) 2012-2014 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

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
    String GDPR_URL = "https://gdpr.adjust.com";
    String SUBSCRIPTION_URL = "https://subscription.adjust.com";
    String PURCHASE_VERIFICATION_URL = "https://ssrv.adjust.com";

    String BASE_URL_FORMAT = "https://app.%s";
    String GDPR_URL_FORMAT = "https://gdpr.%s";
    String SUBSCRIPTION_URL_FORMAT = "https://subscription.%s";
    String PURCHASE_VERIFICATION_URL_FORMAT = "https://ssrv.%s";

    String BASE_URL_NO_SUB_DOMAIN_FORMAT = "https://%s";

    String SCHEME = "https";
    String AUTHORITY = "app.adjust.com";
    String CLIENT_SDK = "android5.0.2";
    String LOGTAG = "Adjust";
    String REFTAG = "reftag";
    String INSTALL_REFERRER = "install_referrer";
    String REFERRER_API_GOOGLE = "google";
    String REFERRER_API_HUAWEI_ADS = "huawei_ads";
    String REFERRER_API_HUAWEI_APP_GALLERY = "huawei_app_gallery";
    String REFERRER_API_SAMSUNG = "samsung";
    String REFERRER_API_XIAOMI = "xiaomi";
    String REFERRER_API_VIVO = "vivo";
    String REFERRER_API_META = "meta";
    String DEEPLINK = "deeplink";
    String PUSH = "push";
    String THREAD_PREFIX = "Adjust-";

    String ACTIVITY_STATE_FILENAME = "AdjustIoActivityState";
    String ATTRIBUTION_FILENAME = "AdjustAttribution";
    String GLOBAL_CALLBACK_PARAMETERS_FILENAME = "AdjustGlobalCallbackParameters";
    String GLOBAL_PARTNER_PARAMETERS_FILENAME = "AdjustGlobalPartnerParameters";

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
    int MINIMAL_ERROR_STATUS_CODE = 400;

    String CALLBACK_PARAMETERS = "callback_params";
    String PARTNER_PARAMETERS = "partner_params";

    String FCM_PAYLOAD_KEY = "adjust_purpose";

    String FCM_PAYLOAD_VALUE = "uninstall detection";

    int MAX_INSTALL_REFERRER_RETRIES = 2;

    String FB_AUTH_REGEX = "^(fb|vk)[0-9]{5,}[^:]*://authorize.*access_token=.*";

    String PREINSTALL = "preinstall";
    String SYSTEM_PROPERTIES = "system_properties";
    String SYSTEM_PROPERTIES_REFLECTION = "system_properties_reflection";
    String SYSTEM_PROPERTIES_PATH = "system_properties_path";
    String SYSTEM_PROPERTIES_PATH_REFLECTION = "system_properties_path_reflection";
    String CONTENT_PROVIDER = "content_provider";
    String CONTENT_PROVIDER_INTENT_ACTION = "content_provider_intent_action";
    String CONTENT_PROVIDER_NO_PERMISSION = "content_provider_no_permission";
    String FILE_SYSTEM = "file_system";
    String SYSTEM_INSTALLER_REFERRER = "system_installer_referrer";

    String ADJUST_PREINSTALL_SYSTEM_PROPERTY_PREFIX = "adjust.preinstall.";
    String ADJUST_PREINSTALL_SYSTEM_PROPERTY_PATH = "adjust.preinstall.path";
    String ADJUST_PREINSTALL_CONTENT_URI_AUTHORITY = "com.adjust.preinstall";
    String ADJUST_PREINSTALL_CONTENT_URI_PATH = "trackers";
    String ADJUST_PREINSTALL_CONTENT_PROVIDER_INTENT_ACTION = "com.attribution.REFERRAL_PROVIDER";
    String ADJUST_PREINSTALL_FILE_SYSTEM_PATH = "/data/local/tmp/adjust.preinstall";
    String EXTRA_SYSTEM_INSTALLER_REFERRER = "com.attribution.EXTRA_SYSTEM_INSTALLER_REFERRER";
}
