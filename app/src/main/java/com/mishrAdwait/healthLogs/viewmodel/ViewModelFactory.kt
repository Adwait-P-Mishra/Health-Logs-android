package com.mishrAdwait.healthLogs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mishrAdwait.healthLogs.data.AppRepository
import com.mishrAdwait.healthLogs.data.PreferenceRepository
import com.squareup.moshi.Moshi

class ViewModelFactory(
    private val appRepository: AppRepository,
    private val preferenceRepository: PreferenceRepository,
    private val moshi: Moshi
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(WorkoutViewModel::class.java) -> {
                WorkoutViewModel(appRepository, preferenceRepository) as T
            }
            modelClass.isAssignableFrom(MealViewModel::class.java) -> {
                MealViewModel(appRepository, preferenceRepository) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel() as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(appRepository, preferenceRepository, moshi) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
