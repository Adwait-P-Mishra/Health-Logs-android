package com.adprmi.healthLogs.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.core.content.edit

class PreferenceRepository(context: Context) {
    private val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    private val _onboardingCompleted = MutableStateFlow(hasCompletedOnboarding())
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted

    private val _isDarkMode = MutableStateFlow(getIsDarkMode())
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode

    private val _targetCalories = MutableStateFlow(getTargetCalories())
    val targetCalories: StateFlow<Int?> = _targetCalories

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean("has_completed_onboarding", false)
    }

    fun getIsDarkMode(): Boolean? {
        if (!prefs.contains("is_dark_mode")) return null
        return prefs.getBoolean("is_dark_mode", false)
    }

    fun setIsDarkMode(isDark: Boolean?) {
        if (isDark == null) {
            prefs.edit { remove("is_dark_mode") }
        } else {
            prefs.edit { putBoolean("is_dark_mode", isDark) }
        }
        _isDarkMode.value = isDark
    }

    fun getTargetCalories(): Int? {
        if (!prefs.contains("target_calories")) return null
        return prefs.getInt("target_calories", 0)
    }

    fun setTargetCalories(calories: Int?) {
        if (calories == null) {
            prefs.edit { remove("target_calories") }
        } else {
            prefs.edit { putInt("target_calories", calories) }
        }
        _targetCalories.value = calories
    }

}
