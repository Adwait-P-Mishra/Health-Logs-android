package com.adprmi.healthLogs.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
@Entity(tableName = "weight_logs")
data class WeightEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long, // timestamp
    val weightKg: Double
)
