package com.peter.georeminder;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.Gson;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.peter.georeminder.models.Location;
import com.peter.georeminder.models.Reminder;
import com.peter.georeminder.utils.viewpager.FragmentViewPagerAdapter;
import com.peter.georeminder.utils.viewpager.ListLocationFragment;
import com.peter.georeminder.utils.viewpager.ListLocationFragment.ListLocationListener;
import com.peter.georeminder.utils.viewpager.ListReminderFragment;
import com.peter.georeminder.utils.viewpager.ListReminderFragment.ListReminderListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MainScreen extends AppCompatActivity implements
        ListReminderListener, ListLocationListener, OnSharedPreferenceChangeListener{
    //TODO: put Build.VERSION.SDK_INT into shared preference so that it wouldn't have to check every time

    // ToolBar
    private FloatingActionButton seeMap;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    // Status Bar Colour
    private ValueAnimator statusBarAnimator;

    // "Add" fab menu
    private com.github.clans.fab.FloatingActionMenu newReminder;
    private com.github.clans.fab.FloatingActionButton addGeoReminder;
    private com.github.clans.fab.FloatingActionButton addNorReminder;
    private int scrolledDistance = 0;               // for showing and hiding the fam
    private static final int SHOW_THRESHOLD = 20;
    private static final int HIDE_THRESHOLD = 50;

    // Preferences
    private boolean useAnimation;

    private static final int CREATE_NEW_GEO_REMINDER_REQUEST_CODE           = 0x001;
    private static final int CREATE_NEW_NOR_REMINDER_REQUEST_CODE           = 0x002;
    private static final int EDIT_EXISTING_REMINDER_REQUEST_CODE            = 0x003;
    private static final int SETTINGS_REQUEST_CODE                          = 0x004;
    private static final int LOGIN_REQUEST_CODE                             = 0x005;

    private static final int PERMISSION_ACCESS_FINE_LOCATION_REQUEST_CODE   = 0x051;

    // Importante
    // DataList
    private static List<Reminder> reminderList;
    private static List<Location> locationList;
    private Gson gson;

    // For custom Nav Drawer
    private AccountHeader drawerHeader = null;
    private Drawer drawer = null;
    private IProfile userProfile;
    // Identifiers
    private static final int LOCAL_USER_IDENTIFIER                          = 0x101;
    private static final int ONLINE_USER_IDENTIFIER                         = 0x102;
    private static final int ALL_IDENTIFIER                                 = 0x11;
    private static final int GEO_IDENTIFIER                                 = 0x12;
    private static final int NOR_IDENTIFIER                                 = 0x13;
    private static final int DRAFT_IDENTIFIER                               = 0x14;
    private static final int VIEW_MAP_IDENTIFIER                            = 0x15;
    private static final int ABOUT_IDENTIFIER                               = 0x21;
    private static final int SUPPORT_IDENTIFIER                             = 0x22;
    private static final int FEEDBACK_IDENTIFIER                            = 0x51;
    private static final int SETTINGS_IDENTIFIER                            = 0x52;

    // Record the last time "Back" key was pressed, to implement "double-click-exit"
    private long firstBackPress;

    private static boolean isDark = false;          // the colour of the StatusBar

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ListReminderFragment reminderFragment;
    private ListLocationFragment locationFragment;
    private ArrayList<String> reminderIdList;
    private static int reminderListOffset = 0;      // to get back to same place when coming back from the right panel (location list)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        showIntro();

        initData();             // load from sharedPreferences list of reminders

        initView(savedInstanceState);       // Bundle for creating drawer header

        initEvent();

        checkServices();

        loadPref();             //using SharedPreferences
    }

    private void showIntro() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainScreen.this);
        if (!sharedPreferences.getBoolean(getString(R.string.shared_pref_tutorial_shown), false)) {
            // first time launch
            Intent toIntroScreen = new Intent(MainScreen.this, IntroScreen.class);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.shared_pref_tutorial_shown), true)
                    .apply();

            startActivity(toIntroScreen);
        }
    }

    private void initData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        gson = new Gson();

        reminderList = new LinkedList<>();
        reminderIdList = new ArrayList<>();
        // TODO: get reminders from local storage
        String localReminders = sharedPreferences.getString(getString(R.string.shared_pref_local_reminders), null);
        if (localReminders != null){
            LinkedList local = gson.fromJson(localReminders, LinkedList.class);
            for (Object reminder : local) {
                reminderList.add((Reminder) reminder);
                reminderIdList.add(((Reminder) reminder).getUuid());
            }
        }

        locationList = new LinkedList<>();
        // TODO: remove these and actually get the reminders from local data storage
        locationList.add(new Location(this).setTitle("Location 1"));
        locationList.add(new Location(this).setTitle("Location 2"));
        locationList.add(new Location(this).setTitle("Location 3"));
        locationList.add(new Location(this).setTitle("Location 4"));
        locationList.add(new Location(this).setTitle("Location 5"));
        locationList.add(new Location(this).setTitle("Location 6"));
        locationList.add(new Location(this).setTitle("Location 7"));
        locationList.add(new Location(this).setTitle("Location 8"));

        //TODO: add list of reminders


        // Nav Drawer
        // create user profile
        //TODO: if user is registered and logged in, skip this step and go ahead to load the profile as the user profile
        if (ParseUser.getCurrentUser() == null) {
            userProfile = new ProfileDrawerItem()
                    .withName(getString(R.string.nav_head_appname))
                    .withEmail(getString(R.string.nav_local_email))
                    .withIcon(R.mipmap.ic_default_avatar)
                    .withIdentifier(LOCAL_USER_IDENTIFIER);
        } else {
            ParseUser currentUser = ParseUser.getCurrentUser();
            userProfile  = new ProfileDrawerItem()
                    .withName(currentUser.getUsername())
                    .withEmail(currentUser.getEmail())
                    .withIcon(R.mipmap.ic_default_avatar)
                    .withIdentifier(currentUser.getInt(getString(R.string.parse_user_identifier)));
        }
    }

    private void initView(Bundle savedInstanceState) {
        viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

//        reminderFragment = (ListReminderFragment) adapter.getItem(0);
//        locationFragment = (ListLocationFragment) adapter.getItem(1);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        // initialise StatusBar color
        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(MainScreen.this, R.color.colorPrimary));
            //TODO: decide whether to change the navigation bar color or not
//            getWindow().setNavigationBarColor(ContextCompat.getColor(MainScreen.this, R.color.colorPrimary));
        }

        // this buttons takes user to a page
        // the blue one with a map icon
        // and display all the reminders on a map
        seeMap = (FloatingActionButton) findViewById(R.id.fab_see_map);

        // The two mini add buttons (in floating action menu)
        addNorReminder = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_new_norreminder);
        addGeoReminder = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_new_georeminder);

        newReminder = (FloatingActionMenu) findViewById(R.id.fam_add_new);
        newReminder.hideMenuButton(false);
        new Handler().postDelayed(new Runnable() {                      // fam show and hide animation
            @Override
            public void run() {
                newReminder.showMenuButton(true);
                newReminder.setMenuButtonShowAnimation(AnimationUtils.loadAnimation(MainScreen.this, R.anim.jump_from_down));
                newReminder.setMenuButtonHideAnimation(AnimationUtils.loadAnimation(MainScreen.this, R.anim.jump_to_down));
            }
        }, 300);

        // Toolbar, preferably not make any changes to that
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




        /* Below is initialisation of gadgets on the screen */

        // Changes the color of status bar, with animation (using ValueAnimator)
        // will only happen if higher than Lollipop
        if(Build.VERSION.SDK_INT >= 21) {
            statusBarAnimator = ValueAnimator.ofArgb
                    (ContextCompat.getColor(MainScreen.this, R.color.colorPrimary),
                            ContextCompat.getColor(MainScreen.this, R.color.colorPrimaryDark));
            statusBarAnimator.setDuration(500);
            statusBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                // how it works is that every time it updates, it goes to change the color by a little bit
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        getWindow().setStatusBarColor((Integer) statusBarAnimator.getAnimatedValue());
                    }
                }
            });
        }

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // AppBar Layout, the top area
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {        // when collapsed, do not enbale
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                reminderListOffset = verticalOffset;

//                Log.i("Offset", verticalOffset + "");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.pref_refresh_enabled), verticalOffset == 0)
                        .apply();
//                swipeRefreshLayout.setEnabled(verticalOffset == 0);

                // only version higher than 21 (Lollipop) will be getting this status bar animation
                if (Build.VERSION.SDK_INT >= 21) {
                    if (verticalOffset < -150) {                // negative indicates it has been moved up
                        if (!isDark) {
                            statusBarAnimator.start();
                            isDark = true;
                        }
                    } else {
                        if (isDark) {
                            statusBarAnimator.reverse();
                            isDark = false;
                        }
                    }
                }
            }
        });

        // initialise drawer
        // create account header
        buildHeader(false, savedInstanceState);         // drawerHeader is built in this method

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(drawerHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(ALL_IDENTIFIER).withName(getString(R.string.nav_opt_all)).withIcon(R.drawable.ic_nav_all).withBadge(8 + "").withBadgeStyle(new BadgeStyle().withTextColor(ContextCompat.getColor(MainScreen.this, R.color.md_white_1000)).withColorRes(R.color.colorPrimary)),
                        new PrimaryDrawerItem().withIdentifier(GEO_IDENTIFIER).withName(getString(R.string.nav_opt_geo)).withIcon(R.drawable.ic_nav_geo),
                        new PrimaryDrawerItem().withIdentifier(NOR_IDENTIFIER).withName(getString(R.string.nav_opt_nor)).withIcon(R.drawable.ic_nav_nor),
                        new PrimaryDrawerItem().withIdentifier(DRAFT_IDENTIFIER).withName(getString(R.string.nav_opt_draft)).withIcon(R.drawable.ic_nav_draft),
                        new PrimaryDrawerItem().withIdentifier(VIEW_MAP_IDENTIFIER).withName(getString(R.string.nav_opt_view_in_map)).withIcon(R.drawable.ic_nav_view_map).withSelectable(false),

                        new SectionDrawerItem().withName(getString(R.string.nav_sec_other)).withTextColor(ContextCompat.getColor(MainScreen.this, R.color.colorAccent)),
                        new SecondaryDrawerItem().withIdentifier(ABOUT_IDENTIFIER).withName(getString(R.string.nav_opt_about)).withSelectable(false),
                        new SecondaryDrawerItem().withIdentifier(SUPPORT_IDENTIFIER).withName(getString(R.string.nav_opt_support)).withSelectable(false)
                )
                                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                    @Override
                                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                        switch ((int) drawerItem.getIdentifier()) {
                                            case ALL_IDENTIFIER:

                                                break;
                                            case GEO_IDENTIFIER:

                                                break;
                                            case NOR_IDENTIFIER:

                                                break;
                                            case DRAFT_IDENTIFIER:

                                                break;
                                            case VIEW_MAP_IDENTIFIER:
                                                toWholeMap(false);
                                                break;
                                            case ABOUT_IDENTIFIER:
                                                Intent toMyWebsite = new Intent(Intent.ACTION_VIEW);
                                                Uri homePageUri = Uri.parse("http://tpeterw.github.io");
                                                toMyWebsite.setData(homePageUri);
                                                startActivity(toMyWebsite);
                                                break;
                                            case SUPPORT_IDENTIFIER:
                                                Toast thank_msg = Toast.makeText(MainScreen.this, getString(R.string.support_thank_msg), Toast.LENGTH_LONG);
                                                thank_msg.setGravity(Gravity.CENTER, 0, 0);
                                                thank_msg.show();
                                                break;
                                            case FEEDBACK_IDENTIFIER:
                                                String uriText = "mailto:peterwangtao0@hotmail.com"
                                                        + "?subject=" + Uri.encode(getString(R.string.feedback_subject))
                                                        + "&body=" + Uri.encode(getString(R.string.feedback_content));
                                                Uri emailUri = Uri.parse(uriText);
                                                Intent sendFeedbackEmail = new Intent(Intent.ACTION_SENDTO);                // this will only pop up the apps that can send e-mails
                                                sendFeedbackEmail.setData(emailUri);                                             // do not use setType, it messes things up
                                                try {
                                                    startActivity(Intent.createChooser(sendFeedbackEmail, getString(R.string.send_feedback)));
                                                } catch (ActivityNotFoundException e) {
                                                    Snackbar.make(newReminder, getString(R.string.activity_not_fonud), Snackbar.LENGTH_SHORT)
                                                            .setAction("Action", null)
                                                            .show();
                                                }
                                                break;
                                            case SETTINGS_IDENTIFIER:
                                                Intent toSettingScreen = new Intent(MainScreen.this, SettingsScreen.class);
                                                startActivityForResult(toSettingScreen, SETTINGS_REQUEST_CODE);
                                                break;
                                        }

                                        drawer.closeDrawer();
                                        return true;
                                    }
                                })
                                .addStickyDrawerItems(
                                        new PrimaryDrawerItem().withName(getString(R.string.nav_feedback)).withIdentifier(FEEDBACK_IDENTIFIER).withIcon(R.drawable.ic_nav_feedback).withSelectable(false),
                                        new PrimaryDrawerItem().withName(getString(R.string.nav_setting)).withIdentifier(SETTINGS_IDENTIFIER).withIcon(R.drawable.ic_nav_setting).withSelectable(false)
                                )
                                .withSavedInstance(savedInstanceState)
                                .build();
        // set status bar color
        drawer.getDrawerLayout().setStatusBarBackgroundColor(ContextCompat.getColor(MainScreen.this, R.color.colorPrimary));
    }

    private void initEvent() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        firstBackPress = System.currentTimeMillis() - 2000;             // in case some idiot just presses back button when they enters the app

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // TODO: this might not work, check later, if not delete
                switch (position) {
                    case 0:
                        toolbar.setTitle(getString(R.string.app_name));
                        break;
                    case 1:
//                        appBarLayout.setExpanded(false, true);
                        toolbar.setTitle(getString(R.string.title_location));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // this buttons takes user to a page
        // the blue one with a map icon
        // and display all the reminders on a map
        seeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toWholeMap(true);
            }
        });

        // The two mini add buttons (in floating action menu)
        addNorReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newReminder.close(true);
                Intent toEditScreen = new Intent(MainScreen.this, EditorScreen.class);
                toEditScreen.putExtra(getString(R.string.bundle_with_map), false)
                        .putExtra(getString(R.string.bundle_new_reminder), true);
                //TODO: add specifications about the reminder to be created

                // activity transition animation
                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setExitTransition(new Explode()
                            .excludeTarget(android.R.id.navigationBarBackground, true)
                            .excludeTarget(android.R.id.statusBarBackground, true));
                    getWindow().setReenterTransition(new Explode()
                            .excludeTarget(android.R.id.navigationBarBackground, true)
                            .excludeTarget(android.R.id.statusBarBackground, true));
                    startActivityForResult(toEditScreen, CREATE_NEW_NOR_REMINDER_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
                } else
                    startActivityForResult(toEditScreen, CREATE_NEW_NOR_REMINDER_REQUEST_CODE);
            }
        });

        addGeoReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newReminder.close(true);

                if (Build.VERSION.SDK_INT >= 23)
                    if (ContextCompat.checkSelfPermission(MainScreen.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_DENIED) {
                        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_ACCESS_FINE_LOCATION_REQUEST_CODE);
                        return;
                    }

                Intent toEditScreen = new Intent(MainScreen.this, EditorScreen.class);
                toEditScreen.putExtra(getString(R.string.bundle_with_map), true)
                        .putExtra(getString(R.string.bundle_new_reminder), true);
                //TODO: add specifications about the reminder to be created

                // activity transition animation
                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setExitTransition(new Explode()
                            .excludeTarget(android.R.id.navigationBarBackground, true)
                            .excludeTarget(android.R.id.statusBarBackground, true));
                    getWindow().setReenterTransition(new Explode()
                            .excludeTarget(android.R.id.navigationBarBackground, true)
                            .excludeTarget(android.R.id.statusBarBackground, true));
                    startActivityForResult(toEditScreen, CREATE_NEW_GEO_REMINDER_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
                } else
                    startActivityForResult(toEditScreen, CREATE_NEW_GEO_REMINDER_REQUEST_CODE);
            }
        });

        Log.i("MainScreen", "Create");
    }

    private void loadPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        useAnimation = sharedPreferences.getBoolean(getString(R.string.shared_pref_show_animation), true);
        // TODO: apply fancy animations here
    }

    private void checkServices() {
        // TODO: remove
        if (Build.VERSION.SDK_INT >= 23) {
            Log.i("Network Permission", (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) + "");
            Log.i("Internet Permission", (checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) + "");
        }
        
        // not sure which version of code is correct
//        switch (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainScreen.this)) {
//            case ConnectionResult.API_UNAVAILABLE:
//                break;
//        }
        // TODO: check if the user wants to use Amap first, before checking google play services

        // TODO: make sure Toast only appears once

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext())) {
            case ConnectionResult.SUCCESS:
                editor.putBoolean(getString(R.string.shared_pref_google_avail), true);
                break;

            case ConnectionResult.API_UNAVAILABLE:
                editor.putBoolean(getString(R.string.shared_pref_google_avail), false);
                //TODO: only show this message once
                Toast.makeText(MainScreen.this, getString(R.string.svcs_unavail), Toast.LENGTH_SHORT).show();
                break;

            case ConnectionResult.SERVICE_DISABLED:
                editor.putBoolean(getString(R.string.shared_pref_google_avail), false);
                Toast.makeText(MainScreen.this, getString(R.string.svcs_disabled), Toast.LENGTH_SHORT).show();
                break;

            case ConnectionResult.SERVICE_MISSING:
                editor.putBoolean(getString(R.string.shared_pref_google_avail), false);
                Toast.makeText(MainScreen.this, getString(R.string.svcs_missing), Toast.LENGTH_SHORT).show();
                break;
            
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                editor.putBoolean(getString(R.string.shared_pref_google_avail), false);
                Toast.makeText(MainScreen.this, getString(R.string.svcs_req_update), Toast.LENGTH_SHORT).show();
                break;

            default:
                editor.putBoolean(getString(R.string.shared_pref_google_avail), false);
                Toast.makeText(MainScreen.this, getString(R.string.svcs_other), Toast.LENGTH_SHORT).show();
                break;
        }
        editor.apply();

        //TODO: check other availabilities such as Internet connection
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(MainScreen.this, new String[]{permission}, requestCode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (newReminder.isOpened())         // close fam if is open
            newReminder.close(true);

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent toSettingScreen = new Intent(MainScreen.this, SettingsScreen.class);
                startActivityForResult(toSettingScreen, SETTINGS_REQUEST_CODE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        switch (requestCode) {
            // TODO: check if list is till empty, otherwise hide the new reminder button and text
            case CREATE_NEW_GEO_REMINDER_REQUEST_CODE:
                Log.i("MainScreen", "From new geo");
//                Bundle resultFromCreating = data.getExtras();
                // TODO:
                if (resultCode == EditorScreen.SAVED_AS_REMINDER) {
                    Log.i("MainScreen", "Reminder saved");

                } else if (resultCode == EditorScreen.SAVED_TO_DRAFT) {

                } else if (resultCode == EditorScreen.EDIT_CANCELLED) {

                }

                break;
            case CREATE_NEW_NOR_REMINDER_REQUEST_CODE:
                Log.i("MainScreen", "From new nor");
                // TODO:
                if (resultCode == EditorScreen.SAVED_AS_REMINDER) {
                    Log.d("MainScreen", "ResultCode SAVED_AS_REMINDER");
                    String reminderInJSONString = sharedPreferences.getString(getString(R.string.bundle_most_recent_reminder), null);
                    if (reminderInJSONString != null) {
                        Reminder editedReminder = gson.fromJson(reminderInJSONString, Reminder.class);
                        if (reminderIdList.contains(editedReminder.getUuid())) {        // editted
                            int index = reminderIdList.indexOf(editedReminder.getUuid());
                            ListReminderFragment.replaceReminder(index, editedReminder);
                        } else {                    // new reminder
                            ListReminderFragment.addReminder(null, editedReminder);
                        }
                        Log.d("MainScreen", editedReminder.getUuid() + " " + editedReminder.getTitle() + " " + editedReminder.getDescription());
                    } else {
                        Toast.makeText(MainScreen.this, getString(R.string.cannot_save_reminder), Toast.LENGTH_SHORT).show();
                    }

                } else if (resultCode == EditorScreen.SAVED_TO_DRAFT) {
                    Log.d("MainScreen", "ResultCode SAVED_TO_DRAFT");
                } else if (resultCode == EditorScreen.EDIT_CANCELLED) {
                    Log.d("MainScreen", "ResultCode EDIT_CANCELLED");
                } else {
                    Log.d("MainScreen", "ResultCode UNKNOWN");
                }
                break;

            case LOGIN_REQUEST_CODE:
                loadPref();

                //TODO: change avatar and sync all reminders
                switch (resultCode) {
                    case LoginScreen.LOGIN_CANCELLED:

                        break;
                    case LoginScreen.LOGIN_SUCCESS:

                        break;
                }
                break;

            case SETTINGS_REQUEST_CODE:
                loadPref();
                return;
        }

        // for Facebook integration
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // granted do nothing
                } else {
                    Toast.makeText(MainScreen.this, getString(R.string.permission_need_fine_location), Toast.LENGTH_SHORT).show();
                }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(drawer.isDrawerOpen() || newReminder.isOpened()) {
                    drawer.closeDrawer();
                    newReminder.close(true);
                    return true;
                } else if (viewPager.getCurrentItem() == 1) {            // on the location page
                    viewPager.setCurrentItem(0, true);
                    return true;
                } else {
                    if(sharedPreferences.getBoolean(getString(R.string.pref_is_refreshing), false)){
                        appBarLayout.setExpanded(true, true);
                        setTitle(getString(R.string.app_name));     // change title back
                        toolbar.setTitle(getString(R.string.app_name));
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getString(R.string.pref_is_refreshing), false)        // SwipeRefresh is refreshing
                                .apply();
//                        editor.putBoolean(getString(R.string.pref_back_to_top), true)           // recycler view back to top
//                                .apply();

                        editor.putBoolean(getString(R.string.pref_app_bar_enabled), true);
                        return true;
                    } else {
                        // if two presses differ from each other in time for more than 2 seconds
                        long currentBackPress = System.currentTimeMillis();         // then user has to press one more time
                        if((currentBackPress - firstBackPress) > 2000) {
                            Snackbar snackbar = Snackbar.make(newReminder, getString(R.string.press_again_exit), Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null);
                            firstBackPress = currentBackPress;

                            snackbar.show();
                            return true;
                        }
                    }
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void buildHeader(boolean compact, Bundle savedInstanceState) {
        // Create the AccountHeader
        drawerHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.colorPrimary)
                .withCompactStyle(compact)
                .addProfiles(
                        userProfile,        // TODO: figure out the click event for profile image
                        //don't ask but google uses 14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
//                        new ProfileSettingDrawerItem().withName(getResources().getString(R.string.nav_acct_switch)).withDescription(getResources().getString(R.string.nav_desc_switch)).withIcon(R.drawable.ic_nav_add).withIdentifier(PROFILE_SETTING),
                        new ProfileSettingDrawerItem()
                                .withName(getString(R.string.nav_acct_manage))
                                .withDescription(getString(R.string.nav_desc_manage))
                                .withIcon(R.drawable.ic_nav_manage)
                                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                    @Override
                                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                        // TODO: change to jump to UserInfoPage
                                        if (ParseUser.getCurrentUser() != null) {
                                            // TODO: delete this toast and jump to user page
                                            Toast.makeText(MainScreen.this, "Already Logged In", Toast.LENGTH_SHORT).show();
                                            return false;
                                        } else {
                                            Intent toLoginScreen = new Intent(MainScreen.this, LoginScreen.class);
                                            if (Build.VERSION.SDK_INT >= 21) {
                                                getWindow().setExitTransition(null);
                                            }
                                            startActivityForResult(toLoginScreen, LOGIN_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
                                        }

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                drawer.closeDrawer();
                                            }
                                        }, 200);        // wait for the activity to start then close the drawer

                                        return false;
                                    }
                                }),
                        // TODO: remove this and create a new drawer
                        new ProfileSettingDrawerItem()
                                .withName(getString(R.string.nav_acct_logout))
                                .withDescription(getString(R.string.nav_desc_logout))
                                .withIcon(R.drawable.ic_nav_logout)
                                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                    @Override
                                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                        if (ParseUser.getCurrentUser() != null) {
                                            ParseUser.logOutInBackground();
//                                            drawerHeader.removeProfileByIdentifier(ParseUser.getCurrentUser().getInt(getString(R.string.parse_user_identifier)));
                                            drawerHeader.getActiveProfile().withEmail(getString(R.string.nav_local_email));
                                            drawerHeader.getActiveProfile().withName(getString(R.string.nav_head_appname));
                                            drawerHeader.getActiveProfile().withIcon(R.mipmap.ic_default_avatar);
                                            drawerHeader.getActiveProfile().withIdentifier(LOCAL_USER_IDENTIFIER);
                                        }
                                        return true;
                                    }
                                })
                )
                .withSavedInstance(savedInstanceState)
                .withCloseDrawerOnProfileListClick(false)
                .build();

        // TODO: build another drawerHeader for logged in situation
    }

    private void toWholeMap(Boolean animateExit) {
        if (Build.VERSION.SDK_INT >= 23)
            if (ContextCompat.checkSelfPermission(MainScreen.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_DENIED) {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_ACCESS_FINE_LOCATION_REQUEST_CODE);
                return;
            }

        Intent toViewWholeMap = new Intent(MainScreen.this, WholeMapScreen.class);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setExitTransition(new Explode()
                    .excludeTarget(android.R.id.navigationBarBackground, true));
            getWindow().setReenterTransition(new Explode()
                    .excludeTarget(android.R.id.navigationBarBackground, true));

            startActivity(toViewWholeMap, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
        } else
            startActivity(toViewWholeMap);
    }

    // listener methods
    // TODO: move to above
    @Override
    public void onReminderClicked(View view, int position) {
        if (newReminder.isOpened())
            newReminder.close(true);
        else {
            // TODO: do a check, which edit screen to go to
        }
    }

    @Override
    public void onReminderLongClicked(View view, final int position) {
        if (newReminder.isOpened())
            newReminder.close(true);
    }

    @Override
    public void onReminderListScrolled(RecyclerView recyclerView, int dx, int dy) {
        onScroll(dx, dy);
    }

    @Override
    public void onReminderListRefresh() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_is_refreshing), true)
                .apply();

        appBarLayout.setExpanded(false, true);

        editor.putBoolean(getString(R.string.pref_app_bar_enabled), false);     // disable AppBar
        //TODO: figure out how to disable app bar
    }

    @Override
    public void onLocationClicked(View view, int position) {
        if (newReminder.isOpened())
            newReminder.close(true);
        else {
            // TODO: what do we do when user clicks on a location
        }

//        Log.i("MainScreen", "onLocationClicked");
    }

    @Override
    public void onLocationLongClicked(View view, int position) {
        if (newReminder.isOpened())
            newReminder.close(true);

//        Log.i("MainScreen", "onLocationLongClicked");
    }

    @Override
    public void onLocationListScrolled(RecyclerView recyclerView, int dx, int dy) {
        onScroll(dx, dy);
    }

    @Override
    public void onLocationListRefresh() {
        // presumably nothing
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_app_bar_enabled))) {         // this one doesn't really do anything
            appBarLayout.setEnabled(sharedPreferences.getBoolean(getString(R.string.pref_app_bar_enabled), true));
        }
    }

    private void onScroll(int dx, int dy) {
        if (scrolledDistance > HIDE_THRESHOLD && !newReminder.isMenuHidden()) {
            newReminder.hideMenu(true);
            scrolledDistance = 0;               // if menu is hidden, reset the scrolledDistance
        } else if (scrolledDistance < -SHOW_THRESHOLD && newReminder.isMenuHidden()) {
            newReminder.showMenu(true);
            scrolledDistance = 0;               // ditto here
        }

        if ((!newReminder.isMenuHidden() && dy > 0) || (newReminder.isMenuHidden() && dy < 0)) {
            scrolledDistance += dy;
        }
    }

    public static List<Reminder> getReminderList() {
        return reminderList;
    }

    public static List<Location> getLocationList() {
        return locationList;
    }

    // Below: code for testing and debugging




















    private void sendParseTestObject() {
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("Name", "Tao Peter Wang");
        testObject.put("Location", "NULL");
        Log.i("Cloud", "Sent Parse TestObject");
        testObject.saveInBackground();
    }

    @Override
    protected void onStop() {
        Log.i("MainScreen", "Stop");
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        Log.i("MainScreen", "Destroy");
        super.onDestroy();
    }
    @Override
    protected void onStart() {
        Log.i("MainScreen", "Start");
        super.onStart();
    }
    @Override
    protected void onResume() {
        Log.i("MainScreen", "Resume");
        super.onResume();
        AppEventsLogger.activateApp(this, getString(R.string.facebook_app_id));
    }
    @Override
    protected void onRestart() {
        Log.i("MainScreen", "Restart");
        super.onRestart();
    }
    @Override
    protected void onPause() {
        Log.i("MainScreen", "Pause");
        super.onPause();
        AppEventsLogger.deactivateApp(this, getString(R.string.facebook_app_id));
    }
}
