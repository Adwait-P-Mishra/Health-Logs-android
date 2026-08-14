package com.adprmi.healthLogs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adprmi.healthLogs.data.AppRepository
import com.adprmi.healthLogs.data.CalorieRepository
import com.adprmi.healthLogs.data.PreferenceRepository
import com.adprmi.healthLogs.data.ExerciseEntity
import com.adprmi.healthLogs.data.WeightEntity
import com.adprmi.healthLogs.model.WorkoutSet
import com.adprmi.healthLogs.model.WeightUnit
import com.adprmi.healthLogs.model.CalorieUiState
import com.adprmi.healthLogs.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class WorkoutViewModel(
    private val repository: AppRepository,
    private val preferenceRepository: PreferenceRepository,
    private val calorieRepository: CalorieRepository
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
    val draftSets = MutableStateFlow(listOf(WorkoutSet(unit = weightUnit.value)))
    val caloriesBurned = MutableStateFlow("")
    val notes = MutableStateFlow("")
    val showingSuggestions = MutableStateFlow(false)

    private val _calorieUiState = MutableStateFlow<CalorieUiState>(CalorieUiState.Idle)
    val calorieUiState: StateFlow<CalorieUiState> = _calorieUiState

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
        // copy sets
        draftSets.value = log.sets.map { WorkoutSet(weight = it.weight, reps = it.reps, unit = weightUnit.value) }
        caloriesBurned.value = log.caloriesBurned?.toString() ?: ""
        notes.value = log.notes
        showingSuggestions.value = false
    }

    fun startNewLog(name: String = "") {
        editingLogId.value = null
        exerciseName.value = name
        
        if (name.isNotEmpty()) {
            val lastLog = allLogs.value
                .filter { it.exerciseName.trim().equals(name.trim(), ignoreCase = true) }
                .maxByOrNull { it.date }
            
            if (lastLog != null) {
                draftSets.value = lastLog.sets.map { WorkoutSet(weight = it.weight, reps = it.reps, unit = weightUnit.value) }
                caloriesBurned.value = lastLog.caloriesBurned?.toString() ?: ""
                notes.value = lastLog.notes
            } else {
                draftSets.value = listOf(WorkoutSet(unit = weightUnit.value))
                caloriesBurned.value = ""
                notes.value = ""
            }
        } else {
            draftSets.value = listOf(WorkoutSet(unit = weightUnit.value))
            caloriesBurned.value = ""
            notes.value = ""
        }
        
        showingSuggestions.value = false
    }

    fun startEditLog(log: ExerciseEntity) {
        editingLogId.value = log.id
        exerciseName.value = log.exerciseName
        draftSets.value = log.sets.map { WorkoutSet(id = it.id, weight = it.weight, reps = it.reps, unit = it.unit) }
        caloriesBurned.value = log.caloriesBurned?.toString() ?: ""
        notes.value = log.notes
        showingSuggestions.value = false
    }

    val canSaveLog: StateFlow<Boolean> = combine(exerciseName, draftSets) { name, sets ->
        name.trim().isNotEmpty() && sets.isNotEmpty() && sets.all { it.weight.isNotEmpty() && it.reps.isNotEmpty() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun saveCurrentLog() {
        if (!canSaveLog.value) return
        viewModelScope.launch {
            val id = editingLogId.value ?: UUID.randomUUID().toString()
            val currentUnit = weightUnit.value
            val entity = ExerciseEntity(
                id = id,
                date = _selectedDate.value.time,
                exerciseName = exerciseName.value.trim(),
                sets = draftSets.value.map { it.copy(unit = currentUnit) },
                notes = notes.value.trim(),
                caloriesBurned = caloriesBurned.value.toIntOrNull()
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

    fun estimateCalorieBurn() {
        val description = exerciseName.value
        if (description.isBlank()) return

        if (preferenceRepository.getAiProviderConfig() == null) {
            _calorieUiState.value = CalorieUiState.NotConfigured
            return
        }

        // Use the weight from the selected date for AI prompt
        val endOfSelectedDay = DateUtils.getEndOfDay(_selectedDate.value).time
        val weightForDate = allWeights.value
            .filter { it.date <= endOfSelectedDay }
            .maxByOrNull { it.date }?.weightKg

        if (weightForDate == null || weightForDate == 0.0) {
            _calorieUiState.value = CalorieUiState.Error("Please set your weight first for accurate estimation.")
            return
        }

        val setsDescription = draftSets.value.joinToString { "${it.weight} ${it.unit.displayName} x ${it.reps} reps" }
        val fullDescription = "$description ($setsDescription)"

        viewModelScope.launch {
            _calorieUiState.value = CalorieUiState.Loading
            calorieRepository.estimateExercise(fullDescription, weightForDate, null)
                .onSuccess { (estimate, prompt) ->
                    _calorieUiState.value = CalorieUiState.Success(estimate, prompt)
                    caloriesBurned.value = estimate.calories.toString()
                }
                .onFailure { error ->
                    _calorieUiState.value = CalorieUiState.Error(error.message ?: "Failed to estimate")
                }
        }
    }

    fun resetCalorieUiState() {
        _calorieUiState.value = CalorieUiState.Idle
    }
}
