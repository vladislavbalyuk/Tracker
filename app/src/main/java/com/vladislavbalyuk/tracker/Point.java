package com.vladislavbalyuk.tracker;

import java.util.Date;
import java.util.UUID;

public class Point {
    private double longitude;
    private double latitude;
    private Date date;
    private String uuid;
    private String adress;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public void setRandomUuid() {
        this.uuid = String.valueOf(UUID.randomUUID());
    }

    public void setCurrentDate() {
        this.date = new Date();
    }

}
