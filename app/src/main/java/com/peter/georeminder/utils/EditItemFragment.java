package com.peter.georeminder.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.peter.georeminder.R;

/**
 * Created by Peter on 10/6/15.
 */
public class EditItemFragment extends Fragment {

    private boolean withMap;
    private Bundle bundle;

    public EditItemFragment() {
        bundle = getArguments();
        withMap = bundle.getBoolean("withMap");
        //TODO: and other specifications of the reminder to check
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        if(withMap){
            view = inflater.inflate(R.layout.geo_reminder_edit_screen, container, false);
        }
        else {
            view = inflater.inflate(R.layout.normal_reminder_edit_screen, container, false);
        }

        return view;
//        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
