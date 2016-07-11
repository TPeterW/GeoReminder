package com.peter.georeminder.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Peter on 10/6/15.
 * TODO: Broadcast receiver maybe, this will be the last thing to implement
 */
public class BackgroundLocationService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
