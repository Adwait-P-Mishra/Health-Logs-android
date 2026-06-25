package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.ExerciseEntity
import com.example.data.MealEntity
import com.example.model.SearchKind
import com.example.model.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SearchViewModel : ViewModel() {
    val searchText = MutableStateFlow("")
    private val _results = MutableStateFlow<List<SearchResult>>(emptyList())
    val results: StateFlow<List<SearchResult>> = _results

    val placeholderText = "Search meals or exercises"

    fun updateResults(workoutLogs: List<ExerciseEntity>, mealLogs: List<MealEntity>) {
        val query = searchText.value.trim().lowercase()
        if (query.length < 2) {
            _results.value = emptyList()
            return
        }

        val map = mutableMapOf<String, SearchResult>()

        for (log in workoutLogs) {
            val title = log.exerciseName.trim()
            val normalized = title.lowercase()
            if (title.isNotEmpty() && normalized.contains(query)) {
                val existing = map[normalized]
                if (existing == null || log.date > existing.latestDate) {
                    map[normalized] = SearchResult(
                        kind = SearchKind.EXERCISE,
                        title = title,
                        latestDate = log.date
                    )
                }
            }
        }

        for (log in mealLogs) {
            val title = log.mealName.trim()
            val normalized = title.lowercase()
            if (title.isNotEmpty() && normalized.contains(query)) {
                val existing = map[normalized]
                if (existing == null || log.date > existing.latestDate) {
                    map[normalized] = SearchResult(
                        kind = SearchKind.MEAL,
                        title = title,
                        latestDate = log.date
                    )
                }
            }
        }

        val combined = map.values.sortedWith { r1, r2 ->
            if (r1.latestDate == r2.latestDate) {
                r1.title.compareTo(r2.title, ignoreCase = true)
            } else {
                r2.latestDate.compareTo(r1.latestDate) // Descending date
            }
        }

        _results.value = combined
    }

    fun clear() {
        searchText.value = ""
        _results.value = emptyList()
    }

}
