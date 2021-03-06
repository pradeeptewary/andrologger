package com.andrologger;
/**
 * CalendarWatcher.java
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
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/** This class detects added calendar events. **/
public class CalendarWatcher extends BroadcastReceiver {
    // Initialize constants and variables
    public static final String TAG = "CalendarWatcher";
    public static long COLLECTION_INTERVAL = 1000 * 60 * 60 * 12;
    private Context context;

    /**
     * This method sets the collection interval.
     *
     * @param interval Collection interval.
     */
    public void setInterval(long calendarInterval) {
        COLLECTION_INTERVAL = calendarInterval;
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
        long epochMillis = -1;
        long transferTime = TransferManager.getMostRecentCompletedTransferTime(context);
        if (transferTime == -1) {
            // Find epoch time at midnight of yesterday
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            cal.clear(Calendar.HOUR);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            epochMillis = cal.getTimeInMillis();
        } else
            epochMillis = transferTime * 1000L;
        // Find calendar events since the given time
        getCalendarHistory(epochMillis);
    }

    /**
     * This method retrieves and logs calendar events.
     *
     * @param epochMillis The oldest point in time from which to retrieve events.
     */
    private void getCalendarHistory(long epochMillis) {
        // Initialize calendar content provider URI
        Uri calUri = Uri.parse("content://com.android.calendar/event_entities");
        // Query calendar for events since the given time
        String[] calProj = new String[]{"dtstart", "title", "_id"};
        String calSel = "dtstart >= " + epochMillis;
        Cursor calCursor = null;
        try {
            calCursor = context.getContentResolver().query(calUri, calProj, calSel, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Unable to query " + calUri + ": " + e.getMessage());
            return;
        }
        // Parse query results
        if (calCursor == null) {
            Log.e(TAG, "Unable to query " + calUri);
            return;
        }
        Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;
        if (calCursor.moveToFirst() && calCursor.getCount() > 0) {
            String title;
            Date date;
            long id;
            while (!(calCursor.isAfterLast())) {
                // Get event details
                title = calCursor.getString(calCursor.getColumnIndex("title"));
                date = new Date(calCursor.getLong(calCursor.getColumnIndex("dtstart")));
                id = calCursor.getLong(calCursor.getColumnIndex("_id"));
                // Check to see if this event is already in the andrologger calendar database
                String[] projection = new String[]{AndroLoggerProvider.Calendar.ID};
                String selection = "event_id = " + String.valueOf(id);
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(AndroLoggerProvider.Calendar.CONTENT_URI, projection, selection, null, null);
                } catch (Exception e) {
                    Log.e(TAG, "Error querying calendar table in results.db: " + e.getMessage());
                    return;
                }
                if (cursor == null) {
                    Log.e(TAG, "Error querying calendar table in results.db");
                    return;
                }
                if (cursor.getCount() == 0) {
                    // Get current time
                    long currentTime = System.currentTimeMillis();
                    // Log calendar event in events table
                    ContentValues eventValues = new ContentValues();
                    eventValues.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
                    eventValues.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "Calendar Event Added");
                    eventValues.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, date.getTime());
                    eventValues.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, title);
                    eventValues.put(AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN, "ID:" + id + ";");
                    context.getContentResolver().insert(eventsUri, eventValues);
                    // Log calendar event in calendar table
                    ContentValues calendarValues = new ContentValues();
                    calendarValues.put(AndroLoggerProvider.Calendar.ID, id);
                    calendarValues.put(AndroLoggerProvider.Calendar.DATE_ADDED, currentTime);
                    calendarValues.put(AndroLoggerProvider.Calendar.NAME, title);
                    calendarValues.put(AndroLoggerProvider.Calendar.EVENT_DATE, date.getTime());
                    context.getContentResolver().insert(AndroLoggerProvider.Calendar.CONTENT_URI, calendarValues);
                    //Log.i(TAG, "Title: "+title+" Date: "+date.toString()+" ID: "+id);
                }
                calCursor.moveToNext();
            }
        }
    }

    /**
     * This method sets new alarm.
     *
     * @param context
     * @param am
     */
    public void setAlarm(Context context, AlarmManager am) {
        if (COLLECTION_INTERVAL <= 0)
            return;
        Intent intent = new Intent(context, CalendarWatcher.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), COLLECTION_INTERVAL, pi);
    }

    /**
     * This method cancels existing alarm.
     *
     * @param context
     * @param am
     */
    public void cancelAlarm(Context context, AlarmManager am) {
        if (COLLECTION_INTERVAL <= 0)
            return;
        Intent intent = new Intent(context, CalendarWatcher.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }
}
