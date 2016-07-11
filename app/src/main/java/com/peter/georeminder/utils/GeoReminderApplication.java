package com.peter.georeminder.utils;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.peter.georeminder.R;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Peter on 10/12/15.
 * Custom Application class
 */
public class GeoReminderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Set up Facebook Environment
        FacebookSdk.sdkInitialize(this);

        // Set up Twitter Environment
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));

        // Set up Crashlytics Environment
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    // for test environment only
    @Override
    public void onTerminate() {
        Log.i("Application", "Terminate");
        super.onTerminate();
    }
}
