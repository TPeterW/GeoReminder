package com.peter.georeminder.utils;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Peter on 10/12/15.
 */
public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Set up Parse Environment TODO: add backup for reminders
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "j5LhuXf9kpeP81DZkf2MFFqhTGZijntJoT290hLY", "pcbALD79URPIFzwjZm4oitYsatmRsLljSFHpsbRq");
    }
}
