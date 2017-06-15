package com.andrologger;
/**
 * BrowserHistoryWatcher.java
 *
 * @author Pradeep Tiwari
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Browser;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;



/** This class detects browser history and search events. **/
public class BrowserHistoryWatcher extends BroadcastReceiver {
    // Initialize constants and variables
    public static final String TAG = "BrowserWatcher";
    public static long COLLECTION_INTERVAL = 1000 * 60 * 60 * 6;
    private Context context;

    /**
     * This method sets the collection interval.
     *
     * @param interval Collection interval.
     */
    public void setInterval(long browserHistoryInterval) {
        COLLECTION_INTERVAL = browserHistoryInterval;
    }

    /**
     * This method handles the broadcasted alarm intent.
     *
     * @param context The application's context.
     * @param intent The broadcasted intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        // Determine the oldest time from which to retrieve events
        long epochMillis = -1;
        long transferTime = TransferManager.getMostRecentCompletedTransferTime(context);
        if (transferTime == -1) {
            // Use the epoch time at midnight of yesterday if there is no last transfer time
            Log.i(TAG, "Last transfer time not found");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            cal.clear(Calendar.HOUR);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            epochMillis = cal.getTimeInMillis();
        } else
            epochMillis = transferTime * 1000L;
        // Get Visited Websites
        getBrowserHistory(epochMillis);
        // Get User Searches
        getBrowserSearches(epochMillis);
    }

    /**
     * This method retrieves recent browser searches.
     *
     * @param startTime The oldest time to start collecting events.
     */
    private void getBrowserSearches(long startTime) {
        // Initialize events URI
        Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;
        // Query searches database
        String sel = "date >= " + startTime;
        Cursor browserCursor = null;
        try {
            browserCursor = context.getContentResolver().query(Browser.SEARCHES_URI, Browser.SEARCHES_PROJECTION, sel, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Unable to query browsersearches content provider");
            return;
        }
        // Parse query results
        if (browserCursor == null) {
            Log.e(TAG, "Unable to query browsersearches content provider");
            return;
        }
        if (browserCursor.moveToFirst() && browserCursor.getCount() > 0) {
            String search;
            Date date;
            while (!(browserCursor.isAfterLast())) {
                // Get search event details
                search = browserCursor.getString(browserCursor.getColumnIndex("search"));
                date = new Date(browserCursor.getLong(browserCursor.getColumnIndex("date")));
                // Check to see if this search is already in the database
                String[] projection = new String[]{AndroLoggerDatabase.DETECTOR_COLUMN, AndroLoggerDatabase.EVENT_ACTION_COLUMN, AndroLoggerDatabase.EVENT_DATE_COLUMN, AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN};
                String selection = AndroLoggerDatabase.DETECTOR_COLUMN + " = ? AND " + AndroLoggerDatabase.EVENT_ACTION_COLUMN + " = ? AND " + AndroLoggerDatabase.EVENT_DATE_COLUMN + " = ? AND " + AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN + " = ?";
                String[] selectionArgs = new String[]{TAG, "Browser Search", String.valueOf(date.getTime()), search};
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(eventsUri, projection, selection, selectionArgs, null);
                } catch (Exception e) {
                    Log.e(TAG, "Unable to query events table");
                    return;
                }
                // Log search event
                if (cursor == null) {
                    Log.e(TAG, "Unable to query events table");
                    return;
                }
                if (cursor.getCount() == 0) {
                    // Insert new browser search into AndroLogger
                    ContentValues values = new ContentValues();
                    values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
                    values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "Browser Search");
                    values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, date.getTime());
                    values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, search);
                    context.getContentResolver().insert(eventsUri, values);
                    //Log.i(TAG, "Search: "+search+" Date: "+date.toString());
                }
                browserCursor.moveToNext();
            }
        }
    }

    /**
     * This method retrieves the history of URLs visited.
     *
     * @param startTime The oldest time to start collecting events.
     */
    private void getBrowserHistory(long timeStarted) {
        // Initialize AndroLogger events database URI
        Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;
        // Query browser history database
        String[] proj = new String[]{Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL, Browser.BookmarkColumns.DATE};
        String sel = Browser.BookmarkColumns.BOOKMARK + " = 0 and " + Browser.BookmarkColumns.DATE + " >= " + timeStarted; // 0 = history, 1 = bookmark
        Cursor historyCursor = null;
        try {
            historyCursor = context.getContentResolver().query(Browser.BOOKMARKS_URI, proj, sel, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Unable to query the browserhistory content provider");
            return;
        }
        // Parse query results
        if (historyCursor == null) {
            Log.e(TAG, "Unable to query the browserhistory content provider");
            return;
        }
        String title = "";
        String url = "";
        Date date = null;
        if (historyCursor.moveToFirst() && historyCursor.getCount() > 0) {
            while (!(historyCursor.isAfterLast())) {
                // Get website history event
                title = historyCursor.getString(historyCursor.getColumnIndex(Browser.BookmarkColumns.TITLE));
                url = historyCursor.getString(historyCursor.getColumnIndex(Browser.BookmarkColumns.URL));
                date = new Date(historyCursor.getLong(historyCursor.getColumnIndex(Browser.BookmarkColumns.DATE)));
                // Check to see if it's in the database already
                String[] projection = new String[]{AndroLoggerDatabase.DETECTOR_COLUMN, AndroLoggerDatabase.EVENT_ACTION_COLUMN, AndroLoggerDatabase.EVENT_DATE_COLUMN, AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN};
                String selection = AndroLoggerDatabase.DETECTOR_COLUMN + " = ? AND " + AndroLoggerDatabase.EVENT_ACTION_COLUMN + " = ? AND " + AndroLoggerDatabase.EVENT_DATE_COLUMN + " = ? AND " + AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN + " = ?";
                String[] selectionArgs = new String[]{TAG, "Browser Navigation", String.valueOf(date.getTime()), url};
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(eventsUri, projection, selection, selectionArgs, null);
                } catch (Exception e) {
                    Log.e(TAG, "Unable to query events table");
                    return;
                }
                // Log history event
                if (cursor == null) {
                    Log.e(TAG, "Unable to query events table");
                    return;
                }
                if (cursor.getCount() == 0) {
                    // Insert new browser history event into AndroLogger
                    ContentValues values = new ContentValues();
                    values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
                    values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "Browser Navigation");
                    values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, date.getTime());
                    values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, title);
                    values.put(AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN, url);
                    context.getContentResolver().insert(eventsUri, values);
                    //Log.i(TAG,"Title: "+title+" URL: "+url+ " LastVisited: "+date.toString());
                }
                historyCursor.moveToNext();
            }
        }
    }

    /**
     * This method sets a new alarm.
     *
     * @param context
     * @param am
     */
    public void setAlarm(Context context, AlarmManager am) {
        if (COLLECTION_INTERVAL <= 0)
            return;
        Intent historyIntent = new Intent(context, BrowserHistoryWatcher.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, historyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), COLLECTION_INTERVAL, pi);
    }

    /**
     * This method cancels an existing alarm.
     *
     * @param context
     * @param am
     */
    public void cancelAlarm(Context context, AlarmManager am) {
        if (COLLECTION_INTERVAL <= 0)
            return;
        Intent intent = new Intent(context, BrowserHistoryWatcher.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }
}




