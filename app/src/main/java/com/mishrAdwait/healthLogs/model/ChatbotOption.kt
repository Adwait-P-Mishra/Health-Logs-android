package com.mishrAdwait.healthLogs.model

enum class ChatbotOption(
    val displayName: String,
    val appPackage: String?,
    val webUrl: String
) {
    CHATGPT("ChatGPT", "com.openai.chatgpt", "https://chatgpt.com/"),
    GEMINI("Google Gemini", "com.google.android.apps.bard", "https://gemini.google.com/app"),
    CLAUDE("Claude", "ai.claude.app", "https://claude.ai/new"),
    PERPLEXITY("Perplexity", "ai.perplexity.app", "https://www.perplexity.ai/"),
    GENERIC_SHARE("Generic Share / System Chooser", null, "")
}
