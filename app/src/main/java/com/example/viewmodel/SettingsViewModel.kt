package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.model.BackupData
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.OutputStreamWriter
import java.io.InputStreamReader

class SettingsViewModel(
    private val repository: AppRepository,
    private val moshi: Moshi
) : ViewModel() {

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val exercises = repository.allExercisesFlow.first()
                val meals = repository.allMealsFlow.first()
                val backupData = BackupData(exercises, meals)
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
                            repository.importData(backupData.exercises, backupData.meals)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
