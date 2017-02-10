package com.andrologger;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * This class detects last known locations.
 **/
public class LocationWatcher extends BroadcastReceiver {
    // Initialize constants and variables
    public static final String TAG = "LocationWatcher";
    public static long COLLECTION_INTERVAL = 1000 * 60 * 60;

    /**
     * This method sets the collection interval.
     *
     * @param interval Collection interval.
     */
    public void setInterval(long locationInterval) {
        COLLECTION_INTERVAL = locationInterval;
    }

    /**
     * This method handles the broadcasted alarm intent.
     *
     * @param context The application's context.
     * @param intent  The broadcasted intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        //New Code
        String locationKey = LocationManager.KEY_LOCATION_CHANGED;
        String providerEnabledKey = LocationManager.KEY_PROVIDER_ENABLED;
        if (intent.hasExtra(providerEnabledKey)) {
            if (!intent.getBooleanExtra(providerEnabledKey, true)) {
                /*Toast.makeText(context,
						"Provider disabled",
						Toast.LENGTH_SHORT).show(); */
            } else {
				/*Toast.makeText(context,
						"Provider enabled",
						Toast.LENGTH_SHORT).show();*/
            }
        }
        if (intent.hasExtra(locationKey)) {
            Location loc = (Location) intent.getExtras().get(locationKey);
            Toast.makeText(context,
                    "Location changed : Lat: " + loc.getLatitude() +
                            " Lng: " + loc.getLongitude(),
                    Toast.LENGTH_SHORT).show();
        }
        // Get Location Service
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Specify Accuracy & Provider
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = locationManager.getBestProvider(criteria, true);

        // Get Last Known Location
        Location lastLocation = null;
        try {
            lastLocation = locationManager.getLastKnownLocation(bestProvider);
        } catch (Exception e) {
            Log.e(TAG, "Error: Last location not found");
            return;
        }

        // Log location
        if (lastLocation != null) {
            // Initialize the DroidWatch events content provider URI
            Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;

            // Make sure the location is not already listed
            String[] projection = new String[]{AndroLoggerDatabase.DETECTOR_COLUMN, AndroLoggerDatabase.EVENT_DATE_COLUMN};
            String selection = AndroLoggerDatabase.DETECTOR_COLUMN + " = ? AND " + AndroLoggerDatabase.EVENT_DATE_COLUMN + " = ?";
            String[] selectionArgs = new String[]{TAG, String.valueOf(lastLocation.getTime())};
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
                // Insert the new location into DroidWatch
                ContentValues values = new ContentValues();
                values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
                values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "LastKnownLocation Received");
                values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, lastLocation.getTime());
                values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, "Lat:" + lastLocation.getLatitude() + "; Lng:" + lastLocation.getLongitude());
                context.getContentResolver().insert(eventsUri, values);

                //Log.i(TAG, "Time: "+lastLocation.getTime()+" Lat: " + lastLocation.getLatitude() +" Lng: " + lastLocation.getLongitude());
            }
        }
    }
}