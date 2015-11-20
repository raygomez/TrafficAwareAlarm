package com.silex.ragomez.trafficawarealarm.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

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


    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
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
}
