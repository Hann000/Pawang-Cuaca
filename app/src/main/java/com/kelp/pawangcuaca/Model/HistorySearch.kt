package com.kelp.pawangcuaca.Model

data class HistorySearch(
    val locationName: String,
    val weatherStatus: String? = null,
    val lowTempText: String? = null,
    val highTempText: String? = null,
    val mainTemp: String? = null
)