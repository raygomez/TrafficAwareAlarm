package com.silex.ragomez.trafficawarealarm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;

import com.example.android.common.activities.SampleActivityBase;

public class DialogActivity extends SampleActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        final long[] pattern = {0, 100, 1000};
        v.vibrate(pattern, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Time's Up").setCancelable(
                false).setPositiveButton("Cancel Alarm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        r.stop();
                        v.cancel();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
