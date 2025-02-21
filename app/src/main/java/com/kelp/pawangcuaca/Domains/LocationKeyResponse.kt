package com.kelp.pawangcuaca.Domains

import com.google.gson.annotations.SerializedName

data class LocationKeyResponse(
    @SerializedName("Key")
    val key: String,
    @SerializedName("LocalizedName")
    val localizedName: String
)
