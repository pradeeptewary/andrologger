package com.andrologger;
/**
 * ScreenWatcher.java
 *
 * @author Pradeep Tiwari
 */

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/** This class detects changes to the screen's lock status (e.g., locked, unlocked, off). **/
public class ScreenWatcher extends BroadcastReceiver {
    // Initialize constants and variables
    public static final String TAG = "ScreenWatcher";

    /**
     * This method handles a broadcasted intent.
     *
     * @param context The application context.
     * @param intent The broadcasted intent.
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        // Get screen status
        String action = intent.getAction();
        // Log event
        Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;
        ContentValues values;
        if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            values = new ContentValues();
            values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
            values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "Screen Off");
            values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
            context.getContentResolver().insert(eventsUri, values);
            //Log.i(TAG, "Screen Off");
        } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
            values = new ContentValues();
            values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
            values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "Screen Locked");
            values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
            context.getContentResolver().insert(eventsUri, values);
            //Log.i(TAG, "Screen Locked");
        } else if (action.equals(Intent.ACTION_USER_PRESENT)) {
            values = new ContentValues();
            values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
            values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "Screen Unlocked");
            values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
            context.getContentResolver().insert(eventsUri, values);
            //Log.i(TAG, "Screen Unlocked");
        }
    }
}
