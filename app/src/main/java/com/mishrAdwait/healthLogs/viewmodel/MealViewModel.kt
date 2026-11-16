package com.mishrAdwait.healthLogs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mishrAdwait.healthLogs.data.AppRepository
import com.mishrAdwait.healthLogs.data.MealEntity
import com.mishrAdwait.healthLogs.data.PreferenceRepository
import com.mishrAdwait.healthLogs.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MealViewModel(
    private val repository: AppRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(Date())

    val selectedChatbot: StateFlow<com.mishrAdwait.healthLogs.model.ChatbotOption> = preferenceRepository.selectedChatbot

    val allLogs: StateFlow<List<MealEntity>> = repository.allMealsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayLogs: StateFlow<List<MealEntity>> = combine(allLogs, _selectedDate) { logs, date ->
        logs.filter { DateUtils.isSameDay(Date(it.date), date) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summed calories and macronutrients for today
    val todayCaloriesSum: StateFlow<Int> = todayLogs.map { logs ->
        logs.sumOf { it.calories }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayProteinSum: StateFlow<Double> = todayLogs.map { logs ->
        logs.sumOf { it.protein ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayCarbsSum: StateFlow<Double> = todayLogs.map { logs ->
        logs.sumOf { it.carbs ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayFatSum: StateFlow<Double> = todayLogs.map { logs ->
        logs.sumOf { it.fat ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Form states
    var editingLogId = MutableStateFlow<String?>(null)
    val mealName = MutableStateFlow("")
    val calories = MutableStateFlow("")
    val protein = MutableStateFlow("")
    val carbs = MutableStateFlow("")
    val fat = MutableStateFlow("")
    val notes = MutableStateFlow("")
    val showingSuggestions = MutableStateFlow(false)
    val isEstimateLater = MutableStateFlow(false)

    val mealSuggestions: StateFlow<List<MealEntity>> = combine(allLogs, mealName) { logs, name ->
        val query = name.trim().lowercase()
        if (query.length >= 2) {
            logs.filter { it.mealName.trim().lowercase().contains(query) }
                .groupBy { it.mealName.trim().lowercase() }
                .map { (_, group) -> group.maxByOrNull { it.date }!! }
                .take(5)
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastMatchingLog: StateFlow<MealEntity?> = combine(allLogs, mealName) { logs, name ->
        val query = name.trim().lowercase()
        if (query.isNotEmpty()) {
            logs.filter { it.mealName.trim().lowercase() == query }
                .maxByOrNull { it.date }
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun changeSelectedDate(date: Date) {
        _selectedDate.value = date
    }

    fun applySuggestion(log: MealEntity) {
        mealName.value = log.mealName
        calories.value = log.calories.toString()
        protein.value = log.protein?.toString() ?: ""
        carbs.value = log.carbs?.toString() ?: ""
        fat.value = log.fat?.toString() ?: ""
        notes.value = log.notes
        showingSuggestions.value = false
        isEstimateLater.value = log.calories == 0
    }

    fun startNewLog(name: String = "") {
        editingLogId.value = null
        mealName.value = name
        isEstimateLater.value = false

        if (name.isNotEmpty()) {
            val lastLog = allLogs.value
                .filter { it.mealName.trim().equals(name.trim(), ignoreCase = true) }
                .maxByOrNull { it.date }
                
            if (lastLog != null) {
                calories.value = if (lastLog.calories > 0) lastLog.calories.toString() else ""
                protein.value = lastLog.protein?.toString() ?: ""
                carbs.value = lastLog.carbs?.toString() ?: ""
                fat.value = lastLog.fat?.toString() ?: ""
                notes.value = lastLog.notes
                isEstimateLater.value = lastLog.calories == 0
            } else {
                calories.value = ""
                protein.value = ""
                carbs.value = ""
                fat.value = ""
                notes.value = ""
            }
        } else {
            calories.value = ""
            protein.value = ""
            carbs.value = ""
            fat.value = ""
            notes.value = ""
        }
        
        showingSuggestions.value = false
    }

    fun startEditLog(log: MealEntity) {
        editingLogId.value = log.id
        mealName.value = log.mealName
        calories.value = if (log.calories > 0) log.calories.toString() else ""
        protein.value = log.protein?.toString() ?: ""
        carbs.value = log.carbs?.toString() ?: ""
        fat.value = log.fat?.toString() ?: ""
        notes.value = log.notes
        showingSuggestions.value = false
        isEstimateLater.value = log.calories == 0
    }

    val canSaveLog: StateFlow<Boolean> = combine(mealName, calories, isEstimateLater) { name, kcal, estimateLater ->
        name.trim().isNotEmpty() && (estimateLater || kcal.toIntOrNull() != null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun saveCurrentLog() {
        if (!canSaveLog.value) return
        viewModelScope.launch {
            val id = editingLogId.value ?: UUID.randomUUID().toString()
            val finalCalories = if (isEstimateLater.value) 0 else (calories.value.toIntOrNull() ?: 0)
            val entity = MealEntity(
                id = id,
                date = _selectedDate.value.time,
                mealName = mealName.value.trim(),
                calories = finalCalories,
                protein = protein.value.toDoubleOrNull(),
                carbs = carbs.value.toDoubleOrNull(),
                fat = fat.value.toDoubleOrNull(),
                notes = notes.value.trim()
            )
            repository.insertMeal(entity)
            startNewLog()
        }
    }

    fun updateMeal(log: MealEntity) {
        viewModelScope.launch {
            repository.insertMeal(log)
        }
    }

    fun deleteLog(id: String) {
        viewModelScope.launch {
            repository.deleteMealById(id)
        }
    }
}
