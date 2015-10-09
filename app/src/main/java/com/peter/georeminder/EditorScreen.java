package com.peter.georeminder;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.peter.georeminder.utils.EditItemFragment;

/**
 * Created by Peter on 10/8/15.
 */
public class EditorScreen extends AppCompatActivity {

    private boolean withMap;
    private Bundle reminderSpecs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reminder);

        initEvent();

        createFragment();
    }

    private void initEvent() {
        getSpecifications();
        //TODO:
    }

    private void createFragment() {
        EditItemFragment editItemFragment = new EditItemFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean(getResources().getString(R.string.bundle_with_map), withMap);

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
//        Toast.makeText(EditorScreen.this, reminderSpecs.getBoolean(getResources().getString(R.string.bundle_with_map)) + "", Toast.LENGTH_SHORT).show();
        withMap = reminderSpecs.getBoolean(getResources().getString(R.string.bundle_with_map));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                //TODO: if doesn't have content
                finish();
        }

        return super.onKeyDown(keyCode, event);
    }
}