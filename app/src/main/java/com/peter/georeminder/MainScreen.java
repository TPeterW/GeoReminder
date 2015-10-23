package com.peter.georeminder;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.parse.ParseObject;
import com.peter.georeminder.models.Reminder;
import com.peter.georeminder.utils.RecyclerAdapter;

import java.util.LinkedList;
import java.util.List;

public class MainScreen extends AppCompatActivity{

    // ToolBar
    private FloatingActionButton seeMap;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    // "Add" fab menu
    private com.github.clans.fab.FloatingActionMenu newReminder;
    private com.github.clans.fab.FloatingActionButton addGeoReminder;
    private com.github.clans.fab.FloatingActionButton addNorReminder;
    private int scrolledDistance = 0;               // for showing and hiding the fam
    private static final int SHOW_THRESHOLD = 20;
    private static final int HIDE_THRESHOLD = 50;

    // Main content (RecyclerView)
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Empty list
    private TextView textNoReminder;
    private Button borderlessNewReminder;

    private static final int CREATE_NEW_GEO_REMINDER_REQUEST_CODE = 0x001;
    private static final int CREATE_NEW_NOR_REMINDER_REQUEST_CODE = 0x002;
    private static final int EDIT_EXISTING_REMINDER_REQUEST_CODE = 0x003;
    private static final int SETTINGS_REQUEST_CODE = 0x004;

    // Importante
    // DataList
    private List<Reminder> reminderList;



    // Record the last time "Back" key was pressed, to implement "double-click-exit"
    private long firstBackPress;

    private static boolean isDark = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // TODO: delete when implementing actual back functions
//        sendParseTestObject();

        initData();             // load from sharedPreferences list of reminders

        initView();

        initEvent();

        loadPref();             //using SharedPreferences

        checkServices();
    }

    private void initData() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        // TODO: get data from shared preferences

        reminderList = new LinkedList<>();
        // TODO: remove these and actually get the reminders
        reminderList.add(new Reminder());
        reminderList.add(new Reminder());
        reminderList.add(new Reminder());
        reminderList.add(new Reminder());
        reminderList.add(new Reminder());
        reminderList.add(new Reminder());
        reminderList.add(new Reminder());
        reminderList.add(new Reminder());
    }

    private void initView() {
        // initialise StatusBar color
        if(Build.VERSION.SDK_INT >= 21)
            getWindow().setStatusBarColor(ContextCompat.getColor(MainScreen.this, R.color.colorPrimary));

        // The main layout ------ RecyclerView
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);        // color scheme
        recyclerView = (RecyclerView) findViewById(R.id.recycler_layout);


        // this buttons takes user to a page
        // the blue one with a map icon
        // and display a map image(or a GoogleMap object that shows all current reminders)
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

        // Empty list
        textNoReminder = (TextView) findViewById(R.id.text_no_reminder);
        borderlessNewReminder = (Button) findViewById(R.id.borderless_btn_new_reminder);

        // Toolbar, preferably not make any changes to that
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




        /* Below is initialisation of gadgets on the screen */

        @SuppressWarnings("all")
        // Changes the color of status bar, with animation (using ValueAnimator)
        // will only happen if higher than Lollipop
        final ValueAnimator statusBarAnimator = ValueAnimator.ofArgb
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

        // AppBar Layout, the top area
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {        // when collapsed, do not enbale
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                swipeRefreshLayout.setEnabled(verticalOffset == 0);
                // only version higher than 21 (Lollipop) will be getting this status bar animation
                if (Build.VERSION.SDK_INT >= 21) {
                    if (verticalOffset < -150) {
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
    }

    private void initEvent() {
        firstBackPress = System.currentTimeMillis() - 2000;             // in case some idiot just presses back button when they enters the app

        // use linear layout manager to set Recycler view
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerAdapter(MainScreen.this, reminderList);
        adapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {              // click event for each reminder item
            @Override
            public void onItemClick(View view, int position) {
                // TODO: temporary test code, delete and change later
//                adapter.addReminder(position, new Reminder());
//                Toast.makeText(MainScreen.this, position + "", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                // to alert the user about deleting by vibrating
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                // TODO: add code
                AlertDialog.Builder builder = new AlertDialog.Builder(MainScreen.this);
                builder.setTitle(getResources().getString(R.string.dialog_delete_title))
                        .setMessage(getResources().getString(R.string.dialog_delete_msg))
                        .setPositiveButton(getResources().getString(R.string.dialog_pos_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.deleteReminder(position);
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.dialog_neg_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // let's do nothing
                            }
                        })
                        .setIcon(ContextCompat.getDrawable(MainScreen.this, R.drawable.ic_dialog_warning));
                AlertDialog dialog = builder.create();
                // vibrate, TODO: check disable vibration
                vibrator.vibrate(20);
                dialog.show();

//                if (reminderList.size() == 0) {
//                    textNoReminder.setAlpha(1);
//                    borderlessNewReminder.setAlpha(1);
//                    borderlessNewReminder.setClickable(true);
//                }
            }
        });

        recyclerView.setAdapter(adapter);
        // set hide and show animation when user scrolls
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (scrolledDistance > HIDE_THRESHOLD && !newReminder.isMenuHidden()) {
                    newReminder.hideMenu(true);
                    scrolledDistance = 0;
                } else if (scrolledDistance < -SHOW_THRESHOLD && newReminder.isMenuHidden()) {
                    newReminder.showMenu(true);
                    scrolledDistance = 0;
                }

                if((!newReminder.isMenuHidden() && dy>0) || (newReminder.isMenuHidden() && dy<0)) {
                    scrolledDistance += dy;
                }
            }
        });


        // add dividers
        // Currently not needed


        // Set up Swipe to Refresh
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO: update from server and actually refresh

                swipeRefreshLayout.setRefreshing(true);
            }
        });


        // this buttons takes user to a page
        // the blue one with a map icon
        // and display a map image(or a GoogleMap object that shows all current reminders)
        seeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toViewWholeMap = new Intent(MainScreen.this, WholeMapScreen.class);
                //TODO:

                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setExitTransition(new Explode());
                    getWindow().setReenterTransition(new Explode());
                    startActivity(toViewWholeMap, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
                } else
                    startActivity(toViewWholeMap);
            }
        });

        // The two mini add buttons (in floating action menu)
        addNorReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newReminder.close(true);
                Intent toEditScreen = new Intent(MainScreen.this, EditorScreen.class);
                toEditScreen.putExtra(getResources().getString(R.string.bundle_with_map), false);
                //TODO: add specifications about the reminder to be created

                // activity transition animation
                if(Build.VERSION.SDK_INT >= 21){
                    getWindow().setExitTransition(new Fade());
                    getWindow().setReenterTransition(new Fade());
                    startActivityForResult(toEditScreen, CREATE_NEW_NOR_REMINDER_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
                }
                else
                    startActivityForResult(toEditScreen, CREATE_NEW_NOR_REMINDER_REQUEST_CODE);
            }
        });
        addGeoReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newReminder.close(true);
                Intent toEditScreen = new Intent(MainScreen.this, EditorScreen.class);
                toEditScreen.putExtra(getResources().getString(R.string.bundle_with_map), true);
                //TODO: add specifications about the reminder to be created

                // activity transition animation
                if(Build.VERSION.SDK_INT >= 21){
                    getWindow().setExitTransition(new Fade());
                    getWindow().setReenterTransition(new Fade());
                    startActivityForResult(toEditScreen, CREATE_NEW_GEO_REMINDER_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
                }
                else
                    startActivityForResult(toEditScreen, CREATE_NEW_GEO_REMINDER_REQUEST_CODE);
            }
        });

        // Empty list
        borderlessNewReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newReminder = new Intent(MainScreen.this, EditorScreen.class);       // default is a new GeoReminder
                newReminder.putExtra(getResources().getString(R.string.bundle_with_map), true);
                //TODO: more specifications

                if(Build.VERSION.SDK_INT >= 21){
                    getWindow().setExitTransition(new Fade());
                    getWindow().setReenterTransition(new Fade());
                    startActivityForResult(newReminder, CREATE_NEW_GEO_REMINDER_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(MainScreen.this).toBundle());
                }
                else
                    startActivityForResult(newReminder, CREATE_NEW_GEO_REMINDER_REQUEST_CODE);
            }
        });
        if(reminderList.size() != 0) {
            textNoReminder.setAlpha(0);
            borderlessNewReminder.setAlpha(0);
            borderlessNewReminder.setClickable(false);
        }
    }

    private void loadPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //TODO:
    }

    private void checkServices() {
        //TODO: check Google Service availability
        switch (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainScreen.this)){
            case ConnectionResult.API_UNAVAILABLE:
                break;
        }

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
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent toSettingScreen = new Intent(MainScreen.this, SettingScreen.class);
                startActivityForResult(toSettingScreen, SETTINGS_REQUEST_CODE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.nav_settings:
//                Intent toSettingScreen = new Intent(MainScreen.this, SettingScreen.class);
//                startActivityForResult(toSettingScreen, SETTINGS_REQUEST_CODE);
//                break;
//
//            case R.id.nav_feedback:
//                String uriText = "mailto:peterwangtao0@hotmail.com"
//                                + "?subject=" + Uri.encode("Feedback on GeoReminder")
//                                + "&body=" + Uri.encode("Hi Peter,\n\nI would like to say a few words about GeoReminder: \n");
//                Uri uri = Uri.parse(uriText);
//                Intent sendFeedbackEmail = new Intent(Intent.ACTION_SENDTO);                // this will only pop up the apps that can send e-mails
//                sendFeedbackEmail.setData(uri);                                             // do not use setType, it messes things up
//                try {
//                    startActivity(Intent.createChooser(sendFeedbackEmail, "Send Feedback..."));
//                }
//                catch (ActivityNotFoundException e){
//                    Snackbar.make(newReminder, getResources().getString(R.string.activity_not_fonud), Snackbar.LENGTH_SHORT)
//                            .setAction("Action", null)
//                            .show();
//                }
//                break;
//        }
//
//        // close the drawer after clicking on an item
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            // TODO: check if list is till empty, otherwise hide the new reminder button and text
            case CREATE_NEW_GEO_REMINDER_REQUEST_CODE:
//                Bundle resultFromCreating = data.getExtras();
                return;
            case CREATE_NEW_NOR_REMINDER_REQUEST_CODE:

                return;

            case SETTINGS_REQUEST_CODE:
                loadPref();
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:                                     // if two presses differ from each other in time for more than 2 seconds
                if(newReminder.isOpened()){
                    newReminder.close(true);
                }
//                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//                if (drawer.isDrawerOpen(GravityCompat.START)) {
//                    drawer.closeDrawer(GravityCompat.START);
//                    return true;
//                }
                else {
                    if(swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                        return true;
                    }
                    else {
                        long currentBackPress = System.currentTimeMillis();         // then user has to press one more time
                        if((currentBackPress - firstBackPress) > 2000){
                            Snackbar snackbar = Snackbar.make(newReminder, getResources().getString(R.string.press_again_exit), Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null);             // TODO: make sure don't press again while fab is up
                            firstBackPress = currentBackPress;

                            snackbar.show();
                            return true;
                        }
                    }
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    // Below: code for testing and debugging




















    private void sendParseTestObject() {
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("Name", "Tao Peter Wang");
        testObject.put("Location", "NULL");
        Log.i("Cloud", "Sent Parse TestObject");
        testObject.saveInBackground();
    }
}
