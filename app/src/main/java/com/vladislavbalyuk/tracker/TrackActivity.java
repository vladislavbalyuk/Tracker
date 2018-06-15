package com.vladislavbalyuk.tracker;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

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
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int size = Math.min(width, height);

            LatLngBounds latLngBounds = latLngBuilder.build();
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 100);
            googleMap.moveCamera(cameraUpdate);
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
            fragment.getTrack();
        }
    }

}
