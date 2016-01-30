package com.peter.georeminder;

import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.transition.Slide;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.peter.georeminder.utils.EditItemFragment;
import com.peter.georeminder.utils.EditItemFragment.MapListener;
import com.peter.georeminder.utils.swipeback.SwipeBackLayout;
import com.peter.georeminder.utils.swipeback.app.SwipeBackActivity;

/**
 * Created by Peter on 10/8/15.
 * Screen that loads two different fragments depending on the type of reminder
 */
public class EditorScreen extends SwipeBackActivity implements MapListener, ColorChooserDialog.ColorCallback {

    private boolean withMap;
    private boolean newReminder;

    private double currentLatitude;
    private double currentLongitude;

    private EditItemFragment currentFragment;

    // result code
    public static final int SAVED_TO_DRAFT                  = 0x91;
    public static final int SAVED_AS_REMINDER               = 0x92;
    public static final int EDIT_CANCELLED                  = 0x93;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_screen);

        initData();

        initEvent();

        createFragment();
    }

    private void initData() {
        newReminder = false;
    }

    private void initEvent() {
        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(GravityCompat.END)
                    .excludeTarget(android.R.id.statusBarBackground, true)
                    .excludeTarget(android.R.id.navigationBarBackground, true));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        getSpecs();

        hideKeyboard();
    }

    private void createFragment() {
        currentFragment = new EditItemFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean(getString(R.string.bundle_with_map), withMap);
        bundle.putBoolean(getString(R.string.bundle_new_reminder), newReminder);

        //TODO: and other specifications

        currentFragment.setArguments(bundle);              // pass the withMap parameter to the fragment with a bundle
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction showEditLayout = fragmentManager.beginTransaction();
        showEditLayout.add(R.id.edit_screen_layout, currentFragment)
                .addToBackStack(null)
                .commit();
    }

    private void getSpecs() {
        //TODO:
        Bundle reminderSpecs = getIntent().getExtras();
        withMap = reminderSpecs.getBoolean(getResources().getString(R.string.bundle_with_map));
        newReminder = reminderSpecs.getBoolean(getString(R.string.bundle_new_reminder));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                saveToDraft();
                scrollToFinishActivity();
                break;

            case R.id.edit_action_save:
                currentFragment.saveReminder();
                setResult(SAVED_AS_REMINDER);
                scrollToFinishActivity();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_editor_screen, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                saveToDraft();
                scrollToFinishActivity();
        }

        return super.onKeyDown(keyCode, event);
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    private void saveToDraft() {
        // TODO:

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

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(selectedColor));
        if (getActionBar() != null)
            getActionBar().setBackgroundDrawable(new ColorDrawable(selectedColor));

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(selectedColor);
            getWindow().setNavigationBarColor(selectedColor);
        }

        // make changes to reminder object
        currentFragment.onColorChange(selectedColor);
    }
}