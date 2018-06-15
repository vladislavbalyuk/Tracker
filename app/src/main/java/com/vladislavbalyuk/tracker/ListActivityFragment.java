package com.vladislavbalyuk.tracker;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListActivityFragment extends Fragment {

    private View view;

    private Db db;

    private ListActivity activity;

    private boolean menuItemVisible;

    private TrackAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        db = Db.getInstance(activity);
        menuItemVisible = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
        } else {
            view = inflater.inflate(R.layout.fragment_list, container, false);

            getListTrack();

        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (ListActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    public boolean isMenuItemVisible() {
        return menuItemVisible;
    }

    private void setRecycler(List list){
        RecyclerView listView = (RecyclerView) view.findViewById(R.id.rcTracks);
        listView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        adapter = new TrackAdapter(activity, list);
        listView.setAdapter(adapter);
    }

    public void deleteEnabled(){
        new DeleteTrackTask().execute();
    }

    public void getListTrack(){
        new GetListTrackTask().execute();
    }

    private void setVisibilityButtonRemove(){
        menuItemVisible = (adapter.listEnabled.size() > 0);
        activity.setItemMenuVisible(menuItemVisible);
    }

    public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder> {

        private LayoutInflater inflater;
        private List<Track> list;
        private List<String> listEnabled;

        public TrackAdapter(Context context, List<Track> list) {
            this.list = list;
            this.inflater = LayoutInflater.from(context);
            this.listEnabled = new ArrayList<String>();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = inflater.inflate(R.layout.item_recycler, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Track track = list.get(position);

            holder.textViewName.setText(track.getName());
            holder.textViewDistance.setText(String.format("%.0f",track.getTotalDistance()) + "m");
            holder.textViewTime.setText(track.getTotalTime());

            holder.setTrackUuid(track.getUuid());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        private void modifyListEnabled(String uuid, boolean enabled){
            if(enabled)
                listEnabled.add(uuid);
            else
                listEnabled.remove(uuid);

            setVisibilityButtonRemove();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            final TextView textViewName;
            final TextView textViewDistance;
            final TextView textViewTime;

            private String uuid;
            private boolean enabled;

            ViewHolder(View view){
                super(view);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(listEnabled.size() > 0)
                            changeEnabled(v);
                        else{
                            Intent intent = new Intent(getActivity(), TrackActivity.class);
                            intent.putExtra("uuid",uuid);
                            startActivity(intent);
                        }

                    }
                });

                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        changeEnabled(v);
                        return true;
                    }
                });

                textViewName = (TextView) view.findViewById(R.id.textName);
                textViewDistance = (TextView) view.findViewById(R.id.textDistance);
                textViewTime = (TextView) view.findViewById(R.id.textTime);

                enabled = false;

            }

            void changeEnabled(View v){
                enabled = !enabled;
                v.setBackgroundColor(getResources().getColor(enabled?R.color.colorEnabledItem:R.color.colorNotEnabledItem));
                modifyListEnabled(uuid,enabled);
            }

            public String getTrackUuid() {
                return uuid;
            }

            public void setTrackUuid(String uuid) {
                this.uuid = uuid;
            }
        }
    }

    class GetListTrackTask extends AsyncTask<Void, Void, List<Track>>{
        @Override
        protected List<Track> doInBackground(Void... params) {
            List<Track> list = db.getTracks();
            return list;
        }

        @Override
        protected void onPostExecute(List<Track> list) {
            super.onPostExecute(list);
            setRecycler(list);
            setVisibilityButtonRemove();
        }
    }

    class DeleteTrackTask extends  AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            for(String uuid: adapter.listEnabled){
                Track track = new Track();
                track.setUuid(uuid);
                db.deleteTrack(track);
            }

            getListTrack();

            return null;
        }

    }
}
