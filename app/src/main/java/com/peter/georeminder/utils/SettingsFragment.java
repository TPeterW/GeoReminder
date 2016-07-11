package com.peter.georeminder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        // TODO: get Google Service Availability from SharedPreference and maybe disable the preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // check google map service availability
        boolean googleServicesAvailable = sharedPreferences.getBoolean(getString(R.string.shared_pref_google_avail), false);
        getPreferenceScreen().findPreference("whichMap").setEnabled(googleServicesAvailable);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        Log.i("SettingsFragment", "Attach");
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        Log.i("SettingsFragment", "Detach");
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        Log.i("SettingsFragment", "Destroy");
        super.onDestroy();
    }

    @Override
    public void onPause() {
        Log.i("SettingsFragment", "Pause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i("SettingsFragment", "Stop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.i("SettingsFragment", "DestroyView");
        super.onDestroyView();
    }
}
