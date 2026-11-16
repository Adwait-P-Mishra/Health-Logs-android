package com.mishrAdwait.healthLogs.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mishrAdwait.healthLogs.model.ChatbotOption
import com.mishrAdwait.healthLogs.model.HeightUnit
import com.mishrAdwait.healthLogs.model.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.core.content.edit

class PreferenceRepository(context: Context) {
    private val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _onboardingCompleted = MutableStateFlow(hasCompletedOnboarding())
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted

    private val _isDarkMode = MutableStateFlow(getIsDarkMode())
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode

    private val _targetCalories = MutableStateFlow(getTargetCalories())
    val targetCalories: StateFlow<Int?> = _targetCalories

    private val _weightUnit = MutableStateFlow(getWeightUnit())
    val weightUnit: StateFlow<WeightUnit> = _weightUnit

    private val _userWeightKg = MutableStateFlow(getUserWeightKg())
    val userWeightKg: StateFlow<Double?> = _userWeightKg

    private val _userHeightCm = MutableStateFlow(getUserHeightCm())
    val userHeightCm: StateFlow<Double?> = _userHeightCm

    private val _userGender = MutableStateFlow(getUserGender())
    val userGender: StateFlow<String?> = _userGender

    private val _heightUnit = MutableStateFlow(getHeightUnit())
    val heightUnit: StateFlow<HeightUnit> = _heightUnit

    private val _selectedChatbot = MutableStateFlow(getSelectedChatbot())
    val selectedChatbot: StateFlow<ChatbotOption> = _selectedChatbot

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

    fun getWeightUnit(): WeightUnit {
        val name = prefs.getString("weight_unit", WeightUnit.KG.name)
        return try {
            WeightUnit.valueOf(name ?: WeightUnit.KG.name)
        } catch (e: Exception) {
            WeightUnit.KG
        }
    }

    fun setWeightUnit(unit: WeightUnit) {
        prefs.edit { putString("weight_unit", unit.name) }
        _weightUnit.value = unit
    }

    fun getUserWeightKg(): Double? {
        if (!prefs.contains("user_weight_kg")) return null
        return prefs.getFloat("user_weight_kg", 0.0f).toDouble()
    }

    fun setUserWeightKg(weight: Double?) {
        if (weight == null) {
            prefs.edit { remove("user_weight_kg") }
        } else {
            prefs.edit { putFloat("user_weight_kg", weight.toFloat()) }
        }
        _userWeightKg.value = weight
    }

    fun getUserHeightCm(): Double? {
        if (!prefs.contains("user_height_cm")) return null
        return prefs.getFloat("user_height_cm", 0.0f).toDouble()
    }

    fun setUserHeightCm(height: Double?) {
        if (height == null) {
            prefs.edit { remove("user_height_cm") }
        } else {
            prefs.edit { putFloat("user_height_cm", height.toFloat()) }
        }
        _userHeightCm.value = height
    }

    fun getUserGender(): String? {
        return prefs.getString("user_gender", null)
    }

    fun setUserGender(gender: String?) {
        if (gender == null) {
            prefs.edit { remove("user_gender") }
        } else {
            prefs.edit { putString("user_gender", gender) }
        }
        _userGender.value = gender
    }

    fun getHeightUnit(): HeightUnit {
        val name = prefs.getString("height_unit", HeightUnit.CM.name)
        return try {
            HeightUnit.valueOf(name ?: HeightUnit.CM.name)
        } catch (e: Exception) {
            HeightUnit.CM
        }
    }

    fun setHeightUnit(unit: HeightUnit) {
        prefs.edit { putString("height_unit", unit.name) }
        _heightUnit.value = unit
    }

    fun getSelectedChatbot(): ChatbotOption {
        val name = prefs.getString("selected_chatbot", ChatbotOption.GENERIC_SHARE.name)
        return try {
            ChatbotOption.valueOf(name ?: ChatbotOption.GENERIC_SHARE.name)
        } catch (e: Exception) {
            ChatbotOption.GENERIC_SHARE
        }
    }

    fun setSelectedChatbot(option: ChatbotOption) {
        prefs.edit { putString("selected_chatbot", option.name) }
        _selectedChatbot.value = option
    }

    fun clearDataOnly() {
        prefs.edit {
            remove("target_calories")
            remove("user_weight_kg")
            remove("user_height_cm")
            remove("user_gender")
        }
        _targetCalories.value = null
        _userWeightKg.value = null
        _userHeightCm.value = null
        _userGender.value = null
    }

    fun clearAll() {
        prefs.edit { clear() }
        encryptedPrefs.edit { clear() }
        
        // Reset state flows to defaults
        _onboardingCompleted.value = false
        _isDarkMode.value = null
        _targetCalories.value = null
        _weightUnit.value = WeightUnit.KG
        _userWeightKg.value = null
        _userHeightCm.value = null
        _userGender.value = null
        _heightUnit.value = HeightUnit.CM
        _selectedChatbot.value = ChatbotOption.GENERIC_SHARE
    }
}
