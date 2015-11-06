package com.peter.georeminder.utils.viewpager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.peter.georeminder.R;

/**
 * Created by Peter on 11/5/15.
 */
public class ListLocationFragment extends Fragment {

    public static ListLocationFragment getInstance(){
        return new ListLocationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_list_location_fragment, container, false);

        return rootView;
    }
}
