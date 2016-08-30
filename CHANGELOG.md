### Version 4.9.0 ( 2016)
#### Added
 - Support for `Supress` log level
 - Allow to delay the start of the first session
 - Support for session parameters to be send in every session/event:
  - External device id
  - Callback parameters
  - Partner parameters
 - Inject User-agent of each request
 - Teardown
 - Reading permission in the manifest

#### Changed
 - Update target api level to 24
 - Replaced handler threads with custom sheduled executor
 - Normalize properties attributes
 - Naming standard of background blocks
 - Use of weakself strongself pattern for background blocks
 - Open defferred deeplink from the attribution response
 - Log level logic moved to config object

#### Fixed
 - Read files only in the background
 - Allow foreground/background timer to work in offline mode
 - Use `synchronized` blocks to prevent write deadlock/contention
 - Don't create/use background timer if the option is not configured
 - Replace strong references with weak when possible 

---
