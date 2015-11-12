package com.peter.georeminder.utils.viewpager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peter.georeminder.R;
import com.peter.georeminder.models.Location;
import com.peter.georeminder.utils.recyclerview.LocationRecyclerAdapter;

import java.util.List;

/**
 * Created by Peter on 11/5/15.
 *
 */
public class ListLocationFragment extends Fragment implements OnSharedPreferenceChangeListener {

    private ListLocationListener listener;

    private List<Location> locationList;

    private RecyclerView recyclerView;

    private ImageView imgNoLocation;
    private TextView txtNoLocation;


    private LocationRecyclerAdapter recyclerAdapter;
    private StaggeredGridLayoutManager layoutManager;

    public static ListLocationFragment getInstance(List<Location> locationList) {
        return new ListLocationFragment(locationList);
    }

    public ListLocationFragment(List<Location> locationList){
        this.locationList = locationList;
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

        setUpOthers();

        return rootView;
    }

    private void setUpRecyclerView() {
        recyclerAdapter = new LocationRecyclerAdapter(getActivity(), locationList);
        recyclerAdapter.setOnItemClickListener(new LocationRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                listener.onLocationClicked(view, position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                listener.onLocationLongClicked(view, position);
                // TODO: figure out later what to do for long click
                recyclerAdapter.deleteLocation(position);

                if(locationList.size() == 0){
                    imgNoLocation.setVisibility(View.VISIBLE);
                    txtNoLocation.setVisibility(View.VISIBLE);
                }
            }
        });

//        layoutManager = new StaggeredGridLayoutManager((int) getActivity().getResources().getDimension(R.dimen.staggered_space),
//                StaggeredGridLayoutManager.VERTICAL);

        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                listener.onLocationListScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void setUpOthers() {
        if(locationList.size() != 0) {
            imgNoLocation.setVisibility(View.INVISIBLE);
            txtNoLocation.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAttach(Context context) {
        this.listener = (ListLocationListener) context;
        super.onAttach(context);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_is_refreshing))) {
            if(!sharedPreferences.getBoolean(getString(R.string.pref_is_refreshing), false)){       // not refreshing
                recyclerView.setNestedScrollingEnabled(true);
                layoutManager.smoothScrollToPosition(recyclerView, null, 0);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.pref_app_bar_enabled), true)
                        .apply();
            } else {        // is refreshing
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.pref_app_bar_enabled), false)
                        .apply();
                recyclerView.setNestedScrollingEnabled(false);
            }
            Log.i("ListLocationFragment", "Refresh Status: " + sharedPreferences.getBoolean(getString(R.string.pref_is_refreshing), true));
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

        void onLocationListRefresh();           // TODO: if not needed in the end, just remove it
    }
}
