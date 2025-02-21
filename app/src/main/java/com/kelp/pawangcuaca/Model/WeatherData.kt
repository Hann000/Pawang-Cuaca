package com.kelp.pawangcuaca.Model

import com.google.gson.annotations.SerializedName
import com.kelp.pawangcuaca.Domains.Temperature

data class WeatherData(
    @SerializedName("LocalObservationDateTime")
    val localObservationDateTime: String,
    @SerializedName("WeatherText")
    val weatherText: String,
    @SerializedName("Temperature")
    val temperature: Temperature,
    @SerializedName("RealFeelTemperature")
    val realFeelTemperature: Temperature = com.kelp.pawangcuaca.Domains.Temperature(
        Metric(
            0.0,
            ""
        )
    ),
    @SerializedName("WeatherIcon")
    val weatherIcon: Int,
    @SerializedName("TemperatureSummary")
    val temperatureSummary: TemperatureSummary? = null
)

data class Temperature(
    @SerializedName("Metric")
    val metric: Metric
)

data class Metric(
    @SerializedName("Value")
    val value: Double,
    @SerializedName("Unit")
    val unit: String
)

data class TemperatureSummary(
    @SerializedName("Past6HourRange")
    val past6HourRange: Range
)

data class Range(
    @SerializedName("Minimum")
    val minimum: Metric,
    @SerializedName("Maximum")
    val maximum: Metric
)
