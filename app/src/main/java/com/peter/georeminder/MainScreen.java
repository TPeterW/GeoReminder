package com.peter.georeminder;

import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.parse.ParseObject;
import com.peter.georeminder.models.Location;
import com.peter.georeminder.models.Reminder;
import com.peter.georeminder.utils.viewpager.FragmentViewPagerAdapter;
import com.peter.georeminder.utils.viewpager.ListLocationFragment.ListLocationListener;
import com.peter.georeminder.utils.viewpager.ListReminderFragment.ListReminderListener;

import java.util.LinkedList;
import java.util.List;

public class MainScreen extends AppCompatActivity implements
        ListReminderListener, ListLocationListener, OnSharedPreferenceChangeListener{
    //TODO: put Build.VERSION.SDK_INT into shared preference so that it wouldn't have to check every time

    // Analytics Tracker
    AnalyticsTrackers analyticsTrackers;

    // ToolBar
    private FloatingActionButton seeMap;
    private CoordinatorLayout coordinatorLayout;
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

    private static final int CREATE_NEW_GEO_REMINDER_REQUEST_CODE = 0x001;
    private static final int CREATE_NEW_NOR_REMINDER_REQUEST_CODE = 0x002;
    private static final int EDIT_EXISTING_REMINDER_REQUEST_CODE = 0x003;
    private static final int SETTINGS_REQUEST_CODE = 0x004;
    private static final int LOGIN_REQUEST_CODE = 0x005;

    // Importante
    // DataList
    private List<Reminder> reminderList;
    private List<Location> locationList;

    // For custom Nav Drawer
    private AccountHeader drawerHeader = null;
    private Drawer drawer = null;
    private IProfile userProfile;
    // Identifiers
    private static final int LOCAL_USER_IDENTIFIER =    101;
    private static final int ONLINE_USER_IDENTIFIER =   102;
    private static final int ALL_IDENTIFIER =           11;
    private static final int GEO_IDENTIFIER =           12;
    private static final int NOR_IDENTIFIER =           13;
    private static final int DRAFT_IDENTIFIER =         14;
    private static final int VIEW_MAP_IDENTIFIER =      15;
    private static final int ABOUT_IDENTIFIER =         21;
    private static final int SUPPORT_IDENTIFIER =       22;
    private static final int FEEDBACK_IDENTIFIER =      51;
    private static final int SETTINGS_IDENTIFIER =      52;

    // Record the last time "Back" key was pressed, to implement "double-click-exit"
    private long firstBackPress;

    private static boolean isDark = false;          // the colour of the StatusBar

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static int reminderListOffset = 0;      // to get back to same place when coming back from the right panel (location list)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // TODO: check if the intro page has been shown before
        // implement in the method, not here
        showIntro(true);

        initData();             // load from sharedPreferences list of reminders

        initView(savedInstanceState);       // Bundle for creating drawer header

        initEvent();

        checkServices();

        loadPref();             //using SharedPreferences

        Log.i("MainScreen", "Create");  //TODO: delete
    }

    private void showIntro(boolean toShow) {
        //TODO: this is temporary
        if(toShow){
            Intent toIntroScreen = new Intent(MainScreen.this, IntroScreen.class);
            startActivity(toIntroScreen);
        }
    }

    private void initData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        // TODO: get data from shared preferences

        // Trackers
        analyticsTrackers = AnalyticsTrackers.getInstance();

        reminderList = new LinkedList<>();
        // TODO: remove these and actually get the reminders from local data storage
        reminderList.add(new Reminder(this).setTitle("Reminder 1"));
        reminderList.add(new Reminder(this).setTitle("Reminder 2"));
        reminderList.add(new Reminder(this).setTitle("Reminder 3"));
        reminderList.add(new Reminder(this).setTitle("Reminder 4"));
        reminderList.add(new Reminder(this).setTitle("Reminder 5"));
        reminderList.add(new Reminder(this).setTitle("Reminder 6"));
        reminderList.add(new Reminder(this).setTitle("Reminder 7"));
        reminderList.add(new Reminder(this).setTitle("Reminder 8"));


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
        //TODO: if user is registered and logged, skip this step and go ahead to load the profile as the user profile
        userProfile = new ProfileDrawerItem()
                .withName(getResources().getString(R.string.nav_head_appname))
                .withEmail(getResources().getString(R.string.nav_local_email))
                .withIcon(ContextCompat.getDrawable(MainScreen.this, R.mipmap.ic_default_avatar))
                .withIdentifier(LOCAL_USER_IDENTIFIER);
    }

    private void initView(Bundle savedInstanceState) {
        viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(getSupportFragmentManager(), reminderList, locationList);
        viewPager.setAdapter(adapter);

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

        // The main layout
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coor_layout);


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
                .withStatusBarColor(ContextCompat.getColor(MainScreen.this, R.color.colorPrimary))
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(ALL_IDENTIFIER).withName(getString(R.string.nav_opt_all)).withIcon(R.drawable.ic_nav_all),
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
                        switch (drawerItem.getIdentifier()) {
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
                toEditScreen.putExtra(getString(R.string.bundle_with_map), false);
                //TODO: add specifications about the reminder to be created

                // activity transition animation
                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setExitTransition(new Fade());
                    getWindow().setReenterTransition(new Fade());
                    startActivityForResult(toEditScreen, CREATE_NEW_NOR_REMINDER_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
                } else
                    startActivityForResult(toEditScreen, CREATE_NEW_NOR_REMINDER_REQUEST_CODE);
            }
        });
        // TODO: check permission here
        addGeoReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newReminder.close(true);
                Intent toEditScreen = new Intent(MainScreen.this, EditorScreen.class);
                toEditScreen.putExtra(getString(R.string.bundle_with_map), true);
                //TODO: add specifications about the reminder to be created

                // activity transition animation
                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setExitTransition(new Fade());
                    getWindow().setReenterTransition(new Fade());
                    startActivityForResult(toEditScreen, CREATE_NEW_GEO_REMINDER_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
                } else
                    startActivityForResult(toEditScreen, CREATE_NEW_GEO_REMINDER_REQUEST_CODE);
            }
        });
    }

    private void loadPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        useAnimation = sharedPreferences.getBoolean("showAnim", true);
        // TODO: apply fancy animations here
    }

    private void checkServices() {
        // not sure which version of code is correct
//        switch (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainScreen.this)) {
//            case ConnectionResult.API_UNAVAILABLE:
//                break;
//        }
        //TODO: check if the user wants to use Amap first, before checking google play services

        //TODO: make sure Toast only appears once

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
        switch (requestCode) {
            // TODO: check if list is till empty, otherwise hide the new reminder button and text
            case CREATE_NEW_GEO_REMINDER_REQUEST_CODE:
//                Bundle resultFromCreating = data.getExtras();
                return;
            case CREATE_NEW_NOR_REMINDER_REQUEST_CODE:

                return;

            case LOGIN_REQUEST_CODE:
                loadPref();
                //TODO: change avatar and sync all reminders
                return;

            case SETTINGS_REQUEST_CODE:
                loadPref();
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                        new ProfileSettingDrawerItem().withName(getString(R.string.nav_acct_manage)).withDescription(getString(R.string.nav_desc_manage))
                                .withIcon(R.drawable.ic_nav_manage).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                Intent toLoginScreen = new Intent(MainScreen.this, LoginScreen.class);
                                if(Build.VERSION.SDK_INT >= 21){ getWindow().setExitTransition(null); }
                                startActivityForResult(toLoginScreen, LOGIN_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        drawer.closeDrawer();
                                    }
                                }, 200);        // wait for the activity to start then close the drawer
                                return false;
                            }
                        })
                )
                .withSavedInstance(savedInstanceState)
                .withCloseDrawerOnProfileListClick(false)
                .build();
    }

    private void toWholeMap(Boolean animateExit) {
        Intent toViewWholeMap = new Intent(MainScreen.this, WholeMapScreen.class);
        //TODO: to check all the reminders and drafts

        if (Build.VERSION.SDK_INT >= 21) {
            if(animateExit) {
                getWindow().setExitTransition(new Explode());
            } else {
                getWindow().setExitTransition(null);
            }
            getWindow().setReenterTransition(new Explode());
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.shared_pref_anim_pref_enabled), true)
                .apply();
        super.onDestroy();
    }
    @Override
    protected void onStart() {
        Log.i("MainScreen", "Start");
        super.onStart();
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
    }
}
