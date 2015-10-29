package com.peter.georeminder.utils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.peter.georeminder.R;

/**
 * Created by Peter on 10/9/15.
 *
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        //TODO: get Google Service Availability from SharedPreference and maybe remove the preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean googleServicesAvailable = sharedPreferences.getBoolean(getString(R.string.shared_pref_google_avail), false);
        if(!googleServicesAvailable){
            ListPreference prefMapService = (ListPreference) findPreference("whichMap");
            prefMapService.setEnabled(false);
        }
    }
}
