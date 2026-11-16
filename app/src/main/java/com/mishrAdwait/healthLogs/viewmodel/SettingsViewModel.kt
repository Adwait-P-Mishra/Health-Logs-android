package com.mishrAdwait.healthLogs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mishrAdwait.healthLogs.data.AppRepository
import com.mishrAdwait.healthLogs.data.PreferenceRepository
import com.mishrAdwait.healthLogs.model.BackupData
import com.mishrAdwait.healthLogs.model.HeightUnit
import com.mishrAdwait.healthLogs.model.WeightUnit
import com.squareup.moshi.Moshi
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
    private val moshi: Moshi
) : ViewModel() {

    val targetCalories: StateFlow<Int?> = preferenceRepository.targetCalories
    val weightUnit: StateFlow<WeightUnit> = preferenceRepository.weightUnit
    val userWeightKg: StateFlow<Double?> = preferenceRepository.userWeightKg
    val userHeightCm: StateFlow<Double?> = preferenceRepository.userHeightCm
    val userGender: StateFlow<String?> = preferenceRepository.userGender
    val heightUnit: StateFlow<HeightUnit> = preferenceRepository.heightUnit
    val selectedChatbot: StateFlow<com.mishrAdwait.healthLogs.model.ChatbotOption> = preferenceRepository.selectedChatbot

    fun setTargetCalories(calories: Int?) {
        preferenceRepository.setTargetCalories(calories)
    }

    fun setSelectedChatbot(option: com.mishrAdwait.healthLogs.model.ChatbotOption) {
        preferenceRepository.setSelectedChatbot(option)
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
