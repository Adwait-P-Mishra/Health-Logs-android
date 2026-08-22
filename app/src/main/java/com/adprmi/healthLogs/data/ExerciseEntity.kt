package com.adprmi.healthLogs.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adprmi.healthLogs.model.WorkoutSet
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
@Entity(tableName = "exercise_logs")
data class ExerciseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long, // timestamp
    val exerciseName: String,
    val exerciseType: String = "strength", // "strength" or "cardio"
    val sets: List<WorkoutSet> = emptyList(),
    val cardioAmount: Double? = null,
    val cardioUnit: String? = null,
    val notes: String = "",
    val caloriesBurned: Int? = null
)
