package com.andrologger;
/**
 * CalendarCheck.java
 *
 * @author Pradeep Tiwari
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/** This class performs operations related to the AndroLogger calendar table. **/
public class CalendarCheck {
    // Initialize constants and variables
    public static final String TAG = "CalendarCheck";

    /**
     * This method checks the status of the calendar database.
     *
     * @param context The application context.
     */
    public static void checkAndroLoggerCalendar(Context context) {
        // AndroLogger calendar content provider URI
        Uri dwURI = AndroLoggerProvider.Status.CONTENT_URI;
        // Query content provider
        String[] proj = new String[]{AndroLoggerDatabase.CALENDAR_FILLED_FLAG_COLUMN};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(dwURI, proj, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Unable to query results.db: " + e.getMessage());
            return;
        }
        if (cursor == null) {
            Log.e(TAG, "Unable to query results.db");
            return;
        }
        // Populate table if needed, otherwise skip
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            int filledFlag = cursor.getInt(cursor.getColumnIndex(AndroLoggerDatabase.CALENDAR_FILLED_FLAG_COLUMN));
            if (filledFlag == 0) {
                populateCalendarTable(context);
                ContentValues values = new ContentValues();
                values.put(AndroLoggerDatabase.CALENDAR_FILLED_FLAG_COLUMN, "1");
                context.getContentResolver().update(dwURI, values, null, null);
            }
        }
    }

    /**
     * This method populates the AndroLogger calendar table.
     *
     * @param context The application context.
     */
    private static void populateCalendarTable(Context context) {
        // Initialize the Android calendar content provider URI (undocumented)
        Uri calUri = Uri.parse("content://com.android.calendar/event_entities");
        // Query calendar for events
        String[] calProj = new String[]{"dtstart", "title", "_id"};
        Cursor calendarCursor = null;
        try {
            calendarCursor = context.getContentResolver().query(calUri, calProj, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Unable to query calendar content provider: " + e.getMessage());
            return;
        }
        // Parse and log query results
        if (calendarCursor == null) {
            Log.e(TAG, "Unable to query calendar content provider");
            return;
        }
        if (calendarCursor.moveToFirst() && calendarCursor.getCount() > 0) {
            long id = -1;
            String name = null;
            long eventTime = -1;
            long currentTime = System.currentTimeMillis();
            ContentValues values;
            while (!calendarCursor.isAfterLast()) {
                id = calendarCursor.getLong(calendarCursor.getColumnIndex("_id"));
                name = calendarCursor.getString(calendarCursor.getColumnIndex("title"));
                eventTime = calendarCursor.getLong(calendarCursor.getColumnIndex("dtstart"));
                // Insert new event into AndroLogger calendar database
                values = new ContentValues();
                values.put(AndroLoggerDatabase.CALENDAR_EVENT_ID_COLUMN, id);
                values.put(AndroLoggerDatabase.CALENDAR_EVENT_NAME_COLUMN, name);
                values.put(AndroLoggerDatabase.CALENDAR_EVENT_ADDED_COLUMN, currentTime);
                values.put(AndroLoggerDatabase.CALENDAR_EVENT_DATE_COLUMN, eventTime);
                context.getContentResolver().insert(AndroLoggerProvider.Calendar.CONTENT_URI, values);
                calendarCursor.moveToNext();
            }
        }
    }
}




