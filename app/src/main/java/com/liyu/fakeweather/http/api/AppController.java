package com.liyu.fakeweather.http.api;

import com.liyu.fakeweather.http.BaseAppResponse;
import com.liyu.fakeweather.http.BaseHeWeatherCityResponse;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by liyu on 2016/12/1.
 */

public interface AppController {

    @GET("https://raw.githubusercontent.com/li-yu/FakeWeather/master/api/weatherkey.json")
    Observable<BaseAppResponse<String>> getWeatherKey();

    @GET("https://raw.githubusercontent.com/li-yu/FakeWeather/master/api/heweather_city_list.json")
    Observable<BaseHeWeatherCityResponse> getHeWeatherCityList();
}
