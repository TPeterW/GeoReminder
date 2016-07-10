package com.peter.georeminder.models;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.peter.georeminder.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 11/6/15.
 *
 */
public class Location {

    private LatLng googleMapLatLng;
    private com.amap.api.maps.model.LatLng aMapLatLng;

    public double latitude;
    public double longitude;

    private String name;
    private List<Reminder> reminders;

    private transient Context context;

    public Location(Context context){
        this(context, context.getString(R.string.location_default_title));
    }

    public Location(Context context, String title) {
        this(context, title, new ArrayList<Reminder>());
    }

    public Location(Context context, String title, List<Reminder> reminders) {
        this.context = context;
        setName(title);
        this.reminders = reminders;
    }

    public LatLng getGoogleMapLatLng() {
        return googleMapLatLng;
    }

    public com.amap.api.maps.model.LatLng getAMapLatLng() {
        return aMapLatLng;
    }

    public Location setLatLng(@Nullable Double latitude, @Nullable Double longitude) {
        if (latitude == null || longitude == null) {
            this.latitude = 0;
            this.longitude = 0;
            googleMapLatLng = null;
            aMapLatLng = null;
            return this;
        }
        this.latitude = latitude;
        this.longitude = longitude;
        googleMapLatLng = new LatLng(latitude, longitude);
        aMapLatLng = new com.amap.api.maps.model.LatLng(latitude, longitude);
        return this;
    }

    public String getName() {
        return name;
    }

    public Location setName(String name) {
        this.name = name;
        return this;
    }

    // reminders at this location
    public List<Reminder> getReminders() {
        return reminders;
    }

    public Location removeReminder(String uuid) {
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getUuid().equals(uuid)) {
                reminders.remove(i);
                return this;
            }
        }

        return this;
    }

    public Location addReminder(Reminder reminder) {
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getUuid().equals(reminder.getUuid()))
                return this;
        }

        reminders.add(reminder);
        return this;
    }
}
