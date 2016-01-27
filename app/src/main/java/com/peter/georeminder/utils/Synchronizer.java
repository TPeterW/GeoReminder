package com.peter.georeminder.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 1/26/16.
 *
 * Takes care of synchronising data
 *
 */
public class Synchronizer {

    private List<SynchronizerListener> callbacks = new ArrayList<>();

    private static Synchronizer instance = new Synchronizer();

    public static Synchronizer getInstance() {
        return instance;
    }

    public void registerListener(Context context) {
        if (!callbacks.contains(context)) {
            Log.i("Synchronizer", "Activity already registered");
            callbacks.add((SynchronizerListener) context);
        }
    }




    public interface SynchronizerListener {

        void onGetObjectComplete();

        void onGetObjectLocalComplete();

        void onGetListObjectsComplete();

        void onGetListObjectsLocalComplete();

    }

}
