package com.vladislavbalyuk.tracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String PREFERENCE_BOUND = "bound";
    private final String PREFERENCE_ZOOM = "zoom";
    private final String PREFERENCE_LAT = "lat";
    private final String PREFERENCE_LNG = "lng";

    private MainActivityFragment fragment;

    private FloatingActionButton fab;

    private GoogleMap googleMap;

    private boolean isBound;

    private double prefLat, prefLng;
    private float prefZoom;
    private boolean useLastLocation;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestPermitions(savedInstanceState);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                isBound = !fragment.isBound();
                setFloatingActionButton(isBound);
                fragment.bindUnbindService(false);
                Snackbar.make(fab, isBound ? getResources().getString(R.string.start_tracking) : getResources().getString(R.string.end_tracking), Snackbar.LENGTH_SHORT).show();
            }
        });

        fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        if (fragment.isBound()) {
            setFloatingActionButton(true);
            isBound = true;
        }

        useLastLocation = (savedInstanceState == null);

        new GetPreferenceTask().execute();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setPreference();
    }

    private void initMap() {
        double lat, lng;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.clear();

        Location currentLocation = null;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (currentLocation == null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (useLastLocation && currentLocation != null) {
            lat = currentLocation.getLatitude();
            lng = currentLocation.getLongitude();
        } else {
            lat = prefLat;
            lng = prefLng;
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(prefZoom)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);
        googleMap.getUiSettings().setCompassEnabled(true);
        //           googleMap.setMyLocationEnabled(true);
        //googleMap.getUiSettings().setZoomControlsEnabled(true);
        //          googleMap.getUiSettings().setMyLocationButtonEnabled(true);

    }

    public void showTrack(Track track) {
        Point start, finish = null;

        googleMap.clear();

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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, ListActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setPreference() {
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean(PREFERENCE_BOUND, isBound);
        ed.putFloat(PREFERENCE_ZOOM, googleMap.getCameraPosition().zoom);
        ed.putFloat(PREFERENCE_LAT, (float) googleMap.getCameraPosition().target.latitude);
        ed.putFloat(PREFERENCE_LNG, (float) googleMap.getCameraPosition().target.longitude);
        ed.apply();
    }

    public void setFloatingActionButton(boolean enabled) {
        if (enabled) {
            fab.setImageResource(R.drawable.ic_location_on_black_24dp);
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorControlRec)));
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            getSupportActionBar().hide();

        } else {
            fab.setImageResource(R.drawable.ic_location_off_black_24dp);
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorControlNormal)));
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(0);
            getSupportActionBar().show();
        }
    }


    private void requestPermitions(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        0);
            }
        }
    }

    private class GetPreferenceTask extends AsyncTask<Void, Void, Void> {
        SharedPreferences sPref;
        boolean showTrack;

        @Override
        protected Void doInBackground(Void... params) {
            sPref = getPreferences(MODE_PRIVATE);

            if (!isBound && sPref.getBoolean(PREFERENCE_BOUND, false)) {
                fragment.bindUnbindService(true);
                isBound = true;
                showTrack = false;
            } else
                showTrack = true;

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
                        initMap();
                        if (showTrack) {
                            Track track = fragment.getTrack();
                            showTrack(track);
                        }
                    }
                });
            } catch (NullPointerException e) {
            }
            ;
        }
    }

}
