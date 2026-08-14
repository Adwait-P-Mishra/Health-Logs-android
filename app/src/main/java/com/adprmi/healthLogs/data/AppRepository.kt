package com.adprmi.healthLogs.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val exerciseDao: ExerciseDao,
    private val mealDao: MealDao,
    private val weightDao: WeightDao
) {
    val allExercisesFlow: Flow<List<ExerciseEntity>> = exerciseDao.getAllLogsFlow()
    val allMealsFlow: Flow<List<MealEntity>> = mealDao.getAllLogsFlow()
    val allWeightsFlow: Flow<List<WeightEntity>> = weightDao.getAllWeightsFlow()

    suspend fun insertExercise(log: ExerciseEntity) {
        exerciseDao.insertLog(log)
    }

    suspend fun deleteExerciseById(id: String) {
        exerciseDao.deleteLogById(id)
    }

    suspend fun insertMeal(log: MealEntity) {
        mealDao.insertLog(log)
    }

    suspend fun deleteMealById(id: String) {
        mealDao.deleteLogById(id)
    }

    suspend fun insertWeight(weight: WeightEntity) {
        weightDao.insertWeight(weight)
    }

    suspend fun deleteWeightById(id: String) {
        weightDao.deleteWeightById(id)
    }

    suspend fun clearAllData() {
        exerciseDao.clearAll()
        mealDao.clearAll()
        weightDao.clearAll()
    }

    suspend fun importData(
        exercises: List<ExerciseEntity>,
        meals: List<MealEntity>,
        weights: List<WeightEntity>
    ) {
        exercises.forEach { exerciseDao.insertLog(it) }
        meals.forEach { mealDao.insertLog(it) }
        weights.forEach { weightDao.insertWeight(it) }
    }
}
