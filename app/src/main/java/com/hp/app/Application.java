package com.hp.app;

import android.net.wifi.WifiManager;
import android.util.Log;

import com.hp.app.view.MainActivity;
import com.hp.app.view.fragment.CameraFragment;
import com.hp.app.view.fragment.WeatherFragment;
import com.hp.service.ExampleService;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;

public class Application extends android.app.Application {

    private final String TAG = getClass().getCanonicalName();

    private static Application application;

    private ApplicationComponent component;

    private Properties properties;

    // Services that need to be started & stopped
    @Inject
    ExampleService exampleService;

    @Singleton
    @Component(modules = Module.class)
    public interface ApplicationComponent {
        void inject(Application application);
        void inject(MainActivity mainActivity);
        void inject(WeatherFragment homeDashboardFragment);
        void inject(CameraFragment cameraFragment);
    }

    @Override public void onCreate() {
        super.onCreate();

        properties = loadProperties("application.properties");
        application = this;

        Module module = new Module(this);
        component = DaggerApplication_ApplicationComponent.builder()
                .module(module)
                .build();
        component().inject(this);
    }

    public ApplicationComponent component() {
        return component;
    }

    public static String getProperty(String name) {
        return application.properties.getProperty(name);
    }

    protected Properties loadProperties(String name) {
        Properties props = new Properties();
        try {
            props.load(getAssets().open(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    public static void startServices(MainActivity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //application.exampleService.start();
            }
        });
    }

    public static void stopServices(MainActivity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                application.exampleService.stop();
            }
        });
    }

    public static String getIpAddress() {
        WifiManager wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString = null;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }

        Log.i(application.TAG, "getIpAddress() --> " + ipAddressString);
        return ipAddressString;
    }

}
