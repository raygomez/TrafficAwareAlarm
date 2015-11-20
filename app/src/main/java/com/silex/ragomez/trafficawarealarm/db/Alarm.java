package com.silex.ragomez.trafficawarealarm.db;

public class Alarm {

    private int id;
    private String name;

    private String origin;
    private double origin_lat;
    private double origin_long;

    private String dest;
    private double dest_lat;
    private double dest_long;

    private long prep_time;

    private long default_alarm;

    private long eta;

    public int getId() {
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

    public double getOrigin_lat() {
        return origin_lat;
    }

    public void setOrigin_lat(double origin_lat) {
        this.origin_lat = origin_lat;
    }

    public double getOrigin_long() {
        return origin_long;
    }

    public void setOrigin_long(double origin_long) {
        this.origin_long = origin_long;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public double getDest_lat() {
        return dest_lat;
    }

    public void setDest_lat(double dest_lat) {
        this.dest_lat = dest_lat;
    }

    public double getDest_long() {
        return dest_long;
    }

    public void setDest_long(double dest_long) {
        this.dest_long = dest_long;
    }

    public long getPrep_time() {
        return prep_time;
    }

    public void setPrep_time(long prep_time) {
        this.prep_time = prep_time;
    }

    public long getDefault_alarm() {
        return default_alarm;
    }

    public void setDefault_alarm(long default_alarm) {
        this.default_alarm = default_alarm;
    }

    public long getEta() {
        return eta;
    }

    public void setEta(long eta) {
        this.eta = eta;
    }
}
