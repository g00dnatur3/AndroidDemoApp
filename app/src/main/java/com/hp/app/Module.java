package com.hp.app;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.WindowManager;

import com.hp.camera.CameraUtils;
import com.hp.service.WeatherService;
import com.loopj.android.http.AsyncHttpClient;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Provides;


/**
 * Created by developer on 11/9/15.
 */
@dagger.Module
public class Module {

    // if you need to log, uncomment...
    //protected final String TAG = getClass().getCanonicalName();

    private final Application application;

    public Module(Application application) {
        this.application = application;
    }

    @Provides AsyncHttpClient provideAsyncHttpClient() {
        return new AsyncHttpClient();
    }

    @Provides @Singleton WeatherService provideWeatherService(AsyncHttpClient httpClient) {
        return new WeatherService(httpClient);
    }

    @Provides @Singleton CameraManager provideLocationManager() {
        return (CameraManager) application.getSystemService(Context.CAMERA_SERVICE);
    }

    @Provides @Singleton @Named("camera.front.id")
    String provideFrontCameraId(CameraManager cameraManager) {
        return CameraUtils.getFrontCameraId(cameraManager);
    }

    @Provides @Singleton @Named("camera.front.outputSizes")
    List<Size> provideFrontCameraOutputSizes(CameraManager cameraManager, @Named("camera.front.id") String cameraId) {
        return CameraUtils.getOutputSizes(cameraManager, cameraId);
    }

    @Provides @Singleton @Named("camera.front.maxOutputSize")
    Size provideFrontCameraMaxOutputSize(@Named("camera.front.outputSizes") List<Size> outputSizes) {
        return CameraUtils.maxOutputSize(outputSizes);
    }

    @Provides @Singleton
    WindowManager provideWindowManager() {
        return (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
    }

    @Provides @Singleton
    DisplayMetrics provideDisplayMetrics(WindowManager windowManager) {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }
}
