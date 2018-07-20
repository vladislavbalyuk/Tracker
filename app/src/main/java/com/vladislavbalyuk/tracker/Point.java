package com.vladislavbalyuk.tracker;

import java.util.Date;
import java.util.UUID;

public class Point {
    private double longitude;
    private double latitude;
    private double altitude;
    private Date date;
    private String uuid;
    private String adress;

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;

        Point point = (Point) o;

        if (Double.compare(point.getLongitude(), getLongitude()) != 0) return false;
        if (Double.compare(point.getLatitude(), getLatitude()) != 0) return false;
        if (Double.compare(point.getAltitude(), getAltitude()) != 0) return false;
        if (getDate() != null ? !getDate().equals(point.getDate()) : point.getDate() != null)
            return false;
        if (getUuid() != null ? !getUuid().equals(point.getUuid()) : point.getUuid() != null)
            return false;
        return getAdress() != null ? getAdress().equals(point.getAdress()) : point.getAdress() == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(getLongitude());
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getLatitude());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getAltitude());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (getDate() != null ? getDate().hashCode() : 0);
        result = 31 * result + (getUuid() != null ? getUuid().hashCode() : 0);
        result = 31 * result + (getAdress() != null ? getAdress().hashCode() : 0);
        return result;
    }
}
