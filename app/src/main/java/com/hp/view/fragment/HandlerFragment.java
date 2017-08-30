package com.hp.view.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by developer on 7/18/17.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public abstract class HandlerFragment extends LoggingFragment {

    private static HandlerThread backgroundThread;

    private Handler backgroundHandler;

    protected Handler getBackgroundHandler() {
        return backgroundHandler;
    }

    private void stopBackgroundThread() {
        if (backgroundThread == null) {
            Log.i(TAG, "stopBackgroundThread - already stopped");
            return;
        }
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
        } catch (InterruptedException e) {
            Log.i(TAG, "stopBackgroundThread - interrupted");
            e.printStackTrace();
        } finally {
            backgroundThread = null;
            Log.i(TAG, "stopBackgroundThread - stopped");
        }
    }

    private void startBackgroundThread() {
        if (backgroundThread == null) {
            backgroundThread = new HandlerThread("background");
            backgroundThread.start();
            Log.i(TAG, "startBackgroundThread - started");
        }
        else {
            Log.i(TAG, "startBackgroundThread - already started");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    @Override
    public void onPause() {
        stopBackgroundThread();
        backgroundHandler = null;
        super.onPause();
    }
}
