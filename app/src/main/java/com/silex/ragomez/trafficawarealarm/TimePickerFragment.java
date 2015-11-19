package com.silex.ragomez.trafficawarealarm;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TimePicker;

import com.example.android.common.logger.Log;

import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    int source;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        Date date = new Date(getArguments().getLong("time", 0L));

        c.setTime(date);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        source = getArguments().getInt("source");

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        EditText textView = (EditText) getActivity().findViewById(source);

        String am_pm;
        int hour;

        if(hourOfDay >= 12) {
            hour = hourOfDay != 12 ? hourOfDay - 12 : 12;
            am_pm = "PM";
        } else {
            hour = hourOfDay != 0 ? hourOfDay : 12;
            am_pm = "AM";
        }

        textView.setText(String.format("%02d", hour) + " : " + String.format("%02d", minute) + " " + am_pm) ;

    }
}