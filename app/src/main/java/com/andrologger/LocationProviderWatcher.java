package com.andrologger;
/**
 * LocationProviderWatcher.java
 *
 * @author Pradeep Tiwari
 */

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;

/** This class detects changes to the location provider settings. **/
public class LocationProviderWatcher extends BroadcastReceiver {
    // Initialize constants and variables
    public static final String TAG = "ProviderChangeWatcher";

    /**
     * This method handles a broadcasted intent.
     *
     * @param context The application context.
     * @param intent The broadcasted intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!(intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)))
            return;
        // Check status of each provider
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // Store status of each provider
        String networkStatus = "";
        if (networkEnabled)
            networkStatus = "on";
        else
            networkStatus = "off";
        String gpsStatus = "";
        if (gpsEnabled)
            gpsStatus = "on";
        else
            gpsStatus = "off";
        // Log the event in AndroLogger
        Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;
        ContentValues values;
        values = new ContentValues();
        values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
        values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "Provider Status Changed");
        values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
        values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, "Network:" + networkStatus + "; GPS:" + gpsStatus + ";");
        context.getContentResolver().insert(eventsUri, values);
        //Log.i(TAG, "change in provider detected. Network: "+networkStatus+" GPS: "+gpsStatus);
    }
}
