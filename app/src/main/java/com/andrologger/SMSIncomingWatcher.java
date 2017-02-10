package com.andrologger;
/**
 * SMSIncomingWatcher.java
 *
 * @author Pradeep Tiwari
 */

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.Date;

/** This class detects incoming SMS messages. **/
public class SMSIncomingWatcher extends BroadcastReceiver {
    // Initialize constants and variables
    public static final String TAG = "IncomingSMSWatcher";

    /**
     * This method handles the broadcasted intent.
     *
     * @param context The application context.
     * @param intent The broadcasted intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")))
            return;
        // Scrape "extra" information out of the intent
        Bundle extras = intent.getExtras();
        if (extras != null) {
            // Reconstruct SMS
            Object[] pdus = (Object[]) extras.get("pdus");
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[0]);
            // Extract SMS date, address, and body
            Date date = new Date(sms.getTimestampMillis());
            String sender = sms.getDisplayOriginatingAddress();
            String body = sms.getDisplayMessageBody();
            // Get contact name
            String displayName = ContactFinder.findContact(context, sender);
            // Insert new incoming SMS message into AndroLogger
            Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;
            ContentValues values = new ContentValues();
            values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
            values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "SMS Received");
            values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, date.getTime());
            values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, body);
            values.put(AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN, "SenderAddress:" + sender + "; SenderContact:" + displayName);
            context.getContentResolver().insert(eventsUri, values);
            //Log.i(TAG, "Address: "+sender +" Contact: "+displayName+" Body: "+body +" Time: "+date.toString());
        }
    }
}
