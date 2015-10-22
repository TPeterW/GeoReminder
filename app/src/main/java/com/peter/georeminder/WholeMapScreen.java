package com.peter.georeminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.peter.georeminder.utils.MapFragment;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;

public class WholeMapScreen extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap reminderMap;

    private SearchBox searchBox;
    private android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whole_map_screen);

        initTransitions();

        initMap();

        initSearchBox();
    }

    private void initTransitions() {
        if(Build.VERSION.SDK_INT > 20) {
            getWindow().setEnterTransition(new Slide(Gravity.RIGHT));
            getWindow().setReturnTransition(new Slide(Gravity.LEFT));
        }
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
                    ContextCompat.getDrawable(WholeMapScreen.this, R.mipmap.ic_launcher)); //TODO: change to ic_history
            searchBox.addSearchable(option);
        }
        searchBox.setMenuListener(new SearchBox.MenuListener(){

            @Override
            public void onMenuClick() {
                //Hamburger has been clicked
                Toast.makeText(WholeMapScreen.this, "Menu click", Toast.LENGTH_LONG).show();
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
        searchBox.setOverflowMenu(R.menu.overflow_menu);
        searchBox.setOverflowMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.test_menu_item:
                        Toast.makeText(WholeMapScreen.this, "Clicked!", Toast.LENGTH_SHORT).show();
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
    public void onBackPressed() {
        if(searchBox.isFocused()){
            searchBox.clearFocus();
        }
        super.onBackPressed();
    }
}
