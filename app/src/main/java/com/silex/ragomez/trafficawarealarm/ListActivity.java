package com.silex.ragomez.trafficawarealarm;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.silex.ragomez.trafficawarealarm.db.DatabaseHandler;

public class ListActivity extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        displayListView();

    }

    private void displayListView() {

        Cursor cursor = DatabaseHandler.getInstance(this).fetchAllAlarms();

        SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(this, R.layout.listview_each_alarm, cursor,
                new String[]{ DatabaseHandler.KEY_NAME },
                new int[]{ R.id.alarm_item }, 0);

        listView = (ListView) findViewById(R.id.alarm_list_view);
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                intent.putExtra(DatabaseHandler.KEY_ID, cursor.getLong(cursor.getColumnIndex(DatabaseHandler.KEY_ID)));
                intent.putExtra(DatabaseHandler.KEY_NAME, cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_NAME)));
                intent.putExtra(DatabaseHandler.KEY_ORIGIN, cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_ORIGIN)));
                intent.putExtra(DatabaseHandler.KEY_ORIGIN_LAT, cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_ORIGIN_LAT)));
                intent.putExtra(DatabaseHandler.KEY_ORIGIN_LONG, cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_ORIGIN_LONG)));
                intent.putExtra(DatabaseHandler.KEY_DEST, cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_DEST)));
                intent.putExtra(DatabaseHandler.KEY_DEST_LAT, cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_DEST_LAT)));
                intent.putExtra(DatabaseHandler.KEY_DEST_LONG, cursor.getDouble(cursor.getColumnIndex(DatabaseHandler.KEY_DEST_LONG)));
                intent.putExtra(DatabaseHandler.KEY_PREP_TIME, cursor.getInt(cursor.getColumnIndex(DatabaseHandler.KEY_PREP_TIME)));
                intent.putExtra(DatabaseHandler.KEY_DEFAULT_ALARM, cursor.getLong(cursor.getColumnIndex(DatabaseHandler.KEY_DEFAULT_ALARM)));
                intent.putExtra(DatabaseHandler.KEY_ETA, cursor.getLong(cursor.getColumnIndex(DatabaseHandler.KEY_ETA)));

                startActivity(intent);
            }
        });
    }

    public void loadMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        Cursor cursor = DatabaseHandler.getInstance(this).fetchAllAlarms();

        SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(this, R.layout.listview_each_alarm, cursor,
                new String[]{ DatabaseHandler.KEY_NAME },
                new int[]{ R.id.alarm_item }, 0);

        listView.setAdapter(dataAdapter);
    }
}
