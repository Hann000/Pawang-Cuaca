package com.kelp.pawangcuaca.API;

import com.kelp.pawangcuaca.Domains.Hourly;
import com.kelp.pawangcuaca.R;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeatherRepository {
    private static final String BASE_URL = "https://dataservice.accuweather.com/";
    private static final String LOCATION_URL = "locations/v1/cities/geoposition/search";
    private static final String FORECAST_URL = "forecasts/v1/hourly/12hour/";
    private OkHttpClient client;

    public WeatherRepository() {
        client = new OkHttpClient();
    }

    // Fungsi untuk mengonversi waktu menjadi format 12 jam dengan AM/PM
    private String extractHourFromDateTime(String dateTime) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            Date date = format.parse(dateTime);
            SimpleDateFormat hourFormat = new SimpleDateFormat("hh:mm a");
            return hourFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Fungsi untuk menerjemahkan IconPhrase ke dalam bahasa Indonesia
    public String translateIconPhrase(String iconPhrase) {
        System.out.println("IconPhrase from API: " + iconPhrase);
        switch (iconPhrase.toLowerCase()) {
            case "cloudy":
                return "Berawan";
            case "thunderstorms":
                return "Badai Petir";
            case "intermittent clouds":
                return "Awan Berselang";
            case "clear":
                return "Cerah";
            case "mostly cloudy":
                return "Cerah Berawan";
            case "partly cloudy":
                return "Cerah Berawan";
            case "rain":
                return "Hujan";
            case "showers":
                return "Hujan Ringan";
            case "snow":
                return "Salju";
            default:
                return "Tidak Diketahui";
        }
    }

    // Fungsi untuk menentukan ikon cuaca berdasarkan IconPhrase
    private int getDrawableForWeather(String iconPhrase) {
        switch (iconPhrase.toLowerCase()) {
            case "berawan":
                return R.drawable.cloudy;
            case "cerah":
                return R.drawable.sunny;
            case "hujan":
                return R.drawable.rain;
            case "hujan ringan":
                return R.drawable.rainy;
            case "badai petir":
                return R.drawable.storm;
            case "awan berselang":
                return R.drawable.cloudy_sunny;
            case "salju":
                return R.drawable.snowy;
            default:
                return R.drawable.sunny;
        }
    }

    // Fungsi untuk mengambil Location Key berdasarkan latitude dan longitude
    public void fetchLocationKey(String apiKey, double latitude, double longitude, final LocationCallback callback) {
        String query = latitude + "," + longitude;
        String url = BASE_URL + LOCATION_URL + "?apikey=" + apiKey + "&q=" + query;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    callback.onFailure("Response body is null");
                    return;
                }

                String responseBody = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseBody);
                    if (jsonObject.has("Key")) {
                        String locationKey = jsonObject.getString("Key");
                        callback.onSuccess(locationKey);
                    } else {
                        callback.onFailure("Location Key not found");
                    }
                } catch (Exception e) {
                    callback.onFailure("Error parsing location key: " + e.getMessage());
                }
            }
        });
    }

    // Fungsi untuk mengambil data cuaca per jam
    public void fetchWeather(String apiKey, String locationKey, final WeatherCallback callback) {
        String url = BASE_URL + FORECAST_URL + locationKey + "?apikey=" + apiKey + "&details=true";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    callback.onFailure("Response body is null");
                    return;
                }

                String responseBody = response.body().string();
                try {
                    List<Hourly> hourlyList = new ArrayList<>();
                    JSONArray jsonArray = new JSONArray(responseBody);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        String dateTime = obj.optString("DateTime", "");
                        String hour = extractHourFromDateTime(dateTime);

                        double tempCelsius = -1.0;
                        if (obj.optJSONObject("Temperature") != null) {
                            JSONObject tempObj = obj.getJSONObject("Temperature");
                            double tempValue = tempObj.optDouble("Value", -1.0);
                            String unit = tempObj.optString("Unit", "C");

                            if (unit.equals("F")) {
                                tempCelsius = convertFahrenheitToCelsius(tempValue);
                            } else {
                                tempCelsius = tempValue;
                            }
                        }

                        String formattedTempCelsius = String.format("%.1f", tempCelsius);

                        double realFeelTempCelsius = -1.0;
                        if (obj.optJSONObject("RealFeelTemperature") != null) {
                            JSONObject realFeelObj = obj.getJSONObject("RealFeelTemperature");
                            double tempValue = realFeelObj.optDouble("Value", -1.0);
                            String unit = realFeelObj.optString("Unit", "C");

                            if (unit.equals("F")) {
                                realFeelTempCelsius = convertFahrenheitToCelsius(tempValue);
                            } else {
                                realFeelTempCelsius = tempValue;
                            }
                        }

                        String iconPhrase = obj.optString("IconPhrase", "Unknown");
                        System.out.println("Original IconPhrase: " + iconPhrase);
                        String translatedIconPhrase = translateIconPhrase(iconPhrase);
                        System.out.println("Translated IconPhrase: " + translatedIconPhrase);

                        int drawableIcon = getDrawableForWeather(translatedIconPhrase);

                        // Konversi Wind Speed
                        double windSpeed = 0.0;
                        if (obj.optJSONObject("Wind") != null && obj.getJSONObject("Wind").optJSONObject("Speed") != null) {
                            JSONObject windSpeedObj = obj.getJSONObject("Wind").getJSONObject("Speed");
                            double speedValue = windSpeedObj.optDouble("Value", 0.0);
                            String unit = windSpeedObj.optString("Unit", "mi/h");

                            if (unit.equals("mi/h")) {
                                windSpeed = convertMphToKph(speedValue);
                            } else {
                                windSpeed = speedValue;
                            }
                        }
                        String formattedWindSpeed = String.format("%.1f", windSpeed);
                        int humidity = obj.optInt("RelativeHumidity", 0);

                        // UV Index
                        double uvIndex = obj.optDouble("UVIndex", 0.0);

                        hourlyList.add(new Hourly(hour, formattedTempCelsius, drawableIcon, translatedIconPhrase, formattedWindSpeed, humidity, uvIndex));
                    }

                    callback.onSuccess(hourlyList);
                } catch (Exception e) {
                    callback.onFailure("Error parsing weather data: " + e.getMessage());
                }
            }
        });
    }

    // Fungsi untuk konversi Fahrenheit ke Celsius
    private double convertFahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5.0 / 9.0;
    }

    // Fungsi untuk konversi mil per jam (mi/h) ke kilometer per jam (km/h)
    private double convertMphToKph(double mph) {
        return mph * 1.60934;
    }

    public interface LocationCallback {
        void onSuccess(String locationKey);
        void onFailure(String error);
    }

    // Callback Interface untuk Weather
    public interface WeatherCallback {
        void onSuccess(List<Hourly> hourlyList);
        void onFailure(String error);
    }
}
