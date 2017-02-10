package com.andrologger;
/**
 * AppWatcher.java
 *
 * @author Pradeep Tiwari
 */

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/** This class detects app installs and removals. **/
public class AppWatcher extends BroadcastReceiver {
    // Initialize variables and constants
    public static final String TAG = "AppWatcher";

    /**
     * This method handles the broadcasted intent.
     *
     * @param context The application context.
     * @param intent The broadcasted intent.
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        // Initialize Events Log URI
        Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;
        // Get data from the received intent
        Uri data = intent.getData();
        String pkgName = data.getEncodedSchemeSpecificPart();
        String action = intent.getAction();
        // Log application installs & removals
        ContentValues values;
        if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            values = new ContentValues();
            values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
            values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "Package Installed");
            values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
            values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, pkgName);
            context.getContentResolver().insert(eventsUri, values);
            //Log.i(TAG, "Package Installed: "+pkgName);
        } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            values = new ContentValues();
            values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
            values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "Package Removed");
            values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
            values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, pkgName);
            context.getContentResolver().insert(eventsUri, values);
            //Log.i(TAG, "Package Removed: "+pkgName);
        }
    }
}
