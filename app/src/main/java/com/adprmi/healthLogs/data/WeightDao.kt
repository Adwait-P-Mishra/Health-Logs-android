package com.adprmi.healthLogs.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs ORDER BY date DESC")
    fun getAllWeightsFlow(): Flow<List<WeightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: WeightEntity)

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteWeightById(id: String)

    @Query("SELECT * FROM weight_logs WHERE date >= :startOfDay AND date <= :endOfDay LIMIT 1")
    suspend fun getWeightForDay(startOfDay: Long, endOfDay: Long): WeightEntity?

    @Query("DELETE FROM weight_logs")
    suspend fun clearAll()
}
