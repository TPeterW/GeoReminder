package com.peter.georeminder.models;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;

import com.peter.georeminder.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Peter on 10/6/15.
 * TODO: encapsulate all fields
 */
public class Reminder {

    private String Uid;             // Uid = title_lat_lng_createDate_createTime

    private Date createDateTime;
    private Date startDate;
    private Date endDate;
    private int startTime;          // hour * 60 + min
    private int endTime;
    private long timeFromNow;

    private Double createLat;
    private Double createLng;
    private Double remindLat;
    private Double remindLng;
    private int locationAccuracy;
    private double distanceToHere;

    private String title;
    private String description;
    private String additional = null;
    private int importance;         // importance: 1, 2, 3, 4
    private int colorInt;

    private boolean withLocation;
    private boolean withTime;

    // how to remind
    private boolean vibrate;
    private int notification;
    private int repeatTimes;
    private int repeatType;         // 0 = interval everyday, 1 = from point to point
    public static final int REPEAT_EVERYDAY             = 0x0;
    public static final int POINT_TO_POINT              = 0x1;
    public static final int ALL_DAY                     = 0x2;          // means not with time

    // reminder type
    private int reminderType;       // 0 = notes, 1 = geo, 2 = normal
    public static final int NOTES                       = 0x0;
    public static final int GEO                         = 0x1;
    public static final int NOR                         = 0x2;


    // logistics
    private Context context;


    // utilities
    Calendar cal = Calendar.getInstance();

    public Reminder(Context context){
        initialise(context);
    }

    private void initialise(Context context) {
        this.context = context;

        startDate = cal.getTime();
        title = context.getString(R.string.reminder_default_title);
        distanceToHere = 0;
        startDate = null;
        endDate = null;
        createDateTime = new Date();
        colorInt = R.color.colorPrimary;
        vibrate = true;
        repeatType = REPEAT_EVERYDAY;
        withTime = false;
    }

    // Title
    public String getTitle() {
        return title;
    }

    public Reminder setTitle(String title) {
        this.title = title;
        return this;
    }

    // Description
    public String getDescription() {
        return description;
    }

    public Reminder setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getAdditional() {
        return additional;
    }

    public Reminder setAdditional(String additional) {
        this.additional = additional;
        return this;
    }

    // withLocation
    public boolean isWithLocation() {
        return withLocation;
    }

    public Reminder withLocation(boolean withLocation) {
        this.withLocation = withLocation;
        return this;
    }

    // withTime
    public boolean isWithTime() {
        return withTime;
    }

    public Reminder withTime(boolean withTime) {
        this.withTime = withTime;
        return this;
    }

    // latlng
    public Double getCreateLat() {
        return createLat;
    }

    public Reminder setCreateLat(@Nullable Double createLat) {
        this.createLat = createLat;
        return this;
    }

    public Double getCreateLng() {
        return createLng;
    }

    public Reminder setCreateLng(@Nullable Double createLng) {
        this.createLng = createLng;
        return this;
    }

    public Double getRemindLat() {
        return remindLat;
    }

    public Reminder setRemindLat(@Nullable Double remindLat) {
        this.remindLat = remindLat;
        return this;
    }

    public Double getRemindLng() {
        return remindLng;
    }

    public Reminder setRemindLng(@Nullable Double remindLng) {
        this.remindLng = remindLng;
        return this;
    }

    // distanceToHere
    public double getDistanceToHere() {
        return distanceToHere;
    }

    public Reminder setDistanceToHere(double distanceToHere) {
        this.distanceToHere = distanceToHere;
        return this;
    }

    // timeFromNow
    public long getTimeFromNow() {
        return timeFromNow;
    }

    public Reminder setTimeFromNow(long timeFromNow) {
        this.timeFromNow = timeFromNow;
        return this;
    }

    // createDateTime
    public Date getCreateDateTime() {
        return createDateTime;
    }

    public Reminder setCreateDateTime(Date createDateTime) {
        this.createDateTime = createDateTime;
        return this;
    }

    // startDate
    public Date getStartDate() {
        return startDate;
    }

    public Reminder setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    // endDate
    public Date getEndDate() {
        return endDate;
    }

    public Reminder setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    // startTime
    public int getStartTime() {
        return startTime;
    }

    public Reminder setStartTime(int startTime) {
        this.startTime = startTime;
        return this;
    }

    // endTime
    public int getEndTime() {
        return endTime;
    }

    public Reminder setEndTime(int endTime) {
        this.endTime = endTime;
        return this;
    }

    // color
    public int getColorInt() {
        return colorInt;
    }

    public Reminder setColorInt(@ColorInt int colorInt) {
        this.colorInt = colorInt;
        return this;
    }

    // repeat type
    public int getRepeatType() {
        return repeatType;
    }

    public Reminder setRepeatType(int repeatType) {
        this.repeatType = repeatType;
        return this;
    }

    // reminder type
    public int getReminderType() {
        return reminderType;
    }

    public Reminder setReminderType(int reminderType) {
        this.reminderType = reminderType;
        return this;
    }
}
