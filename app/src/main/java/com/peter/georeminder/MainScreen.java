package com.peter.georeminder;

import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.parse.Parse;
import com.parse.ParseObject;
import com.peter.georeminder.models.Reminder;
import com.peter.georeminder.utils.RecyclerAdapter;
import com.quinny898.library.persistentsearch.SearchBox;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.LinkedList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    // ToolBar
    private FloatingActionButton seeMap;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    // "Add" fab menu
    private com.github.clans.fab.FloatingActionMenu newReminder;
    private com.github.clans.fab.FloatingActionButton addGeoReminder;
    private com.github.clans.fab.FloatingActionButton addNorReminder;

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

    SharedPreferences sharedPreferences;                // to load preferences set in Settings page/fragment

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

        // code below is to test the Parse functions TODO: delete when implementing actual back functions
//        ParseObject testObject = new ParseObject("TestObject");
//        testObject.put("Name", "Tao Peter Wang");
//        testObject.put("Location", "NULL");
//        Log.i("Cloud", "Sent Parse TestObject");
//        testObject.saveInBackground();

        initData();             // load from sharedPreferences list of reminders

        initView();

        initEvent();

        loadPref();             //using SharedPreferences
    }

    private void initData() {
        // initialise StatusBar color
        if(Build.VERSION.SDK_INT >= 21)
            getWindow().setStatusBarColor(ContextCompat.getColor(MainScreen.this, R.color.colorPrimary));

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        // get data from shared preferences

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
        // The main layout ------ RecyclerView
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);        // color scheme
        recyclerView = (RecyclerView) findViewById(R.id.recycler_layout);

//        persistentSearch = (SearchBox) findViewById(R.id.search);
//        persistentSearch.revealFromMenuItem(R.id.action_search, this);

        // this buttons takes user to a page
        // the blue one with a map icon
        // and display a map image(or a GoogleMap object that shows all current reminders)
        seeMap = (FloatingActionButton) findViewById(R.id.fab_see_map);
        seeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toViewWholeMap = new Intent(MainScreen.this, WholeMapScreen.class);
                startActivity(toViewWholeMap);
                //TODO:
            }
        });

        // The two mini add buttons (in floating action menu)
        addNorReminder = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_new_norreminder);
        addNorReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newReminder.close(true);
                Intent toEditScreen = new Intent(MainScreen.this, EditorScreen.class);
                toEditScreen.putExtra(getResources().getString(R.string.bundle_with_map), false);
                //TODO: add specifications about the reminder to be created

                startActivityForResult(toEditScreen, CREATE_NEW_NOR_REMINDER_REQUEST_CODE);
            }
        });
        addGeoReminder = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_new_georeminder);
        addGeoReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newReminder.close(true);
                Intent toEditScreen = new Intent(MainScreen.this, EditorScreen.class);
                toEditScreen.putExtra(getResources().getString(R.string.bundle_with_map), true);
                //TODO: add specifications about the reminder to be created

                startActivityForResult(toEditScreen, CREATE_NEW_GEO_REMINDER_REQUEST_CODE);
            }
        });
        newReminder = (FloatingActionMenu) findViewById(R.id.fam_add_new);

        // Empty list
        textNoReminder = (TextView) findViewById(R.id.text_no_reminder);
        borderlessNewReminder = (Button) findViewById(R.id.borderless_btn_new_reminder);


        // Toolbar, preferably not make any changes to that
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        @SuppressWarnings("all")
        // Changes the color of status bar, with animation (using ValueAnimator)
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
                if(Build.VERSION.SDK_INT >= 21){
                    if(verticalOffset < -150){
//                       getWindow().setStatusBarColor(ContextCompat.getColor(MainScreen.this, R.color.colorPrimaryDark));
                        if(!isDark) {
                            statusBarAnimator.start();
                            isDark = true;
                        }
                    }
                    else {
//                       getWindow().setStatusBarColor(ContextCompat.getColor(MainScreen.this, R.color.colorPrimary));
                        if(isDark) {
                            statusBarAnimator.reverse();
                            isDark = false;
                        }
                    }
                }
            }
        });


        // Navigation Bar
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initEvent() {
        firstBackPress = System.currentTimeMillis() - 2000;             // in case some idiot just presses back button when they enters the app

        // use linear layout manager to set Recycler view
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerAdapter(MainScreen.this, reminderList);
        adapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // TODO: temporary test code, delete and change later
//                adapter.addReminder(position, new Reminder());
//                Toast.makeText(MainScreen.this, position + "", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // TODO: temporary test code, delete and change later
                adapter.deleteReminder(position);
                if(reminderList.size() == 0){
                    textNoReminder.setAlpha(1);
                    borderlessNewReminder.setAlpha(1);
                    borderlessNewReminder.setClickable(true);
                }
            }
        });
        recyclerView.setAdapter(adapter);

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


        // TODO: delete code below as they are temporary
        // will implement UltimateRecyclerView



        // Empty list
        borderlessNewReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newReminder = new Intent(MainScreen.this, EditorScreen.class);       // default is a new GeoReminder
                newReminder.putExtra(getResources().getString(R.string.bundle_with_map), true);
                //TODO: more specifications
                startActivityForResult(newReminder, CREATE_NEW_GEO_REMINDER_REQUEST_CODE);
            }
        });
        if(reminderList.size() != 0){
            textNoReminder.setAlpha(0);
            borderlessNewReminder.setAlpha(0);
            borderlessNewReminder.setClickable(false);
        }
    }

    private void loadPref() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //TODO:
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                Intent toSettingScreen = new Intent(MainScreen.this, SettingScreen.class);
                startActivityForResult(toSettingScreen, SETTINGS_REQUEST_CODE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
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
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                }
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

//                            snackbar.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
//                                @Override
//                                public void onViewAttachedToWindow(View v) {
//                                    newReminder.animate().translationYBy(-136);
//                                }
//
//                                @Override
//                                public void onViewDetachedFromWindow(View v) {
//                                    newReminder.animate().translationYBy(136);
//                                }
//                            });

                            snackbar.show();
                            return true;
                        }
                    }
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){

        }

        // close the drawer after clicking on an item
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
