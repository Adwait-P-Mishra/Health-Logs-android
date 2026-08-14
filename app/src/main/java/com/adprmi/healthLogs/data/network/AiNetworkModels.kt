package com.adprmi.healthLogs.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @Json(name = "response_format") val responseFormat: ResponseFormat? = null
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(val type: String)

@JsonClass(generateAdapter = true)
data class ChatMessage(val role: String, val content: String)

@JsonClass(generateAdapter = true)
data class ChatResponse(val choices: List<Choice>)

@JsonClass(generateAdapter = true)
data class Choice(val message: ChatMessage)
