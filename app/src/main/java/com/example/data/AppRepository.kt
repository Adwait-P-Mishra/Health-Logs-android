package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val exerciseDao: ExerciseDao,
    private val mealDao: MealDao
) {
    val allExercisesFlow: Flow<List<ExerciseEntity>> = exerciseDao.getAllLogsFlow()
    val allMealsFlow: Flow<List<MealEntity>> = mealDao.getAllLogsFlow()

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

    suspend fun clearAllData() {
        exerciseDao.clearAll()
        mealDao.clearAll()
    }

    suspend fun importData(exercises: List<ExerciseEntity>, meals: List<MealEntity>) {
        exercises.forEach { exerciseDao.insertLog(it) }
        meals.forEach { mealDao.insertLog(it) }
    }
}
