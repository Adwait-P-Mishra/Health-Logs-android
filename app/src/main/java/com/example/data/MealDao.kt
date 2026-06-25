package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_logs ORDER BY date DESC")
    fun getAllLogsFlow(): Flow<List<MealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MealEntity)

    @Update
    suspend fun updateLog(log: MealEntity)

    @Delete
    suspend fun deleteLog(log: MealEntity)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteLogById(id: String)

    @Query("DELETE FROM meal_logs")
    suspend fun clearAll()
}
