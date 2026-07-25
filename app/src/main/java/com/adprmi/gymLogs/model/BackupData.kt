package com.adprmi.gymLogs.model

import com.adprmi.gymLogs.data.ExerciseEntity
import com.adprmi.gymLogs.data.MealEntity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupData(
    val exercises: List<ExerciseEntity>,
    val meals: List<MealEntity>
)
