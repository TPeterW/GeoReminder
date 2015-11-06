package com.peter.georeminder.utils.viewpager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peter.georeminder.R;
import com.peter.georeminder.models.Reminder;
import com.peter.georeminder.utils.recyclerview.ReminderRecyclerAdapter;

import java.util.List;

/**
 * Created by Peter on 11/5/15.
 *
 */
public class ListLocationFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ListLocationListener listener;

    private List<Reminder> reminderList;

    private RecyclerView recyclerView;

    private ImageView imgNoLocation;
    private TextView txtNoLocation;


    private ReminderRecyclerAdapter recyclerAdapter;
    private StaggeredGridLayoutManager layoutManager;

    public static ListLocationFragment getInstance(){
        return new ListLocationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_list_location_fragment, container, false);

        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.location_recycler_layout);

        imgNoLocation = (ImageView) rootView.findViewById(R.id.image_no_location);
        txtNoLocation = (TextView) rootView.findViewById(R.id.text_no_location);

        setUpRecyclerView();

        return rootView;
    }

    private void setUpRecyclerView() {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_is_refreshing))){
            if(!sharedPreferences.getBoolean(getString(R.string.pref_is_refreshing), false)){
                // TODO: set the recycler view NestedScrollEnabled to true

            }
        }
    }




    @Override
    public void onResume() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    public interface ListLocationListener {

        void onLocationClicked(View view, int position);

        void onLocationLongClicked(View view, final int position);

        void onLocationListScrolled(RecyclerView recyclerView, int dx, int dy);

        void onLocationListRefresh();
    }
}
