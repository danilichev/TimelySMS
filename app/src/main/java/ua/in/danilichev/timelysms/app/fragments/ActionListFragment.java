package ua.in.danilichev.timelysms.app.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ua.in.danilichev.timelysms.app.R;
import ua.in.danilichev.timelysms.app.helper.DateFormatHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class ActionListFragment extends Fragment implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    ArrayList<String> actionList;
    ArrayAdapter<String> actionListAdapter;
    Spinner spinnerAction;

    private Calendar mSendingTime = null;

    private ActionWithSms mCurrentAction;

    public enum ActionWithSms {
        SEND_NOW, SEND_IN_SET_TIME, SAVE_AS_DRAFT
    }

    private final int SEND_NOW = 11;
    private final int SEND_IN_SET_TIME = 22;
    private final int SAVE_AS_DRAFT = 33;

    private final String LAST_SPINNER_ITEM = "lastSpinnerItem";
    private final String LAST_CALENDAR_VALUE = "lastCalendarValue";
    private final String LAST_ACTION = "lastAction";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentAction = ActionWithSms.SEND_NOW;

        actionList = new ArrayList<String>(Arrays.asList(getResources()
                .getStringArray(R.array.action_list)));

        actionListAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.spinner_item,
                actionList);
        actionListAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        if (savedInstanceState != null) {
            switch (savedInstanceState.getInt(LAST_ACTION)) {
                case SEND_NOW:
                    mCurrentAction = ActionWithSms.SEND_NOW;
                    break;
                case SEND_IN_SET_TIME:
                    mCurrentAction = ActionWithSms.SEND_IN_SET_TIME;
                    break;
                case SAVE_AS_DRAFT:
                    mCurrentAction = ActionWithSms.SAVE_AS_DRAFT;
            }

            mSendingTime = Calendar.getInstance();
            mSendingTime.setTimeInMillis(savedInstanceState.getLong(LAST_CALENDAR_VALUE));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_action_list, container, false);

        spinnerAction = (Spinner) view.findViewById(R.id.spinnerActionList);
        spinnerAction.setAdapter(actionListAdapter);
        spinnerAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent, View view, int position, long id) {

                String choice = actionList.get(position);
                if (choice.equals(getResources().getString(R.string.item_save_draft))) {
                    mCurrentAction = ActionWithSms.SAVE_AS_DRAFT;
                    cleanActionListFromNewItems();
                    return;
                }
                if (choice.equals(getResources().getString(R.string.item_edit_time))) {
                    mCurrentAction = ActionWithSms.SEND_IN_SET_TIME;
                    cleanActionListFromNewItems();
                    DatePickerFragment datePicker = new DatePickerFragment();
                    datePicker.show(getActivity().getSupportFragmentManager(), "datePicker");
                    return;
                }
                if (choice.equals(getResources().getString(R.string.item_send_now))) {
                    mCurrentAction = ActionWithSms.SEND_NOW;
                    cleanActionListFromNewItems();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(LAST_SPINNER_ITEM)) {
            addNewItemToActionList(savedInstanceState.getString(LAST_SPINNER_ITEM));
        }

        return view;
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LAST_SPINNER_ITEM, getSelectedItem());

        int action = 0;
        switch (mCurrentAction) {
            case SEND_NOW:
                action = SEND_NOW;
                break;
            case SEND_IN_SET_TIME:
                action = SEND_IN_SET_TIME;
                break;
            case SAVE_AS_DRAFT:
                action = SAVE_AS_DRAFT;
        }
        outState.putInt(LAST_ACTION, action);

        if (mSendingTime != null) {
            outState.putLong(LAST_CALENDAR_VALUE, mSendingTime.getTimeInMillis());
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        setSendingDate(year, monthOfYear, dayOfMonth);

        TimePickerFragment timePicker = new TimePickerFragment();
        timePicker.show(getActivity().getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        setSendingTime(hourOfDay, minute);

        String statusSmsWillBeSent = getResources().getString(R.string.sms_will_be_sent);
        String newItem = statusSmsWillBeSent + " " +
                DateFormatHelper.getDate(mSendingTime) + " " +
                getResources().getString(R.string.at) + " " +
                DateFormatHelper.getTime(mSendingTime);
        addNewItemToActionList(newItem);
    }

    private void setSendingDate(int year, int month, int day) {
        if (mSendingTime == null) mSendingTime = Calendar.getInstance();
        mSendingTime.set(Calendar.YEAR, year);
        mSendingTime.set(Calendar.MONTH, month);
        mSendingTime.set(Calendar.DAY_OF_MONTH, day);
    }

    private void setSendingTime(int hour, int minute) {
        if (mSendingTime == null) mSendingTime = Calendar.getInstance();
        mSendingTime.set(Calendar.HOUR_OF_DAY, hour);
        mSendingTime.set(Calendar.MINUTE, minute);
    }

    public void setSendingTime(Calendar sendingTime) {
        mSendingTime = sendingTime;
    }

    public Calendar getSendingTime() {
        return mSendingTime;
    }

    public ActionWithSms getCurrentAction() { return mCurrentAction; }

    public void setAction(ActionWithSms action) { mCurrentAction = action; }

    public void cleanActionListFromNewItems() {
        String[] actionArray = getResources()
                .getStringArray(R.array.action_list);
        if (actionList.size() != actionArray.length) {
            actionList.clear();
            for (int i = 0; i < actionArray.length; i++) {
                actionList.add(i, actionArray[i]);
            }
        }
        actionListAdapter.notifyDataSetChanged();

        if (mSendingTime != null) mSendingTime = null;
    }

    public void addNewItemToActionList(String item) {
        int newPosition = actionList.size();
        actionList.add(newPosition, item);
        actionListAdapter.notifyDataSetChanged();
        spinnerAction.setSelection(newPosition);
    }

    public String getSelectedItem() {
        return spinnerAction.getSelectedItem().toString();
    }
}
