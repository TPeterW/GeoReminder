package com.peter.georeminder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.peter.georeminder.EditorScreen;
import com.peter.georeminder.R;
import com.peter.georeminder.models.Reminder;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by Peter on 10/6/15.
 *
 */
public class EditItemFragment extends Fragment implements OnMapReadyCallback, LocationSource {

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

    private FrameLayout googleMapContainer;
    private FrameLayout aMapContainer;

    private Marker googleMapMarker;
    private com.amap.api.maps.model.Marker aMapMarker;


    // the editor views
    private MaterialEditText reminderTitle;
    private MaterialEditText reminderDescription;
    private TextView colorPicker;


    View rootView;

    private static final int COARSE_LOCATION_PERMISSION_REQUEST_CODE = 0x001;
    private static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 0x002;

    public EditItemFragment() {
        this.savedInstanceState = getArguments();           // why did I do this again?
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (!getSpecs())
            return null;

        if (withMap) {
            rootView = inflater.inflate(R.layout.reminder_geo_edit_screen, container, false);

            googleMapContainer = (FrameLayout) rootView.findViewById(R.id.google_map_container);
            aMapContainer = (FrameLayout) rootView.findViewById(R.id.amap_map_container);

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
            newReminder = arguments.getBoolean(getString(R.string.bundle_new_reminder));
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
        if (newReminder) {
            currentReminder = new Reminder(getActivity());
            Log.i("EditItemFragment", "New reminder");
        } else {            // from draft or edit existing
            Log.i("EditItemFragment", "Edit reminder");
            try {
                String existingInJSON = arguments.getString(getString(R.string.bundle_most_recent_reminder), null);
                if (existingInJSON != null) {
                    // existing reminder is passed in as JSON string
                    Gson gson = new Gson();
                    currentReminder = gson.fromJson(existingInJSON, Reminder.class);
                    setAllColors(currentReminder.getColorInt());

                    // change parent activity color
                    if (((EditorScreen) getActivity()).getSupportActionBar() != null) {
                        ((EditorScreen) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(currentReminder.getColorInt()));
                    }
                    if (getActivity().getActionBar() != null)
                        getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(currentReminder.getColorInt()));

                    if (Build.VERSION.SDK_INT >= 21) {
                        getActivity().getWindow().setStatusBarColor(currentReminder.getColorInt());
                        getActivity().getWindow().setNavigationBarColor(currentReminder.getColorInt());
                    }
                }
            } catch (Exception e) {
                // if cannot get the reminder, then we exit and say sorry
                ((EditorScreen) getActivity()).scrollToFinishActivity();
                Toast.makeText(getActivity(), getString(R.string.error_retrieving_reminder), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressWarnings("all")
    private void initView(View rootView) {
        colorPicker = (TextView) rootView.findViewById(R.id.textview_color_picker);
        reminderTitle = (MaterialEditText) rootView.findViewById(R.id.edittext_title);
        reminderDescription = (MaterialEditText) rootView.findViewById(R.id.edittext_description);
    }

    private void initEvent() {
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

    private void setUpMap() {
        supportGoogleMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.edit_google_map);

        supportAMapFragment = (com.amap.api.maps.SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.edit_amap_map);

        if(useGoogleMap) {
            aMapContainer.setVisibility(View.GONE);
            supportAMapFragment.onDestroy();        // hide AMap
            supportGoogleMapFragment.getMapAsync(this);
        } else {        // use AMap
            googleMapContainer.setVisibility(View.GONE);
            supportGoogleMapFragment.onDetach();   // hide Google Map
            supportGoogleMapFragment.onDestroyView();
            supportAMapFragment.onCreate(savedInstanceState);
            aMap = supportAMapFragment.getMap();

            initAMap();
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

        if(coarseLocation == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    COARSE_LOCATION_PERMISSION_REQUEST_CODE);
        }

        if(fineLocation == PackageManager.PERMISSION_DENIED) {
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

        colorPicker.setTextColor(selectedColor);
    }

    public void saveReminder() {
        Log.i("EditItemFragment", "Saving reminder...");

        currentReminder.setTitle(reminderTitle.getText().toString());
        currentReminder.setDescription(reminderDescription.getText().toString());
        // TODO: more

        // convert to gson
        Gson gson = new Gson();
        String currentReminderInJSONString = gson.toJson(currentReminder);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putString(getString(R.string.bundle_most_recent_reminder), currentReminderInJSONString).apply();
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
        super.onResume();
        if (withMap && !useGoogleMap)
            supportAMapFragment.onResume();
    }

    @Override
    public void onPause() {
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
        super.onDestroy();
        if (withMap && !useGoogleMap)
            supportAMapFragment.onDestroyView();
    }

    @Override
    public void onDestroyView() {
        if (Build.VERSION.SDK_INT >= 21)
            getActivity().getWindow().setReturnTransition(new Slide(Gravity.END));
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
