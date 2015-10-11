package com.peter.georeminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.peter.georeminder.models.Reminder;
import com.peter.georeminder.utils.RecyclerAdapter;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.LinkedList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainScreen extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "CaEup4hD9PE80usRXTqez80Yo";
    private static final String TWITTER_SECRET = "kDEkAOOz2oFnvBn8aneY7YtJtaBP5npSNT4VtnKP826A3OMIRi";

    // ToolBar
    private FloatingActionButton seeMap;
    private Toolbar toolbar;

    // "Add" fab menu
    private com.github.clans.fab.FloatingActionMenu newReminder;
    private com.github.clans.fab.FloatingActionButton addGeoReminder;
    private com.github.clans.fab.FloatingActionButton addNorReminder;

    // Main content (RecyclerView)
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapter adapter;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main_screen);


        initView();

        initData();

        initEvent();

        loadPref();             //using SharedPreferences
    }

    private void initData() {
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

    private void loadPref() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //TODO:
    }

    private void initEvent() {
        firstBackPress = System.currentTimeMillis() - 2000;

        // use linear layout manager to set Recycler view
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerAdapter(MainScreen.this, reminderList);
        adapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // TODO: temporary test code, delete and change later
//                adapter.addReminder(position, new Reminder());
                Toast.makeText(MainScreen.this, position + "", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // TODO: temporary test code, delete and change later
                adapter.deleteReminder(position);
            }
        });
        recyclerView.setAdapter(adapter);

        // add dividers
        // Currently not needed

        // TODO: delete code below as they are temporary
        // will implement UltimateRecyclerView
    }

    private void initView() {
        // The main layout ------ RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recycler_layout);

        // this buttons takes user to a page
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

        // The two mini add buttons
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


        // Toolbar, preferably not make any changes to that
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
                long currentBackPress = System.currentTimeMillis();         // then user has to press one more time
                if((currentBackPress - firstBackPress) > 2000){
                    Snackbar snackbar = Snackbar.make(newReminder, getResources().getString(R.string.press_again_exit), Snackbar.LENGTH_SHORT)
                    .setAction("Action", null);             // TODO: make sure don't press again while fab is up
                    firstBackPress = currentBackPress;

                    snackbar.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                        @Override
                        public void onViewAttachedToWindow(View v) {
                            newReminder.animate().translationYBy(-136);
                        }

                        @Override
                        public void onViewDetachedFromWindow(View v) {
                            newReminder.animate().translationYBy(136);
                        }
                    });

                    snackbar.show();
                    return true;
                }
        }

        return super.onKeyDown(keyCode, event);
    }
}
