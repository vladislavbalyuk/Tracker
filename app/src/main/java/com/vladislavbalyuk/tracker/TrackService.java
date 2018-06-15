package com.vladislavbalyuk.tracker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Date;

public class TrackService extends Service {

    private IBinder binder = new TrackBinder();

    private Db db;

    private LocationManager locationManager;
    private volatile Track track;
    private volatile int number = 0;

    private Date beginTime;

    @Override
    public void onCreate() {
        super.onCreate();
        db = Db.getInstance(getApplicationContext());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent intentActivity = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),100,intentActivity,PendingIntent.FLAG_NO_CREATE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText("Tracker")
                .setContentTitle("Tracker")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_location_on_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.map_round));

        Notification notification;
        if (Build.VERSION.SDK_INT < 16)
            notification = builder.getNotification();
        else
            notification = builder.build();

        startForeground(10,notification);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public int getNumber() {
        return number;
    }

    public void startTrack() {

        track = createTrack();
        number = 1;

        beginTime = new Date();

        new TrackTask().execute();
    }

    private void startLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 5, 1, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 5, 1,
                locationListener);
    }

    private Track createTrack() {
        Track track = new Track();
        track.setRandomUuid();
        track.setCurrentName();
        return track;
    }

    private Point createPoint(double lat, double lon) {
        Point point = new Point();
        point.setRandomUuid();
        point.setLatitude(lat);
        point.setLongitude(lon);
        point.setCurrentDate();
        return point;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyTag", "" + location.getLatitude() + "  " + location.getLongitude());
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            Point point = createPoint(lat, lon);

            new PointTask().execute(point);

        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    class TrackBinder extends Binder {
        TrackService getService() {
            return TrackService.this;
        }
    }

    public Track getTrack() {
        return track;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    class TrackTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            db.updateTrack(track);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            startLocation();
        }
    }

    class PointTask extends AsyncTask<Point, Void, Void> {

        @Override
        protected Void doInBackground(Point... params) {
            db.updatePoint(params[0], track, number++);

            Intent intentReceiver = new Intent("com.vladislavbalyuk.tracker.servicebackbroadcast");
            intentReceiver.putExtra(getApplicationContext().getResources().getString(R.string.track_uuid),track.getUuid());
            sendBroadcast(intentReceiver);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
