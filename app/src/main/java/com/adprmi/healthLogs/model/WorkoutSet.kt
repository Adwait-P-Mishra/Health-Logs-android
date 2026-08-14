package com.adprmi.healthLogs.model

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class WorkoutSet(
    val id: String = UUID.randomUUID().toString(),
    val weight: String = "",
    val reps: String = "",
    val unit: WeightUnit = WeightUnit.KG
)
