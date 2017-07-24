### Version 4.12.0 (Xth July 2017)
#### Added
- Don't send `sdk_click` and `sdk_info` packages when disabled
- Set external device id

#### Changed
- Setting enable/disable or offline/online is now queued
- Guarantee that first package is send even with buffering
- Disable before starting the sdk does not create and send first session package
- Reload reading device ids for every package, not just at beginning
- Save referrer in local storage and send it only after first session

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
