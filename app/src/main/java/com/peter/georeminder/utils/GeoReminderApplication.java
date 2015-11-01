package com.peter.georeminder.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.peter.georeminder.AnalyticsTrackers;
import com.peter.georeminder.R;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Peter on 10/12/15.
 * Custom Application class
 */
public class GeoReminderApplication extends Application {
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "DMsWio2hohKMIz1dq065X82vQ";
    private static final String TWITTER_SECRET = "CfiiWkDfktGHVDeFfbMWqWC9daXISRJbDIpBlMkwb09M2uqkhS";

    @Override
    public void onCreate() {
        super.onCreate();
        // Set up Parse Environment TODO: add backup for reminders
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "j5LhuXf9kpeP81DZkf2MFFqhTGZijntJoT290hLY", "pcbALD79URPIFzwjZm4oitYsatmRsLljSFHpsbRq");

        // Set up Twitter Environment
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());

        // Set up Google Analytics
        AnalyticsTrackers.initialize(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        Log.i("Application", "Terminate");
        super.onTerminate();
    }
}
