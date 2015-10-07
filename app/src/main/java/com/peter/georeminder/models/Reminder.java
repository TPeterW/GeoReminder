package com.peter.georeminder.models;

import android.graphics.Color;
import android.location.Location;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Peter on 10/6/15.
 */
public class Reminder {
    private Date startingDate;
    private Date endDate;

    private Location createLocation;
    private Location remindLocation;
    private Location lastKnownLocation;
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


    // utilities
    Calendar cal = Calendar.getInstance();

    public Reminder(){
        initialise();
    }

    private void initialise() {
        startingDate = cal.getTime();
//        endDate



    }
}
