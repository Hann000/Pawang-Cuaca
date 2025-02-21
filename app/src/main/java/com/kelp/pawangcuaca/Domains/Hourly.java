package com.kelp.pawangcuaca.Domains;

public class Hourly {
    private String hour;
    private String temperature;
    private int PicPath;
    private String weatherDescription;
    private String windSpeed;
    private int humidity;
    private double uvIndex;




    public Hourly(String hour, String tempCelsius, int PicPath, String weatherDescription, String windSpeed, int humidity , double uvIndex) {
        this.hour = hour;
        this.temperature = tempCelsius;
        this.PicPath = PicPath;
        this.weatherDescription = weatherDescription;
        this.windSpeed = windSpeed;
        this.humidity = humidity;
        this.uvIndex = uvIndex;

    }

    public String getHour() {
        return hour;
    }
    public double getUvIndex() {
        return uvIndex;
    }
    public String getTempCelsius() {
        return temperature;
    }

    public int getPicPath() {
        return PicPath;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public int getHumidity() {
        return humidity;
    }
}
