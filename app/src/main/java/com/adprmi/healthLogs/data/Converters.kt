package com.adprmi.healthLogs.data

import androidx.room.TypeConverter
import com.adprmi.healthLogs.model.WorkoutSet
import com.adprmi.healthLogs.model.WeightUnit

class Converters {
    @TypeConverter
    fun fromSetsList(sets: List<WorkoutSet>?): String {
        if (sets == null) return ""
        return sets.joinToString(";") { "${it.weight}:${it.reps}:${it.id}:${it.unit.name}" }
    }

    @TypeConverter
    fun toSetsList(data: String?): List<WorkoutSet> {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(";").mapNotNull {
            val parts = it.split(":")
            if (parts.size >= 2) {
                val weight = parts[0]
                val reps = parts[1]
                val id = if (parts.size >= 3) parts[2] else java.util.UUID.randomUUID().toString()
                val unit = if (parts.size >= 4) {
                    try {
                        WeightUnit.valueOf(parts[3])
                    } catch (e: Exception) {
                        WeightUnit.KG
                    }
                } else WeightUnit.KG
                WorkoutSet(id = id, weight = weight, reps = reps, unit = unit)
            } else null
        }
    }
}
