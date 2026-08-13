package com.adprmi.healthLogs.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adprmi.healthLogs.data.ExerciseEntity
import com.adprmi.healthLogs.data.MealEntity
import com.adprmi.healthLogs.model.SearchKind
import com.adprmi.healthLogs.model.SearchResult
import com.adprmi.healthLogs.ui.components.AddButtonsRow
import com.adprmi.healthLogs.ui.components.ExpandableCalendarView
import com.adprmi.healthLogs.ui.components.SearchBar
import com.adprmi.healthLogs.ui.components.SearchResultsList
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    todayWorkouts: List<ExerciseEntity>,
    todayMeals: List<MealEntity>,
    totalCalories: Int,
    targetCalories: Int?,
    totalProtein: Double,
    totalCarbs: Double,
    totalFat: Double,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<SearchResult>,
    placeholderText: String,
    onClearSearch: () -> Unit,
    onNavigateToExerciseHistory: (String) -> Unit,
    onNavigateToMealHistory: (String) -> Unit,
    onLogWorkoutClicked: () -> Unit,
    onLogMealClicked: () -> Unit,
    onEditWorkoutClicked: (ExerciseEntity) -> Unit,
    onNavigateToDailyWorkouts: (Long) -> Unit,
    onNavigateToDailyMeals: (Long) -> Unit,
    onLogWorkoutWithTitle: (String) -> Unit,
    onLogMealWithTitle: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Health Logs",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {},
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Open settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )
            }
        },
        modifier = modifier.testTag("dashboard_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = placeholderText,
                onClear = onClearSearch
            )

            if (searchQuery.trim().length >= 2) {
                SearchResultsList(
                    results = searchResults,
                    onResultClick = { result ->
                        onClearSearch()
                        focusManager.clearFocus()
                        if (result.kind == SearchKind.EXERCISE) {
                            onNavigateToExerciseHistory(result.title)
                        } else {
                            onNavigateToMealHistory(result.title)
                        }
                    },
                    onAddClick = { result ->
                        onClearSearch()
                        focusManager.clearFocus()
                        if (result.kind == SearchKind.EXERCISE) {
                            onLogWorkoutWithTitle(result.title)
                        } else {
                            onLogMealWithTitle(result.title)
                        }
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    ExpandableCalendarView(
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                    )

                    AddButtonsRow(
                        onLogWorkoutClicked = onLogWorkoutClicked,
                        onLogMealClicked = onLogMealClicked
                    )

                    WorkoutsSection(
                        todayWorkouts = todayWorkouts,
                        onEditLog = onEditWorkoutClicked,
                        onLogWorkoutClicked = onLogWorkoutClicked
                    ) { onNavigateToDailyWorkouts(selectedDate.time) }

                    NutritionSection(
                        totalCalories = totalCalories,
                        targetCalories = targetCalories,
                        totalProtein = totalProtein,
                        totalCarbs = totalCarbs,
                        totalFat = totalFat,
                        todayMeals = todayMeals,
                        onLogMealClicked = onLogMealClicked,
                    ) { onNavigateToDailyMeals(selectedDate.time) }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    MyApplicationTheme {
        DashboardContent(
            selectedDate = Date(),
            onDateSelected = {},
            todayWorkouts = emptyList(),
            todayMeals = emptyList(),
            totalCalories = 600,
            targetCalories = null,
            totalProtein = 30.0,
            totalCarbs = 70.0,
            totalFat = 20.0,
            searchQuery = "",
            onSearchQueryChange = {},
            searchResults = emptyList(),
            placeholderText = "Search exercises or meals...",
            onClearSearch = {},
            onNavigateToExerciseHistory = {},
            onNavigateToMealHistory = {},
            onLogWorkoutClicked = {},
            onLogMealClicked = {},
            onEditWorkoutClicked = {},
            onNavigateToDailyWorkouts = {},
            onNavigateToDailyMeals = {},
            onLogWorkoutWithTitle = {},
            onLogMealWithTitle = {},
            onNavigateToSettings = {}
        )
    }
}
