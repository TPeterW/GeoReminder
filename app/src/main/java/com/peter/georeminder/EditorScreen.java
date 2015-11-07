package com.peter.georeminder;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.LatLng;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.peter.georeminder.utils.EditItemFragment;
import com.peter.georeminder.utils.EditItemFragment.MapListener;
import com.peter.georeminder.utils.swipeback.SwipeBackLayout;
import com.peter.georeminder.utils.swipeback.app.SwipeBackActivity;

/**
 * Created by Peter on 10/8/15.
 * Screen that loads two different fragments depending on the type of reminder
 */
public class EditorScreen extends SwipeBackActivity implements MapListener{
    
    private Bundle reminderSpecs;
    private boolean withMap;

    private double currentLatitude;
    private double currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_editor);

        initData();

        initEvent();

        createFragment(savedInstanceState);
    }

    private void initData() {

    }

    private void initEvent() {
        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(GravityCompat.END));
            getWindow().setReturnTransition(new Slide(GravityCompat.END));
            getWindow().setExitTransition(new Slide(GravityCompat.END));
        }

        SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        getSpecifications();
        //TODO:
    }

    private void createFragment(Bundle savedInstanceState) {
        EditItemFragment editItemFragment = new EditItemFragment(savedInstanceState);

        Bundle bundle = new Bundle();
        bundle.putBoolean(getString(R.string.bundle_with_map), withMap);

        //TODO: and other specifications
        editItemFragment.setArguments(bundle);              // pass the withMap parameter to the fragment with a bundle
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction showEditLayout = fragmentManager.beginTransaction();
        showEditLayout.add(R.id.edit_screen_layout, editItemFragment)
                .addToBackStack(null)
                .commit();
    }

    private void getSpecifications() {
        //TODO:
        reminderSpecs = getIntent().getExtras();
        withMap = reminderSpecs.getBoolean(getResources().getString(R.string.bundle_with_map));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                //TODO: if doesn't have content
                finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onMapClick(double latitude, double longitude) {
        animateLatLngChange(latitude, longitude);

        currentLatitude = latitude;
        currentLongitude = longitude;
    }

    @Override
    public void onMapLongClick(double latitude, double longitude) {

    }

    @Override
    public void onMarkerDragStart() {
        setTitle(getString(R.string.marker_dragging));
    }

    @Override
    public void onMarkerDragEnd(double latitude, double longitude) {
        animateLatLngChange(latitude, longitude);

        currentLatitude = latitude;
        currentLongitude = longitude;
    }

    private void animateLatLngChange(double latitude, double longitude){
        final ValueAnimator latitudeAnimator = ValueAnimator.ofFloat((float) currentLatitude, (float) latitude).setDuration(600);
        final ValueAnimator longitudeAnimator = ValueAnimator.ofFloat((float) currentLongitude, (float) longitude).setDuration(600);

        latitudeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setTitle(Math.round ((float) latitudeAnimator.getAnimatedValue() * 100000.0) / 100000.0 + ", "
                        + Math.round ((float) longitudeAnimator.getAnimatedValue() * 100000.0) / 100000.0);
            }
        });

        longitudeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setTitle(Math.round ((float) latitudeAnimator.getAnimatedValue() * 100000.0) / 100000.0 + ", "
                        + Math.round ((float) longitudeAnimator.getAnimatedValue() * 100000.0) / 100000.0);
            }
        });

        latitudeAnimator.start();
        longitudeAnimator.start();
    }
}