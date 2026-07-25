package com.adprmi.gymLogs.data

import androidx.room.TypeConverter
import com.adprmi.gymLogs.model.WorkoutSet

class Converters {
    @TypeConverter
    fun fromSetsList(sets: List<WorkoutSet>?): String {
        if (sets == null) return ""
        return sets.joinToString(";") { "${it.weight}:${it.reps}:${it.id}" }
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
                WorkoutSet(id = id, weight = weight, reps = reps)
            } else null
        }
    }
}
