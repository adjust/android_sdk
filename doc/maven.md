## Build adjust using Maven

We started using Google Play Services to obtain the advertising ID if the user didn’t opt out.
Because this library is not present in the Maven public repository it's necessary to import it manually.

Add the following command to your maven install build.
```
mvn install:install-file \
-DgroupId=com.google.android.gms \
-DartifactId=google-play-services \
-Dversion=4.3.23 \
-Dpackaging=jar \
-Dfile=libs/google-play-services.jar
```
