package com.peter.georeminder.utils;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.facebook.FacebookSdk;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;
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
        // Set up Parse Environment
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));

        // Set up Facebook Environment
        FacebookSdk.sdkInitialize(this);
        ParseFacebookUtils.initialize(this);

        // Set up Twitter Environment
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));
        ParseTwitterUtils.initialize(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));

        // Set up Crashlytics Environment
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
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
