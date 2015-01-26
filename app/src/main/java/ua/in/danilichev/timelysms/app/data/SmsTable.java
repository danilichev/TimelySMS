package ua.in.danilichev.timelysms.app.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Defines table and constants for the table name and the columns.
 */
public class SmsTable {

    //Database table
    public static final String TABLE_NAME = "sms_history";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME_OF_CONTACT = "name_of_contact";
    public static final String COLUMN_PHONE_NO = "phone_no";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_STATUS_UPDATE_DATE = "status_update_date";
    public static final String COLUMN_STATUS_UPDATE_TIME = "status_update_time";

    //Database creation SQL statement
    private static final String DATABASE_CREATE = "create table " +
            TABLE_NAME + " (" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_NAME_OF_CONTACT + " text null, " +
            COLUMN_PHONE_NO + " text not null, " +
            COLUMN_MESSAGE + " text not null, " +
            COLUMN_STATUS + " text null, " +
            COLUMN_STATUS_UPDATE_DATE + " text null, " +
            COLUMN_STATUS_UPDATE_TIME + " text null);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(
            SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(SmsTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }
}
