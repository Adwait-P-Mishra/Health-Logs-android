package com.example.model

import com.example.data.ExerciseEntity
import com.example.data.MealEntity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupData(
    val exercises: List<ExerciseEntity>,
    val meals: List<MealEntity>
)
