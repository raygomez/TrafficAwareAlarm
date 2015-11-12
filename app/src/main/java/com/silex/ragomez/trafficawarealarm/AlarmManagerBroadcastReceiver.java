package com.silex.ragomez.trafficawarealarm;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.example.android.common.logger.Log;

import java.util.Date;


//http://www.javacodegeeks.com/2012/09/android-alarmmanager-tutorial.html
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmManagerBroadcastReceiver";
    private PendingIntent pi;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "tag");
        wl.acquire();

        Intent i = new Intent(context, DialogActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

        wl.release();
    }

    public void setOnetimeTimer(Context context, long timeOfExpiration) {
        cancel(context);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        am.set(AlarmManager.RTC_WAKEUP, timeOfExpiration, pi);
        Log.i(TAG, "timer started:" + new Date().toString()+" timeOfExpiration: "+new Date(timeOfExpiration).toString());
    }

    public void cancel(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        Log.i(TAG, "timer cancelled");
    }
}
