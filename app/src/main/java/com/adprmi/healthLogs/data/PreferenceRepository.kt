package com.adprmi.healthLogs.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.adprmi.healthLogs.model.AiProviderConfig
import com.adprmi.healthLogs.model.HeightUnit
import com.adprmi.healthLogs.model.WeightUnit
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.core.content.edit

class PreferenceRepository(context: Context, private val moshi: Moshi) {
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

    private val _aiApiKey = MutableStateFlow(getAiApiKey())
    val aiApiKey: StateFlow<String?> = _aiApiKey

    private val _aiBaseUrl = MutableStateFlow(getAiBaseUrl())
    val aiBaseUrl: StateFlow<String?> = _aiBaseUrl

    private val _aiModelName = MutableStateFlow(getAiModelName())
    val aiModelName: StateFlow<String?> = _aiModelName

    private val aiConfigAdapter = moshi.adapter(AiProviderConfig::class.java)

    private val _aiProviderConfig = MutableStateFlow(getAiProviderConfig())
    val aiProviderConfig: StateFlow<AiProviderConfig?> = _aiProviderConfig

    private val _aiOnboardingShown = MutableStateFlow(isAiOnboardingShown())
    val aiOnboardingShown: StateFlow<Boolean> = _aiOnboardingShown

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

    fun getAiApiKey(): String? {
        return encryptedPrefs.getString("ai_api_key", null)
    }

    fun setAiApiKey(apiKey: String?) {
        if (apiKey == null) {
            encryptedPrefs.edit { remove("ai_api_key") }
        } else {
            encryptedPrefs.edit { putString("ai_api_key", apiKey) }
        }
        _aiApiKey.value = apiKey
    }

    fun getAiBaseUrl(): String? {
        return prefs.getString("ai_base_url", "https://api.openai.com/v1")
    }

    fun setAiBaseUrl(baseUrl: String?) {
        if (baseUrl == null) {
            prefs.edit { remove("ai_base_url") }
        } else {
            prefs.edit { putString("ai_base_url", baseUrl) }
        }
        _aiBaseUrl.value = baseUrl
    }

    fun getAiModelName(): String? {
        return prefs.getString("ai_model_name", "gpt-4o-mini")
    }

    fun setAiModelName(modelName: String?) {
        if (modelName == null) {
            prefs.edit { remove("ai_model_name") }
        } else {
            prefs.edit { putString("ai_model_name", modelName) }
        }
        _aiModelName.value = modelName
    }

    fun getAiProviderConfig(): AiProviderConfig? {
        val json = encryptedPrefs.getString("ai_provider_config", null) ?: return null
        return try {
            aiConfigAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    fun setAiProviderConfig(config: AiProviderConfig?) {
        if (config == null) {
            encryptedPrefs.edit { remove("ai_provider_config") }
        } else {
            val json = aiConfigAdapter.toJson(config)
            encryptedPrefs.edit { putString("ai_provider_config", json) }
        }
        _aiProviderConfig.value = config
    }

    fun isAiOnboardingShown(): Boolean {
        return prefs.getBoolean("ai_onboarding_shown", false)
    }

    fun setAiOnboardingShown(shown: Boolean) {
        prefs.edit { putBoolean("ai_onboarding_shown", shown) }
        _aiOnboardingShown.value = shown
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
        
        // Reset all state flows to defaults
        _onboardingCompleted.value = false
        _isDarkMode.value = null
        _targetCalories.value = null
        _weightUnit.value = WeightUnit.KG
        _userWeightKg.value = null
        _userHeightCm.value = null
        _userGender.value = null
        _heightUnit.value = HeightUnit.CM
        _aiApiKey.value = null
        _aiBaseUrl.value = "https://api.openai.com/v1"
        _aiModelName.value = "gpt-4o-mini"
        _aiProviderConfig.value = null
        _aiOnboardingShown.value = false
    }
}
