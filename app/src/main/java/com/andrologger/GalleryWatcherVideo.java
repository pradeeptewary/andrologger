package com.andrologger;
/**
 * GalleryWatcher.java
 *
 * @author Pradeep Tiwari
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore.Video.Media;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/** This class detects newly added photos to the Android Gallery. **/
public class GalleryWatcherVideo extends ContentObserver {
    // Initialize constants and variables
    private static final String TAG = "VideoWatcher";
    private static String action = null;
    private Context context;

    /**
     * Constructor required for content observers.
     *
     * @param handler
     * @param context
     */
    public GalleryWatcherVideo(Handler handler, Context context) {
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
        queryPhotos();
    }

    /**
     * This method finds and identifies newly added pictures.
     */
    private void queryPhotos() {
        // Query photo database
        Cursor photoCursor = null;
        Cursor photoCursor1 = null;
        String[] photoProj = new String[]{Media._ID, Media.DISPLAY_NAME, Media.DATE_TAKEN};
        try {
            photoCursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, photoProj, null, null, "date_added DESC");
        } catch (Exception e) {
            Log.e(TAG, "Unable to query content provider");
            return;
        }
        // Parse query results
        if (photoCursor == null) {
            Log.e(TAG, "Unable to query content provider");
            return;
        }
        Log.i(TAG, "hfgsdfg");
        // Get most recent photo
        long id = -1;
        Date date = null;
        String fileName = null;
        //Delete
        /*if ( photoCursor1.getCount() > photoCursor.getCount()){
			action = "Video Removed";
			id = photoCursor1.getLong(photoCursor.getColumnIndex(Media._ID));
			fileName = photoCursor1.getString(photoCursor.getColumnIndex(Media.DISPLAY_NAME));
			date = new Date(photoCursor1.getLong(photoCursor.getColumnIndex(Media.DATE_TAKEN)));
		}*/
        if (photoCursor.moveToFirst() && photoCursor.getCount() > 0) {
            action = "Video Added";
            id = photoCursor.getLong(photoCursor.getColumnIndex(Media._ID));
            fileName = photoCursor.getString(photoCursor.getColumnIndex(Media.DISPLAY_NAME));
            date = new Date(photoCursor.getLong(photoCursor.getColumnIndex(Media.DATE_TAKEN)));
        }
        if (id == -1 || fileName == null || date == null) {
            Log.e(TAG, "Unable to query photo database");
            return;
        }
		/*try
		{
			photoCursor1 = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, photoProj, null, null, "date_added DESC");
		}
		catch(Exception e)
		{
			Log.e(TAG, "Unable to query content provider");
			return;
		}*/
        // Check the photo's date_added time to see if it's new
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, -5);
        long fiveSecondsAgo = cal.getTimeInMillis();
        if (date.getTime() > fiveSecondsAgo) {
            // Initialize AndroLogger events table URI
            Uri eventsUri = AndroLoggerProvider.Events.CONTENT_URI;
            // Check to see if the photo is already in the local database
            String[] projection = new String[]{AndroLoggerDatabase.DETECTOR_COLUMN, AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN};
            String selection = AndroLoggerDatabase.DETECTOR_COLUMN + " = ? AND " + AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN + " = ?";
            String[] selectionArgs = new String[]{TAG, fileName};
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
                // Insert newly added picture to AndroLogger
                ContentValues values = new ContentValues();
                values.put(AndroLoggerDatabase.DETECTOR_COLUMN, TAG);
                values.put(AndroLoggerDatabase.EVENT_ACTION_COLUMN, action);
                values.put(AndroLoggerDatabase.EVENT_DATE_COLUMN, date.getTime());
                values.put(AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN, fileName);
                values.put(AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN, "VideoID:" + id);
                context.getContentResolver().insert(eventsUri, values);
                Log.i(TAG, "ID: " + id + " FileName: " + fileName + " Date: " + date.toString());
            }
        }
    }
}