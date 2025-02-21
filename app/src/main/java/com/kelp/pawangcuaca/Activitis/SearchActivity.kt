package com.kelp.pawangcuaca.Activitis

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kelp.pawangcuaca.API.SearchRepository
import com.kelp.pawangcuaca.API.WeatherService
import com.kelp.pawangcuaca.Adapters.CityAdapter
import com.kelp.pawangcuaca.Model.City
import com.kelp.pawangcuaca.Model.WeatherData
import com.kelp.pawangcuaca.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class SearchActivity : AppCompatActivity(), CityAdapter.OnAddCityClickListener {

    private lateinit var searchView: SearchView
    private lateinit var recyclerViewSuggestions: RecyclerView
    private lateinit var cityAdapter: CityAdapter
    private val cityList = mutableListOf<City>()
    private lateinit var searchRepository: SearchRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupDependencies()
        setupSearchView()
        loadSavedCities()

        cityAdapter.setOnAddCityClickListener(this)
    }

    private fun initViews() {
        searchView = findViewById(R.id.sbarTxt)
        recyclerViewSuggestions = findViewById(R.id.recyclerViewSuggestions)
        recyclerViewSuggestions.layoutManager = LinearLayoutManager(this)
        cityAdapter = CityAdapter(cityList, this)
        recyclerViewSuggestions.adapter = cityAdapter
    }

    private fun setupDependencies() {
        val weatherService = createWeatherService()
        searchRepository = SearchRepository(weatherService, this)
    }

    private fun createWeatherService(): WeatherService {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl("https://dataservice.accuweather.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.takeIf { it.isNotBlank() }?.let {
                    searchLocation(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.takeIf { it.length >= 3 }?.let { query ->
                    lifecycleScope.launch {
                        searchLocations(query)
                    }
                } ?: clearCitySuggestions()
                return true
            }
        })
    }

    private fun searchLocations(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@SearchActivity, Locale("id", "ID"))
                val addressList = geocoder.getFromLocationName(query, 5)
                if (addressList.isNullOrEmpty()) {
                    handleEmptyLocationResult()
                } else {
                    val suggestions = addressList.map { address ->
                        City(
                            address.locality ?: address.subAdminArea ?: address.adminArea ?: "Unknown",
                            "${address.latitude},${address.longitude}"
                        )
                    }
                    updateCitySuggestions(suggestions)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Geocoder error: ${e.message}")
                showErrorToast("Kesalahan saat mencari lokasi")
            }
        }
    }

    private suspend fun updateCitySuggestions(suggestions: List<City>) {
        withContext(Dispatchers.Main) {
            cityList.clear()
            cityList.addAll(suggestions)
            cityAdapter.notifyDataSetChanged()
        }
    }

    private fun searchLocation(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@SearchActivity, Locale("id", "ID"))
                val addressList = geocoder.getFromLocationName(query, 1)

                if (addressList.isNullOrEmpty()) {
                    showErrorToast("Lokasi tidak ditemukan", query)
                } else {
                    val address = addressList[0]
                    val latitude = address.latitude
                    val longitude = address.longitude
                    fetchLocationKey(latitude, longitude)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                showErrorToast("Kesalahan saat mencari lokasi")
            }
        }
    }

    private suspend fun fetchLocationKey(latitude: Double, longitude: Double) {
        val result = searchRepository.fetchLocationKey(latitude, longitude)
        result.onSuccess { response ->
            val city = City(response.localizedName, response.key)
            saveCity(city)
        }.onFailure { error ->
            showErrorToast("Gagal mendapatkan lokasi: ${error.message}")
        }
    }

    private suspend fun fetchWeatherData(locationKey: String) {
        val result = searchRepository.fetchWeatherData(locationKey)
        result.onSuccess { weatherData ->
            showWeatherPopup(weatherData)
        }.onFailure { error ->
            showErrorToast("Gagal mengambil data cuaca: ${error.message}")
        }
    }

    fun showCityDetails(city: City) {
        val dialogBuilder = android.app.AlertDialog.Builder(this)
        dialogBuilder.setTitle("Detail Kota")
            .setMessage("Nama Kota: ${city.name}\nLokasi: ${city.key}")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showWeatherPopup(weatherData: WeatherData?) {
        // Menentukan nilai default untuk cuaca jika data tidak tersedia
        val status = weatherData?.weatherText ?: "Data Tidak Ditemukan"
        val temperature = weatherData?.temperature?.metric?.value?.let {
            "$it ${weatherData.temperature.metric.unit}"
        } ?: "Data Tidak Ditemukan"
        val realFeel = weatherData?.realFeelTemperature?.metric?.value?.let {
            "$it ${weatherData.realFeelTemperature.metric.unit}"
        } ?: "Data Tidak Ditemukan"

        // Menampilkan dialog dengan informasi cuaca atau nilai default
        val dialogBuilder = android.app.AlertDialog.Builder(this)
        dialogBuilder.setTitle("Cuaca Saat Ini")
            .setMessage(
                """
            Status: $status
            Suhu: $temperature
            RealFeel: $realFeel
            """.trimIndent()
            )
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }


    private fun showErrorToast(message: String, query: String? = null) {
        lifecycleScope.launch(Dispatchers.Main) {
            val finalMessage = query?.let { "$message: $it" } ?: message
            Toast.makeText(this@SearchActivity, finalMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearCitySuggestions() {
        cityList.clear()
        cityAdapter.notifyDataSetChanged()
    }

    private fun handleEmptyLocationResult() {
        lifecycleScope.launch(Dispatchers.Main) {
            cityList.clear()
            cityAdapter.notifyDataSetChanged()
            showErrorToast("Lokasi tidak ditemukan")
        }
    }

    override fun onAddCityClick(city: City) {
        lifecycleScope.launch {
            fetchWeatherData(city.key)
        }
    }

    private fun saveCity(city: City) {
        val sharedPreferences = getSharedPreferences("SavedCities", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(city.name, city.key)
        editor.apply()
        Toast.makeText(this, "Kota berhasil disimpan", Toast.LENGTH_SHORT).show()
    }

    private fun loadSavedCities() {
        val sharedPreferences = getSharedPreferences("SavedCities", Context.MODE_PRIVATE)
        val savedCities = sharedPreferences.all.keys
        savedCities.forEach { cityName ->
            val locationKey = sharedPreferences.getString(cityName, null)
            if (locationKey != null) {
                cityList.add(City(cityName, locationKey))
            }
        }
        cityAdapter.notifyDataSetChanged()
    }

    companion object {
        private const val TAG = "SearchActivity"
    }
}
