package com.peter.georeminder.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment;
import com.peter.georeminder.EditorScreen;
import com.peter.georeminder.R;
import com.peter.georeminder.models.Reminder;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import carbon.widget.Divider;
import me.tittojose.www.timerangepicker_library.TimeRangePickerDialog;

/**
 * Created by Peter on 10/6/15.
 *
 */
public class EditItemFragment extends Fragment implements OnMapReadyCallback, LocationSource {

    private static final String TAG = "EditItemFragment";

    private MapListener listener;

    // specs
    private boolean withMap;
    private boolean useGoogleMap;
    private boolean newReminder;

    // data
    private Reminder currentReminder;
    private Bundle savedInstanceState;
    private Bundle arguments;

    private SupportMapFragment supportGoogleMapFragment;
    private GoogleMap googleMap;

    private com.amap.api.maps.SupportMapFragment supportAMapFragment;
    private AMap aMap;

    private FrameLayout mapContainer;

    private Marker googleMapMarker;
    private com.amap.api.maps.model.Marker aMapMarker;
    

    // the editor views
    private MaterialEditText reminderTitle;
    private MaterialEditText reminderDescription;

    private LinearLayout alwaysContainer;
    private TextView alwaysText;
    private Switch alwaysSwitch;

    private LinearLayout allDayContainer;
    private TextView allDayText;
    private Switch allDaySwitch;

    private LinearLayout startDateTimeContainer;
    private TextView startDate;
    private TextView startTime;
    private LinearLayout endDateTimeContainer;
    private TextView endDate;
    private TextView endTime;
    private TextView repeatTimeRange;
    private TextView repeatOptions;

    private TextView colorPicker;
    private MaterialEditText reminderAdditional;

    private Divider dividerUnderTitle;
    private Divider dividerUnderTime;

    private View rootView;

    private Gson gson;

    private static final int COARSE_LOCATION_PERMISSION_REQUEST_CODE    = 0x001;
    private static final int FINE_LOCATION_PERMISSION_REQUEST_CODE      = 0x002;

    private static final String TIME_RANGE_DIALOG_TAG                   = "TIMERANGE";
    private static final String DATE_RANGE_DIALOG_TAG                   = "DATERANGE";

    public EditItemFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.savedInstanceState = savedInstanceState;

        if (!getSpecs())
            return null;

        if (withMap) {
            rootView = inflater.inflate(R.layout.reminder_geo_edit_screen, container, false);

            mapContainer = (FrameLayout) rootView.findViewById(R.id.edit_map_container);

            setUpMap();
        } else {
            rootView = inflater.inflate(R.layout.reminder_normal_edit_screen, container, false);
        }

        initData();

        initView(rootView);

        initEvent();

        return rootView;
    }

    private boolean getSpecs() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        try {
            arguments = getArguments();
            withMap = arguments.getBoolean(getString(R.string.bundle_with_map));
            newReminder = arguments.getBoolean(getString(R.string.bundle_new_reminder));    // default false
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

        // which map to use
        useGoogleMap = sharedPreferences.getBoolean(getString(R.string.shared_pref_google_avail), false)
                && sharedPreferences.getString(getString(R.string.shared_pref_which_map), "0").equals("0");        // "0" is google map

        return true;
    }

    private void initData() {
        // TODO: not sure if works
        gson = new Gson();

        if (newReminder) {
            currentReminder = new Reminder(getActivity());
            currentReminder.setRepeatType(Reminder.ALL_DAY);            // default withTime false

            Calendar calendar = Calendar.getInstance();
            currentReminder.setCreateDateTime(calendar.getTime());
            currentReminder.setStartTime(calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));
            currentReminder.setEndTime(calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.clear();
            calendar.set(year, month, day);
            currentReminder.setStartDate(calendar.getTime());           // for these two we only need year, month, day
            currentReminder.setEndDate(calendar.getTime());

            // set reminder type
            if (withMap)
                currentReminder.setReminderType(Reminder.GEO);
            else
                currentReminder.setReminderType(Reminder.NOR);

            // set create location
            try {
                Location currentLocation = getLastKnownLocation();
                currentReminder.setCreateLat(currentLocation.getLatitude());
                currentReminder.setCreateLng(currentLocation.getLongitude());
            } catch (Exception e) {         // probably null pointer
                currentReminder.setCreateLat(null);
                currentReminder.setCreateLng(null);
            }

            Log.i("EditItemFragment", "New reminder");
        } else {            // from draft or edit existing
            Log.i("EditItemFragment", "Edit reminder");
            try {
                String existingInJSON = arguments.getString(getString(R.string.shared_pref_most_recent_reminder), null);
                if (existingInJSON != null) {
                    // existing reminder is passed in as JSON string
                    currentReminder = gson.fromJson(existingInJSON, Reminder.class);
                    setAllColors(currentReminder.getColorInt());
                }
            } catch (Exception e) {
                // if cannot get the reminder, then we exit and say sorry
                getActivity().onBackPressed();
                Toast.makeText(getActivity(), getString(R.string.error_retrieving_reminder), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressWarnings("all")
    private void initView(View rootView) {
        colorPicker = (TextView) rootView.findViewById(R.id.textview_color_picker);
        reminderTitle = (MaterialEditText) rootView.findViewById(R.id.edittext_title);
        reminderDescription = (MaterialEditText) rootView.findViewById(R.id.edittext_description);

        alwaysContainer = (LinearLayout) rootView.findViewById(R.id.always_container);
        alwaysText = (TextView) rootView.findViewById(R.id.always_txt);
        alwaysSwitch = (Switch) rootView.findViewById(R.id.always_switch);

        // TODO: change visibility accordingly
        allDayContainer = (LinearLayout) rootView.findViewById(R.id.all_day_container);
        allDayText = (TextView) rootView.findViewById(R.id.all_day_txt);
        allDaySwitch = (Switch) rootView.findViewById(R.id.all_day_switch);

        startDateTimeContainer = (LinearLayout) rootView.findViewById(R.id.start_date_time_container);
        endDateTimeContainer = (LinearLayout) rootView.findViewById(R.id.end_date_time_container);
        startDate = (TextView) rootView.findViewById(R.id.start_date);
        startTime = (TextView) rootView.findViewById(R.id.start_time);
        endDate = (TextView) rootView.findViewById(R.id.end_date);
        endTime = (TextView) rootView.findViewById(R.id.end_time);
        repeatTimeRange = (TextView) rootView.findViewById(R.id.repeat_everyday_time_range_txt);
        repeatOptions = (TextView) rootView.findViewById(R.id.repeat_options_txt);

        reminderAdditional = (MaterialEditText) rootView.findViewById(R.id.edittext_additional);

        dividerUnderTitle = (Divider) rootView.findViewById(R.id.divider_under_title);
        dividerUnderTime = (Divider) rootView.findViewById(R.id.divider_under_time);

        // hide keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

        switch (currentReminder.getRepeatType()) {
            case Reminder.ALL_DAY:
                setAllDayLayout();
                break;
            case Reminder.POINT_TO_POINT:
                setPointToPointLayout();
                break;
            case Reminder.REPEAT_EVERYDAY:
                setRepeatEverydayLayout();
                break;
        }

        // TODO: check withMap,

        // change parent activity color
        if (newReminder) {
            if (Build.VERSION.SDK_INT >= 21) {
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            }

            if (withMap) {      // GEO
                alwaysContainer.setVisibility(View.VISIBLE);
                currentReminder.withTime(false);
                allDayContainer.setVisibility(View.GONE);
                startDateTimeContainer.setVisibility(View.GONE);
                endDateTimeContainer.setVisibility(View.GONE);
            } else {            // NOR
                currentReminder.withTime(true);
                alwaysContainer.setVisibility(View.GONE);
            }
        } else {
            if (currentReminder != null) {
                if (((EditorScreen) getActivity()).getSupportActionBar() != null)
                    ((EditorScreen) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(currentReminder.getColorInt()));

                if (getActivity().getActionBar() != null)
                    getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(currentReminder.getColorInt()));

                if (Build.VERSION.SDK_INT >= 21) {
                    getActivity().getWindow().setStatusBarColor(currentReminder.getColorInt());
                    getActivity().getWindow().setNavigationBarColor(currentReminder.getColorInt());
                }
            }
        }
    }

    private void initEvent() {
        // title and description
        reminderTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getActivity().setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() < 1) {
                    getActivity().setTitle(getString(R.string.title_untitled));
                }
            }
        });

        // date
        alwaysContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alwaysSwitch.performClick();
            }
        });

        alwaysText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alwaysSwitch.performClick();
            }
        });

        alwaysSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alwaysSwitch.isChecked()) {
                    currentReminder.withTime(false);

                    allDayContainer.setVisibility(View.GONE);
                    startDateTimeContainer.setVisibility(View.GONE);
                    endDateTimeContainer.setVisibility(View.GONE);
                    repeatTimeRange.setVisibility(View.GONE);
                    repeatOptions.setVisibility(View.GONE);

                    Log.i("EditItemFragment", "Event always");
                } else {
                    currentReminder.withTime(true);

                    allDayContainer.setVisibility(View.VISIBLE);
                    startDateTimeContainer.setVisibility(View.VISIBLE);
                    endDateTimeContainer.setVisibility(View.VISIBLE);

                    switch (currentReminder.getRepeatType()) {
                        case Reminder.ALL_DAY:
                            setAllDayLayout();
                            break;
                        case Reminder.REPEAT_EVERYDAY:
                            setRepeatEverydayLayout();
                            break;
                        case Reminder.POINT_TO_POINT:
                            setPointToPointLayout();
                            break;
                    }

                    Log.i("EditItemFragment", "Event has date limit");
                }
            }
        });

        // time
        allDayContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allDaySwitch.performClick();
            }
        });

        allDayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allDaySwitch.performClick();
            }
        });

        allDaySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allDaySwitch.isChecked()) {
                    currentReminder.setRepeatType(Reminder.ALL_DAY);
                    setAllDayLayout();
                    Log.i("EditItemFragment", "Event all day");
                    // TODO:

                } else {
                    Log.i("EditItemFragment", "Event has time limit");
                    if (repeatOptions.getText().toString().equals(getString(R.string.repeat_option_repeat_everyday))) {
                        currentReminder.setRepeatType(Reminder.REPEAT_EVERYDAY);
                        setRepeatEverydayLayout();
                    } else {
                        currentReminder.setRepeatType(Reminder.POINT_TO_POINT);
                        setPointToPointLayout();
                    }
                    // TODO:

                }
            }
        });

        startDateTimeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentReminder.getRepeatType() == Reminder.ALL_DAY
                        || currentReminder.getRepeatType() == Reminder.REPEAT_EVERYDAY) {
                    showDateRangeDialog();
                }
            }
        });

        endDateTimeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentReminder.getRepeatType() == Reminder.ALL_DAY
                        || currentReminder.getRepeatType() == Reminder.REPEAT_EVERYDAY) {
                    showDateRangeDialog();
                }
            }
        });

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateRangeDialog();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateRangeDialog();
            }
        });

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeRangeDialog();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeRangeDialog();
            }
        });

        // repeat
        repeatTimeRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeRangeDialog();
            }
        });

        repeatOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.repeat_options)
                        .setItems(R.array.repeat_option_entry, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:         // repeat everyday
                                        currentReminder.setRepeatType(Reminder.REPEAT_EVERYDAY);
                                        repeatOptions.setText(R.string.repeat_option_repeat_everyday);
                                        setRepeatEverydayLayout();
                                        break;
                                    case 1:         // point to point
                                        currentReminder.setRepeatType(Reminder.POINT_TO_POINT);
                                        repeatOptions.setText(R.string.repeat_option_point_to_point);
                                        setPointToPointLayout();
                                        break;
                                }
                            }
                        });
                builder.create().show();
            }
        });

        colorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorChooserDialog.Builder((EditorScreen) getActivity(), R.string.color_picker_title)
                        .titleSub(R.string.color_picker_title)
                        .accentMode(false)
                        .doneButton(R.string.button_done)
                        .cancelButton(R.string.button_cancel)
                        .backButton(R.string.button_back)
                        .customButton(R.string.button_custom)
                        .show();
            }
        });
    }

    private void showDateRangeDialog() {
        Calendar calendar = Calendar.getInstance();

        SmoothDateRangePickerFragment smoothDateRangePickerFragment = SmoothDateRangePickerFragment.newInstance(new SmoothDateRangePickerFragment.OnDateRangeSetListener() {
            @Override
            public void onDateRangeSet(SmoothDateRangePickerFragment view, int yearStart, int monthStart, int dayStart, int yearEnd, int monthEnd, int dayEnd) {
                Calendar startCalendar = Calendar.getInstance();
                Calendar endCalendar = Calendar.getInstance();
                startCalendar.clear();
                endCalendar.clear();
                startCalendar.set(yearStart, monthStart, dayStart);
                endCalendar.set(yearEnd, monthEnd, dayEnd);

                currentReminder.setStartDate(startCalendar.getTime());
                currentReminder.setEndDate(endCalendar.getTime());

                DateFormat dateFormat = DateFormat.getDateInstance();
                dateFormat.setTimeZone(TimeZone.getDefault());

                startDate.setText(dateFormat.format(startCalendar.getTime()));
                endDate.setText(dateFormat.format(endCalendar.getTime()));

                Log.i("EditItemFragment", "Start date: " + startDate.getText().toString());
                Log.i("EditItemFragment", "End date: " + endDate.getText().toString());
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        smoothDateRangePickerFragment.vibrate(true);
        smoothDateRangePickerFragment.setMinDate(calendar);
        smoothDateRangePickerFragment.show(getActivity().getFragmentManager(), DATE_RANGE_DIALOG_TAG);
    }

    private void showTimeRangeDialog() {
        TimeRangePickerDialog timeRangePickerDialog = TimeRangePickerDialog.newInstance(new TimeRangePickerDialog.OnTimeRangeSelectedListener() {
            @Override
            public void onTimeRangeSelected(int startHour, int startMin, int endHour, int endMin) {
                currentReminder.setStartTime(startHour * 60 + startMin);
                currentReminder.setEndTime(endHour * 60 + endMin);

                startTime.setText(getString(R.string.point_to_point_time_format, startHour, startMin));
                endTime.setText(getString(R.string.point_to_point_time_format, endHour, endMin));
                repeatTimeRange.setText(getString(R.string.time_range_time_format, startHour, startMin, endHour, endMin));

                Log.i("EditItemFragment", "Start time: " + startHour + ":" + startMin);
                Log.i("EditItemFragment", "End time: " + endHour + ":" + endMin);
            }
        }, true);

//        timeRangePickerDialog.setPrimaryColor(currentReminder.getColorInt());

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        timeRangePickerDialog.show(fragmentManager, TIME_RANGE_DIALOG_TAG);
    }

    private void setUpMap() {
        // TODO: change TAG
        if (useGoogleMap) {
            supportGoogleMapFragment = SupportMapFragment.newInstance();
            supportGoogleMapFragment.getMapAsync(this);

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.edit_map_container, supportGoogleMapFragment)
                    .addToBackStack(null)
                    .commit();
        } else {        // use AMap
            try {
                MapsInitializer.initialize(getContext());

                supportAMapFragment = com.amap.api.maps.SupportMapFragment.newInstance();
                supportAMapFragment.onCreate(savedInstanceState);
                aMap = supportAMapFragment.getMap();

                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.edit_map_container, supportAMapFragment)
                        .addToBackStack(null)
                        .commit();

                initAMap();
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to initialise AMap");
            }
        }
    }

    @SuppressWarnings("all")
    @Override
    public void onMapReady(GoogleMap inputMap) {
        googleMap = inputMap;
        googleMap.setMyLocationEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.setTrafficEnabled(true);
        com.google.android.gms.maps.UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setIndoorLevelPickerEnabled(false);

        // put the original marker on
        com.google.android.gms.maps.model.MarkerOptions markerOptions = new com.google.android.gms.maps.model.MarkerOptions();
        markerOptions.flat(false)
                .draggable(true)
                .title(getString(R.string.my_location));
        try {           // in case GPS is not available
            markerOptions.position(new com.google.android.gms.maps.model.LatLng(getLastKnownLocation().getLatitude(), getLastKnownLocation().getLongitude()));
            googleMapMarker = googleMap.addMarker(markerOptions);
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new com.google.android.gms.maps.model.LatLng(getLastKnownLocation().getLatitude(), getLastKnownLocation().getLongitude())));
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), getString(R.string.GPS_unavail), Toast.LENGTH_SHORT).show();
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(com.google.android.gms.maps.model.LatLng latLng) {
                listener.onMapClick(latLng.latitude, latLng.longitude);

                com.google.android.gms.maps.model.MarkerOptions markerOptions = new com.google.android.gms.maps.model.MarkerOptions();
                markerOptions.flat(false)
                        .draggable(true)
                        .title(getString(R.string.reminder_location))
                        .position(latLng);

                googleMap.clear();
                googleMapMarker = googleMap.addMarker(markerOptions);
                // TODO: add latlng to reminder
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                listener.onMarkerDragStart();
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // could let action bar show the current latlng
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                listener.onMarkerDragEnd(marker.getPosition().latitude, marker.getPosition().longitude);
            }
        });
    }

    private void initAMap() {
        aMap.setMyLocationEnabled(true);
        aMap.setLocationSource(this);
        aMap.setTrafficEnabled(true);
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setScaleControlsEnabled(true);
        uiSettings.setZoomControlsEnabled(false);

        // put the original marker on
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.setFlat(false)
                .setGps(false)
                .draggable(true)
                .title(getString(R.string.my_location));
        try {           // in case GPS is not available
            markerOptions.position(new LatLng(getLastKnownLocation().getLatitude(), getLastKnownLocation().getLongitude()));
            aMap.addMarker(markerOptions);
            aMap.animateCamera(com.amap.api.maps.CameraUpdateFactory.newLatLng(new LatLng(getLastKnownLocation().getLatitude(), getLastKnownLocation().getLongitude())));
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), getString(R.string.GPS_unavail), Toast.LENGTH_SHORT).show();
        }

        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                listener.onMapClick(latLng.latitude, latLng.longitude);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.setFlat(false)
                        .setGps(false)
                        .draggable(true)
                        .title(getString(R.string.reminder_location))
                        .position(latLng);

                aMap.clear();
                aMapMarker = aMap.addMarker(markerOptions);
                // TODO: add latlng to reminder
            }
        });

        aMap.setOnMarkerDragListener(new AMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(com.amap.api.maps.model.Marker marker) {
                listener.onMarkerDragStart();
            }

            @Override
            public void onMarkerDrag(com.amap.api.maps.model.Marker marker) {
                // could let action bar show the current latlng
            }

            @Override
            public void onMarkerDragEnd(com.amap.api.maps.model.Marker marker) {
                listener.onMarkerDragEnd(marker.getPosition().latitude, marker.getPosition().longitude);
            }
        });
    }

    private Location getLastKnownLocation() {
        int coarseLocation = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int fineLocation = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (coarseLocation == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    COARSE_LOCATION_PERMISSION_REQUEST_CODE);
        }

        if (fineLocation == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION_REQUEST_CODE);
        }

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    public void onColorChange(@ColorInt int selectedColor) {
        currentReminder.setColorInt(selectedColor);

        setAllColors(selectedColor);
    }

    private void setAllColors(@ColorInt int selectedColor) {
        // TODO: change all colors
        reminderTitle.setPrimaryColor(selectedColor);
        reminderDescription.setPrimaryColor(selectedColor);
        reminderAdditional.setPrimaryColor(selectedColor);

        dividerUnderTitle.setBackgroundColor(selectedColor);
        dividerUnderTime.setBackgroundColor(selectedColor);

        colorPicker.setTextColor(selectedColor);
    }

    private void setRepeatEverydayLayout() {
        startDate.setVisibility(View.VISIBLE);
        endDate.setVisibility(View.VISIBLE);
        startDate.setText(DateFormat.getDateInstance().format(currentReminder.getStartDate()));
        endDate.setText(DateFormat.getDateInstance().format(currentReminder.getEndDate()));

        startTime.setVisibility(View.GONE);
        endTime.setVisibility(View.GONE);

        repeatTimeRange.setVisibility(View.VISIBLE);
        int startHour = currentReminder.getStartTime() / 60;
        int startMin = currentReminder.getStartTime() % 60;
        int endHour = currentReminder.getEndTime() / 60;
        int endMin = currentReminder.getEndTime() % 60;
        repeatTimeRange.setText(getString(R.string.time_range_time_format, startHour, startMin, endHour, endMin));

        repeatOptions.setVisibility(View.VISIBLE);
        repeatOptions.setText(R.string.repeat_option_repeat_everyday);
    }

    private void setPointToPointLayout() {
        startDate.setVisibility(View.VISIBLE);
        endDate.setVisibility(View.VISIBLE);
        startDate.setText(DateFormat.getDateInstance().format(currentReminder.getStartDate()));
        endDate.setText(DateFormat.getDateInstance().format(currentReminder.getEndDate()));

        startTime.setVisibility(View.VISIBLE);
        endTime.setVisibility(View.VISIBLE);

        repeatTimeRange.setVisibility(View.GONE);
        int startHour = currentReminder.getStartTime() / 60;
        int startMin = currentReminder.getStartTime() % 60;
        int endHour = currentReminder.getEndTime() / 60;
        int endMin = currentReminder.getEndTime() % 60;
        startTime.setText(getString(R.string.point_to_point_time_format, startHour, startMin));
        endTime.setText(getString(R.string.point_to_point_time_format, endHour, endMin));

        repeatOptions.setVisibility(View.VISIBLE);
        repeatOptions.setText(R.string.repeat_option_point_to_point);
    }

    private void setAllDayLayout() {
        startDate.setVisibility(View.VISIBLE);
        endDate.setVisibility(View.VISIBLE);
        startDate.setText(DateFormat.getDateInstance().format(currentReminder.getStartDate()));
        endDate.setText(DateFormat.getDateInstance().format(currentReminder.getEndDate()));

        startTime.setVisibility(View.GONE);
        endTime.setVisibility(View.GONE);

        repeatTimeRange.setVisibility(View.GONE);

        repeatOptions.setVisibility(View.GONE);
    }

    public boolean saveReminder() {
        Log.i("EditItemFragment", "Saving reminder...");

        boolean isDraft = false;

        if (reminderTitle.getText() == null)
            isDraft = true;
//        else if (withMap && )     // TODO: get location parameters
        else
            currentReminder.setTitle(reminderTitle.getText().toString());
        currentReminder.setDescription(reminderDescription.getText().toString());
        currentReminder.setAdditional(reminderAdditional.getText().toString());
        // TODO: more

        // convert to gson
        String currentReminderInJSONString = gson.toJson(currentReminder, Reminder.class);

        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                .putString(getString(R.string.shared_pref_most_recent_reminder), currentReminderInJSONString)
                .apply();

        return isDraft;
    }

    @Override
    public void onAttach(Context context) {
        listener = (MapListener) context;
        super.onAttach(context);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case COARSE_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("Permission Granted: ", "ACCESS_COARSE_LOCATION");
                } else {
                    Toast.makeText(getActivity(), getString(R.string.GPS_unavail), Toast.LENGTH_SHORT).show();
                }
                break;

            case FINE_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("Permission Granted: ", "ACCESS_FINE_LOCATION");
                } else {
                    Toast.makeText(getActivity(), getString(R.string.GPS_unavail), Toast.LENGTH_SHORT).show();
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }















    /**
     * Standard override for AMap
     */

    @Override
    public void onResume() {
        Log.d("EditItemFragment", "onResume");
        super.onResume();
        if (withMap && !useGoogleMap)
            supportAMapFragment.onResume();
    }

    @Override
    public void onPause() {
        Log.d("EditItemFragment", "onPause");
        super.onPause();
        if (withMap && !useGoogleMap)
            supportAMapFragment.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (withMap && !useGoogleMap)
            supportAMapFragment.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        Log.d("EditItemFragment", "onDestroy");
        super.onDestroy();
        if (withMap && !useGoogleMap)
            supportAMapFragment.onDestroyView();
    }

    @Override
    public void onDestroyView() {
        Log.d("EditItemFragment", "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {

    }

    @Override
    public void deactivate() {

    }

    public interface MapListener {

        void onMapClick(double latitude, double longitude);

        void onMapLongClick(double latitude, double longitude);

        void onMarkerDragStart();

        void onMarkerDragEnd(double latitude, double longitude);
    }
}
