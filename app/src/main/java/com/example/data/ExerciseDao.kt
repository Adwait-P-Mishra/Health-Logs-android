package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise_logs ORDER BY date DESC")
    fun getAllLogsFlow(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ExerciseEntity)

    @Update
    suspend fun updateLog(log: ExerciseEntity)

    @Delete
    suspend fun deleteLog(log: ExerciseEntity)

    @Query("DELETE FROM exercise_logs WHERE id = :id")
    suspend fun deleteLogById(id: String)

    @Query("DELETE FROM exercise_logs")
    suspend fun clearAll()
}
