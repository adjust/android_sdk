# Google Play Services

We access the Google Play Services (GPS) to get the advertising ID if the user didn’t opt out. 
To know more about the advertising ID check the [Android documentation][doc]

We have a private jar of GPS, so the app using adjust SDK is not forced to use GPS as well. 
You can remove the GPS jar if you want to save space or to link with your own GPS jar. 

To link to your own, first delete our GPS jar located at `libs/google-play-services.jar`. 
Then link the adjust SDK project to your GPS jar.

[doc]:https://developer.android.com/google/play-services/id.html
