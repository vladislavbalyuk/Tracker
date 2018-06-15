package com.vladislavbalyuk.tracker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MainActivityFragment extends Fragment {

    private View view;

    private TextView textTime, textDistance, textSpeed, textAdress;

    private Date beginTime;

    private TimeTask timeTask;

    private Intent intent;
    private ServiceConnection sConn;
    private TrackService trackService;
    private boolean bound;
    private String trackUuid;

    private Db db;

    private BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        bound = false;

        sConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ((MainActivity) getActivity()).setFloatingActionButton(true);
                trackService = ((TrackService.TrackBinder) service).getService();
                bound = true;
                if (trackService.getNumber() == 0)
                    startTrack();

                Track track = trackService.getTrack();
                if (track != null)
                    trackUuid = track.getUuid();
                else
                    trackUuid = null;
                startTrackTask();

                timeTask = new TimeTask();
                timeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                trackService = null;
                bound = false;
                interruptTimeTask();
            }
        };

        Context context = getActivity().getApplicationContext();

        db = Db.getInstance(context);
//        db.getTest();
        intent = new Intent(context, TrackService.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
        } else {
            view = inflater.inflate(R.layout.fragment_main, container, false);
            textTime = (TextView) view.findViewById(R.id.textTime);
            textDistance = (TextView) view.findViewById(R.id.textDistance);
            textSpeed = (TextView) view.findViewById(R.id.textSpeed);
            textAdress = (TextView) view.findViewById(R.id.textAdress);
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setBroadcastReceiver();

        IntentFilter intFilt = new IntentFilter("com.vladislavbalyuk.tracker.servicebackbroadcast");
        context.registerReceiver(broadcastReceiver, intFilt);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getContext().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        interruptTimeTask();
    }

    public boolean isBound() {
        return bound;
    }

    public void bindUnbindService(boolean tryBindCurrentService) {

        Context context = getActivity().getApplicationContext();

        if (tryBindCurrentService) {
            context.bindService(intent, sConn, 0);
        } else if (bound) {
            context.unbindService(sConn);
            context.stopService(intent);
            bound = false;
            interruptTimeTask();
        } else {
            context.startService(intent);
            context.bindService(intent, sConn, 0);
        }
    }

    private void setBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (trackService != null) {
                    trackUuid = trackService.getTrack().getUuid();
                    startTrackTask();
                }
            }
        };
    }


    private void startTrackTask() {
        if (trackUuid != null)
            new TrackTask().execute();

    }

    private void startTrack() {
        trackService.startTrack();
        textDistance.setText("0m");
        textSpeed.setText("0km/h");
        textAdress.setText("");
    }

    private void setAdress(Track track) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(
                        JacksonConverterFactory.create(objectMapper))
                .build();
        GoogleApi googleApi = retrofit.create(GoogleApi.class);

        List<Point> points = track.getPoints();
        if (points.size() > 0) {
            final Point lastPoint = points.get(points.size() - 1);

            Double latitude = lastPoint.getLatitude();
            Double longitude = lastPoint.getLongitude();

            if (latitude != null && longitude != null) {
                try {
                    Response response = googleApi.getData(latitude.toString() + "," + longitude.toString(), "ru", getResources().getString(R.string.API_KEY)).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                googleApi.getData(latitude.toString() + "," + longitude.toString(),
                        "en", getResources().getString(R.string.API_KEY)).enqueue(new Callback<Results>() {
                    @Override
                    public void onResponse(Call<Results> call, Response<Results> response) {
                        if (response.body() != null) {
                            Results results = response.body();
                            Result result = results.getResults().get(0);
                            String adress = result.getFormattedAddress();
                            textAdress.setText(adress);
                            lastPoint.setAdress(adress);
                            new PointTask().execute(lastPoint);
                        }
                    }

                    @Override
                    public void onFailure(Call<Results> call, Throwable t) {

                    }
                });
            }
        }

    }

    private void interruptTimeTask() {
        if (timeTask != null)
            timeTask.interrupted = true;
    }

    public Track getTrack() {
        Track track = null;
        if(trackUuid != null) {
            track = db.getTrack(trackUuid);
            track.setPoints(db.getPoints(track));
        }
        return track;
    }

    class TrackTask extends AsyncTask<Void, Void, Track> {

        @Override
        protected Track doInBackground(Void... params) {

            Track track = getTrack();

            if(track != null) {
                setAdress(track);
            }

            return track;
        }

        @Override
        protected void onPostExecute(Track track) {
            if (beginTime == null) {
                beginTime = trackService.getBeginTime();
            }
            double time = (new Date().getTime() - beginTime.getTime()) / 1000;

            if(track != null) {
                ((MainActivity) getActivity()).showTrack(track);
                double distance = track.getTotalDistance();
                double speed = distance > 0.1 ? 3.6 * distance / time : 0;
                textDistance.setText(String.format("%.0f", distance) + "m");
                textSpeed.setText(String.format("%.0f", speed) + "km/h");
            }

        }
    }

    class TimeTask extends AsyncTask<Void, String, Void> {

        boolean interrupted = false;

        @Override
        protected Void doInBackground(Void... params) {
            while (!interrupted) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (beginTime == null) {
                    beginTime = trackService.getBeginTime();
                }

                long time = (new Date().getTime() - beginTime.getTime()) / 1000;

                long hours = time / 3600;
                long minutes = (time - hours * 3600) / 60;
                long seconds = time - hours * 3600 - minutes * 60;

                publishProgress(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }

            beginTime = null;

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            textTime.setText(values[0]);
        }
    }

    class PointTask extends AsyncTask<Point, Void, Void> {

        @Override
        protected Void doInBackground(Point... params) {
            Point point = params[0];
            String uuidTrack = db.getUuidTrackForPoint(point.getUuid());
            int num = db.getNumForPoint(point.getUuid());
            Track track = new Track();
            track.setUuid(uuidTrack);
            db.updatePoint(point, track, num);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public interface GoogleApi {
        @GET("/maps/api/geocode/json")
        Call<Results> getData(
                @Query("latlng") String latlng,
                @Query("language") String language,
                @Query("key") String key
        );
    }


}
