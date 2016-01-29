package com.peter.georeminder;

import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.transition.Slide;
import android.view.KeyEvent;

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

        createFragment();
    }

    private void initData() {

    }

    private void initEvent() {
        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(GravityCompat.END)
                    .excludeTarget(android.R.id.statusBarBackground, true)
                    .excludeTarget(android.R.id.navigationBarBackground, true));
//            getWindow().setReturnTransition(new Slide(GravityCompat.END));
//            getWindow().setExitTransition(new Slide(GravityCompat.END));
        }

        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        getSpecs();
        //TODO:
    }

    private void createFragment() {
        EditItemFragment editItemFragment = new EditItemFragment();

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

    private void getSpecs() {
        //TODO:
        reminderSpecs = getIntent().getExtras();
        withMap = reminderSpecs.getBoolean(getResources().getString(R.string.bundle_with_map));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // TODO: if has content
                // save to cache

                //TODO: if doesn't have content
                scrollToFinishActivity();
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

    private void animateLatLngChange(double latitude, double longitude) {
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