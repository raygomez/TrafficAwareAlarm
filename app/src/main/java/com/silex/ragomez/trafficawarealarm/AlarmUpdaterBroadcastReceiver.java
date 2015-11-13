package com.silex.ragomez.trafficawarealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.example.android.common.logger.Log;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class AlarmUpdaterBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "AlarmUpdaterService";
    private static final long POLLING_INTERVAL = 5 * 1000;
    private static final long START_OF_POLLING_TIME = 2 * 60 * 60 * 1000;


    public void createRepeatingAlarmTimer(Context context, Double originLatitude, Double originLongitude,
                                          Double destinationLatitude, Double destinationLongitude,
                                          long defaultAlarmTime, long targetAlarmTime, long prepTime) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmUpdaterBroadcastReceiver.class);

        intent.putExtra("originLatitude",originLatitude);
        intent.putExtra("originLongitude", originLongitude);
        intent.putExtra("destinationLatitude", destinationLatitude);
        intent.putExtra("destinationLongitude", destinationLongitude);
        intent.putExtra("targetAlarmDate", targetAlarmTime);
        intent.putExtra("defaultDate", defaultAlarmTime);
        intent.putExtra("prepTime", prepTime);

        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);


        long startTime = defaultAlarmTime - START_OF_POLLING_TIME;
        am.setRepeating(AlarmManager.RTC, startTime,
                POLLING_INTERVAL, sender);

        Log.i(TAG, "createRepeatingAlarmTimer startTime:" + new Date(startTime).toString());
        Log.i(TAG, "createRepeatingAlarmTimer interval:" + (POLLING_INTERVAL) + " seconds");

        Log.i(TAG, "originLatitude" + originLatitude);
        Log.i(TAG, "originLongitude" + originLongitude);
        Log.i(TAG, "destinationLatitude" + destinationLatitude);
        Log.i(TAG, "destinationLongitude" + destinationLongitude);
        Log.i(TAG, "targetAlarmDate" + new Date(targetAlarmTime).toString());
        Log.i(TAG, "defaultDate" + new Date(defaultAlarmTime).toString());
        Log.i(TAG, "prepTime" + prepTime);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Double originLatitude = intent.getDoubleExtra("originLatitude", 0D);
        Double originLongitude = intent.getDoubleExtra("originLongitude", 0D);
        Double destinationLatitude = intent.getDoubleExtra("destinationLatitude", 0D);
        Double destinationLongitude = intent.getDoubleExtra("destinationLongitude", 0D);
        Long targetAlarmDate = intent.getLongExtra("targetAlarmDate", 0L);
        Long defaultDate = intent.getLongExtra("defaultDate", 0L);
        Long prep = intent.getLongExtra("prepTime", 0L);

        Log.i(TAG, "a:originLatitude" + originLatitude);
        Log.i(TAG, "a:originLongitude" + originLongitude);
        Log.i(TAG, "a:destinationLatitude" + destinationLatitude);
        Log.i(TAG, "a:destinationLongitude" + destinationLongitude);
        Log.i(TAG, "a:targetAlarmDate" + new Date(targetAlarmDate).toString());
        Log.i(TAG, "a:defaultDate" + new Date(defaultDate).toString());
        Log.i(TAG, "a:prepTime" + prep);

        int tripDuration = 0;
        try {
            tripDuration = new PollServerTask().execute(originLatitude, originLongitude, destinationLatitude, destinationLongitude).get();
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException occurred");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.e(TAG, "ExecutionException occurred");
            e.printStackTrace();
        }
        Log.i(TAG, String.format("onReceive: http://silex-archnat.rhcloud.com/rest/api/v1/compute_travel_time?to=%s,%s&from=%s,%s", destinationLatitude, destinationLongitude, originLatitude, originLongitude));
        Log.i(TAG, "" + defaultDate);
        long newComputedTime = computeEstimatedWakeUpTime(tripDuration, targetAlarmDate, defaultDate, context);
        updateAlarmTime(context, newComputedTime, defaultDate);
    }

    private void updateAlarmTime(Context context, long newComputedTime, long defaultAlarmTime){

        //if newComputedTime is less than the polling interval before default alarm time
        //  setOneTimeTimer to the default alarmtime
        if(newComputedTime < ( defaultAlarmTime - POLLING_INTERVAL )){
            setOneTimeTimer(context, newComputedTime);
            cancel(context);
            return;
        }

        //if newComputedTime is less than the polling interval
        // setOneTimeTimer to the newComputedTime
        if(defaultAlarmTime < (System.currentTimeMillis() + POLLING_INTERVAL)){
            setOneTimeTimer(context, defaultAlarmTime);
            cancel(context);
        }
    }


    private void setOneTimeTimer(Context context, long expiration) {
        AlarmManagerBroadcastReceiver oneTimeAlarm = new AlarmManagerBroadcastReceiver();
        oneTimeAlarm.setOnetimeTimer(context, expiration);
        Log.i(TAG, "setOneTimeTimer expiration:" + new Date(expiration));
    }

    private long computeEstimatedWakeUpTime(int duration, long targetDate, long defaultDate, Context context) {
        Log.i(TAG, "Travel Time: " + duration/60 + " minutes");
        long targetAlarmDate = targetDate -  (duration * 1000);
        Log.i(TAG, "Target Alarm Date: " + new Date(targetAlarmDate).toString());
        Log.i(TAG, "Default Alarm Date: " + new Date(defaultDate).toString());

        long newComputedAlarm;
        if(targetAlarmDate < defaultDate) {
            newComputedAlarm = targetAlarmDate;
        } else {
            newComputedAlarm = defaultDate;
        }
        Log.i(TAG, "new actualAlarm.getTime(): " + new Date(newComputedAlarm).toString());
        return newComputedAlarm;
    }

    public void cancel(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmUpdaterBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.cancel(sender);

        Log.i(TAG, "timer cancelled");

    }

    private class PollServerTask extends AsyncTask<Double, Integer, Integer>{

        @Override
        protected Integer doInBackground(Double... params) {
            return pollServer(params[0], params[1], params[2], params[3]);
        }

        private int pollServer(Double originLatitude, Double originLongitude, Double destinationLatitude, Double destinationLongitude) {
            String urlStr = String.format("http://silex-archnat.rhcloud.com/rest/api/v1/compute_travel_time?to=%s,%s&from=%s,%s",
                    destinationLatitude, destinationLongitude, originLatitude, originLongitude);
            Log.i(TAG, " gonna connect to " + urlStr);
            InputStream inputStream;
            String result = "no result";
            URL url;
            int tripDuration = 0;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urlStr);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    result = convertInputStreamToString(inputStream);
                    JSONObject json = new JSONObject(result);
                    tripDuration = json.getInt("duration_in_seconds");

                } else {
                    Log.v(TAG, "Response code:" + responseCode);
                }
                Log.i(TAG, "result: "+result);

            } catch (Exception e) {
                Log.i(TAG, "urlConnection exception occurred");
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


    }
}