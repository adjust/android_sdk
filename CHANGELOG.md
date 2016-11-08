### Version 4.10.2 (16th September 2016)
#### Changed
Remove disconnect after URLConnection creation. Allow to do it after response is read

---

### Version 4.10.1 (14th September 2016)
#### Added
- Set explicit keep alive time for ScheduledThreadPoolExecutor to prevent wrong default of older API's

---

### Version 4.10.0 (13th September 2016)
#### Added
- Support for Suppress log level
- Allow to delay the start of the first session
- Support for session parameters to be send in every session/event:
- Callback parameters
- Partner parameters
- Inject User-agent of each request
- Teardown
- Reading permission in the manifest

---

### Version 4.7.0 (4th May 2016)
#### Added
- Send in the background feature
- Deeplink launch callback listener with decision to launch
- Correct behaviour so the SDK does not start when is put enabled/disabled or online/offline
- Send all referrer and deeplink with sdk_click
- Send sdk_click more immediately with a dedicated handler

---

### Version 4.3.0 (11th September 2015)
#### Fixed
- Fixed errors on `pre iOS 8` due to accessing `calendarWithIdentifier` method.

---

### Version 4.6.0 (15th March 2016)
#### Added
- Delegate callbacks for tracked events and sessions.

---

### Version 4.2.3 (8th February 2016)
#### Added
- Fix Sociomantic plugin
- Rename get device ids

---

### Version 4.2.2 (1st February 2016)
#### Added
- Easy access to Google Play Ad Id

---

### Version 4.2.1 (8th January 2016)
#### Changed
- Send api level and replace os version
- Update proguard rules to add inner class notation

---

### Version 4.2.0 (22nd December 2015)
#### Added
- Add gradle to the repository.
- Update different tools versions.
- Explicit cast of read objects.
- Explicitly naming of deeplink recipient.
- Explicit setting of target package of deeplink.
- Exposing setDeviceKnown method in AdjustConfig class.

---

### Version 4.1.5 (30th November 2015)
#### Added
- Update criteo plugin to send deeplink
- Create pom files for plugins to publish in maven repository

---

### Version 4.1.4 (16th November 2015)
#### Added
- Adds support for Trademob plugin. Follow the plugin [guide](doc/trademob_plugin.md)

---

### Version 4.1.3 (26th October 2015)
#### Added
- Catch possible error in reading files, a Class cast exception, if trying to read different file than expected

---

### Version 4.1.2 (28th August 2015)
#### Added
- HttpClient replaced by HttpURLConnection
- Send full referrer if it contains an adjust parameter


---

### Version 4.1.1 (29th July 2015)
#### Added
- Check if state is valid to prevent exception


---

### Version 4.1.0 (17th July 2015)
#### Added
- Preventing access to invalid state
- Doc update


---

### Version 4.0.9 (30th June 2015)
#### Added
- Add sociomantic partner id
- Read response of click packages
- Revenue logs match value send

---

### Version 4.0.8 (10th June 2015)
#### Added
- Install referrer does not send the first session
- Timer is not created every time it starts
- Hash functions accessible for plugins

---

### Version 4.0.7 (5th June 2015)
#### Added
- New click label parameter in attribution
- Inject optional parameters in criteo events
- Add partner id optional parameter in criteo

---

### Version 4.0.6 (8th May 2015)
#### Fixed
- Support for muti-process apps
- Criteo plugin update

---

### Version 4.0.5 (30th April 2015)
#### Added
- Criteo plugin update
- Google Play Services availability check changed

---

### Version 4.0.4 (23rd April 2015)
#### Changed
- Prefix sociomantic params to avoid ambiguities


---

### Version 4.0.3 (20th April 2015)
#### Added
- Added Sociomantic plugin
- Fix Criteo product string parsing
- Update documentation to Google play services v7

---

### Version 4.0.2 (25th March 2015)
#### Fixed
- String formatting using US locale.
- Closes issue #102 and PR #100 

---

### Version 4.0.1 (23rd March 2015)
#### Fixed
- Update Criteo Plugin with new events
- See doc for more information.
- Improved serialization and migration.

---

### Version 4.0.0 (13th March 2015)
#### Added
- Replaced Response Data delegate with Attribution changed delegate
- Config objects to launch SDK and track events
- Send currency with revenue
- Add Partner parameters
- Offline mode
- Criteo plugin allows to track Criteo type of events
- See [doc](https://github.com/adjust/android_sdk/blob/master/doc/criteo_plugin.md) for more information.

---

### Version 3.6.2 (22nd December 2014)
#### Changed
- Change Android SDK target to 21, a.k.a, Lollipop


---

### Version 3.6.1 (3rd December 2014)
#### Fixed
- You can now call the Adjust api with a Context instead of an Activity.
Updated pom.xml for maven

---

### Version 3.6.0 (3rd December 2014)
#### Added
- Creation of the plugin mechanism for the SDK.


---

### Version 3.5.0 (25th July 2014)
#### Added
- Use Google advertisement ID as default device ID. If Google Play Services is present, don't send MAC Address.
- Remove Google Play Services jar. Only the one provided by the app is needed.

---

### Version 3.4.0 (17th July 2014)
#### Added
- Opens deeplink if the server responds with one after the install


---

### Version 3.3.6 (16th July 2014)
#### Changed
- Fix check malformed app token
- Fixed bug that didn't handle malformed app token properly


---

### Version 3.3.5 (1st July 2014)
#### Added
- Send tracking_enabled to know if the user opt-out of tracking
- Always sending Google Play Services Advertisement Id

[Migration Guide](https://github.com/adjust/adjust_android_sdk/blob/master/doc/migrate.md).

---

### Version 3.3.4 (25th January 2014)
#### Added
- New response data fields for tracker information

---

### Version 3.3.3 (10th January 2014)
#### Added
- Removed static dependency to Google Play
- Android Studio build via Gradle.properties
- Google Advertising ID obtained via reflection

[Migration Guide](https://github.com/adjust/adjust_android_sdk/blob/master/doc/migrate.md).

---

### Version 3.3.2 (8th May 2014)
#### Added
- Using local repository in Maven for Google Play Services dependency.
- Instructions at [Maven Guide](https://github.com/adjust/adjust_android_sdk/blob/master/doc/maven.md).

[Migration Guide](https://github.com/adjust/adjust_android_sdk/blob/master/doc/migrate.md).

---

### Version 3.3.1 (29th April 2014)
#### Added
- Gradle build fix

---

### Version 3.3.0 (16th April 2014)
#### Added
- Add deep link parameters
- Fix new fields on migrating devices

[Migration Guide](https://github.com/adjust/adjust_android_sdk/blob/master/doc/migrate.md).

---

### Version 3.2.0 (7th April 2014)
#### Added
- Add option to disable and enable the SDK temporarily.
- Added support for Google Play Services Advertising ID

[Migration Guide](https://github.com/adjust/adjust_android_sdk/blob/master/doc/migrate.md).

---

### Version 3.0.0 (24th February 2014)
#### Added
- In-App Source Access
- Add listener to support in-app source access.

#### Changed
- We renamed `AdjustIo` to `Adjust`. 

Please refer to the [migration guide](https://github.com/adjust/adjust_android_sdk/blob/master/doc/migrate.md) to update your projects.

---

### Version 2.1.6 (13th January 2014)
#### Added
- Add option to disable offline tracking
- Added event buffering feature.
- Added `sandbox` environment.
- Added sending of `tracking_enabled` parameter.

---

### Version 2.1.5 (10th January 2014)
#### Fixed
Fix bugs introduced in recent refactorings.

---

### Version 2.1.4 (26th November 2013)
#### Added
- Handle PackageQueue persistence NullPointerExceptions

---

### Version 2.1.3 (21st November 2013)
#### Added
Support for SDK wrappers for Unity and Adobe Air
Also fixes a crash that resulted from our changed ActivityPackage representation that was introduced in `v2.1.2`.

[Migration Guide](https://github.com/adjust/adjust_android_sdk/blob/master/doc/migrate.md).

---

### Version 2.1.2 (15th November 2013)
#### Added
- Big cleanup, fix context crash

---

### Version 2.1.1 (1st October 2013)
#### Added
- Update version in pom.xml

Check out our [migration guide](https://github.com/adjust/adjust_android_sdk/blob/master/doc/migrate.md).

---

### Version 2.1.0 (18th September 2013)
#### Added
- Buffered, sandbox, tracker 
- event buffering
- sandbox environment
- default tracker

Check out our [migration guide](https://github.com/adjust/adjust_android_sdk/blob/master/doc/migrate.md).

---

### Version 2.0.1 (17th August 2012)
#### Changed
- Fix memory leak
- Use `ApplicationContext` instead of `ActivityContext` to avoid leakage.

Check out our [migration guide](https://github.com/adjust/adjust_android_sdk/blob/master/doc/migrate.md) to upgrade from `v1.x`.

---

### Version 2.0 (16th July 2013)
#### Changed
- Asynchronous, persistent, aggregated
- support for iOS7
- session aggregation
- meta information for sessions and events
- offline tracking
- persistent storage (crash safe)
- multi threaded
- Add migration guide

---

### Version 1.0.0 (24th October 2012)
#### Added
- Initial release of the adjust SDK for android.
