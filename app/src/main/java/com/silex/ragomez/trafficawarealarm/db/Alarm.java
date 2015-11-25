package com.silex.ragomez.trafficawarealarm.db;

public class Alarm {

    private Integer id;
    private String name;

    private String origin;
    private double originLatitude;
    private double originLongitude;

    private String destination;
    private double destLatitude;
    private double destLongitude;

    private long prepTime;

    private long defaultAlarm;

    private long eta;

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
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

    public double getOriginLatitude() {
        return originLatitude;
    }

    public void setOriginLatitude(double originLatitude) {
        this.originLatitude = originLatitude;
    }

    public double getOriginLongitude() {
        return originLongitude;
    }

    public void setOriginLongitude(double originLongitude) {
        this.originLongitude = originLongitude;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getDestLatitude() {
        return destLatitude;
    }

    public void setDestLatitude(double destLatitude) {
        this.destLatitude = destLatitude;
    }

    public double getDestLongitude() {
        return destLongitude;
    }

    public void setDestLongitude(double destLongitude) {
        this.destLongitude = destLongitude;
    }

    public long getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(long prepTime) {
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

    @Override
    public String toString() {
        return name;
    }
}
