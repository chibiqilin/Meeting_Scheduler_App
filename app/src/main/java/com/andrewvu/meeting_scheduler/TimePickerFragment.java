package com.andrewvu.meeting_scheduler;

import android.app.Dialog;
import android.app.TimePickerDialog;
import java.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

/**
 * Basic Time Picker Fragment which brings up the widget and writes to timeView.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        TextView textView = (TextView) getActivity().findViewById(R.id.timeView);
        if (minute < 10)
            textView.setText(hourOfDay+":0"+minute);
        else
            textView.setText(hourOfDay+":"+minute);
    }
}
