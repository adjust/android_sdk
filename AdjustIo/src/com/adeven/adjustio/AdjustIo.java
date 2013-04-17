//
//  AdjustIo.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 11.10.12.
//  Copyright (c) 2012 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import android.content.Context;
import java.util.Map;

public class AdjustIo {

  private static final String AMOUNT = "amount";
  private static final String ANDROID_ID = "android_id";
  private static final String APP_ID = "app_id";
  private static final String EVENT_ID = "event_id";
  private static final String ID = "id";
  private static final String MAC = "mac";
  private static final String PARAMS = "params";

  private static RequestThread requestThread;

  // Tell AdjustIo that the application did launch. This is required to
  // initialize AdjustIo. Call this in the onCreate method of your launch
  // activity.
  public static void appDidLaunch(Context context) {
    if (!Util.checkPermissions(context)) {
      return;
    }

    String macAddress = Util.getMacAddress(context);

    appId = context.getPackageName();
    macSha1 = Util.sha1(macAddress);
    macShort = macAddress.replaceAll(":", "");
    userAgent = Util.getUserAgent(context);
    androidId = Util.getAndroidId(context);
    attributionId = Util.getAttributionId(context);

    trackSessionStart();
  }

  // Track any kind of event. You can assign a callback url to the event which
  // will get called every time the event is reported. You can also provide
  // parameters that will be forwarded to these callbacks.
  public static void trackEvent(String eventId) {
    trackEvent(eventId, null);
  }

  public static void trackEvent(String eventId, Map<String, String> parameters) {
    String paramString = Util.getBase64EncodedParameters(parameters);

    TrackingInformation trackingInformation = new TrackingInformation.Builder().setPath("/event")
      .setSuccessMessage("Tracked event " + eventId + ".")
      .setFailureMessage("Failed to track event " + eventId + ".")
      .setUserAgent(userAgent)
      .setTrackingParameters(ID, eventId, APP_ID, appId, MAC, macShort, ANDROID_ID, androidId, PARAMS, paramString)
      .build();
    getRequestThread().track(trackingInformation);
  }

  // Tell AdjustIo that the current user generated some revenue. The amount is
  // measured in cents and rounded to on digit after the decimal point. If you
  // want to differentiate between various types of revenues you can do so by
  // providing different eventIds. If your revenue events have callbacks, you
  // can also pass in parameters that will be forwarded to your server.
  public static void trackRevenue(float amountInCents) {
    AdjustIo.trackRevenue(amountInCents, null);
  }

  public static void trackRevenue(float amountInCents, String eventId) {
    AdjustIo.trackRevenue(amountInCents, eventId, null);
  }

  public static void trackRevenue(float amountInCents, String eventId, Map<String, String> parameters) {
    int amountInMillis = Math.round(10 * amountInCents);
    String amount = Integer.toString(amountInMillis);
    String paramString = Util.getBase64EncodedParameters(parameters);

    TrackingInformation trackingInformation = new TrackingInformation.Builder().setPath("/revenue")
      .setSuccessMessage("Tracked revenue.")
      .setFailureMessage("Failed to track revenue.")
      .setUserAgent(userAgent)
      .setTrackingParameters(APP_ID, appId, MAC, macShort, ANDROID_ID, androidId, AMOUNT, amount, EVENT_ID, eventId,
        PARAMS, paramString)
      .build();
    getRequestThread().track(trackingInformation);
  }

  // This line marks the end of the public interface.

  private static String appId;
  private static String macSha1;
  private static String macShort;
  private static String userAgent;
  private static String androidId;
  private static String attributionId;

  private static void trackSessionStart() {
    TrackingInformation trackingInformation = new TrackingInformation.Builder().setPath("/startup")
      .setSuccessMessage("Tracked session start.")
      .setFailureMessage("Failed to track session start.")
      .setUserAgent(userAgent)
      .setTrackingParameters(APP_ID, appId, MAC, macShort, "mac_sha1", macSha1, ANDROID_ID, androidId,
        "fb_id", attributionId)
      .build();
    getRequestThread().track(trackingInformation);
  }

  private static void trackSessionEnd() {
    TrackingInformation trackingInformation = new TrackingInformation.Builder().setPath("/shutdown")
      .setSuccessMessage("Tracked session end.")
      .setFailureMessage("Failed to track session end.")
      .setUserAgent(userAgent)
      .setTrackingParameters(APP_ID, appId, MAC, macShort, ANDROID_ID, androidId)
      .build();
    getRequestThread().track(trackingInformation);
  }

  private static RequestThread getRequestThread() {
    if (null == requestThread) {
      requestThread = new RequestThread();
    }
    return requestThread;
  }
}
