package ua.in.danilichev.timelysms.app.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import ua.in.danilichev.timelysms.app.R;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private int mCallNumberOfOnTimeSetMethod = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        //without next 2 lines this method is called twice
        if (mCallNumberOfOnTimeSetMethod > 0) return;
        mCallNumberOfOnTimeSetMethod++;

        ActionListFragment fragment = (ActionListFragment) getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.fragmentActionList);
        fragment.onTimeSet(view, hourOfDay, minute);
    }
}
