    package com.kelp.pawangcuaca.Activitis

    import android.Manifest
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.location.Address
    import android.location.Geocoder
    import android.os.Bundle
    import android.util.Log
    import android.widget.ImageView
    import android.widget.TextView
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.app.ActivityCompat
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.google.android.gms.location.FusedLocationProviderClient
    import com.google.android.gms.location.LocationServices
    import com.kelp.pawangcuaca.API.WeatherRepository
    import com.kelp.pawangcuaca.Adapters.HourlyAdapters
    import com.kelp.pawangcuaca.Domains.Hourly
    import com.kelp.pawangcuaca.R
    import java.text.SimpleDateFormat
    import java.util.Calendar
    import java.util.Locale
    import android.os.Handler
    import android.os.Looper


    class MainActivity : AppCompatActivity() {

        private lateinit var recyclerView: RecyclerView
        private lateinit var hourlyAdapter: HourlyAdapters
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private val permissionRequestCode = 100
        private lateinit var weatherRepository: WeatherRepository
        private val hourlyList = ArrayList<Hourly>()
        private lateinit var handler: Handler
        private lateinit var runnable: Runnable

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            initRecyclerView()
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            weatherRepository = WeatherRepository()

            checkLocationPermission()

            handler = Handler(Looper.getMainLooper())
            runnable = object : Runnable {
                override fun run() {
                    val calendar = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("EEEE, MMMM dd | HH:mm", Locale("id", "ID"))
                    val formattedTime = dateFormat.format(calendar.time)
                    findViewById<TextView>(R.id.textView2).text = formattedTime
                    handler.postDelayed(this, 1000)
                }
            }

            handler.post(runnable)

            findViewById<ImageView>(R.id.floatingImage).setOnClickListener {
                val intent = Intent(this, PrediksiActivity::class.java)
                startActivity(intent)
            }

            val navigasiImageView = findViewById<ImageView>(R.id.navigasi)

            navigasiImageView.setOnClickListener {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }

            val next7dayTextView: TextView = findViewById(R.id.nextBtn)

            next7dayTextView.setOnClickListener {
                val intent = Intent(this, FutureActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        private fun initRecyclerView() {
            recyclerView = findViewById(R.id.view1)
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            hourlyAdapter = HourlyAdapters(this, hourlyList)
            recyclerView.adapter = hourlyAdapter
        }

        private fun checkLocationPermission() {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionRequestCode
                )
            } else {
                getLocation()
            }
        }

        private fun fetchLocationKey(latitude: Double, longitude: Double) {
            val apiKey = getString(R.string.api_key)
            weatherRepository.fetchLocationKey(apiKey, latitude, longitude, object : WeatherRepository.LocationCallback {
                override fun onSuccess(locationKey: String) {
                    fetchWeather(locationKey)
                }

                override fun onFailure(error: String?) {
                    Log.e("LocationKey", "Failed to fetch location key")
                }
            })
        }

        private fun updateWeatherUI(currentWeather: Hourly) {
            findViewById<ImageView>(R.id.imageView).setImageResource(currentWeather.getPicPath())
            findViewById<TextView>(R.id.textView3).text = "${currentWeather.getTempCelsius()}°C"
            findViewById<TextView>(R.id.textView5).text = "${currentWeather.getUvIndex()}"
            findViewById<TextView>(R.id.textView11).text = "${currentWeather.getHumidity()}%"
            findViewById<TextView>(R.id.textView9).text = "${currentWeather.getWindSpeed()} km/h"
        }

        private fun fetchWeather(locationKey: String) {
            val apiKey = getString(R.string.api_key)
            weatherRepository.fetchWeather(apiKey, locationKey, object : WeatherRepository.WeatherCallback {
                override fun onSuccess(newData: List<Hourly>) {
                    runOnUiThread {
                        hourlyList.clear()
                        hourlyList.addAll(newData)
                        hourlyAdapter.notifyDataSetChanged()

                        val currentWeather = newData[0]
                        updateWeatherUI(currentWeather)
                    }
                }

                override fun onFailure(error: String?) {
                    Log.e("Weather", "Failed to fetch weather data")
                }
            })
        }

        private fun getLocation() {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        fetchLocationKey(latitude, longitude)

                        // Gunakan Geocoder untuk mendapatkan nama kota dari koordinat
                        val geocoder = Geocoder(this, Locale("id", "ID"))
                        val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1) as List<Address>
                        if (addresses.isNotEmpty()) {
                            val address: Address = addresses[0]
                            val city = address.locality ?: "Unknown location"

                            findViewById<TextView>(R.id.lokasi).text = city
                        }
                    }
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(runnable)
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == permissionRequestCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Log.e("Permission", "Location permission denied")
            }
        }
    }
