package com.silex.ragomez.trafficawarealarm.db;

import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

public class Alarm {

    private Long id;
    private String name;

    private String origin;
    private LatLng originCoordinates;

    private String destination;
    private LatLng destCoordinates;

    private int prepTime;

    private long defaultAlarm;

    private long eta;

    private int status;

    public Alarm(){}

    public Alarm(Bundle b) {
        id = b.getLong(DatabaseHandler.KEY_ID);
        name = b.getString(DatabaseHandler.KEY_NAME);
        origin = b.getString(DatabaseHandler.KEY_ORIGIN);
        originCoordinates = new LatLng(b.getDouble(DatabaseHandler.KEY_ORIGIN_LAT),
                b.getDouble(DatabaseHandler.KEY_ORIGIN_LONG));
        destination = b.getString(DatabaseHandler.KEY_DEST);
        destCoordinates = new LatLng(b.getDouble(DatabaseHandler.KEY_DEST_LAT),
                b.getDouble(DatabaseHandler.KEY_DEST_LONG));
        prepTime = b.getInt(DatabaseHandler.KEY_PREP_TIME);
        defaultAlarm = b.getLong(DatabaseHandler.KEY_DEFAULT_ALARM);
        eta = b.getLong(DatabaseHandler.KEY_ETA);
        status = 1;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public long getDefaultAlarm() {
        return defaultAlarm;
    }

    public void setDefaultAlarm(long defaultAlarm) {
        this.defaultAlarm = defaultAlarm;
    }

    public long getEta() {
        return eta;
    }

    public void setEta(long eta) {
        this.eta = eta;
    }

    public int getStatus() {
        return status;
    }

    public void turnOff(){
        this.status = 0;
    }

    public void turnOn(){
        this.status = 1;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return name;
    }

    public LatLng getOriginCoordinates() {
        return originCoordinates;
    }

    public void setOriginCoordinates(LatLng originCoordinates) {
        this.originCoordinates = originCoordinates;
    }

    public LatLng getDestCoordinates() {
        return destCoordinates;
    }

    public void setDestCoordinates(LatLng destCoordinates) {
        this.destCoordinates = destCoordinates;
    }
}
