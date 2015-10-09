package com.peter.georeminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.github.clans.fab.FloatingActionMenu;

public class MainScreen extends AppCompatActivity {

    // ToolBar
    private FloatingActionButton seeMap;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;

    // "Add" fab menu
    private com.github.clans.fab.FloatingActionMenu newReminder;
    private com.github.clans.fab.FloatingActionButton addGeoReminder;
    private com.github.clans.fab.FloatingActionButton addNorReminder;

    private static final int CREATE_NEW_GEO_REMINDER_REQUEST_CODE = 0x001;
    private static final int CREATE_NEW_NOR_REMINDER_REQUEST_CODE = 0x002;
    private static final int EDIT_EXISTING_REMINDER_REQUEST_CODE = 0x003;
    private static final int SETTINGS_REQUEST_CODE = 0x004;

    SharedPreferences sharedPreferences;                // to load preferences set in Settings page/fragment

    // Record the last time "Back" key was pressed, to implement "double-click-exit"
    private long firstBackPress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);


        initView();

        initEvent();

        loadPref();             //using SharedPreferences
    }

    private void loadPref() {
        //TODO:
    }

    private void initEvent() {
        firstBackPress = System.currentTimeMillis() - 2000;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    }

    private void initView() {
        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toolbar layout
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        AppBarLayout.OnOffsetChangedListener toolbarListener = new AppBarLayout.OnOffsetChangedListener(){
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {            // same trigger that content scrims use
//                if(collapsingToolbar.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapsingToolbar)) {
//                    Toast.makeText(MainScreen.this, "Collapsed", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(MainScreen.this, "Shown", Toast.LENGTH_SHORT).show();
//                }
                //TODO: so far haven't found use to listen to the collapsing of the toolbar layout
            }
        };

        // this buttons takes user to another page(or just change the fragment on the main page, I haven't decided yet)
        // and display a map image(or a GoogleMap object that shows all current reminders)
        seeMap = (FloatingActionButton) findViewById(R.id.fab_see_map);
        seeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    Snackbar.make(newReminder, getResources().getString(R.string.press_again_exit), Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
                    firstBackPress = currentBackPress;
                    return true;
                }
        }

        return super.onKeyDown(keyCode, event);
    }
}
