package com.silex.ragomez.trafficawarealarm.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static DatabaseHandler instance;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "alarmManager.db";
    private static final String TABLE_ALARMS = "alarms";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";

    private static final String KEY_ORIGIN = "origin";
    private static final String KEY_ORIGIN_LAT = "origin_lat";
    private static final String KEY_ORIGIN_LONG = "origin_long";

    private static final String KEY_DEST = "destination";
    private static final String KEY_DEST_LAT = "dest_lat";
    private static final String KEY_DEST_LONG = "dest_long";

    private static final String KEY_PREP_TIME = "prep_time";
    private static final String KEY_DEFAULT_ALARM = "default_alarm";

    private static final String KEY_ETA = "eta";

    public static synchronized DatabaseHandler getInstance(Context context) {
        if(instance == null) {
            instance = new DatabaseHandler(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_ALARM_TABLE = "create table " + TABLE_ALARMS +
                "(" +
                KEY_ID + " integer primary key autoincrement," +
                KEY_NAME + " text not null," +
                KEY_ORIGIN + " text not null," +
                KEY_ORIGIN_LAT + " real not null," +
                KEY_ORIGIN_LONG + " real not null," +
                KEY_DEST + " text not null," +
                KEY_DEST_LAT + " real not null," +
                KEY_DEST_LONG + " real not null," +
                KEY_PREP_TIME + " integer not null," +
                KEY_DEFAULT_ALARM + " integer not null," +
                KEY_ETA + " integer not null," +
                ")";

        db.execSQL(CREATE_ALARM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("drop table if exists " + TABLE_ALARMS);
        onCreate(db);
    }

    public void addAlarm(Alarm alarm) {

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, alarm.getName());
        values.put(KEY_ORIGIN, alarm.getOrigin());
        values.put(KEY_ORIGIN_LAT, alarm.getOriginLatitude());
        values.put(KEY_ORIGIN_LONG, alarm.getOriginLongitude());
        values.put(KEY_DEST, alarm.getDestination());
        values.put(KEY_DEST_LAT, alarm.getDestLatitude());
        values.put(KEY_DEST_LONG, alarm.getDestLongitude());
        values.put(KEY_PREP_TIME, alarm.getPrepTime());
        values.put(KEY_DEFAULT_ALARM, alarm.getDefaultAlarm());
        values.put(KEY_ETA, alarm.getEta());


        db.insertOrThrow(TABLE_ALARMS, null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<Alarm> getAllAlarms() {

        List<Alarm> alarms = new ArrayList<>();
        String ALARM_QUERY = String.format("select * from %s", TABLE_ALARMS);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ALARM_QUERY, null);

        if(cursor.moveToFirst()) {
            do {

                Alarm alarm = new Alarm();
                alarm.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));

                alarm.setOrigin(cursor.getString(cursor.getColumnIndex(KEY_ORIGIN)));
                alarm.setOriginLatitude(cursor.getDouble(cursor.getColumnIndex(KEY_ORIGIN_LAT)));
                alarm.setOriginLongitude(cursor.getDouble(cursor.getColumnIndex(KEY_ORIGIN_LONG)));

                alarm.setDestination(cursor.getString(cursor.getColumnIndex(KEY_DEST)));
                alarm.setDestLatitude(cursor.getDouble(cursor.getColumnIndex(KEY_DEST_LAT)));
                alarm.setDestLongitude(cursor.getDouble(cursor.getColumnIndex(KEY_DEST_LONG)));

                alarm.setPrepTime(cursor.getLong(cursor.getColumnIndex(KEY_PREP_TIME)));
                alarm.setDefaultAlarm(cursor.getLong(cursor.getColumnIndex(KEY_DEFAULT_ALARM)));
                alarm.setEta(cursor.getLong(cursor.getColumnIndex(KEY_ETA)));

                alarms.add(alarm);

            } while(cursor.moveToNext());
        }
        cursor.close();

        return alarms;
    }
}
