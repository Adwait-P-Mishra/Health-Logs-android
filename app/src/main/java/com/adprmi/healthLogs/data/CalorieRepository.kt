package com.adprmi.healthLogs.data

import com.adprmi.healthLogs.data.network.ChatMessage
import com.adprmi.healthLogs.data.network.ChatRequest
import com.adprmi.healthLogs.data.network.GenericAiClient
import com.adprmi.healthLogs.data.network.ResponseFormat
import com.adprmi.healthLogs.model.AiProviderConfig
import com.adprmi.healthLogs.model.CalorieEstimate
import com.adprmi.healthLogs.model.BatchCalorieEstimate
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CalorieRepository(
    private val preferenceRepository: PreferenceRepository,
    private val moshi: Moshi
) {
    private val calorieAdapter = moshi.adapter(CalorieEstimate::class.java)
    private val batchAdapter = moshi.adapter(BatchCalorieEstimate::class.java)

    suspend fun estimateMeal(description: String, userWeightKg: Double?): Result<Pair<CalorieEstimate, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val config = preferenceRepository.getAiProviderConfig()
                ?: throw IllegalStateException("No AI provider configured")

            val height = preferenceRepository.getUserHeightCm()
            val gender = preferenceRepository.getUserGender()
            
            val profileInfo = if (height != null || gender != null || userWeightKg != null) {
                "\nUser Profile: " + listOfNotNull(
                    gender?.let { "Gender: $it" },
                    height?.let { "Height: ${it.toInt()} cm" },
                    userWeightKg?.let { "Weight: $it kg" }
                ).joinToString(", ")
            } else ""

            val fullPrompt = description + profileInfo
            val api = GenericAiClient.buildApi(config.baseUrl.trim())
            val request = ChatRequest(
                model = config.model.trim(),
                messages = listOf(
                    ChatMessage("system", "You are a nutrition estimation engine. Return only JSON: {\"calories\": int, \"protein_g\": number, \"carbs_g\": number, \"fat_g\": number, \"assumptions\": [string]}. No prose, no markdown fences."),
                    ChatMessage("user", fullPrompt)
                ),
                responseFormat = ResponseFormat("json_object")
            )

            val apiKey = config.apiKey?.trim()
            val authHeader = if (apiKey.isNullOrBlank()) null else "Bearer $apiKey"
            val response = api.chatCompletion(authHeader, request)
            val content = response.choices.first().message.content
            val estimate = calorieAdapter.fromJson(stripMarkdownFences(content)) ?: throw Exception("Failed to parse estimate")
            estimate to fullPrompt
        }
    }

    suspend fun estimateExercise(
        description: String,
        userWeightKg: Double?,
        durationMin: Int?
    ): Result<Pair<CalorieEstimate, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val config = preferenceRepository.getAiProviderConfig()
                ?: throw IllegalStateException("No AI provider configured")

            val weight = userWeightKg ?: throw IllegalStateException("User weight is required for exercise estimation")
            val height = preferenceRepository.getUserHeightCm()
            val gender = preferenceRepository.getUserGender()
            
            val profileInfo = if (height != null || gender != null) {
                ", profile: " + listOfNotNull(
                    gender?.let { "gender: $it" },
                    height?.let { "height: ${it.toInt()} cm" }
                ).joinToString(", ")
            } else ""

            val api = GenericAiClient.buildApi(config.baseUrl.trim())
            val durationText = durationMin?.let { " for $it minutes" } ?: ""
            val userPrompt = "$description, user weight: $weight kg$durationText$profileInfo"

            val request = ChatRequest(
                model = config.model.trim(),
                messages = listOf(
                    ChatMessage("system", "You are an exercise calorie burn estimation engine. Use MET values. Return only JSON: {\"calories\": int, \"assumptions\": [string]}. No prose, no markdown fences."),
                    ChatMessage("user", userPrompt)
                ),
                responseFormat = ResponseFormat("json_object")
            )

            val apiKey = config.apiKey?.trim()
            val authHeader = if (apiKey.isNullOrBlank()) null else "Bearer $apiKey"
            val response = api.chatCompletion(authHeader, request)
            val content = response.choices.first().message.content
            val estimate = calorieAdapter.fromJson(stripMarkdownFences(content)) ?: throw Exception("Failed to parse estimate")
            estimate to userPrompt
        }
    }

    suspend fun estimateBatchMeals(
        mealDescriptions: List<String>,
        userWeightKg: Double?
    ): Result<Pair<BatchCalorieEstimate, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val config = preferenceRepository.getAiProviderConfig()
                ?: throw IllegalStateException("No AI provider configured")

            val height = preferenceRepository.getUserHeightCm()
            val gender = preferenceRepository.getUserGender()
            
            val profileInfo = if (height != null || gender != null || userWeightKg != null) {
                "\nUser Profile: " + listOfNotNull(
                    gender?.let { "Gender: $it" },
                    height?.let { "Height: ${it.toInt()} cm" },
                    userWeightKg?.let { "Weight: $it kg" }
                ).joinToString(", ")
            } else ""

            val mealsList = mealDescriptions.mapIndexed { i, desc -> "${i + 1}. $desc" }.joinToString("\n")
            val fullPrompt = "Estimate nutrition for these meals:\n$mealsList$profileInfo"

            val api = GenericAiClient.buildApi(config.baseUrl.trim())
            val request = ChatRequest(
                model = config.model.trim(),
                messages = listOf(
                    ChatMessage("system", "You are a nutrition estimation engine. Return JSON: {\"estimates\": [{\"calories\": int, \"protein_g\": number, \"carbs_g\": number, \"fat_g\": number}], \"assumptions\": [string]}. Order estimates exactly as input."),
                    ChatMessage("user", fullPrompt)
                ),
                responseFormat = ResponseFormat("json_object")
            )

            val apiKey = config.apiKey?.trim()
            val authHeader = if (apiKey.isNullOrBlank()) null else "Bearer $apiKey"
            val response = api.chatCompletion(authHeader, request)
            val content = response.choices.first().message.content
            val estimate = batchAdapter.fromJson(stripMarkdownFences(content)) ?: throw Exception("Failed to parse batch estimate")
            estimate to fullPrompt
        }
    }

    suspend fun estimateBatchExercises(
        exerciseDescriptions: List<String>,
        userWeightKg: Double?
    ): Result<Pair<BatchCalorieEstimate, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val config = preferenceRepository.getAiProviderConfig()
                ?: throw IllegalStateException("No AI provider configured")

            val weight = userWeightKg ?: throw IllegalStateException("User weight is required for exercise estimation")
            val height = preferenceRepository.getUserHeightCm()
            val gender = preferenceRepository.getUserGender()
            
            val profileInfo = if (height != null || gender != null) {
                ", profile: " + listOfNotNull(
                    gender?.let { "gender: $it" },
                    height?.let { "height: ${it.toInt()} cm" }
                ).joinToString(", ")
            } else ""

            val exercisesList = exerciseDescriptions.mapIndexed { i, desc -> "${i + 1}. $desc" }.joinToString("\n")
            val userPrompt = "Estimate calorie burn for these exercises:\n$exercisesList\nUser weight: $weight kg$profileInfo"

            val api = GenericAiClient.buildApi(config.baseUrl.trim())
            val request = ChatRequest(
                model = config.model.trim(),
                messages = listOf(
                    ChatMessage("system", "You are an exercise calorie burn estimation engine. Return JSON: {\"estimates\": [{\"calories\": int}], \"assumptions\": [string]}. Order estimates exactly as input."),
                    ChatMessage("user", userPrompt)
                ),
                responseFormat = ResponseFormat("json_object")
            )

            val apiKey = config.apiKey?.trim()
            val authHeader = if (apiKey.isNullOrBlank()) null else "Bearer $apiKey"
            val response = api.chatCompletion(authHeader, request)
            val content = response.choices.first().message.content
            val estimate = batchAdapter.fromJson(stripMarkdownFences(content)) ?: throw Exception("Failed to parse batch estimate")
            estimate to userPrompt
        }
    }

    suspend fun testConnection(config: AiProviderConfig): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val api = GenericAiClient.buildApi(config.baseUrl.trim())
            val request = ChatRequest(
                model = config.model.trim(),
                messages = listOf(
                    ChatMessage("user", "Hello, are you there? Reply with exactly: OK")
                )
            )
            val apiKey = config.apiKey?.trim()
            val authHeader = if (apiKey.isNullOrBlank()) null else "Bearer $apiKey"
            val response = api.chatCompletion(authHeader, request)
            response.choices.isNotEmpty() && response.choices.first().message.content.contains("OK", ignoreCase = true)
        }
    }

    private fun stripMarkdownFences(text: String): String =
        text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
}
