package com.peter.georeminder;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
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

public class WholeMapScreen extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap reminderMap;

    private SearchBox searchBox;

    private static final int SETTINGS_REQUEST_CODE = 0x004;

    // Nav Drawer
    private DrawerLayout drawer;
    private NavigationView navigationView;

    //TODO: use amap if Google Play services aren't available

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whole_map_screen);

        initTransitions();

        initDrawer();

        initMap();

        initSearchBox();
    }

    private void initTransitions() {
        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(Gravity.END));
            getWindow().setReturnTransition(new Slide(Gravity.START));
        }
    }

    private void initDrawer() {
// Navigation Bar
        drawer = (DrawerLayout) findViewById(R.id.map_drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_map_view);
        navigationView.setNavigationItemSelectedListener(this);

        // the workaround for support:design:23.1.0
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_whole_map);

        //TODO: Init the views
        ImageView avatar = (ImageView) headerLayout.findViewById(R.id.nav_head_avatar);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: go to user account page
                Toast.makeText(WholeMapScreen.this, "Avatar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.whole_map);
        mapFragment.getMapAsync(this);
    }

    private void initSearchBox() {
        searchBox = (SearchBox) findViewById(R.id.map_searchBox);
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
        searchBox.setSearchListener(new SearchBox.SearchListener(){

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
                Toast.makeText(WholeMapScreen.this, searchTerm +" Searched", Toast.LENGTH_LONG).show();
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
        searchBox.setOverflowMenu(R.menu.menu_search_overflow);
        searchBox.setOverflowMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.map_type_normal:
                        navigationView.getMenu().getItem(0).setChecked(true);       // item 0: normal
                        reminderMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        return true;
                    case R.id.map_type_terrain:
                        navigationView.getMenu().getItem(1).setChecked(true);       // item 1: terrain
                        reminderMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        return true;
                    case R.id.map_type_hybrid:
                        navigationView.getMenu().getItem(2).setChecked(true);       // item 2: hybrid
                        reminderMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        return true;
                    case R.id.map_type_satellite:
                        navigationView.getMenu().getItem(3).setChecked(true);       // item 3: satellite
                        reminderMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        return true;
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
    public void onMapReady(GoogleMap googleMap) {
        reminderMap = googleMap;
        reminderMap.setBuildingsEnabled(true);             // enable 3D building view
        reminderMap.setMyLocationEnabled(true);
        UiSettings uiSettings = reminderMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setIndoorLevelPickerEnabled(true);
        uiSettings.setCompassEnabled(true);

        //TODO: set OnCameraChangeListener


        reminderMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
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
                                reminderMap.addMarker(new MarkerOptions().position(latLng).title("My Marker").flat(false));
                                reminderMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));   // instead of moveCamera
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.dialog_neg_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }  // do nothing
                        })
                        .setIcon(ContextCompat.getDrawable(WholeMapScreen.this, R.drawable.ic_nav_geo));        // TODO: might want to change icon
                AlertDialog dialog = builder.create();
                dialog.getWindow().setDimAmount((float).2);            // dim background by n * 100%
                // vibrate, TODO: check disable vibration
                vibrator.vibrate(20);
                dialog.show();
            }
        });





        //TODO: calculate screen height, change dip to pixels
        reminderMap.setPadding(0, getResources().getDimensionPixelSize(R.dimen.compass_padding), 0, 0);           // compass not to be hidden by search bar

        // Add a marker in Sydney and move the camera
        LatLng random = new LatLng(-34, 151);
        reminderMap.addMarker(new MarkerOptions().position(random).title("Random Marker"));
        reminderMap.moveCamera(CameraUpdateFactory.newLatLng(random));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            searchBox.populateEditText(matches.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_map_type_normal:
                reminderMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;

            case R.id.nav_map_type_terrain:
                reminderMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;

            case R.id.nav_map_type_hybrid:
                reminderMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            case R.id.nav_map_type_satellite:
                reminderMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;

            case R.id.nav_settings:
                Intent toSettingScreen = new Intent(WholeMapScreen.this, SettingScreen.class);
                startActivityForResult(toSettingScreen, SETTINGS_REQUEST_CODE);
                break;

            case R.id.nav_feedback:
                String uriText = "mailto:peterwangtao0@hotmail.com"
                        + "?subject=" + Uri.encode("Feedback on GeoReminder")
                        + "&body=" + Uri.encode("Hi Peter,\n\nI would like to say a few words about the map in GeoReminder: \n");
                Uri uri = Uri.parse(uriText);
                Intent sendFeedbackEmail = new Intent(Intent.ACTION_SENDTO);                // this will only pop up the apps that can send e-mails
                sendFeedbackEmail.setData(uri);                                             // do not use setType, it messes things up
                try {
                    startActivity(Intent.createChooser(sendFeedbackEmail, "Send Feedback..."));
                }
                catch (ActivityNotFoundException e){
                    Toast centreToast =  Toast.makeText(WholeMapScreen.this, getResources().getString(R.string.activity_not_fonud), Toast.LENGTH_SHORT);
                    centreToast.setGravity(Gravity.CENTER, 0, 0);
                    centreToast.show();
                }
                break;
        }

        // close the drawer after clicking on an item
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.map_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
}
