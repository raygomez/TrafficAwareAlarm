package com.silex.ragomez.trafficawarealarm;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import com.example.android.common.logger.Log;

import java.util.Date;


//http://www.javacodegeeks.com/2012/09/android-alarmmanager-tutorial.html
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    final public static String ONE_TIME = "onetime";

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "tag");
        wl.acquire();

        Toast.makeText(context, "Timer expired", Toast.LENGTH_LONG).show();
        Log.i("Timer", "timer expired:" + new Date().toString());

        wl.release();
    }

    public void setOnetimeTimer(Context context, long timeOfExpiration) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        am.set(AlarmManager.RTC_WAKEUP, timeOfExpiration, pi);
    }
}
