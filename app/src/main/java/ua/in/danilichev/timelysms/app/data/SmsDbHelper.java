package ua.in.danilichev.timelysms.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Calls the static methods of SmsTable.
 */
public class SmsDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "sms_history.db";
    private static final int DATABASE_VERSION = 1;

    public SmsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        SmsTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SmsTable.onUpgrade(db, oldVersion, newVersion);
    }
}
