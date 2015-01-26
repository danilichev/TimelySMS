package ua.in.danilichev.timelysms.app.sms;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import ua.in.danilichev.timelysms.app.data.SmsContentProvider;
import ua.in.danilichev.timelysms.app.data.SmsTable;
import ua.in.danilichev.timelysms.app.helper.DateFormatHelper;

import java.util.Calendar;

public class SmsDbWriter {

    private Context mContext;
    private Uri mSmsUri;

    public SmsDbWriter(Context context, Uri uri) {
        mContext = context;
        mSmsUri = uri;
    }

    public Uri writeSms(String name, String phoneNumber, String message) {
        ContentValues values = new ContentValues();
        values.put(SmsTable.COLUMN_NAME_OF_CONTACT, name);
        values.put(SmsTable.COLUMN_PHONE_NO, phoneNumber);
        values.put(SmsTable.COLUMN_MESSAGE, message);

        if (mSmsUri == null) mSmsUri = mContext.getContentResolver()
                .insert(SmsContentProvider.CONTENT_URI, values);

        mContext.getContentResolver().update(mSmsUri, values, null, null);

        return mSmsUri;
    }

    public void writeSmsState(String state, Calendar calendar) {
        ContentValues values = new ContentValues();
        values.put(SmsTable.COLUMN_STATUS, state);
        values.put(SmsTable.COLUMN_STATUS_UPDATE_DATE,
                DateFormatHelper.getDate(calendar));
        values.put(SmsTable.COLUMN_STATUS_UPDATE_TIME,
                DateFormatHelper.getTime(calendar));

        mContext.getContentResolver().update(mSmsUri, values, null, null);
    }
}
