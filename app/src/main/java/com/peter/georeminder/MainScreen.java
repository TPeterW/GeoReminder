package com.peter.georeminder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainScreen extends AppCompatActivity {

    private FloatingActionButton newReminder;

    private Boolean newReminderWithMap;

    private static final int CREATE_NEW_REMINDER_REQUEST_CODE = 0x001;
    private static final int EDIT_EXISTING_REMINDER_REQUEST_CODE = 0x002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);


        initView();

        initEvent();

    }

    private void initEvent() {
        //TODO: temporary, remember to ask for it, or set it to default
        newReminderWithMap = true;
    }

    private void initView() {
        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        newReminder = (FloatingActionButton) findViewById(R.id.fab);
        newReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toEditScreen = new Intent(MainScreen.this, EditScreen.class);
                toEditScreen.putExtra(getResources().getString(R.string.bundle_with_map), newReminderWithMap);
                //TODO: add specifications about the reminder to be created
                startActivityForResult(toEditScreen, CREATE_NEW_REMINDER_REQUEST_CODE);
            }
        });
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
                Snackbar.make(newReminder, "Settings", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CREATE_NEW_REMINDER_REQUEST_CODE:
                Bundle resultFromCreating = data.getExtras();
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
