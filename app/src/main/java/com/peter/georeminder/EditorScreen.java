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
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.peter.georeminder.utils.EditItemFragment;
import com.peter.georeminder.utils.EditItemFragment.MapListener;

/**
 * Created by Peter on 10/8/15.
 * Screen that loads two different fragments depending on the type of reminder
 */
public class EditorScreen extends AppCompatActivity implements MapListener, ColorChooserDialog.ColorCallback {

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
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(Gravity.END)
                    .excludeTarget(android.R.id.statusBarBackground, true)
                    .excludeTarget(android.R.id.navigationBarBackground, true));

            // TODO: set exit transition
            getWindow().setReturnTransition(new Slide(Gravity.END)
                    .excludeTarget(android.R.id.statusBarBackground, true)
                    .excludeTarget(android.R.id.navigationBarBackground, true));

            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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
                onBackPressed();
                return true;

            case R.id.edit_action_save:
                currentFragment.saveReminder();
                setResult(SAVED_AS_REMINDER);
                onBackPressed();
                return true;
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
                onBackPressed();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        // clean the fragments at the same time
        FragmentManager fragmentManager = getSupportFragmentManager();
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        Log.d("EditorScreen", "BackStackEntryCount: " + backStackEntryCount);
        for (int i = 0; i < backStackEntryCount; i++) {
            fragmentManager.popBackStack();
        }

        super.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideKeyboard();
        return super.onTouchEvent(event);
    }

    private void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            // then you have a keyboard! for free!
        }
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
                setTitle(Math.round((float) latitudeAnimator.getAnimatedValue() * 100000.0) / 100000.0 + ", "
                        + Math.round((float) longitudeAnimator.getAnimatedValue() * 100000.0) / 100000.0);
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