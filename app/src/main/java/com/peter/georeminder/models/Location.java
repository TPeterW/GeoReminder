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

    private String name;


    public Location(Context context){
        setTitle(context.getString(R.string.location_default_title));
    }

    public LatLng getGoogleMapLatLng() {
        return googleMapLatLng;
    }

    public com.amap.api.maps.model.LatLng getAMapLatLng() {
        return aMapLatLng;
    }

    public Location setLatLng(double latitude, double longitude) {
        googleMapLatLng = new LatLng(latitude, longitude);
        aMapLatLng = new com.amap.api.maps.model.LatLng(latitude, longitude);
        return this;
    }

    public String getTitle() {
        return name;
    }

    public Location setTitle(String name) {
        this.name = name;
        return this;
    }


}
