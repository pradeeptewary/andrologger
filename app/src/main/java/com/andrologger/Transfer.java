package com.andrologger;
/**
 * Transfer.java
 *
 * @author Pradeep Tiwari
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** This class performs scheduled data transfers. **/
public class Transfer extends BroadcastReceiver {
    // Initialize constants and variables
    public static final String TAG = "Transfer";
    public static long TRANSFER_INTERVAL = 1000 * 60 * 4;
    public static String SSL_CERT_NAME = "server.crt";
    public static String SERVER_URL = "http://192.168.102.187/uploads/upload.php";

    /**
     * This method sets the transfer interval.
     *
     * @param interval Collection interval.
     */
    public void setInterval(long transferInterval) {
        TRANSFER_INTERVAL = transferInterval;
    }

    /**
     * This method sets the server connection properties.
     *
     * @param sslCertName The filename of the SSL certificate included in the assets.
     * @param serverURL The URL of the central server.
     */
    public void setConnectionProperties(String sslCertName, String serverURL) {
        //SSL_CERT_NAME = sslCertName;
        SERVER_URL = serverURL;
    }

    /**
     * This method handles the broadcasted alarm intent.
     *
     * @param context The application's context.
     * @param intent The broadcasted intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        TransferManager transferManager = new TransferManager(context.getApplicationContext());
        // Get connection to central server
        boolean connected = transferManager.getConnection(SSL_CERT_NAME, SERVER_URL);
        if (!connected)
            return;
        // Insert new entry in the AndroLogger transfer table
        boolean started = transferManager.startTransfer();
        if (!started)
            return;
        // Transfer logged AndroLogger events to central server
        boolean transferred = transferManager.pushToServer();
        if (!transferred)
            return;
        // Wipe transferred data from the local phone database
        boolean deleted = transferManager.wipeDatabase();
        if (!deleted)
            return;
    }

    /**
     * This method sets a new alarm.
     *
     * @param context
     * @param am
     */
    public void setAlarm(Context context, AlarmManager am) {
        if (TRANSFER_INTERVAL <= 0)
            return;
        Intent intent = new Intent(context, Transfer.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), TRANSFER_INTERVAL, pi);
    }

    /**
     * This method cancels an existing alarm.
     *
     * @param context
     * @param am
     */
    public void cancelAlarm(Context context, AlarmManager am) {
        if (TRANSFER_INTERVAL <= 0)
            return;
        Intent intent = new Intent(context, Transfer.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }
}
