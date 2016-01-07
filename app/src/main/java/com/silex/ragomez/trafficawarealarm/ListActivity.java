package com.silex.ragomez.trafficawarealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.silex.ragomez.trafficawarealarm.db.DatabaseHandler;

public class ListActivity extends AppCompatActivity {

    private static final String TAG = ListActivity.class.getSimpleName();
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        displayListView();

        IntentFilter filter = new IntentFilter(CONNECTIVITY_CHANGE_ACTION);
        this.registerReceiver(changeWifiReceiver, filter);
        adjustInternetConnectivityText(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(changeWifiReceiver != null) {
            unregisterReceiver(changeWifiReceiver);
        }
    }

    private void displayListView() {

        Cursor cursor = DatabaseHandler.getInstance(this).fetchAllAlarms();

        CursorAdapter dataAdapter = new CustomCursorAdapter(getApplicationContext(), cursor);

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
                intent.putExtra(DatabaseHandler.KEY_STATUS, cursor.getInt(cursor.getColumnIndex(DatabaseHandler.KEY_STATUS)));

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

        CursorAdapter dataAdapter = new CustomCursorAdapter(getApplicationContext(), cursor);
        listView.setAdapter(dataAdapter);
    }

    final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    private final BroadcastReceiver changeWifiReceiver = new BroadcastReceiver() {
        public static final String CONNECTED = "Connected";
        public static final String NOT_CONNECTED = "No Internet Connection";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (CONNECTIVITY_CHANGE_ACTION.equals(action))
            {
                adjustInternetConnectivityText(context);
            }
        }
    };

    private void adjustInternetConnectivityText(Context context) {
        TextView internetConnectivityTextView = (TextView) findViewById(R.id.internetConnectivity);
        com.example.android.common.logger.Log.i(TAG, "isConnected(context):" + isConnected(context));
        if(isConnected(context) && internetConnectivityTextView.getVisibility() == View.VISIBLE)
        {
            internetConnectivityTextView.setVisibility(View.GONE);
        }
        else if(!isConnected(context) && internetConnectivityTextView.getVisibility() == View.GONE)
        {
            internetConnectivityTextView.setVisibility(View.VISIBLE);
        }
    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        return isConnected;
    }
}
