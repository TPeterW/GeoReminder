package com.peter.georeminder;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;

//         ___           ___           ___
//        /\  \         /\  \         /\  \
//       |::\  \       /::\  \       /::\  \
//       |:|:\  \     /:/\:\  \     /:/\:\__\
//     __|:|\:\  \   /:/ /::\  \   /:/ /:/  /
//    /::::|_\:\__\ /:/_/:/\:\__\ /:/_/:/  /
//    \:\~~\  \/__/ \:\/:/  \/__/ \:\/:/  /
//     \:\  \        \::/__/       \::/__/
//      \:\  \        \:\  \        \:\  \
//       \:\__\        \:\__\        \:\__\
//        \/__/         \/__/         \/__/

public class WholeMapScreen extends AppCompatActivity implements OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener, LocationSource, OnLocationChangedListener{

    private GoogleMap googleMap;

    private MapView mapView;
    private AMap aMap;

    private SearchBox searchBox;

    private static final int SETTINGS_REQUEST_CODE = 0x004;
    private static final int COARSE_LOCATION_PERMISSION_REQUEST_CODE = 0x001;
    private static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 0x002;

    // Nav Drawer
    private DrawerLayout drawer;
    private NavigationView navigationView;

    private boolean useGoogleMap;

    LocationManager locationManager;

    //TODO: use amap if Google Play services aren't available

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO: check google service availability and decide which map to use
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // if google is available and user chooses to use google, which is default btw
        useGoogleMap = sharedPreferences.getBoolean(getString(R.string.shared_pref_google_avail), false)
                && sharedPreferences.getString("whichMap", "0").equals("0");        // "0" is google map

        if(useGoogleMap){
            setTheme(R.style.AppTheme_TranslucentWindow);
        }
        else {                  // use AMAP
            setTheme(R.style.AppTheme_TranslucentStatusBar);
        }

        super.onCreate(savedInstanceState);

        if(useGoogleMap){
            setContentView(R.layout.activity_google_map_screen);
        }
        else {                  // use AMAP
            setContentView(R.layout.activity_amap_map_screen);
        }

        initTransitions();

        initNavigationBar();

        initDrawer();

        initMap(savedInstanceState);

        initSearchBox();
    }

    private void initTransitions() {
        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(Gravity.END));
            getWindow().setReturnTransition(new Slide(Gravity.START));
        }
    }

    private void initNavigationBar() {
        if(useGoogleMap && Build.VERSION.SDK_INT >= 21){

        }
    }

    private void initDrawer() {
        // TODO: maybe only necessary for AMap
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Navigation Drawer
        if(useGoogleMap){
            drawer = (DrawerLayout) findViewById(R.id.google_map_drawer_layout);

            navigationView = (NavigationView) findViewById(R.id.nav_google_map_view);
            navigationView.setNavigationItemSelectedListener(this);
        }
        else {
            drawer = (DrawerLayout) findViewById(R.id.amap_map_drawer_layout);

            navigationView = (NavigationView) findViewById(R.id.nav_amap_map_view);
            navigationView.setNavigationItemSelectedListener(this);
        }

        // two drawers use the same header
        // the workaround for support:design:23.1.0
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_whole_map);

        //TODO: init the views
        ImageView avatar = (ImageView) headerLayout.findViewById(R.id.nav_head_avatar);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: go to user account page
                Toast.makeText(WholeMapScreen.this, "Avatar", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initMap(Bundle savedInstanceState) {
        if(useGoogleMap){
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.whole_google_map);
            mapFragment.getMapAsync(this);
        }
        else {
            // Initialise AMAP
            mapView = (MapView) findViewById(R.id.whole_amap_map);
            mapView.onCreate(savedInstanceState);
            aMap = mapView.getMap();

            setUpAmap();
        }

    }

    private void initSearchBox() {
        if(useGoogleMap){
            searchBox = (SearchBox) findViewById(R.id.google_map_searchBox);
            searchBox.setOverflowMenu(R.menu.menu_gogole_map_search_overflow);
        }
        else {
            searchBox = (SearchBox) findViewById(R.id.amap_map_searchBox);
            searchBox.setOverflowMenu(R.menu.menu_amap_map_search_overflow);
        }

        searchBox.enableVoiceRecognition(this);
        for(int x = 0; x < 5; x++){
            SearchResult option = new SearchResult("Result " + Integer.toString(x),
                    ContextCompat.getDrawable(WholeMapScreen.this, R.drawable.ic_search_history));
            searchBox.addSearchable(option);
        }
        searchBox.setMenuListener(new SearchBox.MenuListener() {
            @Override
            public void onMenuClick() {
                //Hamburger has been clicked
                if (!drawer.isDrawerOpen(GravityCompat.START))
                    drawer.openDrawer(GravityCompat.START);
            }
        });
        searchBox.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
            }

            @Override
            public void onSearchTermChanged(String term) {
                //React to the search term changing
                //Called after it has updated results
            }

            @Override
            public void onSearch(String searchTerm) {
                Toast.makeText(WholeMapScreen.this, searchTerm + " Searched", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResultClick(SearchResult result) {
                //React to a result being clicked
            }

            @Override
            public void onSearchCleared() {
                //Called when the clear button is clicked
            }

        });

        searchBox.setOverflowMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    // Google Map
                    case R.id.google_map_type_normal:
                        navigationView.getMenu().getItem(0).setChecked(true);       // item 0: normal
                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        return true;
                    case R.id.google_map_type_terrain:
                        navigationView.getMenu().getItem(1).setChecked(true);       // item 1: terrain
                        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        return true;
                    case R.id.google_map_type_hybrid:
                        navigationView.getMenu().getItem(2).setChecked(true);       // item 2: hybrid
                        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        return true;
                    case R.id.google_map_type_satellite:
                        navigationView.getMenu().getItem(3).setChecked(true);       // item 3: satellite
                        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        return true;

                    // AMAP
                    case R.id.amap_my_position:
                        int coarseLocation = ContextCompat.checkSelfPermission(WholeMapScreen.this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
                        int fineLocation = ContextCompat.checkSelfPermission(WholeMapScreen.this, android.Manifest.permission.ACCESS_FINE_LOCATION);

                        if(coarseLocation == PackageManager.PERMISSION_DENIED){
                            ActivityCompat.requestPermissions(WholeMapScreen.this,
                                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                    COARSE_LOCATION_PERMISSION_REQUEST_CODE);
                        }

                        if(fineLocation == PackageManager.PERMISSION_DENIED){
                            ActivityCompat.requestPermissions(WholeMapScreen.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    FINE_LOCATION_PERMISSION_REQUEST_CODE);
                        }

                        Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if (myLocation != null){
                            aMap.animateCamera(com.amap.api.maps.CameraUpdateFactory.changeLatLng(
                                    new com.amap.api.maps.model.LatLng(myLocation.getLatitude(),
                                            myLocation.getLongitude())));
                        }
                        else {
                            Toast.makeText(WholeMapScreen.this, getString(R.string.GPS_unavail), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return false;
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap inputMap) {
        googleMap = inputMap;
        googleMap.setBuildingsEnabled(true);             // enable 3D building view
        googleMap.setMyLocationEnabled(true);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setIndoorLevelPickerEnabled(true);
        uiSettings.setCompassEnabled(true);

        //TODO: set OnCameraChangeListener


        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                // latLng is the position of the click
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);          // this needs a permission

                AlertDialog.Builder builder = new AlertDialog.Builder(WholeMapScreen.this);
                builder.setMessage(getResources().getString(R.string.dialog_new_geo))
                        .setPositiveButton(getResources().getString(R.string.dialog_confirm_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: record the location and create new Reminder
                                googleMap.addMarker(new MarkerOptions().position(latLng).title("My Marker").flat(false).draggable(true));
                                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));   // instead of moveCamera
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.dialog_neg_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }  // do nothing
                        })
                        .setIcon(ContextCompat.getDrawable(WholeMapScreen.this, R.drawable.ic_nav_geo));        // TODO: might want to change icon
                AlertDialog dialog = builder.create();
                dialog.getWindow().setDimAmount((float) .2);            // dim background by n * 100%
                // vibrate, TODO: check disable vibration, and permission for sdk 23
                vibrator.vibrate(20);
                dialog.show();
            }
        });


        //TODO: calculate screen height, change dip to pixels
        googleMap.setPadding(0, getResources().getDimensionPixelSize(R.dimen.compass_padding), 0, getNavigationBarHeight(this, Configuration.ORIENTATION_PORTRAIT));           // compass not to be hidden by search bar

        // Add a marker in Sydney and move the camera
        // TODO: temp
        LatLng random = new LatLng(-34, 151);
        googleMap.addMarker(new MarkerOptions().position(random).title("Random Marker"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(random));

        //TODO: can be used for edit screen
//        if(ContextCompat.checkSelfPermission(WholeMapScreen.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
//            == PackageManager.PERMISSION_GRANTED){
//            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
//                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(),
//                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude())));
//        }
    }

    private void setUpAmap() {
        aMap.setLocationSource(this);                   // TODO: implement methods
        aMap.setMyLocationEnabled(true);
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);          // ROTATE follows and rotates as user moves, FOLLOW only follows
        aMap.setTrafficEnabled(true);
        com.amap.api.maps.UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setScaleControlsEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setMyLocationButtonEnabled(false);

        aMap.setOnMapLongClickListener(new AMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final com.amap.api.maps.model.LatLng latLng) {
                // latLng is the position of the click
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);          // this needs a permission

                AlertDialog.Builder builder = new AlertDialog.Builder(WholeMapScreen.this);
                builder.setMessage(getResources().getString(R.string.dialog_new_geo))
                        .setPositiveButton(getResources().getString(R.string.dialog_confirm_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: record the location and create new Reminder
                                aMap.addMarker(new com.amap.api.maps.model.MarkerOptions().position(latLng).title("My Marker").setFlat(false).draggable(true));
                                aMap.animateCamera(com.amap.api.maps.CameraUpdateFactory.newLatLng(latLng));   // instead of moveCamera
                                aMap.animateCamera(com.amap.api.maps.CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latLng).zoom(15f).build()));     // zoom level: 200m
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.dialog_neg_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }  // do nothing
                        })
                        .setIcon(ContextCompat.getDrawable(WholeMapScreen.this, R.drawable.ic_nav_geo));        // TODO: might want to change icon
                AlertDialog dialog = builder.create();
                dialog.getWindow().setDimAmount((float) .2);            // dim background by n * 100%
                // vibrate, TODO: check disable vibration, and permission for sdk 23
                vibrator.vibrate(20);
                dialog.show();

                //TODO: add to draft list
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            searchBox.populateEditText(matches.get(0));
        }

        switch (requestCode){
            case SETTINGS_REQUEST_CODE:
                //TODO: loadPref();
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_google_map_type_normal:
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;

            case R.id.nav_google_map_type_terrain:
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;

            case R.id.nav_google_map_type_hybrid:
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            case R.id.nav_google_map_type_satellite:
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;

            case R.id.nav_amap_map_type_normal:
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                break;

            case R.id.nav_amap_map_type_night:
                aMap.setMapType(AMap.MAP_TYPE_NIGHT);
                break;

            case R.id.nav_amap_map_type_satellite:
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                break;

            case R.id.nav_amap_map_settings:
            case R.id.nav_google_map_settings:
                Intent toSettingScreen = new Intent(WholeMapScreen.this, SettingScreen.class);
                startActivityForResult(toSettingScreen, SETTINGS_REQUEST_CODE);
                break;

            case R.id.nav_amap_map_feedback:        // falls through
            case R.id.nav_google_map_feedback:
                String uriText = "mailto:peterwangtao0@hotmail.com"
                        + "?subject=" + Uri.encode(getString(R.string.feedback_subject))
                        + "&body=" + Uri.encode(getString(R.string.feedback_content_map));
                Uri uri = Uri.parse(uriText);
                Intent sendFeedbackEmail = new Intent(Intent.ACTION_SENDTO);                // this will only pop up the apps that can send e-mails
                sendFeedbackEmail.setData(uri);                                             // do not use setType, it messes things up
                try {
                    startActivity(Intent.createChooser(sendFeedbackEmail, getString(R.string.send_feedback)));
                }
                catch (ActivityNotFoundException e){
                    Toast centreToast =  Toast.makeText(WholeMapScreen.this, getResources().getString(R.string.activity_not_fonud), Toast.LENGTH_SHORT);
                    centreToast.setGravity(Gravity.CENTER, 0, 0);
                    centreToast.show();
                }
                break;

        }

        // close the drawer after clicking on an item
        if(useGoogleMap){
            drawer = (DrawerLayout) findViewById(R.id.google_map_drawer_layout);
        }
        else {
            drawer = (DrawerLayout) findViewById(R.id.amap_map_drawer_layout);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case COARSE_LOCATION_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // all good, do nothing
                }
                else {
                    Toast.makeText(WholeMapScreen.this, getString(R.string.GPS_unavail), Toast.LENGTH_SHORT).show();
                }
                break;

            case FINE_LOCATION_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // all good, do nothing
                }
                else {
                    Toast.makeText(WholeMapScreen.this, getString(R.string.GPS_unavail), Toast.LENGTH_SHORT).show();
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * All for Amaps
     *
     */

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public void onLocationChanged(Location location) {

    }


    private int getNavigationBarHeight(Context context, int orientation) {
        Resources resources = context.getResources();

        int id = resources.getIdentifier(
                orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape",
                "dimen", "android");
        if (id > 0) {
            return resources.getDimensionPixelSize(id);
        }
        return 0;
    }

    @Override
    public void onBackPressed() {
        if(searchBox.isFocused()){
            searchBox.clearFocus();
        }
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    /**
     * Standard override for AMAP
     */

    @Override
    protected void onResume() {
        super.onResume();
        if(!useGoogleMap)
            mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!useGoogleMap)
            mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(!useGoogleMap)
            mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!useGoogleMap)
            mapView.onDestroy();
    }
}
