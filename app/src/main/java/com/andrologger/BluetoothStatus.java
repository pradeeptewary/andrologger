package com.andrologger;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class BluetoothStatus extends BroadcastReceiver {

    public static final String TAG = "BluetoothChangeWatcher";

    @SuppressWarnings("static-access")
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.e(TAG, "Bluetooth");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "NULL");
            return;
        }
        String networkStatus = "";
        if (bluetoothAdapter.getState() == 10)
            networkStatus = "STATE_OFF";
        else if (bluetoothAdapter.getState() == 12) {
            networkStatus = "STATE_ON";
        } else if (bluetoothAdapter.getState() == 13)
            networkStatus = "STATE_TURNING_OFF";
        else if (bluetoothAdapter.getState() == 11)
            networkStatus = "STATE_TURNING_ON";

        // Log the event in AndroLogger
        Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;
        ContentValues values;
        values = new ContentValues();
        values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
        values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "Bluetooth Status Changed");
        values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
        values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, networkStatus);
        values.put(AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN, String.valueOf(bluetoothAdapter.getBondedDevices()));
        context.getContentResolver().insert(eventsUri, values);

        //Log.e(TAG, "change in provider detected. Network: "+networkStatus+" GPS");
    }

}
