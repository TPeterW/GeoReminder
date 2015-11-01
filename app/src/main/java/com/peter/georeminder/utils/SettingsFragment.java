package com.peter.georeminder.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
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


        //TODO: get Google Service Availability from SharedPreference and maybe remove the preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // check google map service availability
        boolean googleServicesAvailable = sharedPreferences.getBoolean(getString(R.string.shared_pref_google_avail), false);
        getPreferenceScreen().findPreference("whichMap").setEnabled(googleServicesAvailable);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // check animation button enabled (check if it's a re-launch already)
        boolean animPrefEnabled = sharedPreferences.getBoolean(getString(R.string.shared_pref_anim_pref_enabled), false);
        getPreferenceScreen().findPreference("showAnim").setEnabled(animPrefEnabled);

        Log.i("Fragment", "CreateView " + animPrefEnabled);

        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                switch (key) {
                    case "showAnim":
                        if(isAdded()){
                            getPreferenceScreen().findPreference(key).setEnabled(false);        // make sure user wouldn't be able to click on it next time
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.putBoolean(getString(R.string.shared_pref_anim_pref_enabled), false)
                                    .apply();

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            AlertDialog dialog = builder.setCancelable(false)
                                    .setMessage(getString(R.string.pref_anim_dialog_msg))
                                    .setPositiveButton(getString(R.string.dialog_confirm_btn), null)
                                    .create();

                            dialog.show();
                            break;
                        }
                }
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        Log.i("Fragment", "Attach");
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        Log.i("Fragment", "Detach");
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        Log.i("Fragment", "Destroy");
        super.onDestroy();
    }

    @Override
    public void onPause() {
        Log.i("Fragment", "Pause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i("Fragment", "Stop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.i("Fragment", "DestroyView");
        super.onDestroyView();
    }
}
