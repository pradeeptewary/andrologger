package com.andrologger;
/**
 * DeviceAccountWatcher.java
 *
 * @author Pradeep Tiwari
 */

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import java.util.HashMap;

/** This class detects accounts associated with the device. **/
public class DeviceAccountWatcher {
    // Initialize constants and variables
    public final static String TAG = "DeviceAccountRetriever";

    /**
     * This method identifies device accounts.
     *
     * @param context The application context.
     */
    public static void findAccounts(Context context) {
        // Loop through and log available accounts
        HashMap<String, String> accountMap = new HashMap<String, String>();
        Account[] accounts = AccountManager.get(context.getApplicationContext()).getAccounts();
        for (Account account : accounts)
            accountMap.put(account.type, account.name);
        for (String key : accountMap.keySet()) {
            Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;
            ContentValues values = new ContentValues();
            values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
            values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "Device Account Detected");
            values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
            values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, accountMap.get(key));
            values.put(AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN, key);
            context.getContentResolver().insert(eventsUri, values);
        }
    }
}
