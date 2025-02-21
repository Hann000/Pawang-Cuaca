package com.kelp.pawangcuaca.API

import com.kelp.pawangcuaca.Domains.LocationKeyResponse
import com.kelp.pawangcuaca.Model.WeatherData
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherService {

    @Headers("Content-Type: application/json")
    @GET("locations/v1/cities/geoposition/search")
    suspend fun getLocationKey(
        @Query("q") coordinates: String,
        @Query("apikey") apiKey: String
    ): LocationKeyResponse

        @GET("currentconditions/v1/{locationKey}")
        suspend fun getWeather(
            @Path("locationKey") locationKey: String,
            @Query("apikey") apiKey: String,
            @Query("language") language: String,
            @Query("details") details: Boolean
        ): WeatherData
}
