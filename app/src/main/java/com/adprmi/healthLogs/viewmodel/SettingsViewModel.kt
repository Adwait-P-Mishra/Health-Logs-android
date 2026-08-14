package com.adprmi.healthLogs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adprmi.healthLogs.data.AppRepository
import com.adprmi.healthLogs.data.CalorieRepository
import com.adprmi.healthLogs.data.PreferenceRepository
import com.adprmi.healthLogs.model.AiProviderConfig
import com.adprmi.healthLogs.model.BackupData
import com.adprmi.healthLogs.model.CalorieUiState
import com.adprmi.healthLogs.model.HeightUnit
import com.adprmi.healthLogs.model.WeightUnit
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import android.widget.Toast
import java.io.OutputStreamWriter
import java.io.InputStreamReader

class SettingsViewModel(
    private val repository: AppRepository,
    private val preferenceRepository: PreferenceRepository,
    private val calorieRepository: CalorieRepository,
    private val moshi: Moshi
) : ViewModel() {

    val targetCalories: StateFlow<Int?> = preferenceRepository.targetCalories
    val weightUnit: StateFlow<WeightUnit> = preferenceRepository.weightUnit
    val userWeightKg: StateFlow<Double?> = preferenceRepository.userWeightKg
    val userHeightCm: StateFlow<Double?> = preferenceRepository.userHeightCm
    val userGender: StateFlow<String?> = preferenceRepository.userGender
    val heightUnit: StateFlow<HeightUnit> = preferenceRepository.heightUnit
    val aiProviderConfig: StateFlow<AiProviderConfig?> = preferenceRepository.aiProviderConfig
    val aiOnboardingShown: StateFlow<Boolean> = preferenceRepository.aiOnboardingShown

    private val _testResult = MutableStateFlow<Result<Boolean>?>(null)
    val testResult: StateFlow<Result<Boolean>?> = _testResult

    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting

    fun setTargetCalories(calories: Int?) {
        preferenceRepository.setTargetCalories(calories)
    }

    fun setWeightUnit(unit: WeightUnit) {
        preferenceRepository.setWeightUnit(unit)
    }

    fun setUserWeightKg(weight: Double?) {
        preferenceRepository.setUserWeightKg(weight)
    }

    fun setUserHeightCm(height: Double?) {
        preferenceRepository.setUserHeightCm(height)
    }

    fun setUserGender(gender: String?) {
        preferenceRepository.setUserGender(gender)
    }

    fun setHeightUnit(unit: HeightUnit) {
        preferenceRepository.setHeightUnit(unit)
    }

    fun setAiProviderConfig(config: AiProviderConfig?) {
        preferenceRepository.setAiProviderConfig(config)
    }

    fun setAiOnboardingShown(shown: Boolean) {
        preferenceRepository.setAiOnboardingShown(shown)
    }

    fun testConnection(config: AiProviderConfig) {
        viewModelScope.launch {
            _isTesting.value = true
            _testResult.value = calorieRepository.testConnection(config)
            _isTesting.value = false
        }
    }

    fun resetTestResult() {
        _testResult.value = null
    }

    fun clearAllData(keepSettings: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            if (keepSettings) {
                preferenceRepository.clearDataOnly()
            } else {
                preferenceRepository.clearAll()
            }
            onComplete()
        }
    }

    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val exercises = repository.allExercisesFlow.first()
                val meals = repository.allMealsFlow.first()
                val weights = repository.allWeightsFlow.first()
                val backupData = BackupData(exercises, meals, weights)
                val adapter = moshi.adapter(BackupData::class.java)
                val json = adapter.toJson(backupData)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(json)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        val json = reader.readText()
                        val adapter = moshi.adapter(BackupData::class.java)
                        val backupData = adapter.fromJson(json)
                        if (backupData != null) {
                            repository.importData(
                                backupData.exercises,
                                backupData.meals,
                                backupData.weights
                            )
                            Toast.makeText(
                                context,
                                "Imported ${backupData.exercises.size} workouts, ${backupData.meals.size} meals, and ${backupData.weights.size} weights",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
