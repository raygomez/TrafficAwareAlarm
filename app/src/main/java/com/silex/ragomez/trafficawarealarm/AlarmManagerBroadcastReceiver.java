package com.silex.ragomez.trafficawarealarm;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.widget.Toast;

import com.example.android.common.logger.Log;

import java.util.Date;


//http://www.javacodegeeks.com/2012/09/android-alarmmanager-tutorial.html
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    final public static String ONE_TIME = "onetime";
    private PendingIntent pi;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "tag");
        wl.acquire();

        Toast.makeText(context, "Timer expired", Toast.LENGTH_LONG).show();
        Log.i("Timer", "timer expired:" + new Date().toString());

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();

        wl.release();
        try {
            synchronized (this) {
                wait(3000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        r.stop();
    }

    public void setOnetimeTimer(Context context, long timeOfExpiration) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        am.set(AlarmManager.RTC_WAKEUP, timeOfExpiration, pi);
        Log.i("Timer", "timer started:" + new Date().toString());

    }

    public void cancel(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        Log.i("Timer", "timer cancelled");

    }
}
