package com.mosin.weatheronboard.interfaces;

import com.mosin.weatheronboard.model.WeatherRequest;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IOpenWeatherMap {

        //api.openweathermap.org/data/2.5/weather?q={city name}&appid={API key}
    @GET ("data/2.5/weather")
    Call<WeatherRequest> loadWeather(@Query("q") String cityName, @Query("units") String Metric, @Query("appid") String appId);
}
