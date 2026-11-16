package com.mishrAdwait.healthLogs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mishrAdwait.healthLogs.data.AppRepository
import com.mishrAdwait.healthLogs.data.PreferenceRepository
import com.mishrAdwait.healthLogs.data.ExerciseEntity
import com.mishrAdwait.healthLogs.data.WeightEntity
import com.mishrAdwait.healthLogs.model.WorkoutSet
import com.mishrAdwait.healthLogs.model.WeightUnit
import com.mishrAdwait.healthLogs.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class WorkoutViewModel(
    private val repository: AppRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    val weightUnit: StateFlow<WeightUnit> = preferenceRepository.weightUnit

    val allWeights: StateFlow<List<WeightEntity>> = repository.allWeightsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Global selected date for both workouts and meals
    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate

    // All exercise logs from DB
    val allLogs: StateFlow<List<ExerciseEntity>> = repository.allExercisesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered logs for selected date
    val todayLogs: StateFlow<List<ExerciseEntity>> = combine(allLogs, _selectedDate) { logs, date ->
        logs.filter { DateUtils.isSameDay(Date(it.date), date) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form states for creating/editing logs
    var editingLogId = MutableStateFlow<String?>(null)
    val exerciseName = MutableStateFlow("")
    val exerciseType = MutableStateFlow("strength") // "strength" or "cardio"
    val draftSets = MutableStateFlow(listOf(WorkoutSet(unit = weightUnit.value)))
    val cardioAmount = MutableStateFlow("")
    val cardioUnit = MutableStateFlow("minutes")
    val caloriesBurned = MutableStateFlow("")
    val notes = MutableStateFlow("")
    val showingSuggestions = MutableStateFlow(false)
    val isEstimateLater = MutableStateFlow(false)

    // Suggestions based on search of historical logs
    val exerciseSuggestions: StateFlow<List<ExerciseEntity>> = combine(allLogs, exerciseName) { logs, name ->
        val query = name.trim().lowercase()
        if (query.length >= 2) {
            // Find unique exercise names matching query and get the latest log for each
            logs.filter { it.exerciseName.trim().lowercase().contains(query) }
                .groupBy { it.exerciseName.trim().lowercase() }
                .map { (_, group) -> group.maxByOrNull { it.date }!! }
                .take(5)
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Last matching log as reference for previous weights
    val lastMatchingLog: StateFlow<ExerciseEntity?> = combine(allLogs, exerciseName) { logs, name ->
        val query = name.trim().lowercase()
        if (query.isNotEmpty()) {
            logs.filter { it.exerciseName.trim().lowercase() == query }
                .maxByOrNull { it.date }
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun changeSelectedDate(date: Date) {
        _selectedDate.value = date
    }

    fun setUserWeightKg(weight: Double, date: Date = _selectedDate.value) {
        viewModelScope.launch {
            // Update preference repository if it's today's date
            if (DateUtils.isSameDay(date, Date())) {
                preferenceRepository.setUserWeightKg(weight)
            }
            
            // Use date-based ID to ensure one record per day
            val dateId = DateUtils.formatDate(date, "yyyyMMdd")
            repository.insertWeight(
                WeightEntity(
                    id = dateId,
                    weightKg = weight, 
                    date = date.time
                )
            )
        }
    }

    // Set management
    fun addSetCopyingPrevious() {
        val currentSets = draftSets.value
        val newSet = if (currentSets.isNotEmpty()) {
            val lastSet = currentSets.last()
            WorkoutSet(weight = lastSet.weight, reps = lastSet.reps, unit = weightUnit.value)
        } else {
            WorkoutSet(unit = weightUnit.value)
        }
        draftSets.value = currentSets + newSet
    }

    fun updateSet(index: Int, weight: String, reps: String) {
        val currentSets = draftSets.value.toMutableList()
        if (index in currentSets.indices) {
            currentSets[index] = currentSets[index].copy(weight = weight, reps = reps)
            draftSets.value = currentSets
        }
    }

    fun removeSet(id: String) {
        val currentSets = draftSets.value
        if (currentSets.size > 1) {
            draftSets.value = currentSets.filter { it.id != id }
        }
    }

    fun applySuggestion(log: ExerciseEntity) {
        exerciseName.value = log.exerciseName
        exerciseType.value = log.exerciseType
        // copy sets
        draftSets.value = log.sets.map { WorkoutSet(weight = it.weight, reps = it.reps, unit = weightUnit.value) }
        cardioAmount.value = log.cardioAmount?.toString() ?: ""
        cardioUnit.value = log.cardioUnit ?: "minutes"
        caloriesBurned.value = log.caloriesBurned?.toString() ?: ""
        notes.value = log.notes
        showingSuggestions.value = false
        isEstimateLater.value = log.caloriesBurned == 0
    }

    fun startNewLog(name: String = "") {
        editingLogId.value = null
        exerciseName.value = name
        exerciseType.value = "strength"
        isEstimateLater.value = false

        if (name.isNotEmpty()) {
            val lastLog = allLogs.value
                .filter { it.exerciseName.trim().equals(name.trim(), ignoreCase = true) }
                .maxByOrNull { it.date }
            
            if (lastLog != null) {
                exerciseType.value = lastLog.exerciseType
                draftSets.value = lastLog.sets.map { WorkoutSet(weight = it.weight, reps = it.reps, unit = weightUnit.value) }
                cardioAmount.value = lastLog.cardioAmount?.toString() ?: ""
                cardioUnit.value = lastLog.cardioUnit ?: "minutes"
                caloriesBurned.value = if ((lastLog.caloriesBurned ?: 0) > 0) lastLog.caloriesBurned?.toString() ?: "" else ""
                notes.value = lastLog.notes
                isEstimateLater.value = lastLog.caloriesBurned == 0
            } else {
                draftSets.value = listOf(WorkoutSet(unit = weightUnit.value))
                cardioAmount.value = ""
                cardioUnit.value = "minutes"
                caloriesBurned.value = ""
                notes.value = ""
            }
        } else {
            draftSets.value = listOf(WorkoutSet(unit = weightUnit.value))
            cardioAmount.value = ""
            cardioUnit.value = "minutes"
            caloriesBurned.value = ""
            notes.value = ""
        }
        
        showingSuggestions.value = false
    }

    fun startEditLog(log: ExerciseEntity) {
        editingLogId.value = log.id
        exerciseName.value = log.exerciseName
        exerciseType.value = log.exerciseType
        draftSets.value = log.sets.map { WorkoutSet(id = it.id, weight = it.weight, reps = it.reps, unit = it.unit) }
        cardioAmount.value = log.cardioAmount?.toString() ?: ""
        cardioUnit.value = log.cardioUnit ?: "minutes"
        caloriesBurned.value = if ((log.caloriesBurned ?: 0) > 0) log.caloriesBurned?.toString() ?: "" else ""
        notes.value = log.notes
        showingSuggestions.value = false
        isEstimateLater.value = log.caloriesBurned == 0
    }

    val canSaveLog: StateFlow<Boolean> = combine(
        exerciseName, 
        exerciseType, 
        draftSets, 
        cardioAmount, 
        isEstimateLater, 
        caloriesBurned
    ) { flows ->
        val name = flows[0] as String
        val type = flows[1] as String
        val sets = flows[2] as List<WorkoutSet>
        val amount = flows[3] as String
        val estimateLater = flows[4] as Boolean
        val kcal = flows[5] as String
        
        val nameOk = name.trim().isNotEmpty()
        val baseOk = if (type == "strength") {
            nameOk && sets.isNotEmpty() && sets.all { it.weight.isNotEmpty() && it.reps.isNotEmpty() }
        } else {
            nameOk && amount.toDoubleOrNull() != null
        }
        baseOk && (estimateLater || kcal.toIntOrNull() != null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun saveCurrentLog() {
        if (!canSaveLog.value) return
        viewModelScope.launch {
            val id = editingLogId.value ?: UUID.randomUUID().toString()
            val currentUnit = weightUnit.value
            val finalCaloriesBurned = if (isEstimateLater.value) 0 else (caloriesBurned.value.toIntOrNull() ?: 0)
            val entity = ExerciseEntity(
                id = id,
                date = _selectedDate.value.time,
                exerciseName = exerciseName.value.trim(),
                exerciseType = exerciseType.value,
                sets = if (exerciseType.value == "strength") draftSets.value.map { it.copy(unit = currentUnit) } else emptyList(),
                cardioAmount = if (exerciseType.value == "cardio") cardioAmount.value.toDoubleOrNull() else null,
                cardioUnit = if (exerciseType.value == "cardio") cardioUnit.value else null,
                notes = notes.value.trim(),
                caloriesBurned = finalCaloriesBurned
            )
            repository.insertExercise(entity)
            startNewLog()
        }
    }

    fun deleteLog(id: String) {
        viewModelScope.launch {
            repository.deleteExerciseById(id)
        }
    }
}
