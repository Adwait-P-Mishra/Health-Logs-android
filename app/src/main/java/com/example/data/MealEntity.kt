package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
@Entity(tableName = "meal_logs")
data class MealEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long, // timestamp
    val mealName: String,
    val calories: Int,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val notes: String = ""
)
