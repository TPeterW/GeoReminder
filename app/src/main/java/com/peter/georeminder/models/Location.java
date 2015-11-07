package com.peter.georeminder.models;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.peter.georeminder.R;

import java.util.HashMap;

/**
 * Created by Peter on 11/6/15.
 *
 */
public class Location {
    private HashMap<String, Reminder> reminderHashMap;

    private LatLng googleMapLatLng;
    private com.amap.api.maps.model.LatLng aMapLatLng;

    private String title;


    public Location(Context context){
        initialise(context);
    }

    private void initialise(Context context) {

        setTitle(context.getString(R.string.location_default_title));



    }

    public String getTitle() {
        return title;
    }

    public Location setTitle(String title) {
        this.title = title;
        return this;
    }
}
