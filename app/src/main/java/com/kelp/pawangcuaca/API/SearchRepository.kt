package com.kelp.pawangcuaca.API

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.kelp.pawangcuaca.Domains.LocationKeyResponse
import com.kelp.pawangcuaca.Domains.WeatherResponse
import com.kelp.pawangcuaca.Model.WeatherData
import com.kelp.pawangcuaca.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class SearchRepository(
    private val weatherService: WeatherService,
    private val context: Context
) {
    suspend fun fetchLocationKey(latitude: Double, longitude: Double): Result<LocationKeyResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val coordinates = "$latitude,$longitude"
                val apiKey = context.getString(R.string.api_key)
                val response = weatherService.getLocationKey(coordinates, apiKey)
                Result.success(response)
            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                Result.failure(e)
            }
        }
    }

        suspend fun fetchWeatherData(locationKey: String): Result<WeatherData> {
            return withContext(Dispatchers.IO) {
                try {
                    val apiKey = context.getString(R.string.api_key)
                    val response = weatherService.getWeather(locationKey, apiKey, "id-ID", true)
                    Result.success(response)
                } catch (e: IOException) {
                    Log.e(TAG, "Network error: ${e.message}")
                    Result.failure(e)
                } catch (e: Exception) {
                    Log.e(TAG, "Error: ${e.message}")
                    Result.failure(e)
                }
            }
        }


    companion object {
        private const val TAG = "SearchRepository"
    }
}
