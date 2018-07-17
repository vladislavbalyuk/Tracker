package com.vladislavbalyuk.tracker;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TrackActivityFragment extends Fragment {

    private View view;

    private TextView textTime, textDistance, textSpeed, textAdressFrom, textAdressTo;

    private String uuid;

    private Track track;

    private Db db;

    private TrackActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        db = Db.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
        } else {
            view = inflater.inflate(R.layout.fragment_track, container, false);
            textTime = (TextView) view.findViewById(R.id.textTime);
            textDistance = (TextView) view.findViewById(R.id.textDistance);
            textSpeed = (TextView) view.findViewById(R.id.textSpeed);
            textAdressFrom = (TextView) view.findViewById(R.id.textAdressFrom);
            textAdressTo = (TextView) view.findViewById(R.id.textAdressTo);
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (TrackActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Track getTrack() {
        return track;
    }

    public void readTrack() {
        new GetTrackTask().execute();
    }

    class GetTrackTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            track = db.getTrack(uuid);
            track.setPoints(db.getPoints(track));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (track.getPoints().size() > 0) {
                activity.showTrack(track);
                textTime.setText(track.getTotalTime());
                textDistance.setText(String.format("%.0f", track.getTotalDistance()) + "m");
                textSpeed.setText(String.format("%.0f", track.getTotalSpeed()) + "km/h");
                textAdressFrom.setText(track.getPoints().get(0).getAdress());
                textAdressTo.setText(track.getPoints().get(track.getPoints().size() - 1).getAdress());
            }

            activity.setItemMenuVisible(true);
        }
    }
}
