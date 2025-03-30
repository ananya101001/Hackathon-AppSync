// models/GdpData.kt
package com.example.hackathon_appsync.models

import com.google.gson.annotations.SerializedName

data class GdpResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("pages") val pages: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("sourceid") val sourceId: String,
    @SerializedName("lastupdated") val lastUpdated: String,
    @SerializedName("source") val source: List<Source>,
    @SerializedName("data") val data: List<GdpData>
)

data class Source(
    @SerializedName("id") val id: String,
    @SerializedName("value") val value: String
)

data class GdpData(
    @SerializedName("indicator") val indicator: Indicator,
    @SerializedName("country") val country: Country,
    @SerializedName("countryiso3code") val countryIso3Code: String,
    @SerializedName("date") val date: String,
    @SerializedName("value") val value: Double?,
    @SerializedName("unit") val unit: String,
    @SerializedName("obs_status") val obsStatus: String,
    @SerializedName("decimal") val decimal: Int
)

data class Indicator(
    @SerializedName("id") val id: String,
    @SerializedName("value") val value: String
)

data class Country(
    @SerializedName("id") val id: String,
    @SerializedName("value") val value: String
)