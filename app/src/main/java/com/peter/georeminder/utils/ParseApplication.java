package com.peter.georeminder.utils;

import android.app.Application;

import com.parse.Parse;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Peter on 10/12/15.
 */
public class ParseApplication extends Application {
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "CaEup4hD9PE80usRXTqez80Yo";
    private static final String TWITTER_SECRET = "kDEkAOOz2oFnvBn8aneY7YtJtaBP5npSNT4VtnKP826A3OMIRi";

    @Override
    public void onCreate() {
        super.onCreate();
        // Set up Parse Environment TODO: add backup for reminders
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "j5LhuXf9kpeP81DZkf2MFFqhTGZijntJoT290hLY", "pcbALD79URPIFzwjZm4oitYsatmRsLljSFHpsbRq");

        // Set up Twitter Environment
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
    }
}
