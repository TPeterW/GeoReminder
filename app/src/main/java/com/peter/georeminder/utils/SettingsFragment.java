package com.peter.georeminder.utils;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.peter.georeminder.R;

/**
 * Created by Peter on 10/9/15.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
