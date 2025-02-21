package com.kelp.pawangcuaca.Domains;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FutureDomain {
    private String day;  // Nama hari prediksi
    private String picPath;  // Untuk menyimpan ikon cuaca
    private String status;   // Untuk menyimpan deskripsi cuaca
    private int highTemp;    // Suhu maksimum
    private int lowTemp;     // Suhu minimum
    private double windSpeed;  // Kecepatan angin
    private int humidity;    // Kelembapan
    private String airQualityCategory;  // Kategori kualitas udara
    private int airQualityValue;        // Nilai kualitas udara
    private String predictionStartTime; // Waktu prediksi cuaca mulai

    // Constructor yang diperbarui dengan nilai default
    public FutureDomain(String status, String day, String picPath, int highTemp, int lowTemp,
                        double windSpeed, int humidity, String airQualityCategory, int airQualityValue, String predictionStartTime) {
        this.day = day;
        this.picPath = picPath;
        this.status = status;
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.windSpeed = windSpeed != 0 ? windSpeed : -1; // Default -1 jika tidak di-set
        this.humidity = humidity != 0 ? humidity : -1;    // Default -1 jika tidak di-set
        this.airQualityCategory = airQualityCategory != null ? airQualityCategory : "Unknown";
        this.airQualityValue = airQualityValue != 0 ? airQualityValue : -1; // Default -1 jika tidak di-set
        this.predictionStartTime = predictionStartTime; // Menyimpan waktu prediksi mulai
    }

    // Getter dan setter untuk semua field

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(int highTemp) {
        this.highTemp = highTemp;
    }

    public int getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(int lowTemp) {
        this.lowTemp = lowTemp;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public String getAirQualityCategory() {
        return airQualityCategory;
    }

    public void setAirQualityCategory(String airQualityCategory) {
        this.airQualityCategory = airQualityCategory;
    }

    public int getAirQualityValue() {
        return airQualityValue;
    }

    public void setAirQualityValue(int airQualityValue) {
        this.airQualityValue = airQualityValue;
    }

    public String getPredictionStartTime() {
        return predictionStartTime;
    }

    public void setPredictionStartTime(String predictionStartTime) {
        this.predictionStartTime = predictionStartTime;
    }

    // Fungsi untuk mengonversi prediksi start time menjadi nama hari
    public String getFormattedDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  // Format tanggal sesuai dengan format yang diterima
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");  // Format untuk mengambil nama hari
        try {
            Date date = sdf.parse(predictionStartTime);
            return dayFormat.format(date);  // Mengembalikan nama hari dalam minggu
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}
