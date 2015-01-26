package ua.in.danilichev.timelysms.app.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.Toast;
import ua.in.danilichev.timelysms.app.R;
import ua.in.danilichev.timelysms.app.data.SmsTable;

import java.util.Calendar;

/**
 * Sends a message to specified phone number
 */

public class SmsSender {

    private String mPhoneNumber;
    private String mMessage;
    private String mSmsState;
    private Context mContext;
    private Uri mSmsUri;

    public SmsSender(Context context, Uri smsUri) {
        this.mSmsUri = smsUri;
        this.mContext = context;

        String[] resultColumns = new String[] {
                SmsTable.COLUMN_PHONE_NO, SmsTable.COLUMN_MESSAGE  };
        ContentResolver resolver = mContext.getContentResolver();
        Cursor cursor = resolver.query(mSmsUri, resultColumns, null, null, null);
        cursor.moveToFirst();
        int COLUMN_PHONE_NO_INDEX = cursor.getColumnIndexOrThrow(
                SmsTable.COLUMN_PHONE_NO);
        int COLUMN_MESSAGE_INDEX = cursor.getColumnIndexOrThrow(
                SmsTable.COLUMN_MESSAGE);
        mPhoneNumber = cursor.getString(COLUMN_PHONE_NO_INDEX);
        mMessage = cursor.getString(COLUMN_MESSAGE_INDEX);
        cursor.close();
    }

    public void send() {
        String SMS_SENT = "SMS sent";
        PendingIntent pendingIntentSent = PendingIntent
                .getBroadcast(mContext, 0, new Intent(SMS_SENT), 0);

        String DELIVERED = "SMS delivered";
        PendingIntent pendingIntentDelivered = PendingIntent
                .getBroadcast(mContext, 0, new Intent(DELIVERED), 0);

        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        mSmsState = mContext.getResources().getString(R.string.sms_sent);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        mSmsState = mContext.getResources().getString(R.string.generic_failure);
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        mSmsState = mContext.getResources().getString(R.string.no_service);;
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        mSmsState = mContext.getResources().getString(R.string.null_pdu);
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        mSmsState = mContext.getResources().getString(R.string.radio_off);
                        break;
                    default:
                        mSmsState = mContext.getResources().getString(R.string.sms_will_be_sent);
                        break;
                }

                updateDataOfSmsState();

            }
        }, new IntentFilter(SMS_SENT));

        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        mSmsState = mContext.getResources().getString(R.string.sms_delivered);
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        mSmsState = mContext.getResources().getString(R.string.delivery_expected);;
                        break;
                }

                updateDataOfSmsState();
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(mPhoneNumber, null, mMessage,
                pendingIntentSent, pendingIntentDelivered);
    }

    private void updateDataOfSmsState() {
        SmsDbWriter smsDbWriter = new SmsDbWriter(mContext, mSmsUri);
        smsDbWriter.writeSmsState(mSmsState, Calendar.getInstance());

        Toast.makeText(mContext, mSmsState, Toast.LENGTH_LONG).show();
    }
}
