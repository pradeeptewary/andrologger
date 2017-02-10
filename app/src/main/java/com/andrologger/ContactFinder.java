package com.andrologger;
/**
 * ContactFinder.java
 *
 * @author Pradeep Tiwari
 */

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

/** This class finds the names associated with contact addresses. **/
public class ContactFinder {
    // Initialize constants and variables
    public static final String TAG = "ContactFinder";

    /**
     * This method returns the contact name for a given address.
     *
     * @param context The application context.
     * @param address The provided address (phone number).
     * @return The associated contact name.
     */
    public static String findContact(Context context, String address) {
        // Initialize the contacts content provider URI
        Uri contactURI = Phone.CONTENT_URI;
        // Query contacts data
        String[] projection = new String[]{Phone.DISPLAY_NAME,};
        String selectionClause = Phone.NUMBER + " = ?";
        String displayName = null;
        Cursor contactsCursor = null;
        try {
            contactsCursor = context.getContentResolver().query(contactURI, projection, selectionClause, new String[]{address}, null);
        } catch (Exception e) {
            Log.e(TAG, "Unable to query contacts content provider");
            return null;
        }
        // Parse query results
        if (contactsCursor == null) {
            Log.e(TAG, "Unable to query contacts content provider");
            return null;
        }
        if (contactsCursor.moveToFirst() && contactsCursor.getCount() > 0) {
            try {
                displayName = contactsCursor.getString(contactsCursor.getColumnIndex(Phone.DISPLAY_NAME));
            } catch (Exception e) {
                Log.e(TAG, "Unable to find the contact name");
                return null;
            }
        }
        return displayName;
    }
}
