//
//  Util.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2012-10-11.
//  Copyright (c) 2012-2014 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import static com.adjust.sdk.Constants.ENCODING;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.LocaleList;

import com.adjust.sdk.scheduler.AsyncTaskExecutor;
import com.adjust.sdk.scheduler.SingleThreadFutureScheduler;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collects utility functions used by Adjust.
 */
public class Util {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'Z";
    private static final String fieldReadErrorMessage = "Unable to read '%s' field in migration device with message (%s)";
    public static final DecimalFormat SecondsDisplayFormat = newLocalDecimalFormat();
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);

    // https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
    private static volatile SingleThreadFutureScheduler playAdIdScheduler = null;

    private static ILogger getLogger() {
        return AdjustFactory.getLogger();
    }

    protected static String createUuid() {
        return UUID.randomUUID().toString();
    }

    private static DecimalFormat newLocalDecimalFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        return new DecimalFormat("0.0", symbols);
    }

    public static String quote(String string) {
        if (string == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(string);
        if (!matcher.find()) {
            return string;
        }

        return Util.formatString("'%s'", string);
    }

    public static Object getAdvertisingInfoObject(final Context context, long timeoutMilli) {
        return runSyncInPlayAdIdSchedulerWithTimeout(context, new Callable<Object>() {
            @Override
            public Object call() {
                try {
                    return Reflection.getAdvertisingInfoObject(context);
                } catch (Exception e) {
                    return null;
                }
            }
        }, timeoutMilli);
    }

    public static String getPlayAdId(final Context context,
                                     final Object advertisingInfoObject,
                                     long timeoutMilli)
    {
        return runSyncInPlayAdIdSchedulerWithTimeout(context, new Callable<String>() {
            @Override
            public String call() {
                return Reflection.getPlayAdId(context, advertisingInfoObject);
            }
        }, timeoutMilli);
    }

    public static Boolean isPlayTrackingEnabled(final Context context,
                                               final Object advertisingInfoObject,
                                               long timeoutMilli)
    {
        return runSyncInPlayAdIdSchedulerWithTimeout(context, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return Reflection.isPlayTrackingEnabled(context, advertisingInfoObject);
            }
        }, timeoutMilli);
    }

    private static <R> R runSyncInPlayAdIdSchedulerWithTimeout(final Context context,
                                                               Callable<R> callable,
                                                               long timeoutMilli)
    {
        if (playAdIdScheduler == null) {
            synchronized (Util.class) {
                if (playAdIdScheduler == null) {
                    playAdIdScheduler = new SingleThreadFutureScheduler("PlayAdIdLibrary", true);
                }
            }
        }

        ScheduledFuture<R> playAdIdFuture = playAdIdScheduler.scheduleFutureWithReturn(callable, 0);

        try {
            return playAdIdFuture.get(timeoutMilli, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
        } catch (InterruptedException e) {
        } catch (TimeoutException e) {
        }

        return null;
    }

    /**
     * Called to get value of Google Play Advertising Identifier.
     *
     * @param context                  Application context
     * @param onGoogleAdIdReadListener Callback to get triggered once identifier is obtained
     */
    public static void getGoogleAdId(final Context context, final OnGoogleAdIdReadListener onGoogleAdIdReadListener) {
        new AsyncTaskExecutor<Context, String>() {
            @Override
            protected String doInBackground(Context... params) {
                ILogger logger = AdjustFactory.getLogger();
                Context innerContext = params[0];
                String innerResult = Util.getGoogleAdId(innerContext);
                logger.debug("GoogleAdId read " + innerResult);
                return innerResult;
            }

            @Override
            protected void onPostExecute(String playAdiId) {
                if (onGoogleAdIdReadListener != null) {
                    onGoogleAdIdReadListener.onGoogleAdIdRead(playAdiId);
                }
            }
        }.execute(context);
    }

    private static String getGoogleAdId(Context context) {
        String googleAdId = null;
        try {
            GooglePlayServicesClient.GooglePlayServicesInfo gpsInfo =
                    GooglePlayServicesClient.getGooglePlayServicesInfo(context,
                            Constants.ONE_SECOND * 11);
            if (gpsInfo != null) {
                googleAdId = gpsInfo.getGpsAdid();
            }
        } catch (Exception e) {
        }
        if (googleAdId == null) {
            Object advertisingInfoObject = Util.getAdvertisingInfoObject(
                    context, Constants.ONE_SECOND * 11);

            if (advertisingInfoObject != null) {
                googleAdId = Util.getPlayAdId(context, advertisingInfoObject, Constants.ONE_SECOND);
            }
        }

        return googleAdId;
    }

    public static String getAndroidId(Context context) {
        return AndroidIdUtil.getAndroidId(context);
    }

    public static <T> T readObject(Context context, String filename, String objectName, Class<T> type) {
        Closeable closable = null;
        T object = null;
        try {
            FileInputStream inputStream = context.openFileInput(filename);
            closable = inputStream;

            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            closable = bufferedStream;

            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);
            closable = objectStream;

            try {
                object = type.cast(objectStream.readObject());
                getLogger().debug("Read %s: %s", objectName, object);
            } catch (ClassNotFoundException e) {
                getLogger().error("Failed to find %s class (%s)", objectName, e.getMessage());
            } catch (ClassCastException e) {
                getLogger().error("Failed to cast %s object (%s)", objectName, e.getMessage());
            } catch (Exception e) {
                getLogger().error("Failed to read %s object (%s)", objectName, e.getMessage());
            }
        } catch (FileNotFoundException e) {
            getLogger().debug("%s file not found", objectName);
        } catch (Exception e) {
            getLogger().error("Failed to open %s file for reading (%s)", objectName, e);
        }
        try {
            if (closable != null) {
                closable.close();
            }
        } catch (Exception e) {
            getLogger().error("Failed to close %s file for reading (%s)", objectName, e);
        }

        return object;
    }

    public static <T> void writeObject(T object, Context context, String filename, String objectName) {
        Closeable closable = null;
        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            closable = outputStream;

            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            closable = bufferedStream;

            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);
            closable = objectStream;

            try {
                objectStream.writeObject(object);

                getLogger().debug("Wrote %s: %s", objectName, object);
            } catch (NotSerializableException e) {
                getLogger().error("Failed to serialize %s", objectName);
            }
        } catch (Exception e) {
            getLogger().error("Failed to open %s for writing (%s)", objectName, e);
        }
        try {
            if (closable != null) {
                closable.close();
            }
        } catch (Exception e) {
            getLogger().error("Failed to close %s file for writing (%s)", objectName, e);
        }
    }

    public static boolean checkPermission(Context context, String permission) {
        try {
            int result = context.checkCallingOrSelfPermission(permission);
            return result == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            getLogger().debug("Unable to check permission '%s' with message (%s)", permission, e.getMessage());
            return false;
        }
    }

    public static String readStringField(ObjectInputStream.GetField fields, String name, String defaultValue) {
        return readObjectField(fields, name, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public static <T> T readObjectField(ObjectInputStream.GetField fields, String name, T defaultValue) {
        try {
            return (T) fields.get(name, defaultValue);
        } catch (Exception e) {
            getLogger().debug(fieldReadErrorMessage, name, e.getMessage());
            return defaultValue;
        }
    }

    public static boolean readBooleanField(ObjectInputStream.GetField fields, String name, boolean defaultValue) {
        try {
            return fields.get(name, defaultValue);
        } catch (Exception e) {
            getLogger().debug(fieldReadErrorMessage, name, e.getMessage());
            return defaultValue;
        }
    }

    public static int readIntField(ObjectInputStream.GetField fields, String name, int defaultValue) {
        try {
            return fields.get(name, defaultValue);
        } catch (Exception e) {
            getLogger().debug(fieldReadErrorMessage, name, e.getMessage());
            return defaultValue;
        }
    }

    public static long readLongField(ObjectInputStream.GetField fields, String name, long defaultValue) {
        try {
            return fields.get(name, defaultValue);
        } catch (Exception e) {
            getLogger().debug(fieldReadErrorMessage, name, e.getMessage());
            return defaultValue;
        }
    }

    public static double readDoubleField(ObjectInputStream.GetField fields, String name, double defaultValue) {
        try {
            return fields.get(name, defaultValue);
        } catch (Exception e) {
            getLogger().debug(fieldReadErrorMessage, name, e.getMessage());
            return defaultValue;
        }
    }

    public static boolean equalObject(Object first, Object second) {
        if (first == null || second == null) {
            return first == null && second == null;
        }
        return first.equals(second);
    }

    public static boolean equalsDouble(Double first, Double second) {
        if (first == null || second == null) {
            return first == null && second == null;
        }
        return Double.doubleToLongBits(first) == Double.doubleToLongBits(second);
    }

    public static boolean equalString(String first, String second) {
        return equalObject(first, second);
    }

    public static boolean equalEnum(Enum first, Enum second) {
        return equalObject(first, second);
    }

    public static boolean equalLong(Long first, Long second) {
        return equalObject(first, second);
    }

    public static boolean equalInt(Integer first, Integer second) {
        return equalObject(first, second);
    }

    public static boolean equalBoolean(Boolean first, Boolean second) {
        return equalObject(first, second);
    }

    public static int hashBoolean(Boolean value, int hashCode) {
        if (value == null) {
            return 37 * hashCode;
        }
        return 37 * hashCode + value.hashCode();
    }

    public static int hashLong(Long value, int hashCode) {
        if (value == null) {
            return 37 * hashCode;
        }
        return 37 * hashCode + value.hashCode();
    }

    public static int hashDouble(Double value, int hashCode) {
        if (value == null) {
            return 37 * hashCode;
        }
        return 37 * hashCode + value.hashCode();
    }

    public static int hashString(String value, int hashCode) {
        if (value == null) {
            return 37 * hashCode;
        }
        return 37 * hashCode + value.hashCode();
    }

    public static int hashEnum(Enum value, int hashCode) {
        if (value == null) {
            return 37 * hashCode;
        }
        return 37 * hashCode + value.hashCode();
    }

    public static int hashObject(Object value, int hashCode) {
        if (value == null) {
            return 37 * hashCode;
        }
        return 37 * hashCode + value.hashCode();
    }

    public static String hash(final String text, final String method) {
        String hashString = null;
        try {
            final byte[] bytes = text.getBytes(ENCODING);
            final MessageDigest mesd = MessageDigest.getInstance(method);
            mesd.update(bytes, 0, bytes.length);
            final byte[] hash = mesd.digest();
            hashString = convertToHex(hash);
        } catch (Exception e) {
        }
        return hashString;
    }

    public static String convertToHex(final byte[] bytes) {
        final BigInteger bigInt = new BigInteger(1, bytes);
        final String formatString = "%0" + (bytes.length << 1) + "x";
        return Util.formatString(formatString, bigInt);
    }

    public static String[] getSupportedAbis() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Build.SUPPORTED_ABIS;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static String getCpuAbi() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return Build.CPU_ABI;
        }
        return null;
    }

    public static String getReasonString(String message, Throwable throwable) {
        if (throwable != null) {
            return Util.formatString("%s: %s", message, throwable);
        } else {
            return Util.formatString("%s", message);
        }
    }

    public static long getWaitingTime(int retries, BackoffStrategy backoffStrategy) {
        if (retries < backoffStrategy.minRetries) {
            return 0;
        }
        // start with expon 0
        int expon = retries - backoffStrategy.minRetries;
        // get the exponential Time from the power of 2: 1, 2, 4, 8, 16, ... * times the multiplier
        long exponentialTime = ((long) Math.pow(2, expon)) * backoffStrategy.milliSecondMultiplier;
        // limit the maximum allowed time to wait
        long ceilingTime = Math.min(exponentialTime, backoffStrategy.maxWait);
        // get the random range
        double randomDouble = randomInRange(backoffStrategy.minRange, backoffStrategy.maxRange);
        // apply jitter factor
        double waitingTime =  ceilingTime * randomDouble;
        return (long)waitingTime;
    }

    private static double randomInRange(double minRange, double maxRange) {
        Random random = new Random();
        double range = maxRange - minRange;
        double scaled = random.nextDouble() * range;
        double shifted = scaled + minRange;
        return shifted;
    }

    public static boolean isValidParameter(String attribute, String attributeType, String parameterName) {
        if (attribute == null) {
            getLogger().error("%s parameter %s is missing", parameterName, attributeType);
            return false;
        }
        if (attribute.equals("")) {
            getLogger().error("%s parameter %s is empty", parameterName, attributeType);
            return false;
        }

        return true;
    }

    public static boolean isAdjustUninstallDetectionPayload(Map<String, String> payload) {
        if (payload == null) {
            return false;
        }
        return payload.size() == 1 &&
          Objects.equals(payload.get(Constants.FCM_PAYLOAD_KEY), Constants.FCM_PAYLOAD_VALUE);
    }

    public static Map<String, String> mergeParameters(Map<String, String> target,
                                                      Map<String, String> source,
                                                      String parameterName) {
        if (target == null) {
            return source;
        }
        if (source == null) {
            return target;
        }
        Map<String, String> mergedParameters = new HashMap<String, String>(target);
        ILogger logger = getLogger();
        for (Map.Entry<String, String> parameterSourceEntry : source.entrySet()) {
            String oldValue = mergedParameters.put(parameterSourceEntry.getKey(), parameterSourceEntry.getValue());
            if (oldValue != null) {
                logger.warn("Key %s with value %s from %s parameter was replaced by value %s",
                        parameterSourceEntry.getKey(),
                        oldValue,
                        parameterName,
                        parameterSourceEntry.getValue());
            }
        }
        return mergedParameters;
    }

    @SuppressWarnings("deprecation")
    public static Locale getLocale(Configuration configuration) {
        // Configuration.getLocales() added as of API 24.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localesList = configuration.getLocales();
            if (localesList != null && !localesList.isEmpty()) {
                return localesList.get(0);
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return configuration.locale;
        }
        return null;
    }

    public static String formatString(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }

    public static boolean hasRootCause(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string

        return sStackTrace.contains("Caused by:");
    }

    public static String getRootCause(Exception ex) {
        if (!hasRootCause(ex)) {
            return null;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string

        int startOccuranceOfRootCause = sStackTrace.indexOf("Caused by:");
        int endOccuranceOfRootCause = sStackTrace.indexOf("\n", startOccuranceOfRootCause);
        return sStackTrace.substring(startOccuranceOfRootCause, endOccuranceOfRootCause);
    }

    private static String getSdkPrefix(final String clientSdk) {
        if (clientSdk == null) {
            return null;
        }
        if (!clientSdk.contains("@")) {
            return null;
        }

        String[] splitted = clientSdk.split("@");
        if (splitted == null) {
            return null;
        }
        if (splitted.length != 2) {
            return null;
        }

        return splitted[0];
    }

    public static String getSdkPrefixPlatform(final String clientSdk) {
        String sdkPrefix = getSdkPrefix(clientSdk);
        if (sdkPrefix == null) {
            return null;
        }

        String[] splitted = sdkPrefix.split("\\d+", 2);
        if (splitted == null) {
            return null;
        }
        if (splitted.length == 0) {
            return null;
        }

        return splitted[0];
    }

    public static boolean isUrlFilteredOut(Uri url) {
        if (url == null) {
            return true;
        }

        String urlString = url.toString();

        if (urlString == null || urlString.length() == 0) {
            return true;
        }

        // Url with FB credentials to be filtered out
        if (urlString.matches(Constants.FB_AUTH_REGEX)) {
            return true;
        }

        return false;
    }

    public static String getSdkVersion() {
        return Constants.CLIENT_SDK;
    }

    public static boolean resolveContentProvider(final Context applicationContext,
                                                 final String authority) {
        try {
            return (applicationContext.getPackageManager()
                    .resolveContentProvider(authority, 0) != null);

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isEqualReferrerDetails(final ReferrerDetails referrerDetails,
                                                 final String referrerApi,
                                                 final ActivityState activityState) {
        if (referrerApi.equals(Constants.REFERRER_API_GOOGLE)) {
            return isEqualGoogleReferrerDetails(referrerDetails, activityState);
        } else if (referrerApi.equals(Constants.REFERRER_API_HUAWEI_ADS)) {
            return isEqualHuaweiReferrerAdsDetails(referrerDetails, activityState);
        } else if (referrerApi.equals(Constants.REFERRER_API_HUAWEI_APP_GALLERY)) {
            return isEqualHuaweiReferrerAppGalleryDetails(referrerDetails, activityState);
        } else if (referrerApi.equals(Constants.REFERRER_API_SAMSUNG)) {
            return isEqualSamsungReferrerDetails(referrerDetails, activityState);
        } else if (referrerApi.equals(Constants.REFERRER_API_XIAOMI)) {
            return isEqualXiaomiReferrerDetails(referrerDetails, activityState);
        } else if (referrerApi.equals(Constants.REFERRER_API_VIVO)) {
            return isEqualVivoReferrerDetails(referrerDetails, activityState);
        } else if (referrerApi.equals(Constants.REFERRER_API_META)) {
            return isEqualMetaReferrerDetails(referrerDetails, activityState);
        }

        return false;
    }

    public static boolean canReadPlayIds(final AdjustConfig adjustConfig) {
        return !adjustConfig.coppaComplianceEnabled && !adjustConfig.playStoreKidsComplianceEnabled;
    }

    public static boolean canReadNonPlayIds(final AdjustConfig adjustConfig) {
        return !adjustConfig.coppaComplianceEnabled && !adjustConfig.playStoreKidsComplianceEnabled;
    }

    public static boolean isGooglePlayGamesForPC(final Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature("com.google.android.play.feature.HPE_EXPERIENCE");
    }

    private static boolean isEqualGoogleReferrerDetails(final ReferrerDetails referrerDetails,
                                                       final ActivityState activityState) {
        return referrerDetails.referrerClickTimestampSeconds == activityState.clickTime
                && referrerDetails.installBeginTimestampSeconds == activityState.installBegin
                && referrerDetails.referrerClickTimestampServerSeconds == activityState.clickTimeServer
                && referrerDetails.installBeginTimestampServerSeconds == activityState.installBeginServer
                && Util.equalString(referrerDetails.installReferrer, activityState.installReferrer)
                && Util.equalString(referrerDetails.installVersion, activityState.installVersion)
                && Util.equalBoolean(referrerDetails.googlePlayInstant, activityState.googlePlayInstant) ;
    }

    private static boolean isEqualHuaweiReferrerAdsDetails(final ReferrerDetails referrerDetails,
                                                           final ActivityState activityState) {
        return referrerDetails.referrerClickTimestampSeconds == activityState.clickTimeHuawei
                && referrerDetails.installBeginTimestampSeconds == activityState.installBeginHuawei
                && Util.equalString(referrerDetails.installReferrer, activityState.installReferrerHuawei);
    }

    private static boolean isEqualHuaweiReferrerAppGalleryDetails(final ReferrerDetails referrerDetails,
                                                                  final ActivityState activityState) {
        return referrerDetails.referrerClickTimestampSeconds == activityState.clickTimeHuawei
               && referrerDetails.installBeginTimestampSeconds == activityState.installBeginHuawei
               && Util.equalString(referrerDetails.installReferrer, activityState.installReferrerHuaweiAppGallery);
    }

    private static boolean isEqualSamsungReferrerDetails(final ReferrerDetails referrerDetails,
                                                         final ActivityState activityState) {
        return referrerDetails.referrerClickTimestampSeconds == activityState.clickTimeSamsung
               && referrerDetails.installBeginTimestampSeconds == activityState.installBeginSamsung
               && Util.equalString(referrerDetails.installReferrer, activityState.installReferrerSamsung);
    }

    private static boolean isEqualXiaomiReferrerDetails(final ReferrerDetails referrerDetails,
                                                        final ActivityState activityState) {
        return referrerDetails.referrerClickTimestampSeconds == activityState.clickTimeXiaomi
               && referrerDetails.installBeginTimestampSeconds == activityState.installBeginXiaomi
               && referrerDetails.referrerClickTimestampServerSeconds == activityState.clickTimeServerXiaomi
               && referrerDetails.installBeginTimestampServerSeconds == activityState.installBeginServerXiaomi
               && Util.equalString(referrerDetails.installReferrer, activityState.installReferrerXiaomi)
               && Util.equalString(referrerDetails.installVersion, activityState.installVersionXiaomi);
    }

    private static boolean isEqualVivoReferrerDetails(final ReferrerDetails referrerDetails,
                                                        final ActivityState activityState) {
        return referrerDetails.referrerClickTimestampSeconds == activityState.clickTimeVivo
               && referrerDetails.installBeginTimestampSeconds == activityState.installBeginVivo
               && Util.equalString(referrerDetails.installReferrer, activityState.installReferrerVivo)
               && Util.equalString(referrerDetails.installVersion, activityState.installVersionVivo);
    }

    private static boolean isEqualMetaReferrerDetails(final ReferrerDetails referrerDetails,
                                                      final ActivityState activityState) {
        return referrerDetails.referrerClickTimestampSeconds == activityState.clickTimeMeta
                && Util.equalString(referrerDetails.installReferrer, activityState.installReferrerMeta)
                && Util.equalBoolean(referrerDetails.isClick, activityState.isClickMeta);
    }

    public static boolean isEnabledFromActivityStateFile(final Context context) {
        ActivityState activityState = Util.readObject(
                context,
                Constants.ACTIVITY_STATE_FILENAME,
                "Activity state",
                ActivityState.class);
        if (activityState == null) {
            return true;
        } else {
            return activityState.enabled;
        }
    }

    public static AdjustAttribution attributionFromJson(final JSONObject jsonObject,
                                                        final String sdkPlatform) {
        if (jsonObject == null) {
            return null;
        }

        AdjustAttribution attribution = new AdjustAttribution();

        if ("unity".equals(sdkPlatform)) {
            // Unity platform.
            attribution.trackerToken = jsonObject.optString("tracker_token", "");
            attribution.trackerName = jsonObject.optString("tracker_name", "");
            attribution.network = jsonObject.optString("network", "");
            attribution.campaign = jsonObject.optString("campaign", "");
            attribution.adgroup = jsonObject.optString("adgroup", "");
            attribution.creative = jsonObject.optString("creative", "");
            attribution.clickLabel = jsonObject.optString("click_label", "");
            attribution.costType = jsonObject.optString("cost_type", "");
            attribution.costAmount = jsonObject.optDouble("cost_amount", 0);
            attribution.costCurrency = jsonObject.optString("cost_currency", "");
            attribution.fbInstallReferrer = jsonObject.optString("fb_install_referrer", "");
        } else {
            // Rest of all platforms.
            attribution.trackerToken = jsonObject.optString("tracker_token");
            attribution.trackerName = jsonObject.optString("tracker_name");
            attribution.network = jsonObject.optString("network");
            attribution.campaign = jsonObject.optString("campaign");
            attribution.adgroup = jsonObject.optString("adgroup");
            attribution.creative = jsonObject.optString("creative");
            attribution.clickLabel = jsonObject.optString("click_label");
            attribution.costType = jsonObject.optString("cost_type");
            attribution.costAmount = jsonObject.optDouble("cost_amount");
            attribution.costCurrency = jsonObject.optString("cost_currency");
            attribution.fbInstallReferrer = jsonObject.optString("fb_install_referrer");
        }

        return attribution;
    }
}