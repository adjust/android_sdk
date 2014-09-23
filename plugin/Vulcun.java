package com.adjust.sdk.plugin;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Patterns;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.Logger;
import com.adjust.sdk.Util;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by pfms on 17/09/14.
 */
public class Vulcun implements Plugin {

    @Override
    public Map.Entry<String, String> getParameter(Context context) {
        if (context == null) {
            return null;
        }

        Logger logger = AdjustFactory.getLogger();

        if (!(context.checkCallingOrSelfPermission(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED)) {
            logger.error("Permission needed to get email: GET_ACCOUNTS");
            return null;
        }

        String primaryEmail = getPrimaryEmail(context);
        if (primaryEmail == null) {
            return null;
        }
        String salt = getSalt(context, logger);
        String sha1_email = Util.sha1(primaryEmail + salt);

        if (sha1_email == null) {
            return null;
        }
        MapEntry<String, String> mapEntry = new MapEntry<String, String>("vulcun_sha1", sha1_email);
        return mapEntry;
    }

    private String getPrimaryEmail(Context context) {
        Account[] accounts = AccountManager.get(context).getAccounts();
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String email = account.name;
                return email;
            }
        }
        return null;
    }

    private String getSalt(Context context, Logger logger) {
        String defaultSalt = "";
        Bundle bundle = Util.getApplicationBundle(context, logger);
        if (null == bundle) {
            return defaultSalt;
        }
        String readSalt = bundle.getString("AdjustVulcunSalt");

        if (readSalt == null) {
            return defaultSalt;
        } else {
            return readSalt;
        }
    }
}
