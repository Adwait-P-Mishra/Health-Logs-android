package com.adprmi.healthLogs.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BatchCalorieEstimate(
    @Json(name = "estimates") val estimates: List<CalorieEstimate>,
    @Json(name = "assumptions") val assumptions: List<String>
)
