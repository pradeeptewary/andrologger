package com.andrologger;
/**
 * AndroLoggerProvider.java
 *
 * @author Pradeep Tiwari
 */

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

/** This class handles content provider functions required of the AndroLogger database. **/
public class AndroLoggerProvider extends ContentProvider {
    private AndroLoggerDatabase db;
    private SQLiteDatabase sqlDB;
    private static final String AUTHORITY = "com.andrologger.AndroLoggerProvider";
    public static final int EVENTS = 0;
    public static final int EVENT_ID = 1;
    public static final int TRANSFERS = 2;
    public static final int TRANSFER_ID = 3;
    public static final int CONTACTS = 4;
    public static final int CONTACT_ID = 5;
    public static final int CALENDAR = 6;
    public static final int CALENDAR_ID = 7;
    public static final int STATUS = 8;
    private static final UriMatcher URI_MATCHER;

    /** This interface lists available columns for the events table. **/
    public static interface Events extends BaseColumns {
        public static final String CONTENT_PATH = "events";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AndroLoggerProvider.AUTHORITY + "/" + CONTENT_PATH);
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.andrologger.event";
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.andrologger.event";
        public static final String DETECTOR = AndroLoggerDatabase.DETECTOR_COLUMN;
        public static final String DETECTED = AndroLoggerDatabase.DETECT_DATE_COLUMN;
        public static final String ACTION = AndroLoggerDatabase.EVENT_ACTION_COLUMN;
        public static final String EVENT_DATE = AndroLoggerDatabase.EVENT_DATE_COLUMN;
        public static final String DESCRIPTION = AndroLoggerDatabase.EVENT_DESCRIPTION_COLUMN;
        public static final String ADDITIONAL_INFO = AndroLoggerDatabase.ADDITIONAL_INFO_COLUMN;
        public static final String SORT_ORDER_DEFAULT = _ID + " ASC";
    }

    /** This interface lists available columns for the transfers table. **/
    public static interface Transfers extends BaseColumns {
        public static final String CONTENT_PATH = "transfers";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AndroLoggerProvider.AUTHORITY + "/" + CONTENT_PATH);
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.andrologger.transfer";
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.andrologger.transfer";
        public static final String COMPLETED = AndroLoggerDatabase.TRANSFERS_COMPLETED_COLUMN;
        public static final String STARTDATE = AndroLoggerDatabase.TRANSFERS_STARTDATE_COLUMN;
        public static final String DEVICE_ID = AndroLoggerDatabase.TRANSFERS_DEVICE_ID_COLUMN;
    }

    /** This interface lists available columns for the calendar table. **/
    public static interface Calendar extends BaseColumns {
        public static final String CONTENT_PATH = "calendar";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AndroLoggerProvider.AUTHORITY + "/" + CONTENT_PATH);
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.andrologger.calendar";
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.andrologger.calendar";
        public static final String ID = AndroLoggerDatabase.CALENDAR_EVENT_ID_COLUMN;
        public static final String NAME = AndroLoggerDatabase.CALENDAR_EVENT_NAME_COLUMN;
        public static final String EVENT_DATE = AndroLoggerDatabase.CALENDAR_EVENT_DATE_COLUMN;
        public static final String DATE_ADDED = AndroLoggerDatabase.CALENDAR_EVENT_ADDED_COLUMN;
        public static final String SORT_ORDER_DEFAULT = DATE_ADDED + " DESC";
    }

    /** This interface lists available columns for the contacts table. **/
    public static interface Contacts extends BaseColumns {
        public static final String CONTENT_PATH = "contacts";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AndroLoggerProvider.AUTHORITY + "/" + CONTENT_PATH);
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.andrologger.contact";
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.andrologger.contact";
        public static final String ID = AndroLoggerDatabase.CONTACT_ID_COLUMN;
        public static final String NAME = AndroLoggerDatabase.CONTACT_NAME_COLUMN;
        public static final String NUMBER = AndroLoggerDatabase.CONTACT_NUMBER_COLUMN;
        public static final String ADDED = AndroLoggerDatabase.CONTACT_ADDED_COLUMN;
        public static final String SORT_ORDER_DEFAULT = ADDED + " DESC";
    }

    /** This interface lists available columns for the status table. **/
    public static interface Status extends BaseColumns {
        public static final String CONTENT_PATH = "status";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AndroLoggerProvider.AUTHORITY + "/" + CONTENT_PATH);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.andrologger.status";
        public static final String CONTACTS_FILLED_STATUS = AndroLoggerDatabase.CONTACTS_FILLED_FLAG_COLUMN;
        public static final String CALENDAR_FILLED_STATUS = AndroLoggerDatabase.CALENDAR_FILLED_FLAG_COLUMN;
    }

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, Events.CONTENT_PATH, EVENTS);
        URI_MATCHER.addURI(AUTHORITY, Events.CONTENT_PATH + "/#", EVENT_ID);
        URI_MATCHER.addURI(AUTHORITY, Transfers.CONTENT_PATH, TRANSFERS);
        URI_MATCHER.addURI(AUTHORITY, Transfers.CONTENT_PATH + "/#", TRANSFER_ID);
        URI_MATCHER.addURI(AUTHORITY, Contacts.CONTENT_PATH, CONTACTS);
        URI_MATCHER.addURI(AUTHORITY, Contacts.CONTENT_PATH + "/#", CONTACT_ID);
        URI_MATCHER.addURI(AUTHORITY, Calendar.CONTENT_PATH, CALENDAR);
        URI_MATCHER.addURI(AUTHORITY, Calendar.CONTENT_PATH + "/#", CALENDAR_ID);
        URI_MATCHER.addURI(AUTHORITY, Status.CONTENT_PATH, STATUS);
    }

    /**
     * This method provides the deletion capabilities to available content provider URIs.
     *
     * @param uri The content provider URI.
     * @param selection The content fields selected.
     * @param selectionArgs The content selected.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Associate an action with a content provider URI
        int deleteCount = 0;
        switch (URI_MATCHER.match(uri)) {
            case EVENTS:
                deleteCount = sqlDB.delete(AndroLoggerDatabase.EVENTS_TABLE, selection, selectionArgs);
                break;
            case EVENT_ID:
                String eventID = uri.getLastPathSegment();
                String eventWhereClause = Events._ID + " = " + eventID;
                if (!(TextUtils.isEmpty(selection)))
                    eventWhereClause += " AND " + selection;
                deleteCount = sqlDB.delete(AndroLoggerDatabase.EVENTS_TABLE, eventWhereClause, selectionArgs);
                break;
            case TRANSFERS:
                deleteCount = sqlDB.delete(AndroLoggerDatabase.TRANSFERS_TABLE, selection, selectionArgs);
                break;
            case TRANSFER_ID:
                String transferID = uri.getLastPathSegment();
                String transferWhereClause = Transfers._ID + " = " + transferID;
                if (!(TextUtils.isEmpty(selection)))
                    transferWhereClause += " AND " + selection;
                deleteCount = sqlDB.delete(AndroLoggerDatabase.TRANSFERS_TABLE, transferWhereClause, selectionArgs);
                break;
            case CONTACTS:
                deleteCount = sqlDB.delete(AndroLoggerDatabase.CONTACTS_TABLE, selection, selectionArgs);
                break;
            case CONTACT_ID:
                String contactID = uri.getLastPathSegment();
                String contactWhereClause = Contacts.ID + " = " + contactID;
                if (!(TextUtils.isEmpty(selection)))
                    contactWhereClause += " AND " + selection;
                deleteCount = sqlDB.delete(AndroLoggerDatabase.CONTACTS_TABLE, contactWhereClause, selectionArgs);
                break;
            case CALENDAR:
                deleteCount = sqlDB.delete(AndroLoggerDatabase.CALENDAR_TABLE, selection, selectionArgs);
                break;
            case CALENDAR_ID:
                String calendarID = uri.getLastPathSegment();
                String calendarWhereClause = Calendar.ID + " = " + calendarID;
                if (!(TextUtils.isEmpty(selection)))
                    calendarWhereClause += " AND " + selection;
                deleteCount = sqlDB.delete(AndroLoggerDatabase.CALENDAR_TABLE, calendarWhereClause, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }
        if (deleteCount > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return deleteCount;
    }

    /**
     * This method returns the type of a content URI.
     *
     * @param uri The content provider URI.
     */
    @Override
    public String getType(Uri uri) {
        // Determine the URI and associate with a type.
        switch (URI_MATCHER.match(uri)) {
            case EVENTS:
                return Events.CONTENT_TYPE;
            case EVENT_ID:
                return Events.CONTENT_ITEM_TYPE;
            case TRANSFERS:
                return Transfers.CONTENT_TYPE;
            case TRANSFER_ID:
                return Transfers.CONTENT_ITEM_TYPE;
            case CONTACTS:
                return Contacts.CONTENT_TYPE;
            case CONTACT_ID:
                return Contacts.CONTENT_ITEM_TYPE;
            case CALENDAR:
                return Calendar.CONTENT_TYPE;
            case CALENDAR_ID:
                return Calendar.CONTENT_ITEM_TYPE;
            case STATUS:
                return Status.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    /**
     * This method provides the insertion capabilities to available content provider URIs.
     *
     * @param uri The content provider URI.
     * @param values Key:value pairs.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Determine the content provider URI and perform the associated insertion.
        String tableName = null;
        switch (URI_MATCHER.match(uri)) {
            case EVENTS:
                tableName = AndroLoggerDatabase.EVENTS_TABLE;
                break;
            case TRANSFERS:
                tableName = AndroLoggerDatabase.TRANSFERS_TABLE;
                break;
            case CONTACTS:
                tableName = AndroLoggerDatabase.CONTACTS_TABLE;
                break;
            case CALENDAR:
                tableName = AndroLoggerDatabase.CALENDAR_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }
        if (tableName != null) {
            long id = sqlDB.insert(tableName, null, values);
            if (id > 0) {
                Uri itemUri = ContentUris.withAppendedId(uri, id);
                getContext().getContentResolver().notifyChange(itemUri, null);
                return itemUri;
            }
        }
        throw new SQLException("Problem while inserting into events table");
    }

    /**
     * This method helps in the creation of the content provider.
     */
    @Override
    public boolean onCreate() {
        db = new AndroLoggerDatabase(getContext());
        sqlDB = db.getWritableDatabase();
        if (this.sqlDB == null)
            return false;
        if (this.sqlDB.isReadOnly()) {
            this.sqlDB.close();
            this.sqlDB = null;
            return false;
        }
        return true;
    }

    /**
     * This method provides the selection capabilities to available content provider URIs.
     *
     * @param uri The content provider URI.
     * @param projection The columns to be returned.
     * @param selection The content fields selected.
     * @param selectionArgs The content selected.
     * @param sortOrder The sorting order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Determine the content provider URI and perform the associated selection
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (URI_MATCHER.match(uri)) {
            case EVENT_ID:
                queryBuilder.setTables(AndroLoggerDatabase.EVENTS_TABLE);
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = Events.SORT_ORDER_DEFAULT;
                queryBuilder.appendWhere(Events._ID + "=" + uri.getLastPathSegment());
                break;
            case EVENTS:
                queryBuilder.setTables(AndroLoggerDatabase.EVENTS_TABLE);
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = Events.SORT_ORDER_DEFAULT;
                break;
            case TRANSFER_ID:
                queryBuilder.setTables(AndroLoggerDatabase.TRANSFERS_TABLE);
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = null;
                break;
            case TRANSFERS:
                queryBuilder.setTables(AndroLoggerDatabase.TRANSFERS_TABLE);
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = null;
                break;
            case CONTACT_ID:
                queryBuilder.setTables(AndroLoggerDatabase.CONTACTS_TABLE);
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = Contacts.SORT_ORDER_DEFAULT;
                break;
            case CONTACTS:
                queryBuilder.setTables(AndroLoggerDatabase.CONTACTS_TABLE);
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = Contacts.SORT_ORDER_DEFAULT;
                break;
            case CALENDAR_ID:
                queryBuilder.setTables(AndroLoggerDatabase.CALENDAR_TABLE);
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = Calendar.SORT_ORDER_DEFAULT;
                break;
            case CALENDAR:
                queryBuilder.setTables(AndroLoggerDatabase.CALENDAR_TABLE);
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = Calendar.SORT_ORDER_DEFAULT;
                break;
            case STATUS:
                queryBuilder.setTables(AndroLoggerDatabase.STATUS_TABLE);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        Cursor cursor = queryBuilder.query(sqlDB, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * This method provides the update capabilities to available content provider URIs.
     *
     * @param uri The content provider URI.
     * @param values Key:value pairs.
     * @param selection The content fields selected.
     * @param selectionArgs The content selected.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Determine the content provider URI and perform the associated update
        int updateCount = 0;
        switch (URI_MATCHER.match(uri)) {
            case EVENTS:
                updateCount = sqlDB.update(AndroLoggerDatabase.EVENTS_TABLE, values, selection, selectionArgs);
                break;
            case EVENT_ID:
                String eventID = uri.getLastPathSegment();
                String eventWhereClause = Events._ID + " = " + eventID;
                if (!TextUtils.isEmpty(selection))
                    eventWhereClause += " AND " + selection;
                updateCount = sqlDB.update(AndroLoggerDatabase.EVENTS_TABLE, values, eventWhereClause, selectionArgs);
                break;
            case TRANSFERS:
                updateCount = sqlDB.update(AndroLoggerDatabase.TRANSFERS_TABLE, values, selection, selectionArgs);
                break;
            case TRANSFER_ID:
                String transferID = uri.getLastPathSegment();
                String transferWhereClause = Transfers._ID + " = " + transferID;
                if (!TextUtils.isEmpty(selection))
                    transferWhereClause += " AND " + selection;
                updateCount = sqlDB.update(AndroLoggerDatabase.TRANSFERS_TABLE, values, transferWhereClause, selectionArgs);
                break;
            case CONTACTS:
                updateCount = sqlDB.update(AndroLoggerDatabase.CONTACTS_TABLE, values, selection, selectionArgs);
                break;
            case CONTACT_ID:
                String contactID = uri.getLastPathSegment();
                String contactWhereClause = Contacts.ID + " = " + contactID;
                if (!TextUtils.isEmpty(selection))
                    contactWhereClause += " AND " + selection;
                updateCount = sqlDB.update(AndroLoggerDatabase.CONTACTS_TABLE, values, contactWhereClause, selectionArgs);
                break;
            case CALENDAR:
                updateCount = sqlDB.update(AndroLoggerDatabase.CALENDAR_TABLE, values, selection, selectionArgs);
                break;
            case CALENDAR_ID:
                String calendarID = uri.getLastPathSegment();
                String calendarWhereClause = Calendar.ID + " = " + calendarID;
                if (!TextUtils.isEmpty(selection))
                    calendarWhereClause += " AND " + selection;
                updateCount = sqlDB.update(AndroLoggerDatabase.CALENDAR_TABLE, values, calendarWhereClause, selectionArgs);
                break;
            case STATUS:
                updateCount = sqlDB.update(AndroLoggerDatabase.STATUS_TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (updateCount > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }
}
