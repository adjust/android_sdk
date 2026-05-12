package com.adjust.sdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

public class StoreInfoUtil {

    public static Boolean getIsSystemApp(final Context context) {
        try {
            ApplicationInfo ai =
                    context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            return ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                    ? Boolean.TRUE
                    : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Boolean getIsUpdatedSystemApp(final Context context) {
        try {
            ApplicationInfo ai =
                    context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            return ((ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)
                    ? Boolean.TRUE
                    : null;
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public static String getStoreIdFromSystem(final Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                InstallSourceInfo installSourceInfo = packageManager.getInstallSourceInfo(packageName);
                return installSourceInfo.getInstallingPackageName();
            } else {
                return packageManager.getInstallerPackageName(packageName);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static AdjustStoreInfo getStoreInfoFromClient(final AdjustConfig adjustConfig, final Context context) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = applicationInfo.metaData;
            if (metaData == null) {
                return adjustConfig.storeInfo;
            }

            String storeName = metaData.getString("ADJUST_STORE_NAME");
            if (storeName == null || storeName.isEmpty()) {
                return adjustConfig.storeInfo;
            }

            String storeAppId = metaData.getString("ADJUST_STORE_APP_ID");

            AdjustStoreInfo storeInfo = new AdjustStoreInfo(storeName);
            storeInfo.setStoreAppId(storeAppId);
            return storeInfo;

        } catch (Exception e) {
            return adjustConfig.storeInfo;
        }
    }

    public static String getInitiatingPackageName(final Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                InstallSourceInfo installSourceInfo = packageManager.getInstallSourceInfo(packageName);
                return installSourceInfo.getInitiatingPackageName();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static String getOriginatingPackageName(final Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                InstallSourceInfo installSourceInfo = packageManager.getInstallSourceInfo(packageName);
                return installSourceInfo.getOriginatingPackageName();
            }
        } catch (Exception e) {
        }
        return null;
    }
}
