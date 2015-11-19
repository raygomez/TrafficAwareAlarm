package com.silex.ragomez.trafficawarealarm;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.android.common.logger.Log;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public static final String TAG = "DatePickerFragment";
    int source;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        source = getArguments().getInt("source");
        Date date = new Date(getArguments().getLong("date", 0L));

        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() / (1000 * 24 * 3600) * (1000 * 24 * 3600));
        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Log.i(TAG, "Year: " + year);
        Log.i(TAG, "Month: " + (month + 1));
        Log.i(TAG, "Day: " + day);

        EditText textView = (EditText) getActivity().findViewById(source);
        textView.setText(year + "/" + (month + 1) + "/" + day) ;
    }
}