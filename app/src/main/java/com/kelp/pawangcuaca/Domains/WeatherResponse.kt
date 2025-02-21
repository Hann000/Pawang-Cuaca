package com.kelp.pawangcuaca.Domains

import com.kelp.pawangcuaca.Model.Metric
import com.kelp.pawangcuaca.Model.Temperature

data class WeatherResponse(
    val weatherText: String,
    val temperature: Temperature,
    val realFeelTemperature: Temperature
)

data class Temperature(
    val metric: Metric
)

data class UnitValue(
    val value: Double,
    val unit: String
)
