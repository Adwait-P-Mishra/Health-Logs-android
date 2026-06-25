package com.example.model

data class SearchResult(
    val kind: SearchKind,
    val title: String,
    val latestDate: Long
) {
    val id: String get() = "${kind.name}-${title.lowercase()}"
}

enum class SearchKind {
    EXERCISE,
    MEAL
}
