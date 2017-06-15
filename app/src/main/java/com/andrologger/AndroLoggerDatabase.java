package com.andrologger;
/**
 * AndroLoggerDatabase.java
 *
 * @author Pradeep Tiwari
 */

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/** This class provides the schemas for the AndroLogger database tables. **/
public class AndroLoggerDatabase extends SQLiteOpenHelper {
    public static final String TAG = "AndroLoggerDatabase";
    public static final String DATABASE_NAME = "results.db";
    public static final int DATABASE_VERSION = 17;
    public static final String TRANSFERS_TABLE = "transfers";
    public static final String TRANSFER_ID_COLUMN = "_id";
    public static final String TRANSFERS_COMPLETED_COLUMN = "transfer_complete";
    public static final String TRANSFERS_STARTDATE_COLUMN = "transfer_start_time";
    public static final String TRANSFERS_DEVICE_ID_COLUMN = "device_id";
    private static final String TRANSFERS_TABLE_CREATE =
            "CREATE TABLE " + TRANSFERS_TABLE + " (" +
                    TRANSFER_ID_COLUMN + " INTEGER primary key, " +
                    TRANSFERS_COMPLETED_COLUMN + " BOOLEAN default 0, " +
                    TRANSFERS_STARTDATE_COLUMN + " DATETIME default (strftime('%s', 'now')), " +
                    TRANSFERS_DEVICE_ID_COLUMN + " TEXT);";
    public static final String EVENTS_TABLE = "events";
    public static final String EVENT_ID_COLUMN = "_id";
    public static final String DETECTOR_COLUMN = "detector";
    public static final String DETECT_DATE_COLUMN = "detected";
    public static final String EVENT_ACTION_COLUMN = "action";
    public static final String EVENT_DATE_COLUMN = "event_occurred";
    public static final String EVENT_DESCRIPTION_COLUMN = "description";
    public static final String ADDITIONAL_INFO_COLUMN = "additional_info";
    private static final String EVENTS_TABLE_CREATE =
            "CREATE TABLE " + EVENTS_TABLE + " (" +
                    EVENT_ID_COLUMN + " INTEGER primary key, " +
                    DETECTOR_COLUMN + " TEXT, " +
                    DETECT_DATE_COLUMN + " DATETIME default (strftime('%s', 'now')), " +
                    EVENT_ACTION_COLUMN + " TEXT, " +
                    EVENT_DATE_COLUMN + " DATETIME, " +
                    EVENT_DESCRIPTION_COLUMN + " TEXT," +
                    ADDITIONAL_INFO_COLUMN + " TEXT);";
    public static final String CALENDAR_TABLE = "calendar";
    public static final String CALENDAR_ID_PKEY_COLUMN = "_id";
    public static final String CALENDAR_EVENT_ID_COLUMN = "event_id";
    public static final String CALENDAR_EVENT_NAME_COLUMN = "name";
    public static final String CALENDAR_EVENT_DATE_COLUMN = "date";
    public static final String CALENDAR_EVENT_ADDED_COLUMN = "added";
    public static final String CALENDAR_TABLE_CREATE =
            "CREATE TABLE " + CALENDAR_TABLE + " (" +
                    CALENDAR_ID_PKEY_COLUMN + " INTEGER primary key, " +
                    CALENDAR_EVENT_ID_COLUMN + " INTEGER, " +
                    CALENDAR_EVENT_NAME_COLUMN + " TEXT, " +
                    CALENDAR_EVENT_DATE_COLUMN + " DATETIME, " +
                    CALENDAR_EVENT_ADDED_COLUMN + " DATETIME);";
    public static final String CONTACTS_TABLE = "contacts";
    public static final String CONTACT_ID_PKEY_COLUMN = "_id";
    public static final String CONTACT_ID_COLUMN = "contact_id";
    public static final String CONTACT_NUMBER_COLUMN = "number";
    public static final String CONTACT_NAME_COLUMN = "name";
    public static final String CONTACT_ADDED_COLUMN = "added";
    private static final String CONTACTS_TABLE_CREATE =
            "CREATE TABLE " + CONTACTS_TABLE + " (" +
                    CONTACT_ID_PKEY_COLUMN + " INTEGER primary key, " +
                    CONTACT_ID_COLUMN + " INTEGER, " +
                    CONTACT_NUMBER_COLUMN + " TEXT, " +
                    CONTACT_NAME_COLUMN + " TEXT, " +
                    CONTACT_ADDED_COLUMN + " DATETIME);";

    //Table for Call Data
    public static final String CDR_TABLE = "cdr";
    public static final String CDR_ID_PKEY_COLUMN = "_id";
    public static final String CDR_CALL_ID = "call_id";
    public static final String CDR_TYPE = "call_type";
    public static final String CDR_NUMBER = "number";
    public static final String CDR_NAME = "name";
    public static final String CDR_DURATION = "duration";
    public static final String CDR_NO_TYPE = "no_type";
    private static final String CDR_TABLE_CREATE =
            "CREATE TABLE " + CDR_TABLE + " (" +
                    CDR_ID_PKEY_COLUMN + " INTEGER primary key, " +
                    CDR_CALL_ID + " INTEGER, " +
                    CDR_TYPE + " TEXT, " +
                    CDR_NUMBER + " TEXT, " +
                    CDR_NAME + " TEXT, " +
                    CDR_DURATION + " INTEGER, " +
                    CDR_NO_TYPE + " INTEGER);";

    //--------------------------
    //Table for Browser Data
    public static final String BSR_TABLE = "bsr";
    public static final String BSR_ID_PKEY_COLUMN = "_id";
    public static final String BSR_Action = "bsr_action";  //whether browser search/browser navigation
    public static final String BSR_DESCRIPTION = "bsr_description"; //title of the page or keyword of search
    public static final String BSR_URL = "bsr_urlname"; // name of url
    private static final String BSR_TABLE_CREATE =
            "CREATE TABLE " + BSR_TABLE + " (" +
                    BSR_ID_PKEY_COLUMN + " INTEGER primary key, " +
                    BSR_Action + " TEXT, " +
                    BSR_DESCRIPTION + " TEXT, " +
                    BSR_URL + " TEXT);";

    //--------------------------
    //Table for SMS Data
    public static final String SMS_TABLE = "sms";
    public static final String SMS_ID_PKEY_COLUMN = "_id";
    public static final String SMS_Action = "sms_action";  //whether sent/received
    public static final String SMS_SENDER_RECEIVER = "sms_name";
    public static final String SMS_CONTENT = "sms_content"; //body of msg
    private static final String SMS_TABLE_CREATE =
            "CREATE TABLE " + SMS_TABLE + " (" +
                    SMS_ID_PKEY_COLUMN + " INTEGER primary key, " +
                    SMS_Action + " TEXT, " +
                    SMS_SENDER_RECEIVER + " TEXT, " +
                    SMS_CONTENT + " TEXT);";

    //--------------------------
    //Table for location Data
    public static final String LOCATION_TABLE = "location";
    public static final String LOCATION_ID_PKEY_COLUMN = "_id";
    public static final String LOCATION_LAT = "location_lat";
    public static final String LOCATION_LONG = "location_long";
    private static final String LOCATION_TABLE_CREATE =
            "CREATE TABLE " + LOCATION_TABLE + " (" +
                    LOCATION_ID_PKEY_COLUMN + " INTEGER primary key, " +
                    LOCATION_LAT + " TEXT, " +
                    LOCATION_LONG + " TEXT);";

    //--------------------------

    //Table for questions and answers
    public static final String QUES_TABLE = "questions";
    public static final String QUES_ID_PKEY_COLUMN = "_id";
    public static final String QUES_DETECT_DATE_COLUMN = "detected";
    public static final String QUES_TYPE = "question_type";
    public static final String QUES = "ques";
    public static final String QUES_ANSWER = "question_answer";
    public static final String QUES_LOG_TYPE = "question_log_type";
    public static final String QUES_DIFFICULTY = "difficulty";
    private static final String QUES_TABLE_CREATE =
            "CREATE TABLE " + QUES_TABLE + " (" +
                    QUES_ID_PKEY_COLUMN + " INTEGER primary key, " +
                    QUES_DETECT_DATE_COLUMN + " DATETIME default (strftime('%s', 'now')), " +
                    QUES_TYPE + " INTEGER, " +
                    QUES + " TEXT, " +
                    QUES_ANSWER + " TEXT, " +
                    QUES_LOG_TYPE + " INTEGER, " +
                    QUES_DIFFICULTY + " INTEGER);";
    //--------------------------------------------

    public static final String STATUS_TABLE = "status";
    public static final String CONTACTS_FILLED_FLAG_COLUMN = "contacts_is_filled";
    public static final String CALENDAR_FILLED_FLAG_COLUMN = "calendar_is_filled";
    private static final String STATUS_TABLE_CREATE =
            "CREATE TABLE " + STATUS_TABLE + " (" +
                    CONTACTS_FILLED_FLAG_COLUMN + " BOOLEAN NOT NULL, " +
                    CALENDAR_FILLED_FLAG_COLUMN + " BOOLEAN NOT NULL);";
    private static final String STATUS_TABLE_INSERT =
            "INSERT INTO " + STATUS_TABLE + " (" +
                    CONTACTS_FILLED_FLAG_COLUMN + "," + CALENDAR_FILLED_FLAG_COLUMN + ") VALUES (0,0);";

    /**
     * Constructor required for SQLiteOpenHelper
     *
     * @param c The application context.
     */
    public AndroLoggerDatabase(Context c) {
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This method creates the AndroLogger SQLite database.
     *
     * @param db The database to create.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w(TAG, "Upgrading database");
        db.execSQL(TRANSFERS_TABLE_CREATE);
        db.execSQL(EVENTS_TABLE_CREATE);
        db.execSQL(CONTACTS_TABLE_CREATE);
        db.execSQL(CALENDAR_TABLE_CREATE);
        db.execSQL(STATUS_TABLE_CREATE);
        db.execSQL(STATUS_TABLE_INSERT);
        db.execSQL(CDR_TABLE_CREATE);
        db.execSQL(QUES_TABLE_CREATE);
        db.execSQL(BSR_TABLE_CREATE);
        db.execSQL(SMS_TABLE_CREATE);
        db.execSQL(LOCATION_TABLE_CREATE);
    }

    /**
     * This method upgrades an existing AndroLogger SQLite database.
     *
     * @param db The database to upgrade.
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TRANSFERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CALENDAR_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + STATUS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CDR_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + QUES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + BSR_TABLE_CREATE);
        db.execSQL("DROP TABLE IF EXISTS " + SMS_TABLE_CREATE);
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE_CREATE);
        onCreate(db);
    }

    public ArrayList<Cursor> getData(String Query) {
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[]{"mesage"};
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);
        try {
            String maxQuery = Query;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);
            //add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});
            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {
                alc.set(0, c);
                c.moveToFirst();
                return alc;
            }
            return alc;
        } catch (SQLException sqlEx) {
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        } catch (Exception ex) {
            Log.d("printing exception", ex.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }
    }
}
