package ua.in.danilichev.timelysms.app.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

public class SmsContentProvider extends ContentProvider {

    private SmsDbHelper mDatabase;

    private static final String CONTENT_AUTHORITY = "ua.in.danilichev.timelysms.app";

    private static final String BASE_PATH = "all_sms";

    public static final Uri CONTENT_URI = Uri.parse("content://" +
            CONTENT_AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/all_sms";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/sms";

    private static final int ALL_SMS = 11;
    private static final int SMS = 22;
    
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, BASE_PATH, ALL_SMS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, BASE_PATH + "/#", SMS);
    }

    @Override
    public boolean onCreate() {
        mDatabase = new SmsDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        checkColumns(projection);

        queryBuilder.setTables(SmsTable.TABLE_NAME);

        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case ALL_SMS:
                break;
            case SMS:
                queryBuilder.appendWhere(SmsTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = mDatabase.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs,
                null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
        long id;
        switch (uriType) {
            case ALL_SMS:
                id = sqlDB.insert(SmsTable.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse("content://" +
                CONTENT_AUTHORITY + "/" + BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
        int rowsDeleted;

        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case ALL_SMS:
                rowsDeleted = sqlDB.delete(SmsTable.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case SMS:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(SmsTable.TABLE_NAME,
                            SmsTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(SmsTable.TABLE_NAME,
                            SmsTable.COLUMN_ID + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
        int rowsUpdated;
        switch (uriType) {
            case ALL_SMS:
                rowsUpdated = sqlDB.update(SmsTable.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case SMS:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(SmsTable.TABLE_NAME,
                            values,
                            SmsTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(SmsTable.TABLE_NAME,
                            values,
                            SmsTable.COLUMN_ID + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {
                SmsTable.COLUMN_ID,
                SmsTable.COLUMN_NAME_OF_CONTACT,
                SmsTable.COLUMN_PHONE_NO,
                SmsTable.COLUMN_MESSAGE,
                SmsTable.COLUMN_STATUS,
                SmsTable.COLUMN_STATUS_UPDATE_DATE,
                SmsTable.COLUMN_STATUS_UPDATE_TIME };

        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
