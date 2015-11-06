package com.peter.georeminder.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by Peter on 11/6/15.
 *
 */
public class Location {
    private HashMap<String, Reminder> reminderHashMap;

    private LatLng googleMapLatLng;
    private com.amap.api.maps.model.LatLng aMapLatLng;


}
