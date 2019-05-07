package com.liyu.fakeweather.http;

import com.liyu.fakeweather.http.api.AppController;
import com.liyu.fakeweather.http.api.WeatherController;

/**
 * Created by liyu on 2016/8/25.
 */
public class ApiFactory {
    protected static final Object monitor = new Object();

    private static WeatherController weatherController;
    private static AppController appController;

    public static WeatherController getWeatherController() {
        if (weatherController == null) {
            synchronized (monitor) {
                weatherController = RetrofitManager.getInstance().create(WeatherController.class);
            }
        }
        return weatherController;
    }

    public static AppController getAppController() {
        if (appController == null) {
            synchronized (monitor) {
                appController = RetrofitManager.getInstance().create(AppController.class);
            }
        }
        return appController;
    }

    public static void reset() {
        weatherController = null;
    }


}
