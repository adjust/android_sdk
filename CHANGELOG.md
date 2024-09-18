### Version 5.0.1 (18th September 2024)
#### Fixed
- Fixed ANR while reading referrer from Shared Preferences during SDK initialization. 

#### Changed
- Added input parameter validations in certain public Webbrdige APIs.
- Updated log messages to add more clarity and other improvements.

---

### Version 5.0.0 (2nd August 2024)

We're excited to release our major new SDK version (v5). Among many internal improvements, our spoofing protection solution is now included out of the box, reinforcing our commitment to accurate, actionable, and fraud-free data.

To try out SDK v5 in your app, you can follow our new v4 to v5 [migration guide](https://dev.adjust.com/en/sdk/android/migration/v4-to-v5).

On [our Signature releases page](https://github.com/adjust/adjust_signature_sdk/releases), you can find the latest version of the Signature library to integrate into your app.

If you are a current Adjust client and have questions about SDK v5, please email [sdk-v5@adjust.com](mailto:sdk-v5@adjust.com).

In case you were using beta version of the SDK v5, please switch to the official v5 release.

---

### Version 4.38.5 (2nd July 2024)
#### Fixed
- Fixed occasional crash while invoking deeplink resolution callback.

---

### Version 4.38.4 (14th May 2024)
#### Added
- Added sending of the additional SDK observability parameters for debugging purposes.

---

### Version 4.38.3 (26th March 2024)
#### Added
- Added support for Samsung CloudDev SDK to read Google Play Advertising Identifier.

#### Changed
 - Removed webbridge unused code.

---

### Version 4.38.2 (28th February 2024)
#### Fixed
- Fixed occasional invalid signature cases when SDK package payload would be altered right before sending.

#### Changed
 - Updated order of tracking of `third_party_sharing` and `measurement_consent` packages if invoked before SDK initialization in subsequent SDK initializations.

---

### Version 4.38.1 (30th January 2024)
#### Added
- Added support for `TradPlus` ad revenue tracking.

---

### Version 4.38.0 (21st December 2023)
#### Added
- Added `toMap()` method to `AdjustAttribution` class to get all its fields in a `Map<String, String>` (thanks to @ntsk).
- Added ability to process shortened deep links and provide the unshortened link back as a response. You can achieve this by invoking `processDeeplink(Uri url, Context context, OnDeeplinkResolvedListener callback)` method of the `Adjust` instance.

---

### Version 4.37.1 (11th December 2023)
#### Fixed
- Added null check for the `OnDeviceIdsRead` callback object passed to `Adjust.getGoogleAdId` method to avoid `NullPointerException`.

#### Changed
- Switched from usage of deprecated [`PackageInfo.signatures`](https://developer.android.com/reference/android/content/pm/PackageInfo#signatures) to [`PackageInfo.signingInfo`](https://developer.android.com/reference/android/content/pm/PackageInfo#signingInfo) on devices running Android 9 or later.
- Switched from usage of deprecated [`Class.newInstance()`](https://developer.android.com/reference/java/lang/Class#newInstance()) to [`Class.getDeclaredConstructor().newInstance()`](https://developer.android.com/reference/java/lang/reflect/Constructor#newInstance(java.lang.Object[])) on devices running Android 14 or later.

---

### Version 4.37.0 (8th November 2023)
#### Added
- Added a new type of URL strategy called `AdjustConfig.URL_STRATEGY_CN_ONLY`. This URL strategy represents `AdjustConfig.URL_STRATEGY_CN` strategy, but without fallback domains.
- Added `setReadDeviceInfoOnceEnabled(boolean)` method to `AdjustConfig` to indicate if device info to be read only once.

---

### Version 4.36.0 (19th October 2023)
#### Added
- Added support for Meta install referrer.
- Added support for Google Play Games on PC.
- Added support for `TopOn` and `AD(X)` ad revenue tracking.
- Added Getters for certain public classes.

---

### Version 4.35.1 (9th October 2023)
#### Changed
- Added sending of `event_callback_id` parameter (if set) with the event payload.
- Updated Gradle version 8.1.1.

---

### Version 4.35.0 (12th September 2023)
#### Added
- Added support for SigV3 library. Update authorization header building logic to use `adj_signing_id`.
- Added `setFinalAttributionEnabled(boolean)` method to `AdjustConfig` to indicate if only final attribution is needed in attribution callback (by default attribution callback return intermediate attribution as well before final attribution if not enabled with this setter method).

---

### Version 4.34.0 (17th August 2023)
#### Added
- Added support for purchase verification. In case you are using this feature, you can now use it by calling `verifyPurchase` method of the `Adjust` instance.

---

### Version 4.33.5 (5th July 2023)
#### Added
- Added sending of App Set Identifier.

---

### Version 4.33.4 (27th April 2023)
#### Changed
- Updated Samsung Install Referrer library version to 3.0.1.
- Updated IMEI reading attempt to only once.

---

### Version 4.33.3 (16th February 2023)
#### Fixed
- Added catching of exceptions in referrer plugins when thrown anytime while retrieving referrer.

---

### Version 4.33.2 (7th December 2022)
#### Fixed
- Fixed Duplicate class error caused by compiler generating package `a.a`.

---

### Version 4.33.1 (25th November 2022)
#### Added
- Added support for Vivo install referrer.
- Added support for setting a new China Url Strategy. You can choose this setting by calling `setUrlStrategy` method of `AdjustConfig` instance with `AdjustConfig.URL_STRATEGY_CN` parameter.

#### Changed
- Changed log type for Samsung & Xiaomi install referrer error logging.

---

### Version 4.33.0 (19th October 2022)
#### Added
- Added support for Samsung install referrer.
- Added `isAdjustUninstallDetectionPayload()` method to `Adjust` interface to know whether payload originates from Adjust or not.
- Added support to OAID plugin for MSA SDK v2.0.0.

#### Fixed
- Added catching of `NullPointerException` while extracting attribution fields in `webbridge` plugin.

#### Changed
- Updated OAID plugin to read OAID on Huawei devices using Huawei Mobile Sevices Core Ads Identifier SDK.
- Added bundling of Adjust Javascript files to `webbridge` plugin in the `asset` folder:
    - In case you add web bridge AAR from release page to your app, make sure to remove any previous JAR version you had.
    - There is no need for you to keep (and update) Adjust Javascript files inside of your app's assets folder - feel free to delete them since they are now bundled inside of the AAR.

---

### Version 4.32.0 (7th September 2022)
#### Added
- Added partner sharing settings to the third party sharing feature.

---

### Version 4.31.1 (18th August 2022)
#### Added
- Added permission `com.google.android.gms.permission.AD_ID` in the SDK's mainfest.

#### Fixed
- Typo in Xiaomi Install Referrer's maven artifact id.

#### Changed
- Updated Gradle version 7.3.3 and other dependencies.

---

### Version 4.31.0 (28th July 2022)
#### Added
- Added support for Xiaomi install referrer.
- Added support to get Facebook install referrer information in attribution callback.

---

### Version 4.30.1 (17th May 2022)
#### Added
- Added support to publish AAR in maven repository.
- Added support for `Generic` ad revenue tracking.
- Added sending of `deduplication_id` parameter in `event` package.

#### Fixed
- Added catching of `IllegalStateException` while retrieving `SharedPreferences`

---

### Version 4.30.0 (11th April 2022)
#### Added
- Added ability to mark your app as COPPA compliant. You can enable this setting by calling `setCoppaCompliantEnabled` method of `AdjustConfig` instance with boolean parameter `true`.
- Added ability to mark your app as app for the kids in accordance to Google Play Families policies. You can enable this setting by calling `setPlayStoreKidsAppEnabled` method of `AdjustConfig` instance with boolean parameter `true`.

#### Changed
- Removed reading of `network_type` parameter.
- Updated docs.

---

### Version 4.29.1 (8th February 2022)
#### Added
- Added support for `Helium Chartboost` ad revenue tracking.

---

### Version 4.29.0 (8th February 2022)
#### Added
- Added support for `Unity` ad revenue tracking.

#### Changed
- Updated docs.

---

### Version 4.28.9 (15th January 2022)
#### Added
- Added support to OAID plugin for MSA SDK v1.1.0.

#### Changed
- Removed reading of MAC address.
- Updated Gradle version 7.0.4 and Gradle Javadoc task.

#### Fixed
- Fixed crash while iterating through the running processes.
- Fixed ANR while reading Google Play Advertising Id.

---

### Version 4.28.8 (6th December 2021)
#### Changed
- Added extraction & usage of application context for all the APIs.

---

### Version 4.28.7 (15th November 2021)
#### Changed
- Added caching of IDs in the IMEI plugin to avoid frequent reads.

---

### Version 4.28.6 (19th October 2021)
#### Added
- Added Huawei Install Referrer Track ID support.

#### Changed
- Replaced deprecated [`AsyncTask`](https://developer.android.com/reference/android/os/AsyncTask) with custom implementation.
- Added permission `com.google.android.gms.permission.AD_ID` in the example apps for Android 12 & above.

---

### Version 4.28.5 (22nd September 2021)
#### Added
- Added support for `Admost` ad revenue tracking.

---

### Version 4.28.4 (9th August 2021)
#### Added
- Added support for Android TV.
- Added support to OAID plugin for MSA SDK v1.0.26.

#### Changed
- Improved logging.

---

### Version 4.28.3 (21st July 2021)
#### Fixed
- Fixed missing authorization header in retry requests.

---

### Version 4.28.2 (11th June 2021)
#### Changed
- Added deep link URL decoding before parsing its parameters.

---

### Version 4.28.1 (12th May 2021)
#### Added
- [beta] Added data residency support for US region. You can choose this setting by calling `setUrlStrategy` method of `AdjustConfig` instance with `AdjustConfig.DATA_RESIDENCY_US` parameter.
- Added helper class `AdjustLinkResolution` to assist with resolution of links which are wrapping Adjust deep link.

#### Fixed
- Removed 5 decimal places formatting for ad revenue value.

---

### Version 4.28.0 (26th April 2021)
#### Added
- [beta] Added data residency support for Turkey region. You can choose this setting by calling `setUrlStrategy` method of `AdjustConfig` instance with `AdjustConfig.DATA_RESIDENCY_TR` parameter.
- Added `trackAdRevenue(AdjustAdRevnue)` method to `Adjust` interface to allow tracking of ad revenue by passing `AdjustAdRevnue` as parameter. 
- Added support for `AppLovin MAX` ad revenue tracking.

#### Changed
- Removed unused ad revenue constants from `AdjustConfig`.

---

### Version 4.27.0 (17th March 2021)
#### Added
- [beta] Added data residency feature. Support for EU data residency region is added. You can choose this setting by calling `setUrlStrategy` method of `AdjustConfig` instance with `AdjustConfig.DATA_RESIDENCY_EU` parameter.
- Added preinstall tracking with usage of system installer receiver.
- Added support for MSA SDK v1.0.25 to OAID plugin.

#### Changed
- Changed the measurement consent parameter name from `sharing` to `measurement`.

---

### Version 4.26.2 (1st February 2021)
#### Added
- Added Facebook audience network ad revenue source string.

---

### Version 4.26.1 (27th January 2021)
#### Fixed
- Changed minimum supported API version to 17 for `webbridge`, to avoid insecure JavaScript potential injections on lower API versions.
- Moved reading of install referrer details to background thread after service connection establishes.

---

### Version 4.26.0 (16th January 2021)
#### Added
- Added improved measurement consent management and third party sharing system.

---

### Version 4.25.0 (9th December 2020)
#### Added
- Added possibility to get cost data information in attribution callback.
- Added `setNeedsCost(boolean)` method to `AdjustConfig` to indicate if cost data is needed in attribution callback (by default cost data will not be part of attribution callback if not enabled with this setter method).
- Added `setPreinstallTrackingEnabled` method to `adjust_config.js` to allow enabling of preintall tracking from web bridge plugin.

#### Changed
- Switched from usage of deprecated [`getNetworkType()`](https://developer.android.com/reference/android/telephony/TelephonyManager#getNetworkType()) method to [`getDataNetworkType()`](https://developer.android.com/reference/android/telephony/TelephonyManager#getDataNetworkType()) on devices running Android 11 or later.

---

### Version 4.24.1 (10th September 2020)
#### Added
- Added support to OAID plugin for MSA SDK v1.0.23.

#### Changed
- Changed host name verifier used for testing purposes to allow only localhost addresses.

---

### Version 4.24.0 (19th August 2020)
#### Added
- Added `setUrlStrategy(String)` method in `AdjustConfig` class to allow setting of URL strategy for specific market.

---

### Version 4.23.0 (24th July 2020)
#### Added
- Added new ways of handling preinstall campaigns.
- Added reading of additional fields which [Play Install Referrer API](https://developer.android.com/google/play/installreferrer/igetinstallreferrerservice) introduced in v2.0.
- Added support to OAID plugin for MSA SDK v1.0.13.

---

### Version 4.22.0 (29th May 2020)
#### Added
- Added subscription tracking feature.

#### Changed
- Updated OAID reading logic by adding retry mechanism.

#### Fixed
- Fixed memory leak issue caused by holding reference to web view object inside of web bridge (thanks to @evgentset).

---

### Version 4.21.2 (15th May 2020)
#### Added
- Added check for presence of Huawei content provider.
- Added persistence of Huawei referrer information.

#### Changed
- Updated Google Play Advertising Id reading logic.

---

### Version 4.21.1 (8th April 2020)
#### Added
- Added support for Huawei App Gallery install referrer.

---

### Version 4.21.0 (19th March 2020)
#### Added
- Added support for signature library as a plugin.
- Added more aggressive sending retry logic for install session package.
- Added additional parameters to `ad_revenue` package payload.

#### Fixed
- Added timeout when reading Google Play Advertising Identifier.

---

### Version 4.20.0 (15th January 2020)
#### Added
- Added external device ID support.

#### Fixed
- Added AndroidX support for example apps.

---
### Version 4.19.1 (13th December 2019)
#### Added
- Added support to Adjust OAID plugin for reading `OAID` identifier with usage of MSA SDK if present in app and supported on device.

---

### Version 4.19.0 (9th December 2019)
#### Added
- Added `disableThirdPartySharing(Context)` method to `Adjust` interface to allow disabling of data sharing with third parties outside of Adjust ecosystem.

---

### Version 4.18.4 (15th November 2019)
#### Fixed
- Fixed occasional crash when attempting to unbind from OAID service (relevant only for OAID plugin users).

---

### Version 4.18.3 (9th October 2019)
#### Fixed
- Fixed issue in v4.18.2 where released JAR in Maven was empty (thanks to @calvarez-ov).

#### Added
- Added Korean localisation for OAID plugin document.

#### Changed
- Updated Gradle tasks because of path changes in Android Studio and Gradle 3.5.1.

---

### Version 4.18.2 (7th October 2019)
⚠️ **Please skip using this version and update SDK to v4.18.3 or higher (release contains empty JAR).**
#### Added
- Added `adjust-android-oaid` plugin and support for reading Huawei Advertising Identifier (OAID).

---

### Version 4.18.1 (20th August 2019)
#### Fixed
- Fixed low vulnerability issue by adding checks for FB app authenticity when requesting `fb_id` (thanks to @StanKocken).

---

### Version 4.18.0 (26th June 2019)
#### Added
- Added `trackAdRevenue(String, JSONObject)` method to `Adjust` interface to allow tracking of ad revenue. With this release added support for `MoPub` ad revenue tracking.

---

### Version 4.17.0 (30th November 2018)
#### Added
- Added `getSdkVersion()` method to `Adjust` interface to obtain current SDK version string.

#### Fixed
- Fixed `R.class` DEX compilation issue when using plugins (https://github.com/adjust/android_sdk/issues/362).

---

### Version 4.16.0 (7th November 2018)
#### Added
- Added `README` localisation in Chinese, Korean and Japanese.
- Added sending of `android_uuid` with each attribution request.
- Added Gradle tasks for usage in Adjust non native SDKs.

#### Changed
- Refactored scheduler.
- Started to catch potential exceptions in case of `checkCallingOrSelfPermission` method call.
- Renamed Android project modules.

---

### Version 4.15.1 (19th September 2018)
#### Changed
- Changed way how `AdjustAttribution` object is being passed to Unity layer.
- Modified paths in Gradle tasks for building JAR files.

---

### Version 4.15.0 (31st August 2018)
#### Added
- Added `setCallbackId` method on `AdjustEvent` object for users to set custom ID on event object which will later be reported in event success/failure callbacks.
- Added `callbackId` member to `AdjustEventSuccess` class.
- Added `callbackId` member to `AdjustEventFailure` class.
- Added support for tracking Facebook Pixel events with Android web view SDK.
- Aligned feature set of Android web view SDK with native Android SDK.
- Added example app which demonstrates how Android web view SDK can be used to track Facebook Pixel events.

#### Changed
- Marked `setReadMobileEquipmentIdentity` method of `AdjustConfig` object as deprecated.
- SDK will now fire attribution request each time upon session tracking finished in case it lacks attribution info.
- Removed reading of `vm_isa` parameter.
- Removed unneccessary reflection calls.

---

### Version 4.14.0 (8th June 2018)
#### Added
- Added `Adjust.appWillOpenUrl(Uri, Context)` method to enable deep link caching.

#### Changed
- Marked `Adjust.appWillOpenUrl(Uri)` method as **deprecated**. Please, use `Adjust.appWillOpenUrl(Uri, Context)` method instead.

---

### Version 4.13.0 (27th April 2018)
#### Added
- Added `Adjust.gdprForgetMe(Context)` method to enable possibility for user to be forgotten in accordance with GDPR law.

---

### Version 4.12.4 (9th March 2018)
#### Changed
- Added additional null checks into `InstallReferrer` `invoke` method.

---

### Version 4.12.3 (7th March 2018)
#### Fixed
- Fixed random `OutOfMemoryError` occurrences when reading/writing referrers array.

---

### Version 4.12.2 (28th February 2018)
#### Changed
- Capturing information about silently ignored runtime exceptions by scheduled executor.
- Send referrer information upon enabling SDK if it was launched as disabled. 

#### Fixed
- Fixed handling of malformed referrer string values.

---

### Version 4.12.1 (31st January 2018)
#### Fixed
- Formatting all strings with US locale.

---

### Version 4.12.0 (13th December 2017)
#### Added
- Added support for new Google referrer API (https://developer.android.com/google/play/installreferrer/library.html).
- Added `Adjust.getAmazonAdId()` method to obtain value of Amazon Advertising Identifier.
- Added possibility to read Mobile Equipment Identity for non Google Play store apps.
- Added usage of app secret in authorization header.
- Added sending of `raw_referrer` parameter in `sdk_click` package.
- Added reading of MCC.
- Added reading of MNC.
- Added reading of network type.
- Added reading of connectivity type.
- Added log messages for saved actions to be done when the SDK starts.

#### Changed
- Not sending `sdk_click` and `sdk_info` packages when SDK is disabled.
- Setting enable/disable or offline/online is now queued.
- Guaranteeing that first package is sent even with event buffering turned ON.
- Not creating first session package if SDK is disabled before first launch.
- Saving referrer in local storage and send it only after first session.
- Saving push token in local storage and send it only after first session.

---

### Version 4.11.4 (5th May 2017)
#### Added
- Added check if `sdk_click` package response contains attribution information.
- Added sending of attributable parameters with every `sdk_click` package.

#### Changed
- Replaced `assert` level logs with `warn` level.

---

### Version 4.11.3 (5th April 2017)
#### Changed
- Removed connection validity checks.
- Refactored networking code.

---

### Version 4.11.2 (22nd March 2017)
#### Added
- Added sending of the app's install time.
- Added sending of the app's update time.
- Added connection validity checks.

#### Changed
- Garanteed that access of `Activity Handler` to internal methods is done through it's executor.
- Updated gradle version.

#### Fixed
- Fixed random occurrence of attribution request being fired before session request.
- Fixed query string parsing.
- Using separate executor in background timer like previously done for foreground timer.

---

### Version 4.11.1 (27th February 2017)
#### Fixed
- Prevented creation of multiple threads (https://github.com/adjust/android_sdk/issues/265).
- Protected `Package Manager` from throwing unexpected exception (https://github.com/adjust/android_sdk/issues/265).

---

### Version 4.11.0 (14th December 2016)
#### Added
- Added sending of Amazon Fire Advertising Identifier.
- Added `adid` field to the attribution callback response.
- Added possibility to set default tracker for the app by adding `adjust_config.properties` file to the `assets` folder of your app. Mostly meant to be used by the `Adjust Store & Pre-install Tracker Tool`.
- Added method `Adjust.getAdid()` to be able to get `adid` value at any time after obtaining it, not only when session/event callbacks have been triggered.
- Added methd `Adjust.getAttribution()` to be able to get current attribution value at any time after obtaining it, not only when attribution callback has been triggered.

#### Changed
- Updated Criteo plugin:
    - Added new partner parameter `user_segment` to be sent in `injectUserSegmentIntoCriteoEvents` (for all Criteo events).
    - Moved `customer_id` to be sent in `injectCustomerIdIntoCriteoEvents` (for all Criteo events).
    - Added new partner parameter `new_customer` to be sent in `injectTransactionConfirmedIntoEvent`.
- Firing attribution request as soon as install has been tracked, regardless of presence of attribution callback implementation in user's app.
- Refactored attribution timer code.
- Updated docs.

#### Fixed
- Now reading push token value from activity state file when sending package.

---

### Version 4.10.4 (22nd November 2016)
#### Changed
- Removed native C++ code for checking CPU architecture.

---

### Version 4.10.3 (18th November 2016)
#### Added
- Added sending of `os_buid` with `BUILD.ID` info.
- Added sending of the Fire Advertising ID and it's tracking enabled/disabled information.
- Added sending of the vm ISA information from native code.

#### Fixed
- Removed unnecessary attribution changed listner check.
- Using reflection to get locale information due to old method deprecation.

#### Changed
- Sending push token with dedicated package called `sdk_info`.
- Refactored `Reflection` class.
- Removed unused response types.

---

### Version 4.10.2 (16th September 2016)
#### Changed
- Removed disconnect after `URLConnection` creation. Allow to do it after response is read.

#### Fixed
- Fixed bug in network communication for some Android API levels from SDK v4.10.1.

---

### Version 4.10.1 (14th September 2016)
**SDK v4.10.1 should not be integrated, since we noticed errors in network communication for some Android API levels.**

#### Fixed
- Setting explicit keep alive time for `ScheduledThreadPoolExecutor` to prevent wrong default of older APIs.

---

### Version 4.10.0 (13th September 2016)
#### Added
- Added support for suppress log level.
- Added possibility to delay first session.
- Added support for session parameters to be sent in every session/event.
- Added possibility to inject custom user agent to each request.
- Added teardown method.
- Added reading of the permissions from the manifest.

#### Changed
- Updated docs.

---

### Version 4.7.0 (4th May 2016)
#### Added
- Added background tracking feature.
- Added deferred deep link callback listener with decision whether deep link should be launched or not.

#### Changed
- Send whole referrer and deep link with sdk_click.
- Send `sdk_click` immediately with a dedicated handler.
- Updated docs.

#### Fixed
- Correct behaviour so the SDK does not start when is put enabled/disabled or online/offline.

---

### Version 4.6.0 (15th March 2016)
#### Added
- Added delegate callbacks for tracked events and sessions.

---

### Version 4.3.0 (11th September 2015)
#### Fixed
- Fixed errors on `pre iOS 8` devices due to accessing `calendarWithIdentifier` method.

---

### Version 4.2.3 (8th February 2016)
#### Fixed
- Fixed Sociomantic plugin.

#### Changed
- Renamed getting of device identifiers.

---

### Version 4.2.2 (1st February 2016)
#### Changed
- Eased access to Google Play Advertising Id.

---

### Version 4.2.1 (8th January 2016)
#### Changed
- Send `api_level` and replace `os_version`.
- Update ProGuard rules to add inner class notation.

---

### Version 4.2.0 (22nd December 2015)
#### Added
- Added gradle to the repository.
- Exposing `setDeviceKnown` method in `AdjustConfig` class.

#### Changed
- Updated different tools versions.
- Explicit cast of read objects.
- Explicit naming of deep link recipient.
- Explicit setting of target package of deep link.

---

### Version 4.1.5 (30th November 2015)
#### Added
- Added pom.xml files for plugins to publish in maven repository.

#### Changed
- Updated Criteo plugin to send deep link.

---

### Version 4.1.4 (16th November 2015)
#### Added
- Added support for Trademob plugin.

---

### Version 4.1.3 (26th October 2015)
#### Fixed
- Catch possible error in reading files, a class cast exception, if trying to read different file than expected.

---

### Version 4.1.2 (28th August 2015)
#### Changed
- Replaced `HttpClient` by `HttpURLConnection`.
- Sending full referrer if it contains an adjust parameter(s).

---

### Version 4.1.1 (29th July 2015)
#### Fixed
- Checking if state is valid to prevent exception.

---

### Version 4.1.0 (17th July 2015)
#### Changed
- Preventing access to invalid state.
- Updated docs.

---

### Version 4.0.9 (30th June 2015)
#### Added
- Added Sociomantic `partner_id`.
- Added reading of responses to click packages.

#### Changed
- Revenue logs match value send.

---

### Version 4.0.8 (10th June 2015)
#### Fixed
- Install referrer does not send the first session.

#### Changed
- Timer is not created every time it starts.
- Hash functions are now accessible for plugins.

---

### Version 4.0.7 (5th June 2015)
#### Added
- Added new `click label` parameter in attribution.
- Added injection of optional parameters in Criteo events.
- Added `partner_id` optional parameter in Criteo.

---

### Version 4.0.6 (8th May 2015)
#### Added
- Added support for muti-process apps.

#### Changed
- Updated Criteo plugin.

---

### Version 4.0.5 (30th April 2015)
#### Changed
- Updated Criteo plugin.
- Changed Google Play Services availability check.

---

### Version 4.0.4 (23rd April 2015)
#### Changed
- Prefixed Sociomantic params to avoid ambiguities.

---

### Version 4.0.3 (20th April 2015)
#### Added
- Added Sociomantic plugin.

#### Changed
- Updated documentation to Google play services v7.

#### Fixed
- Fixed Criteo product string parsing.

---

### Version 4.0.2 (25th March 2015)
#### Changed
- Formatting string using US locale.

---

### Version 4.0.1 (23rd March 2015)
#### Changed
- Updated Criteo Plugin with new events.
- Improved serialization and migration.

---

### Version 4.0.0 (13th March 2015)
#### Added
- Added config objects to launch SDK and track events.
- Added sending of currency with revenue.
- Added partner parameters feature.
- Added offline mode feature.
- Added Criteo plugin that allows to track Criteo type of events.

#### Changed
- Replaced response data delegate with attribution changed delegate.

---

### Version 3.6.2 (22nd December 2014)
#### Changed
- Changed Android SDK target to 21, a.k.a, Lollipop.

---

### Version 3.6.1 (3rd December 2014)
#### Changed
- You can now call the Adjust API with a `Context` instead of an `Activity`.
- Updated pom.xml for Maven.

---

### Version 3.6.0 (3rd December 2014)
#### Added
- Added creation of the plugin mechanism for the SDK.

---

### Version 3.5.0 (25th July 2014)
#### Added
- Added usage of Google Advertisement ID as default device ID. If Google Play Services is present, don't send MAC Address.

#### Changed
- Removed Google Play Services `.jar` file. Only the one provided by the app is needed.

---

### Version 3.4.0 (17th July 2014)
#### Added
- Added opening of the deep link if the server responds with one after the install.

---

### Version 3.3.6 (16th July 2014)
#### Fixed
- Fixed check for malformed app token.
- Fixed bug that didn't handle malformed app token properly.

---

### Version 3.3.5 (1st July 2014)
#### Added
- Added sending of `tracking_enabled` to know if the user opted out of tracking.

#### Changed
- Always sending Google Play Services Advertisement Id.

---

### Version 3.3.4 (25th January 2014)
#### Added
- Added new response data fields for tracker information.

---

### Version 3.3.3 (10th January 2014)
#### Added
- Added Android Studio build via gradle.properties.

#### Changed
- Removed static dependency to Google Play Services library.
- Obtaining Google Advertising ID via reflection.

---

### Version 3.3.2 (8th May 2014)
#### Added
- Added local repository in Maven for Google Play Services dependency.

---

### Version 3.3.1 (29th April 2014)
#### Fixed
- Added Gradle build fix.

---

### Version 3.3.0 (16th April 2014)
#### Added
- Added deep link parameters.

#### Fixed
- Fixed new fields on migrating devices.

---

### Version 3.2.0 (7th April 2014)
#### Added
- Added option to disable and enable the SDK temporarily.
- Added support for Google Play Services Advertising ID.

---

### Version 3.0.0 (24th February 2014)
#### Added
- Added In-App source access.
- Added listener to support In-App source access.

#### Changed
- Renamed `AdjustIo` to `Adjust`.

---

### Version 2.1.6 (13th January 2014)
#### Added
- Added option to disable offline tracking.
- Added `sandbox` environment.
- Added sending of `tracking_enabled` parameter.

---

### Version 2.1.5 (10th January 2014)
#### Fixed
- Fixed bugs introduced in recent refactorings.

---

### Version 2.1.4 (26th November 2013)
#### Added
- Added `PackageQueue` persistence handling of `NullPointerExceptions`.

---

### Version 2.1.3 (21st November 2013)
#### Added
- Added support for SDK wrappers for Unity and Adobe AIR.

#### Fixed
- Fixed a crash that resulted from our changed `ActivityPackage` representation that was introduced in `v2.1.2`.

---

### Version 2.1.2 (15th November 2013)
#### Changed
- Performed big code cleanup.

#### Fixed
- Fixed context crash.

---

### Version 2.1.1 (1st October 2013)
#### Changed
- Updated version in pom.xml file.

---

### Version 2.1.0 (18th September 2013)
#### Added
- Added event buffering feature.
- Added default tracker feature.

---

### Version 2.0.1 (17th August 2012)
#### Fixed
- Fixed memory leaking issue.
- Using `ApplicationContext` instead of `ActivityContext` to avoid leakage.

---

### Version 2.0 (16th July 2013)
#### Added
- Added support for iOS 7.
- Added session aggregation.
- Added meta information for sessions and events.
- Added offline tracking feature.
- Added persistent storage (crash safe).
- Added multi-threading.
- Added migration guide.

---

### Version 1.0.0 (24th October 2012)
#### Added
- Initial release of the adjust SDK for Android.
