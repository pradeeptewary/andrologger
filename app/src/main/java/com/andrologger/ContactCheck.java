package com.andrologger;
/**
 * ContactCheck.java
 *
 * @author Pradeep Tiwari
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

/** This class handles the operations involving the AndroLogger contacts database table. **/
public class ContactCheck {
    // Initialize constants and variables
    public static final String TAG = "ContactsCheck";

    /**
     * This method checks the status of the AndroLogger contacts table.
     *
     * @param context The application context.
     */
    public static void checkAndroLoggerContacts(Context context) {
        // Initialize the AndroLogger contacts content provider URI
        Uri dwURI = AndroLoggerProvider.Status.CONTENT_URI;
        // Check to see if the database is populated
        String[] proj = new String[]{AndroLoggerDatabase.CONTACTS_FILLED_FLAG_COLUMN};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(dwURI, proj, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Unable to query results.db: " + e.getMessage());
            return;
        }
        if (cursor == null) {
            Log.e(TAG, "Unable to query results.db");
            return;
        }
        // Populate database, if necessary
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            int filledFlag = cursor.getInt(cursor.getColumnIndex(AndroLoggerDatabase.CONTACTS_FILLED_FLAG_COLUMN));
            if (filledFlag == 0) {
                populateContactsTable(context);
                ContentValues values = new ContentValues();
                values.put(AndroLoggerDatabase.CONTACTS_FILLED_FLAG_COLUMN, "1");
                context.getContentResolver().update(dwURI, values, null, null);
            }
        }
    }

    /**
     * This method populates AndroLogger contacts table.
     *
     * @param context The application context.
     */
    private static void populateContactsTable(Context context) {
        // Query contacts database
        Cursor contactsCursor = null;
        String[] contactsProj = new String[]{Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER};
        try {
            contactsCursor = context.getContentResolver().query(Phone.CONTENT_URI, contactsProj, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Unable to query contacts content provider: " + e.getMessage());
        }
        // Parse and log query results
        if (contactsCursor == null)
            return;
        if (contactsCursor.moveToFirst() && contactsCursor.getCount() > 0) {
            long id = -1;
            String name = null;
            String phoneNumber = null;
            long currentTime = System.currentTimeMillis();
            ContentValues values;
            while (!contactsCursor.isAfterLast()) {
                id = contactsCursor.getLong(contactsCursor.getColumnIndex(Phone._ID));
                name = contactsCursor.getString(contactsCursor.getColumnIndex(Phone.DISPLAY_NAME));
                phoneNumber = contactsCursor.getString(contactsCursor.getColumnIndex(Phone.NUMBER));
                // Insert new contact into contact database
                values = new ContentValues();
                values.put(AndroLoggerDatabase.CONTACT_ID_COLUMN, id);
                values.put(AndroLoggerDatabase.CONTACT_NAME_COLUMN, name);
                values.put(AndroLoggerDatabase.CONTACT_ADDED_COLUMN, currentTime);
                values.put(AndroLoggerDatabase.CONTACT_NUMBER_COLUMN, phoneNumber);
                context.getContentResolver().insert(AndroLoggerProvider.Contacts.CONTENT_URI, values);
                contactsCursor.moveToNext();
            }
        }
    }
}
