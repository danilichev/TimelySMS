package ua.in.danilichev.timelysms.app.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import ua.in.danilichev.timelysms.app.R;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private int mCallNumberOfOnDateSetMethod = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        //without next 2 lines this method is called twice
        if (mCallNumberOfOnDateSetMethod > 0) return;
        mCallNumberOfOnDateSetMethod++;

        ActionListFragment fragment = (ActionListFragment) getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.fragmentActionList);
        fragment.onDateSet(view, year, month, day);
    }
}
