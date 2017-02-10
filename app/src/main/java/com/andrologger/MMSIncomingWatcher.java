package com.andrologger;
/**
 * MMSIncomingWatcher.java
 *
 * @author Pradeep Tiwari
 */

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Date;

//import android.util.Log;

/** This class detects incoming MMS messages. **/
public class MMSIncomingWatcher extends BroadcastReceiver {
    public static final String TAG = "IncomingMMSWatcher";

    /**
     * This method handles a broadcasted intent.
     *
     * @param context The application context.
     * @param intent The broadcasted intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!(intent.getAction().equals("android.provider.Telephony.MMS_RECEIVED") || intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED")))
            return;
        // Retrieve the most recent MMS message
        ArrayList<MMS> mmsArray = MMS.getMostRecentIncomingMMS(context, TAG);
        mmsArray = MMS.addMMSPictureNames(context, mmsArray, TAG);
        mmsArray = MMS.addAddresses(context, mmsArray, TAG);
        mmsArray = MMS.addMMSText(context, mmsArray, TAG);
        for (MMS mms : mmsArray) {
            Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;
            int messageID = mms.getId();
            String address = mms.getAddress();
            String displayName = ContactFinder.findContact(context, address);
            Date date = mms.getDate();
            String subject = mms.getSubject();
            String picName = mms.getPicName();
            String text = mms.getText();
            // Substitute text indication place-holders (could not retrieve actual text)
            if (text == null)
                text = "<No Text Included>";
            if (text.equals(""))
                text = "<Text Detected>";
            // Insert new MMS message into AndroLogger
            ContentValues values = new ContentValues();
            values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
            values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "MMS Received");
            values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, date.getTime());
            values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, picName);
            values.put(AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN, "MSG_ID:" + messageID + "; ReceiverAddress:" + address + "; ReceiverContact:" + displayName + "; Subject:" + subject + "; Text:" + text + ";");
            context.getContentResolver().insert(eventsUri, values);
            //Log.i(TAG, "Dir: "+mms.getDirection()+" Address: "+address+" Name: "+displayName+" Sub: "+subject+" Text: "+text+" Pic: "+picName+ " Date: "+mms.getDate().toString()+" MessageID: "+messageID+" ThreadID: "+mms.getThreadId());
        }
    }
}
