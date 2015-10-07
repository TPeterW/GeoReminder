package com.peter.georeminder;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.peter.georeminder.utils.EditItemFragment;

/**
 * Created by Peter on 10/6/15.
 */
public class EditScreen extends AppCompatActivity {

    private Boolean withMap = true;
    private Bundle reminderSpecs;

    //TODO: Add a function that can add saved reminders from history/local storage


    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_edit_reminder);

        generateActionBar();

        getSpecifications();

        createFragment();
    }

    private void generateActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void createFragment() {
        EditItemFragment editItemFragment = new EditItemFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean(getResources().getString(R.string.bundle_with_map), withMap);
        Toast.makeText(EditScreen.this, "Here", Toast.LENGTH_SHORT).show();

        //TODO: Fragment doesn't load layout

        //TODO: and other specifications
        editItemFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction showEditLayout = fragmentManager.beginTransaction();
        showEditLayout.add(R.id.edit_container, editItemFragment);
        showEditLayout.addToBackStack(null);

        showEditLayout.commit();
    }

    private void getSpecifications() {
        //TODO:
        reminderSpecs = getIntent().getExtras();
        withMap = reminderSpecs.getBoolean(getResources().getString(R.string.bundle_with_map));
    }
}
