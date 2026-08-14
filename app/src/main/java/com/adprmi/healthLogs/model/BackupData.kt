package com.adprmi.healthLogs.model

import com.adprmi.healthLogs.data.ExerciseEntity
import com.adprmi.healthLogs.data.MealEntity
import com.adprmi.healthLogs.data.WeightEntity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupData(
    val exercises: List<ExerciseEntity>,
    val meals: List<MealEntity>,
    val weights: List<WeightEntity> = emptyList()
)
