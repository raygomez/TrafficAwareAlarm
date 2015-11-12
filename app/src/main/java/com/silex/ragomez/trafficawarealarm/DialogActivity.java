package com.silex.ragomez.trafficawarealarm;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;

public class DialogActivity extends SampleActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Time's Up").setCancelable(
                false).setPositiveButton("Cancel Alarm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        r.stop();
                        Context context = getApplicationContext();
                        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Intent alarmUpdaterIntent = new Intent(context, AlarmUpdaterBroadcastReceiver.class);
                        PendingIntent alarmUpdaterPendingIntent = PendingIntent.getBroadcast(context, 0, alarmUpdaterIntent, 0);;
                        am.cancel(alarmUpdaterPendingIntent);
                        Log.i(TAG, "cancelled alarmUpdaterPendingIntent");
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
