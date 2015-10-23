package com.peter.georeminder;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;
import android.view.KeyEvent;

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
        setContentView(R.layout.activity_reminder_editor);

        initEvent();

        createFragment();
    }

    private void initEvent() {
        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
            getWindow().setReturnTransition(new Fade());
        }

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