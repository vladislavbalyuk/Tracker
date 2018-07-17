package com.vladislavbalyuk.tracker;

import android.*;
import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class TrackActivity extends AppCompatActivity {

    private final String PREFERENCE_ZOOM = "zoom_track";
    private final String PREFERENCE_LAT = "lat_track";
    private final String PREFERENCE_LNG = "lng_track";

    private double prefLat, prefLng;
    private float prefZoom;
    private boolean useLastLocation;

    private Menu optionsMenu;

    private GoogleMap googleMap;
    private TrackActivityFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_track);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragment = (TrackActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        fragment.setUuid(getIntent().getStringExtra("uuid"));
        useLastLocation = (savedInstanceState == null);

        new GetPreferenceTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setPreference();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track, menu);

        optionsMenu = menu;
        setItemMenuVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_save) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0);
            }
            else
                Kml.sendKml(this,fragment.getTrack());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 0) {
            if(grantResults.length == 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Kml.sendKml(this,fragment.getTrack());

            } else {
                // Permission was denied or request was cancelled
            }
        }
    }
    public void showTrack(Track track) {
        Point start, finish = null;
        LatLngBounds.Builder latLngBuilder;
        googleMap.clear();

        latLngBuilder = new LatLngBounds.Builder();

        if (track != null && track.getPoints().size() > 0) {
            start = track.getPoints().get(0);

            MarkerOptions startMarkerOptions = new MarkerOptions().
                    position(
                            new LatLng(start.getLatitude(), start.getLongitude())).
                    icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.start), 64, 64, false)));
            googleMap.addMarker(startMarkerOptions);

            PolylineOptions line = new PolylineOptions();
            line.width(6f).color(Color.RED);

            for (Point point : track.getPoints()) {
                LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                line.add(latLng);
                latLngBuilder.include(latLng);
                finish = point;
            }


            if (finish != null) {
                MarkerOptions finishMarkerOptions = new MarkerOptions().
                        position(
                                new LatLng(finish.getLatitude(), finish.getLongitude())).
                        icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.finish), 64, 64, false)));
                googleMap.addMarker(finishMarkerOptions);
            }
            googleMap.addPolyline(line);

        }
        CameraUpdate cameraUpdate;
        if(useLastLocation) {
            if(track.getPoints().size() > 0) {
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int size = Math.min(width, height);

                LatLngBounds latLngBounds = latLngBuilder.build();
                cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 100);
                googleMap.moveCamera(cameraUpdate);
            }
        }
        else {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(prefLat, prefLng))
                    .zoom(prefZoom)
                    .build();
            cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.moveCamera(cameraUpdate);
            googleMap.getUiSettings().setCompassEnabled(true);
        }
        //           googleMap.setMyLocationEnabled(true);
        //googleMap.getUiSettings().setZoomControlsEnabled(true);
        //          googleMap.getUiSettings().setMyLocationButtonEnabled(true);

    }

    private void setPreference() {
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putFloat(PREFERENCE_ZOOM, googleMap.getCameraPosition().zoom);
        ed.putFloat(PREFERENCE_LAT, (float) googleMap.getCameraPosition().target.latitude);
        ed.putFloat(PREFERENCE_LNG, (float) googleMap.getCameraPosition().target.longitude);
        ed.apply();
    }

    public void setItemMenuVisible(boolean visible){
        if(optionsMenu != null)
            optionsMenu.getItem(0).setVisible(visible);
    }

    private class GetPreferenceTask extends AsyncTask<Void, Void, Void> {
        SharedPreferences sPref;

        @Override
        protected Void doInBackground(Void... params) {
            sPref = getPreferences(MODE_PRIVATE);


            prefLat = sPref.getFloat(PREFERENCE_LAT, 0f);
            prefLng = sPref.getFloat(PREFERENCE_LNG, 0f);
            prefZoom = sPref.getFloat(PREFERENCE_ZOOM, 0f);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(
                        R.id.map);
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap map) {
                        googleMap = map;
                    }
                });
            } catch (NullPointerException e) {
            }
            ;
            fragment.readTrack();
        }
    }

}
