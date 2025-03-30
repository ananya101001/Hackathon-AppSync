// models/AgriLandData.kt
package com.example.hackathon_appsync.models

import com.google.gson.annotations.SerializedName

data class AgriLandData(
    @SerializedName("indicator") val indicator: Indicator,
    @SerializedName("country") val country: Country,
    @SerializedName("countryiso3code") val countryIso3Code: String,
    @SerializedName("date") val date: String,
    @SerializedName("value") val value: Double?,
    @SerializedName("unit") val unit: String,
    @SerializedName("obs_status") val obsStatus: String,
    @SerializedName("decimal") val decimal: Int
)