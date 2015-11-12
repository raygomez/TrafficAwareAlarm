package com.silex.ragomez.trafficawarealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.logger.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmUpdaterBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "AlarmUpdaterService";

    public void createRepeatingAlarmTimer(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmUpdaterService.class);
        intent.putExtra("originLatitude", "14.657486");
        intent.putExtra("originLongitude", "121.056115");
        intent.putExtra("destinationLatitude", "14.598866");
        intent.putExtra("destinationLongitude", "120.983778");
        intent.putExtra("targetAlarmDate", System.currentTimeMillis()+15000);
        intent.putExtra("defaultDate", System.currentTimeMillis()+1000);

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000,
                3000, pi);
        Log.i("Timer", "createRepeatingAlarmTimer started:" + new Date().toString());

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String originLatitude = intent.getStringExtra("originLatitude");
        String originLongitude = intent.getStringExtra("originLongitude");
        String destinationLatitude = intent.getStringExtra("destinationLatitude");
        String destinationLongitude = intent.getStringExtra("destinationLongitude");
        Long targetAlarmDate = intent.getLongExtra("targetAlarmDate", 0L);
        Long defaultDate = intent.getLongExtra("defaultDate",0L);

        int tripDuration = pollServer(originLatitude, originLongitude, destinationLatitude, destinationLongitude);
        Log.i(TAG, String.format("onReceive: http://silex-archnat.rhcloud.com/rest/api/v1/compute_travel_time?to=%s,%s&from=%s,%s", destinationLatitude, destinationLongitude, originLatitude, originLongitude));
        computeEstimatedWakeUpTime(tripDuration, targetAlarmDate, defaultDate, context);
    }

    private int pollServer(String originLatitude, String originLongitude, String destinationLatitude, String destinationLongitude) {
        String urlStr = String.format("http://silex-archnat.rhcloud.com/rest/api/v1/compute_travel_time?to=%s,%s&from=%s,%s",
                destinationLatitude, destinationLongitude, originLatitude, originLongitude);
        android.util.Log.i("urlConnection", " gonna connect to " + urlStr);
        InputStream inputStream;
        String result = "";
        URL url;
        int tripDuration = 0;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            if (!url.getHost().equals(urlConnection.getURL().getHost())) {
                android.util.Log.i("urlConnection", "we were redirected! Kick the user out to the browser to sign on?");
            }
            int responseCode = urlConnection.getResponseCode();
            System.out.println("urlConnection.responseCode: " + responseCode);
            System.out.println("urlConnection.connection.getResponseMessage(): " + urlConnection.getResponseMessage());
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("nice! urlConnection.responseCode: " + responseCode);
                result = convertInputStreamToString(inputStream);
                JSONObject json = new JSONObject(result);
                tripDuration = json.getInt("duration_in_seconds");

            } else {
                android.util.Log.v("CatalogClient", "Response code:" + responseCode);
                result = "oh noes";
            }
            Log.i("alarm", result);

        } catch (Exception e) {
            android.util.Log.i("urlConnection", "urlConnection exception occurred");
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return tripDuration;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }


    private void computeEstimatedWakeUpTime(int duration, long targetDate, long defaultDate, Context context) {
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd hh : mm a");

//            Date targetDate = format.parse(target_date.getText().toString() + ' ' +
//                    target_time.getText().toString());

//            Date targetAlarmDate = new Date(targetDate.getTime() - 1000 * duration);
//            Date defaultDate = format.parse(default_date.getText().toString() + ' ' +
//                    default_time.getText().toString());

            Log.i(TAG, "Travel Time: " + duration/60 + " minutes");
            long targetAlarmDate = targetDate -  duration;
            Log.i(TAG, "Target Alarm Date: " + targetAlarmDate);
            Log.i(TAG, "Default Alarm Date: " + defaultDate);

            long actualAlarm;
            if(targetAlarmDate < defaultDate) {
                actualAlarm = targetAlarmDate;
            } else {
                actualAlarm = defaultDate;
            }
            Log.i(TAG, "new actualAlarm.getTime(): " + actualAlarm);
//            setOneTimeTimer(actualAlarm.getTime());

    }
}