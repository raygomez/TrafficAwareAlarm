package com.silex.ragomez.trafficawarealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.example.android.common.logger.Log;
import com.silex.ragomez.trafficawarealarm.db.Alarm;
import com.silex.ragomez.trafficawarealarm.db.DatabaseHandler;

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
    private static final long POLLING_INTERVAL = 5 * 60 * 1000;
    private static final long START_OF_POLLING_TIME = 2 * 60 * 60 * 1000;

    public void createRepeatingAlarmTimer(Context context, Alarm alarm) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmUpdaterBroadcastReceiver.class);

        intent.putExtra("id", alarm.getId().intValue());
        intent.putExtra("originLatitude",alarm.getOriginCoordinates().latitude);
        intent.putExtra("originLongitude", alarm.getOriginCoordinates().longitude);
        intent.putExtra("destinationLatitude", alarm.getDestCoordinates().latitude);
        intent.putExtra("destinationLongitude", alarm.getDestCoordinates().longitude);
        intent.putExtra("targetAlarmDate", alarm.getEta());
        intent.putExtra("defaultDate", alarm.getDefaultAlarm());
        intent.putExtra("prepTime", alarm.getPrepTime());
        intent.putExtra("name", alarm.getName());

        int alarmId = alarm.getId().intValue();

        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        long startTime = alarm.getDefaultAlarm() - alarm.getPrepTime() - START_OF_POLLING_TIME;
        am.setRepeating(AlarmManager.RTC_WAKEUP, startTime,
                POLLING_INTERVAL, sender);

        Log.i(TAG, "createRepeatingAlarmTimer startTime:" + new Date(startTime).toString());
        Log.i(TAG, "createRepeatingAlarmTimer interval:" + (POLLING_INTERVAL) + " seconds");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int alarmId = intent.getIntExtra("id", 0);
        Double originLatitude = intent.getDoubleExtra("originLatitude", 0D);
        Double originLongitude = intent.getDoubleExtra("originLongitude", 0D);
        Double destinationLatitude = intent.getDoubleExtra("destinationLatitude", 0D);
        Double destinationLongitude = intent.getDoubleExtra("destinationLongitude", 0D);
        Long targetAlarmDate = intent.getLongExtra("targetAlarmDate", 0L);
        Long defaultDate = intent.getLongExtra("defaultDate", 0L);
        int prepTime = intent.getIntExtra("prepTime", 0);
        String name = intent.getStringExtra("name");

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
        long newComputedTime = computeEstimatedWakeUpTime(tripDuration, targetAlarmDate - prepTime, defaultDate, context);
        updateAlarmTime(context, alarmId, newComputedTime, defaultDate, name);
    }

    private void updateAlarmTime(Context context, int alarmId, long newComputedTime, long defaultAlarmTime, String name){

        //if newComputedTime is less than the polling interval before default alarm time
        //  setOneTimeTimer to the default alarmtime
        if(newComputedTime < ( defaultAlarmTime - POLLING_INTERVAL )){
            setOneTimeTimer(context, newComputedTime, alarmId, name);
            cancelRepeatingPolling(context, alarmId);
            return;
        }

        //if newComputedTime is less than the polling interval
        // setOneTimeTimer to the newComputedTime
        if(defaultAlarmTime < (System.currentTimeMillis() + POLLING_INTERVAL)){
            setOneTimeTimer(context, defaultAlarmTime, alarmId, name);
            cancelRepeatingPolling(context, alarmId);
        }
    }


    private void setOneTimeTimer(Context context, long expiration, int alarmId, String name) {
        AlarmManagerBroadcastReceiver oneTimeAlarm = new AlarmManagerBroadcastReceiver();
        oneTimeAlarm.setOnetimeTimer(context, expiration, alarmId, name);
        Log.i(TAG, "setOneTimeTimer "+name+" expiration:" + new Date(expiration));
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

    public void cancelRepeatingPolling(Context context, int alarmId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmUpdaterBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);

        Log.i(TAG, "cancelRepeatingPolling alarmId:"+alarmId);
    }

    public void cancelAlarm(Context context, Alarm alarm){
        cancelRepeatingPolling(context, alarm.getId().intValue());
        cancelOneTimeAlarm(context, alarm.getId().intValue());

        alarm.turnOff();
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        db.updateAlarm(alarm);

    }

    private void cancelOneTimeAlarm(Context context, int alarmId){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, alarmId, intent, 0);
        am.cancel(pi);

        Log.i(TAG, "cancelOneTimeAlarm alarmId:"+alarmId);
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