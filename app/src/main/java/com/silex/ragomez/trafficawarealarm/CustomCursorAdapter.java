package com.silex.ragomez.trafficawarealarm;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.silex.ragomez.trafficawarealarm.db.Alarm;
import com.silex.ragomez.trafficawarealarm.db.DatabaseHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by bgamboa on 12/4/2015.
 */
public class CustomCursorAdapter extends CursorAdapter{

    private static final String TAG = CustomCursorAdapter.class.getSimpleName();
    private static final String BLACK = "#000000";
    private static final DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
    private final AlarmUpdaterBroadcastReceiver alarmBroadcastReceiver = new AlarmUpdaterBroadcastReceiver();

    public CustomCursorAdapter(Context context, Cursor c){
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.alarm_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView alarmTime = (TextView) view.findViewById(R.id.alarm_item_time);
        TextView alarmText = (TextView) view.findViewById(R.id.alarm_item_text);
        final ToggleButton toggleButton = (ToggleButton) view.findViewById(R.id.alarm_item_toggle_button);

        // Extract properties from cursor
        long time = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEFAULT_ALARM));
        alarmTime.setTextColor(Color.parseColor(BLACK));
        alarmTime.setText(dateFormat.format(new Date(time)));

        String body = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NAME));
        alarmText.setTextColor(Color.parseColor(BLACK));
        alarmText.setText(body);

        int toggleStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_STATUS));
        toggleButton.setChecked(toggleStatus == 1);

        Alarm alarm = getAlarmFromCursor(cursor, context);
        toggleButton.setOnClickListener(new ToggleButtonListener(alarm, toggleButton, context));
    }

    private Alarm getAlarmFromCursor(Cursor cursor, Context context) {
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        return db.getAlarm(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)));
    }

    class ToggleButtonListener implements View.OnClickListener{

        private Alarm alarm;
        private ToggleButton toggleButton;
        private Context context;

        ToggleButtonListener(Alarm alarm, ToggleButton toggleButton, Context context){
            this.alarm = alarm;
            this.toggleButton = toggleButton;
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            if (toggleButton.isChecked()) {
                alarm.turnOn();
                DatabaseHandler db = DatabaseHandler.getInstance(context);
                db.updateAlarm(alarm);
                alarmBroadcastReceiver.createRepeatingAlarmTimer(context, alarm);

                Toast.makeText(context, "alarm has been turned on!",
                        Toast.LENGTH_SHORT).show();
            } else {
                alarm.turnOff();
                DatabaseHandler db = DatabaseHandler.getInstance(context);
                db.updateAlarm(alarm);
                alarmBroadcastReceiver.cancelAlarm(context, alarm);
                Toast.makeText(context, "alarm has been turned off!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
