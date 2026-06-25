package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.WorkoutSet
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
@Entity(tableName = "exercise_logs")
data class ExerciseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long, // timestamp
    val exerciseName: String,
    val sets: List<WorkoutSet>,
    val notes: String = ""
)
