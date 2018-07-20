package com.vladislavbalyuk.tracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static java.lang.Math.*;

public class Track {

    private List<Point> points;
    private String name;
    private String uuid;

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setRandomUuid() {
        this.uuid = String.valueOf(UUID.randomUUID());
    }

    public void setCurrentName() {
        this.name = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private double getDistance(Point point1, Point point2){
        final double radius = 6371*1000;

        double gradusM = abs(point1.getLatitude() - point2.getLatitude());
        double gradusP = abs(point1.getLongitude() - point2.getLongitude());

        double urm = (radius * 2 * PI)/360;
        double urp = cos((point1.getLatitude() + point2.getLatitude())/2) * urm;

        double distanceM = gradusM * urm;
        double distanceP = gradusP * urp;

        double distance = sqrt(pow(distanceM,2) + pow(distanceP,2));

        return  distance;
    }

    public double getTotalDistance(){
        int i;
        double trackDistance, distance;

        trackDistance = 0;

        if(points == null || points.size() < 2) return 0;

        for(i = 0; i < points.size() - 1; i++){
            distance = getDistance(points.get(i), points.get(i+1));
            trackDistance += distance;
        }

        return trackDistance;
    }

    public String getTotalTime(){

        if(points == null || points.size() < 2) return "00:00:00";
        Date start = points.get(0).getDate();
        Date finish = points.get(points.size() - 1).getDate();

        long time = (finish.getTime() - start.getTime()) / 1000;

        long hours = time / 3600;
        long minutes = (time - hours * 3600) / 60;
        long seconds = time - hours * 3600 - minutes * 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public double getTotalSpeed(){

        if(points == null || points.size() < 2) return 0;
        Date start = points.get(0).getDate();
        Date finish = points.get(points.size() - 1).getDate();

        long time = (finish.getTime() - start.getTime()) / 1000;
        double distance = getTotalDistance();
        double speed = distance > 0.1 ? 3.6 * distance / time : 0;

        return speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Track)) return false;

        Track track = (Track) o;

        if (getPoints() != null ? !getPoints().equals(track.getPoints()) : track.getPoints() != null)
            return false;
        if (getName() != null ? !getName().equals(track.getName()) : track.getName() != null)
            return false;
        return getUuid() != null ? getUuid().equals(track.getUuid()) : track.getUuid() == null;

    }

    @Override
    public int hashCode() {
        int result = getPoints() != null ? getPoints().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getUuid() != null ? getUuid().hashCode() : 0);
        return result;
    }
}
