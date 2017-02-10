package com.andrologger;
/**
 * SMSOutgoingWatcher.java
 *
 * @author Pradeep Tiwari
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/** This class detects outgoing SMS messages. **/
public class SMSOutgoingWatcher extends ContentObserver {
    // Initialize constants and variables
    public static final String TAG = "OutgoingSMSWatcher";
    private Context context;

    /**
     * Constructor needed for content observers.
     *
     * @param handler
     * @param context
     */
    public SMSOutgoingWatcher(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    /**
     * This method handles the content observer change notifications.
     *
     * @param selfChange
     */
    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        querySMSLog();
    }

    /**
     * This method attempts to find newly logged outgoing SMS messages.
     */
    private void querySMSLog() {
        // Initialize SMS content provider URI (undocumented)
        Uri uriSMS = Uri.parse("content://sms");
        // Query for SMS id, body, address, date, and type
        String[] smsProj = new String[]{"_id", "body", "address", "date", "type"};
        Cursor smsCursor = null;
        try {
            smsCursor = context.getContentResolver().query(uriSMS, smsProj, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Unable to query URI: " + uriSMS.toString());
            return;
        }
        // Parse query results
        if (smsCursor == null) {
            Log.e(TAG, "Unable to query SMS content provider");
            return;
        }
        if (smsCursor.moveToFirst() && smsCursor.getCount() > 0) {
            int messageID = -1;
            String body = null;
            String address = null;
            Date date = null;
            int smsType = -1;
            // Get SMS message
            try {
                messageID = smsCursor.getInt(smsCursor.getColumnIndex("_id"));
                body = smsCursor.getString(smsCursor.getColumnIndex("body"));
                address = smsCursor.getString(smsCursor.getColumnIndex("address"));
                date = new Date(smsCursor.getLong(smsCursor.getColumnIndex("date")));
                smsType = Integer.valueOf(smsCursor.getString(smsCursor.getColumnIndex("type")));
            } catch (Exception e) {
                Log.e(TAG, "Unable to log SMS");
                return;
            }
            if (messageID == -1 || body == null || address == null || date == null || smsType == -1) {
                Log.e(TAG, "Unable to log SMS");
                return;
            }
            // Get contact name
            String displayName = ContactFinder.findContact(context, address);
            // Log any newly logged outgoing SMS messages
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, -5);
            long fiveSecondsAgo = cal.getTimeInMillis();
            if (date.getTime() > fiveSecondsAgo && smsType == 4) {
                // Check the AndroLogger content provider to make sure event is not a duplicate
                Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;
                String[] projection = new String[]{AndroLoggerDatabase.DETECTOR_COLUMN, AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN};
                String selection = AndroLoggerDatabase.DETECTOR_COLUMN + " = ? AND " + AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN + " LIKE ?";
                String[] selectionArgs = new String[]{TAG, "%MSG_ID:" + messageID + ";%"};
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(eventsUri, projection, selection, selectionArgs, null);
                } catch (Exception e) {
                    Log.e(TAG, "Unable to query events table");
                    return;
                }
                if (cursor == null) {
                    Log.e(TAG, "Unable to query events table");
                    return;
                }
                if (cursor.getCount() == 0) {
                    // Insert new event into AndroLogger
                    ContentValues values = new ContentValues();
                    values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
                    values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "SMS Sent");
                    values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, date.getTime());
                    values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, body);
                    values.put(AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN, "MSG_ID:" + messageID + "; ReceiverAddress:" + address + "; ReceiverContact:" + displayName + ";");
                    context.getContentResolver().insert(eventsUri, values);
                    //Log.i(TAG, "SMS Type: "+smsType+" DisplayName: "+displayName+" Number: "+address+ " Date: "+date.toString()+" Body: "+body+" ID: "+messageID);
                }
            }
        }
    }
}

