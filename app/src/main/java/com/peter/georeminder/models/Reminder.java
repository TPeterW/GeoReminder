package com.peter.georeminder.models;

import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.peter.georeminder.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Peter on 10/6/15.
 * TODO: encapsulate all fields
 */
public class Reminder {

    private String Uid;             // Uid = title_lat_lng_createDate_createTime

    private Date createDate;
    private Date startingDate;
    private Date endDate;

    private LatLng createLocation;
    private LatLng remindLocation;
    private LatLng lastKnownLocation;
    private int locationAccuracy;

    private String title;
    private String content;
    private int importance;
    private Color color;

    private boolean geoRemind;
    private boolean timeRemind;

    // how to remind
    private boolean vibrate;
    private int notification;
    private int repeatTimes;

    // reminder type
    private int type;       // 0 = notes, 1 = geo, 2 = normal

    // logistics
    private Context context;


    // utilities
    Calendar cal = Calendar.getInstance();

    public Reminder(Context context){
        initialise(context);
    }

    private void initialise(Context context) {
        startingDate = cal.getTime();
//        endDate

        setTitle(context.getString(R.string.reminder_default_title));



    }

    public String getTitle() {
        return title;
    }

    public Reminder setTitle(String title) {
        this.title = title;
        return this;
    }
}
