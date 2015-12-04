package com.silex.ragomez.trafficawarealarm;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.silex.ragomez.trafficawarealarm.db.DatabaseHandler;

/**
 * Created by bgamboa on 12/4/2015.
 */
public class CustomCursorAdapter extends CursorAdapter{

    private static final String TAG = CustomCursorAdapter.class.getSimpleName();
    private static final String BLACK = "#000000";

    public CustomCursorAdapter(Context context, Cursor c){
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.alarm_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView alarmText = (TextView) view.findViewById(R.id.alarm_item_text);
        final ToggleButton toggleButton = (ToggleButton) view.findViewById(R.id.alarm_item_toggle_button);

        // Extract properties from cursor
        String body = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NAME));
        alarmText.setTextColor(Color.parseColor(BLACK));
        alarmText.setText(body);

        int toggleStatus = cursor.getInt(cursor.getColumnIndexOrThrow("status"));
        toggleButton.setChecked(toggleStatus == 1);
        toggleButton.setOnClickListener(new ToggleButtonListener(toggleButton, context));
    }

    class ToggleButtonListener implements View.OnClickListener{
        private ToggleButton toggleButton;
        private Context context;

        ToggleButtonListener(ToggleButton toggleButton, Context context){
            this.toggleButton = toggleButton;
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            if (toggleButton.isChecked()) {
                Toast.makeText(context, "alarm has been turned on!",
                        Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(context, "alarm has been turned off!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
