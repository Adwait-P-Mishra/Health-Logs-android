package com.adprmi.healthLogs.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AiProviderConfig(
    val name: String,             // display label, e.g. "My Gemini Key"
    val baseUrl: String,          // e.g. "https://generativelanguage.googleapis.com/v1beta/openai/"
    val apiKey: String?,          // nullable — local endpoints may not need one
    val model: String,            // e.g. "gemini-2.0-flash"
    val isLocal: Boolean = false  // true if configured via the local/self-hosted path
)

@JsonClass(generateAdapter = true)
data class CalorieEstimate(
    val calories: Int,
    val protein_g: Double? = null,
    val carbs_g: Double? = null,
    val fat_g: Double? = null,
    val assumptions: List<String> = emptyList()
)

sealed class CalorieUiState {
    object Idle : CalorieUiState()
    object Loading : CalorieUiState()
    data class Success(val estimate: CalorieEstimate, val prompt: String) : CalorieUiState()
    data class Error(val message: String) : CalorieUiState()
    object NotConfigured : CalorieUiState()  // AI setup was skipped
}

val AI_PRESETS = listOf(
    AiProviderConfig("Gemini", "https://generativelanguage.googleapis.com/v1beta/openai/", null, "gemini-1.5-flash"),
    AiProviderConfig("OpenAI", "https://api.openai.com/v1/", null, "gpt-4o-mini"),
    AiProviderConfig("Groq", "https://api.groq.com/openai/v1/", null, "llama-3.3-70b-versatile"),
    AiProviderConfig("OpenRouter", "https://openrouter.ai/api/v1/", null, "meta-llama/llama-3.3-70b-instruct"),
)
