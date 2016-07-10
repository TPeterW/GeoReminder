package com.peter.georeminder.models;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.peter.georeminder.R;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by Peter on 10/6/15.
 *
 */
public class Reminder {

    private String uuid;

    private long createTime;
    private long startTime;          // hour * 60 + min
    private long endTime;

    private Location createLocation;
    private Location remindLocation;

    private String title;
    private String description;
    private String additional = null;
    private int importance;         // importance: 1, 2, 3, 4
    private @ColorInt int colorInt;

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
    private transient Context context;


    // utilities
    Calendar cal = Calendar.getInstance();

    public Reminder(Context context){
        this(context, context.getString(R.string.reminder_default_title));
    }

    public Reminder(Context context, String title) {
        this(context, title, ContextCompat.getColor(context, R.color.colorPrimary));
    }

    public Reminder(Context context, String title, @ColorInt int colorInt) {
        this.context = context;

        this.title = title;
        createTime = System.currentTimeMillis();
        this.colorInt = colorInt;
        vibrate = true;
        repeatType = REPEAT_EVERYDAY;
        withTime = false;

        uuid = UUID.randomUUID().toString();
    }

    // UUID
    public String getUuid() {
        return uuid;
    }

    public Reminder setUuid(String uuid) {
        this.uuid = uuid;
        return this;
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

    // location created at
    public Location getCreateLocation() {
        return createLocation;
    }

    public Reminder setCreateLocation(@Nullable Double latitude, @Nullable Double longitude) {
        if (createLocation == null)
            createLocation = new Location(context);

        createLocation.setLatLng(latitude, longitude);
        createLocation.setName(null);
        return this;
    }

    // location to remind at
    public Location getRemindLocation() {
        return remindLocation;
    }

    public Reminder setRemindLocation(double latitude, double longitude, String title) {
        if (remindLocation == null)
            remindLocation = new Location(context);

        remindLocation.setLatLng(latitude, longitude);
        remindLocation.setName(title);
        return this;
    }

    // calculate distance to designated location
    public double distanceTo(Location location) {
        float[] result = new float[1];
        android.location.Location.distanceBetween(remindLocation.latitude, remindLocation.longitude,
                location.latitude, location.longitude, result);
        return result[0];
    }

    // timeFromNow
    public long getStartTimeFromNow() {
        return startTime - System.currentTimeMillis();
    }

    public long getEndTimeFromNow() {
        return endTime - System.currentTimeMillis();
    }

    // create time
    public long getCreateTime() {
        return createTime;
    }

    public Reminder setCreateTime(long createTime) {
        this.createTime = createTime;
        return this;
    }

    // start time
    public long getStartTime() {
        return startTime;
    }

    public Reminder setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    // end time
    public long getEndTime() {
        return endTime;
    }

    public Reminder setEndTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    // color
    public @ColorInt int getColorInt() {
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
