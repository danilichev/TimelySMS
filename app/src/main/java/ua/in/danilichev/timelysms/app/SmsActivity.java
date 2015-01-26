package ua.in.danilichev.timelysms.app;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import ua.in.danilichev.timelysms.app.data.SmsTable;
import ua.in.danilichev.timelysms.app.fragments.ActionListFragment;
import ua.in.danilichev.timelysms.app.fragments.MessageFragment;
import ua.in.danilichev.timelysms.app.fragments.PhoneFragment;
import ua.in.danilichev.timelysms.app.fragments.SmsHistoryFragment;
import ua.in.danilichev.timelysms.app.helper.DateFormatHelper;
import ua.in.danilichev.timelysms.app.sms.SmsAlarmReceiver;
import ua.in.danilichev.timelysms.app.sms.SmsDbWriter;
import ua.in.danilichev.timelysms.app.sms.SmsService;

import java.util.Calendar;


public class SmsActivity extends FragmentActivity {

    private Uri mSmsUri;
    private SmsAlarmReceiver alarmReceiver;

    private PhoneFragment mPhoneFragment;
    private MessageFragment mMessageFragment;
    private ActionListFragment mActionFragment;
    private Button mButtonSubmit;

    private boolean mIsAlarmSet = false;
    private final int EDIT_SMS_REQUEST = 222;
    private final String LAST_SMS_URI = "lastSmsUri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        mPhoneFragment = (PhoneFragment) getSupportFragmentManager().
                findFragmentById(R.id.fragmentPhone);

        mMessageFragment = (MessageFragment) getSupportFragmentManager().
                findFragmentById(R.id.fragmentMessage);

        mActionFragment = (ActionListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentActionList);

        mButtonSubmit = (Button) findViewById(R.id.buttonSubmit);
        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                perform();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sms_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.showSmsHistoryActivity:
                Intent i = new Intent(this, SmsHistoryActivity.class);
                startActivityForResult(i, EDIT_SMS_REQUEST);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);

        if (reqCode != EDIT_SMS_REQUEST || resCode != Activity.RESULT_OK) return;

        String smsUriStr = data.getStringExtra(SmsHistoryFragment.INTENT_KEY);
        if (smsUriStr != null) {
            mSmsUri = Uri.parse(smsUriStr);
            fillFields(mSmsUri);
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSmsUri != null) outState.putString(LAST_SMS_URI, mSmsUri.toString());
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(LAST_SMS_URI)) {
            mSmsUri = Uri.parse(savedInstanceState.getString(LAST_SMS_URI));
        }
    }

    private void perform() {
        if (!isFieldsFilled()) return;

        String name = mPhoneFragment.getNameOfContact();
        String phoneNumber = mPhoneFragment.getPhoneNumber();
        String message = mMessageFragment.getMessage();

        alarmReceiver = new SmsAlarmReceiver();

        SmsDbWriter dbWriter = new SmsDbWriter(this, mSmsUri);

        mSmsUri = dbWriter.writeSms(name, phoneNumber, message);

        if (mIsAlarmSet) {
            alarmReceiver.cancelAlarm(SmsActivity.this, mSmsUri);
        }

        switch (mActionFragment.getCurrentAction()) {
            case SEND_NOW:
                startService(new Intent(this, SmsService.class).setData(mSmsUri));
                break;
            case SEND_IN_SET_TIME:
                Calendar sendingTime = mActionFragment.getSendingTime();
                alarmReceiver.setAlarm(
                        SmsActivity.this, sendingTime, mSmsUri);

                String statusSmsWillBeSent = getResources().getString(R.string.sms_will_be_sent);
                dbWriter.writeSmsState(statusSmsWillBeSent, sendingTime);

                Toast.makeText(this, mActionFragment.getSelectedItem(), Toast.LENGTH_LONG).show();
                break;
            case SAVE_AS_DRAFT:
                String statusSmsSaveAsDraft = getResources()
                        .getString(R.string.sms_saved_as_draft);
                dbWriter.writeSmsState(statusSmsSaveAsDraft, Calendar.getInstance());
                Toast.makeText(this, statusSmsSaveAsDraft, Toast.LENGTH_LONG).show();
                break;
        }

        restartActivity();
    }

    private void restartActivity() {
        Intent i = new Intent(SmsActivity.this, SmsActivity.class);
        finish();
        overridePendingTransition(0, 0);
        startActivity(i);
        overridePendingTransition(0, 0);
    }

    private boolean isFieldsFilled() {
        String phoneNumber = mPhoneFragment.getPhoneNumber();
        String message = mMessageFragment.getMessage();
        String field = "";

        if (phoneNumber.equals("")) {
            field = getString(R.string.phone_number);
        } else if (message.equals("")) {
            field = getString(R.string.your_number);
        }

        if (!field.equals("")) {
            Toast.makeText(this, getString(R.string.enter) + field +
                    getString(R.string.please), Toast.LENGTH_SHORT).show();
        }

        return field.equals("");
    }

    private void fillFields(Uri rowAddress) {
        ContentResolver resolver = getContentResolver();
        String[] resultColumns = new String[] {
                SmsTable.COLUMN_NAME_OF_CONTACT,
                SmsTable.COLUMN_PHONE_NO,
                SmsTable.COLUMN_MESSAGE,
                SmsTable.COLUMN_STATUS,
                SmsTable.COLUMN_STATUS_UPDATE_DATE,
                SmsTable.COLUMN_STATUS_UPDATE_TIME };

        Cursor cursor = resolver.query(rowAddress, resultColumns, null, null, null);
        cursor.moveToFirst();

        int COLUMN_NAME_INDEX = cursor.getColumnIndexOrThrow(
                SmsTable.COLUMN_NAME_OF_CONTACT);
        int COLUMN_PHONE_NO_INDEX = cursor.getColumnIndexOrThrow(
                SmsTable.COLUMN_PHONE_NO);
        int COLUMN_MESSAGE_INDEX = cursor.getColumnIndexOrThrow(
                SmsTable.COLUMN_MESSAGE);
        int COLUMN_STATUS_OF_SENDING = cursor.getColumnIndexOrThrow(
                SmsTable.COLUMN_STATUS);
        int COLUMN_DATE_OF_SENDING = cursor.getColumnIndexOrThrow(
                SmsTable.COLUMN_STATUS_UPDATE_DATE);
        int COLUMN_TIME_OF_SENDING = cursor.getColumnIndexOrThrow(
                SmsTable.COLUMN_STATUS_UPDATE_TIME);

        mPhoneFragment.setNameOfContact(cursor.getString(COLUMN_NAME_INDEX));
        mPhoneFragment.setPhoneNumber(cursor.getString(COLUMN_PHONE_NO_INDEX));
        mMessageFragment.setMessage(cursor.getString(COLUMN_MESSAGE_INDEX));

        String statusSmsWillBeSent = getResources().getString(R.string.sms_will_be_sent);
        if (cursor.getString(COLUMN_STATUS_OF_SENDING).equals(statusSmsWillBeSent)) {
            String timeOfSendingStr = statusSmsWillBeSent +
                    cursor.getString(COLUMN_DATE_OF_SENDING) +
                    getResources().getString(R.string.at) +
                    cursor.getString(COLUMN_TIME_OF_SENDING);

            mActionFragment.addNewItemToActionList(timeOfSendingStr);
            mActionFragment.setAction(ActionListFragment.ActionWithSms.SEND_IN_SET_TIME);

            String dateOfSending = cursor.getString(COLUMN_DATE_OF_SENDING);
            String timeOfSending = cursor.getString(COLUMN_TIME_OF_SENDING);
            Calendar sendingTimeCalendar = DateFormatHelper
                    .getCalendar(dateOfSending, timeOfSending);
            mActionFragment.setSendingTime(sendingTimeCalendar);

            mIsAlarmSet = true;
        }
    }

}
