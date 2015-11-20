package com.silex.ragomez.trafficawarealarm;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;

public class DialogActivity extends SampleActivityBase {

    private PowerManager.WakeLock wl;
    private KeyguardManager.KeyguardLock keyguardLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();

        wakeUp(context);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        final long[] pattern = {0, 1000, 100};
        v.vibrate(pattern, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("WakeMeApp");
        builder.setMessage("Time to wake up!").setCancelable(
                false).setPositiveButton("Cancel Alarm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        r.stop();
                        v.cancel();
                        keyguardLock.reenableKeyguard();
                        finish();
                    }
                });

        cancelAlarmUpdater();
        AlertDialog alert = builder.create();
        alert.show();

        wl.release();
    }

    private void wakeUp(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        wl.acquire();

        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock(TAG);
        keyguardLock.disableKeyguard();
    }

    private void cancelAlarmUpdater() {
        Context context = getApplicationContext();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmUpdaterIntent = new Intent(context, AlarmUpdaterBroadcastReceiver.class);
        PendingIntent alarmUpdaterPendingIntent = PendingIntent.getBroadcast(context, 0, alarmUpdaterIntent, 0);
        ;
        am.cancel(alarmUpdaterPendingIntent);
        Log.i(TAG, "cancelled alarmUpdaterPendingIntent");

    }
}
