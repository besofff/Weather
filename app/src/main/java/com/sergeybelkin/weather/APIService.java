package com.sergeybelkin.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.sergeybelkin.weather.model.Weather;

public interface APIService {
 
    @GET("weather?units=metric&lang=ru")
    Call<Weather> getCurrentWeather(@Query("lat") double latitude,
                                    @Query("lon") double longitude,
                                    @Query("appid") String apiKey);

    @GET("forecast?units=metric&lang=ru&cnt=9")
    Call<Weather> getForecastWeather(@Query("lat") double latitude,
                                     @Query("lon") double longitude,
                                     @Query("appid") String apiKey);
}
