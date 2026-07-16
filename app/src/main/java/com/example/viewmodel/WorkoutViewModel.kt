package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.ExerciseEntity
import com.example.data.PreferenceRepository
import com.example.model.WorkoutSet
import com.example.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class WorkoutViewModel(
    private val repository: AppRepository,
    prefRepository: PreferenceRepository
) : ViewModel() {

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

    val onboardingCompleted: StateFlow<Boolean> = prefRepository.onboardingCompleted

    // Form states for creating/editing logs
    var editingLogId = MutableStateFlow<String?>(null)
    val exerciseName = MutableStateFlow("")
    val draftSets = MutableStateFlow(listOf(WorkoutSet()))
    val notes = MutableStateFlow("")
    val showingSuggestions = MutableStateFlow(false)

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

    // Set management
    fun addSetCopyingPrevious() {
        val currentSets = draftSets.value
        val newSet = if (currentSets.isNotEmpty()) {
            val lastSet = currentSets.last()
            WorkoutSet(weight = lastSet.weight, reps = lastSet.reps)
        } else {
            WorkoutSet()
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
        draftSets.value = log.sets.map { WorkoutSet(weight = it.weight, reps = it.reps) }
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
                draftSets.value = lastLog.sets.map { WorkoutSet(weight = it.weight, reps = it.reps) }
                notes.value = lastLog.notes
            } else {
                draftSets.value = listOf(WorkoutSet())
                notes.value = ""
            }
        } else {
            draftSets.value = listOf(WorkoutSet())
            notes.value = ""
        }
        
        showingSuggestions.value = false
    }

    fun startEditLog(log: ExerciseEntity) {
        editingLogId.value = log.id
        exerciseName.value = log.exerciseName
        draftSets.value = log.sets.map { WorkoutSet(id = it.id, weight = it.weight, reps = it.reps) }
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
            val entity = ExerciseEntity(
                id = id,
                date = _selectedDate.value.time,
                exerciseName = exerciseName.value.trim(),
                sets = draftSets.value,
                notes = notes.value.trim()
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
