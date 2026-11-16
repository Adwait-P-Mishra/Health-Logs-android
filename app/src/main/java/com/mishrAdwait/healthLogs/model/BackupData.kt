package com.mishrAdwait.healthLogs.model

import com.mishrAdwait.healthLogs.data.ExerciseEntity
import com.mishrAdwait.healthLogs.data.MealEntity
import com.mishrAdwait.healthLogs.data.WeightEntity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupData(
    val exercises: List<ExerciseEntity>,
    val meals: List<MealEntity>,
    val weights: List<WeightEntity> = emptyList()
)
