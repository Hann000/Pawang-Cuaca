package com.kelp.pawangcuaca.Activitis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kelp.pawangcuaca.Adapters.FutureAdapters
import com.kelp.pawangcuaca.Domains.FutureDomain
import com.kelp.pawangcuaca.R
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class FutureActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageView
    private lateinit var temperatureText: TextView
    private lateinit var statusText: TextView
    private lateinit var iconImageView: ImageView
    private lateinit var WSpeedText: TextView
    private lateinit var HumText: TextView
    private lateinit var airQualityValue: TextView
    private lateinit var recyclerView: RecyclerView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var apiKey: String
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_future)

        // Inisialisasi views
        backBtn = findViewById(R.id.backBtn)
        temperatureText = findViewById(R.id.tv_temperature)
        statusText = findViewById(R.id.tv_description)
        iconImageView = findViewById(R.id.iv_weather_icon)
        WSpeedText = findViewById(R.id.WSpeedText)
        HumText = findViewById(R.id.HumText)
        airQualityValue = findViewById(R.id.airQualityValue)
        recyclerView = findViewById(R.id.recyclerViewWeather)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FutureAdapters(arrayListOf())

        // Tambahkan jarak antar item RecyclerView
        recyclerView.addItemDecoration(SpaceItemDecoration(dpToPx(16)))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Mengambil API Key dari string resources
        apiKey = getString(R.string.api_key)

        // Memeriksa izin lokasi
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        backBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Permission denied. Location data cannot be accessed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isApiKeyValid(): Boolean {
        return apiKey.isNotBlank()
    }

    private fun getCurrentLocation() {
        try {
            if (isApiKeyValid()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        getLocationKey(location.latitude, location.longitude)
                    } else {
                        Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Invalid API key", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getEncodedUrl(latitude: Double, longitude: Double): String {
        val encodedLatLon = URLEncoder.encode("$latitude,$longitude", "UTF-8")
        return "https://dataservice.accuweather.com/locations/v1/cities/geoposition/search?apikey=$apiKey&q=$encodedLatLon"
    }

    private fun getLocationKey(latitude: Double, longitude: Double) {
        val url = getEncodedUrl(latitude, longitude)

        makeApiRequest(url) { response ->
            val locationKey = JSONObject(response).optString("Key", "")
            if (locationKey.isNotEmpty()) {
                getWeatherData(locationKey)
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Failed to get location key", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getWeatherData(locationKey: String) {
        if (!isApiKeyValid()) {
            Toast.makeText(this, "API Key is not valid", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://dataservice.accuweather.com/forecasts/v1/daily/5day/$locationKey?apikey=$apiKey&language=en-us&metric=true&details=true"

        makeApiRequest(url) { response ->
            val dailyForecasts = JSONObject(response).optJSONArray("DailyForecasts")
            val futureData = ArrayList<FutureDomain>()

            if (dailyForecasts != null && dailyForecasts.length() > 0) {
                val todayForecast = dailyForecasts.optJSONObject(0)
                if (todayForecast != null) {
                    val temperature = todayForecast.optJSONObject("Temperature")
                    val minTemp = temperature?.optJSONObject("Minimum")?.optDouble("Value", 0.0)?.toInt() ?: 0
                    val maxTemp = temperature?.optJSONObject("Maximum")?.optDouble("Value", 0.0)?.toInt() ?: 0

                    val dayDetails = todayForecast.optJSONObject("Day")
                    val description = dayDetails?.optString("IconPhrase", "Unknown") ?: "Unknown"
                    val windSpeed = dayDetails?.optJSONObject("Wind")?.optJSONObject("Speed")?.optDouble("Value", 0.0)?.toInt() ?: 0
                    val humidityAverage = todayForecast.optJSONObject("Day")?.optJSONObject("RelativeHumidity")?.optInt("Average") ?: 0

                    val airAndPollen = todayForecast.optJSONArray("AirAndPollen")
                    var airQualityCategory = "Unknown"
                    if (airAndPollen != null) {
                        for (i in 0 until airAndPollen.length()) {
                            val item = airAndPollen.optJSONObject(i)
                            if (item?.optString("Name") == "AirQuality") {
                                airQualityCategory = translateAirQuality(item.optString("Category"))
                                break
                            }
                        }
                    }

                    val windUnit = "km/h"

                    // Update UI untuk data hari ini
                    runOnUiThread {
                        temperatureText.text = "$minTemp°C - $maxTemp°C"
                        statusText.text = translateStatusToIndonesian(description)
                        WSpeedText.text = "$windSpeed $windUnit"
                        HumText.text = "$humidityAverage%"
                        airQualityValue.text = airQualityCategory
                        val drawableId = getDrawableForWeather(description)
                        iconImageView.setImageResource(drawableId)
                    }
                }

                // Parsing data untuk hari berikutnya
                for (i in 1 until dailyForecasts.length()) {
                    val forecast = dailyForecasts.optJSONObject(i)
                    val temperature = forecast?.optJSONObject("Temperature")
                    val highTemp = temperature?.optJSONObject("Maximum")?.optDouble("Value", 0.0)?.toInt() ?: 0
                    val lowTemp = temperature?.optJSONObject("Minimum")?.optDouble("Value", 0.0)?.toInt() ?: 0
                    val status = forecast?.optJSONObject("Day")?.optString("IconPhrase", "Unknown") ?: "Unknown"
                    Log.d("WeatherData", "Received IconPhrase: $status")
                    val translatedStatus = translateStatusToIndonesian(status)
                    if (translatedStatus == "Cuaca Tidak Dikenali") {
                        Log.d("WeatherData", "Unrecognized IconPhrase: $status")
                    }

                    val windSpeed = forecast?.optJSONObject("Day")?.optJSONObject("Wind")?.optJSONObject("Speed")?.optDouble("Value", 0.0) ?: 0.0
                    val humidity = forecast?.optInt("RelativeHumidity", 0) ?: 0
                    val airQualityCategory = "Moderate"
                    val airQualityValue = 50

                    val dateStr = forecast?.optString("Date", null) ?: ""
                    val formattedDate = formatDate(dateStr)

                    futureData.add(FutureDomain(translatedStatus, formattedDate, "", highTemp, lowTemp, windSpeed, humidity, airQualityCategory, airQualityValue, formattedDate))
                }
            }

            runOnUiThread {
                if (futureData.isNotEmpty()) {
                    recyclerView.adapter = FutureAdapters(futureData)
                } else {
                    Toast.makeText(this, "No forecast data available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getDrawableForWeather(status: String): Int {
        return if (status.equals("cloudy", ignoreCase = true)) {
            R.drawable.cloudy
        } else if (status.equals("sunny", ignoreCase = true)) {
            R.drawable.sunny
        } else if (status.equals("clear", ignoreCase = true)) {
            R.drawable.sunny
        } else if (status.equals("rain", ignoreCase = true)) {
            R.drawable.rain
        } else if (status.equals("intermittent clouds", ignoreCase = true)) {
            R.drawable.cloudy_sunny
        } else if (status.equals("partly cloudy", ignoreCase = true)) {
            R.drawable.cloudy_sunny
        } else if (status.equals("thunderstorms", ignoreCase = true)) {
            R.drawable.storm
        } else if (status.equals("showers", ignoreCase = true)) {
            R.drawable.rainy
        } else if (status.equals("snow", ignoreCase = true)) {
            R.drawable.snowy
        } else if (status.equals("mostly cloudy", ignoreCase = true)) {
            R.drawable.cloudy_sunny
        } else {
            R.drawable.sunny
        }
    }

    private fun translateStatusToIndonesian(status: String): String {
        return when (status) {
            "Clear" -> "Cerah"
            "Partly cloudy" -> "Sebagian berawan"
            "Cloudy" -> "Berawan"
            "Rain" -> "Hujan"
            "Thunderstorms" -> "Badai Petir"
            "Snow" -> "Salju"
            "Fog" -> "Kabut"
            "Showers" -> "Hujan Ringan"
            "Overcast" -> "Mendung"
            "Drizzle" -> "Gerimis"
            "Mostly cloudy" -> "Sebagian Berawan"
            else -> "Cerah"
        }
    }

    private fun translateAirQuality(category: String): String {
        return when (category) {
            "Good" -> "Baik"
            "Moderate" -> "Sedang"
            "Unhealthy" -> "Tidak Sehat"
            "Very Unhealthy" -> "Sangat Tidak Sehat"
            "Hazardous" -> "Berbahaya"
            else -> "Tidak Diketahui"
        }
    }

    private fun formatDate(dateStr: String): String {
        try {
            if (dateStr.isNotEmpty()) {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val date = inputFormat.parse(dateStr)
                if (date != null) {
                    val outputFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                    return outputFormat.format(date)
                } else {
                    return "Invalid Date"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Unknown"
        }
        return "Unknown"
    }

    private fun makeApiRequest(url: String, callback: (String) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@FutureActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d("API Response", "Code: ${response.code}, Body: $responseBody")

                if (response.isSuccessful) {
                    callback(responseBody)
                } else {
                    runOnUiThread {
                        val errorMessage = when (response.code) {
                            401 -> "Unauthorized. Please check your API key."
                            403 -> "Access forbidden. Your API key may not have permission."
                            429 -> "Too many requests. Please try again later."
                            else -> "An error occurred: ${response.code}"
                        }
                        Toast.makeText(this@FutureActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = space
        }
    }
}
