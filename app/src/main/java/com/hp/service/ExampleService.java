package com.hp.service;

import android.util.Log;

import com.hp.event.Emitter;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * Created by developer on 11/6/15.
 */
@Singleton
public class ExampleService extends Emitter {

    private final String TAG = getClass().getCanonicalName();

    private boolean isStarted = false;

    @Inject
    public ExampleService() {
        //...
    }

    public void start() {
        if (isStarted) return;
        try {
            //...
            isStarted = true;
            Log.i(TAG, "started");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (!isStarted) return;
        try {
            //...
            isStarted = false;
            Log.i(TAG, "stopped");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
