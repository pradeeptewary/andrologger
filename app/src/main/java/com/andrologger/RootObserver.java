package com.andrologger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.util.Date;

public class RootObserver {
    public static long COLLECTION_INTERVAL = 1000 * 60 * 2;
    public static final String TAG = "RootWatcher";
    private String flag1;
    private Context context = null;

    public void setInterval(long logcatInterval, Context context) {
        COLLECTION_INTERVAL = logcatInterval;
        this.context = context;
        if (findBinary("su")) {
            this.flag1 = "rooted";
        } else {
            this.flag1 = "Not Rooted";
        }
        makeEntry();
    }

    public static boolean findBinary(String binaryName) {
        boolean found = false;
        if (!found) {
            String[] places = {"/sbin/", "/system/bin/", "/system/xbin/",
                    "/data/local/xbin/", "/data/local/bin/",
                    "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
            for (String where : places) {
                if (new File(where + binaryName).exists()) {
                    found = true;

                    break;
                }
            }
        }
        return found;
    }

    public void makeEntry() {
        Date date = new Date();
        Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
        values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, "");
        values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, date.getTime());
        values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, flag1);
        values.put(AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN, "");
        context.getContentResolver().insert(eventsUri, values);
    }

    public void setAlarm(Context context, AlarmManager am) {
        if (COLLECTION_INTERVAL <= 0)
            return;
        Intent logcatIntent = new Intent(context, LogcatWatcher.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, logcatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), COLLECTION_INTERVAL, pi);
    }

    /**
     * This method cancels an existing alarm.
     *
     * @param context
     * @param am
     */
    public void cancelAlarm(Context context, AlarmManager am) {
        if (COLLECTION_INTERVAL <= 0)
            return;
        Intent intent = new Intent(context, LogcatWatcher.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }
}