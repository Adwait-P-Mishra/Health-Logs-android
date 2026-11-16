package com.mishrAdwait.healthLogs.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mishrAdwait.healthLogs.data.ExerciseEntity
import com.mishrAdwait.healthLogs.data.MealEntity
import com.mishrAdwait.healthLogs.model.WorkoutSet
import com.mishrAdwait.healthLogs.model.WeightUnit
import com.mishrAdwait.healthLogs.ui.theme.MyApplicationTheme
import com.mishrAdwait.healthLogs.ui.theme.ThemePreviews
import com.mishrAdwait.healthLogs.util.DateUtils
import com.mishrAdwait.healthLogs.viewmodel.MealViewModel
import com.mishrAdwait.healthLogs.viewmodel.SearchViewModel
import com.mishrAdwait.healthLogs.viewmodel.SettingsViewModel
import com.mishrAdwait.healthLogs.viewmodel.WorkoutViewModel
import java.util.Date

@Composable
fun DashboardScreen(
    workoutViewModel: WorkoutViewModel,
    mealViewModel: MealViewModel,
    searchViewModel: SearchViewModel,
    settingsViewModel: SettingsViewModel,
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
    onNavigateToWeightTrend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedDate by workoutViewModel.selectedDate.collectAsState()
    val todayWorkouts by workoutViewModel.todayLogs.collectAsState()
    val allWeights by workoutViewModel.allWeights.collectAsState()
    
    val weightForSelectedDate = remember(allWeights, selectedDate) {
        val endOfSelectedDay = DateUtils.getEndOfDay(selectedDate).time
        allWeights.filter { it.date <= endOfSelectedDay }
            .maxByOrNull { it.date }
    }
    
    val weightUnit by workoutViewModel.weightUnit.collectAsState()
    val todayMeals by mealViewModel.todayLogs.collectAsState()

    val totalCalories by mealViewModel.todayCaloriesSum.collectAsState()
    val totalProtein by mealViewModel.todayProteinSum.collectAsState()
    val totalCarbs by mealViewModel.todayCarbsSum.collectAsState()
    val totalFat by mealViewModel.todayFatSum.collectAsState()

    val targetCalories by settingsViewModel.targetCalories.collectAsState()

    val searchQuery by searchViewModel.searchText.collectAsState()
    val searchResults by searchViewModel.results.collectAsState()
    val allWorkoutsForSearch by workoutViewModel.allLogs.collectAsState()
    val allMealsForSearch by mealViewModel.allLogs.collectAsState()

    val selectedChatbot by mealViewModel.selectedChatbot.collectAsState()

    // Sync selected dates between ViewModels
    LaunchedEffect(selectedDate) {
        mealViewModel.changeSelectedDate(selectedDate)
    }

    DashboardContent(
        selectedDate = selectedDate,
        onDateSelected = { workoutViewModel.changeSelectedDate(it) },
        todayWorkouts = todayWorkouts,
        todayMeals = todayMeals,
        userWeightKg = weightForSelectedDate?.weightKg,
        weightUnit = weightUnit,
        totalCalories = totalCalories,
        targetCalories = targetCalories,
        totalProtein = totalProtein,
        totalCarbs = totalCarbs,
        totalFat = totalFat,
        searchQuery = searchQuery,
        onSearchQueryChange = {
            searchViewModel.searchText.value = it
            searchViewModel.updateResults(allWorkoutsForSearch, allMealsForSearch)
        },
        searchResults = searchResults,
        placeholderText = searchViewModel.placeholderText,
        onClearSearch = { searchViewModel.clear() },
        onNavigateToExerciseHistory = onNavigateToExerciseHistory,
        onNavigateToMealHistory = onNavigateToMealHistory,
        onLogWorkoutClicked = onLogWorkoutClicked,
        onLogMealClicked = onLogMealClicked,
        onEditWorkoutClicked = onEditWorkoutClicked,
        onNavigateToDailyWorkouts = onNavigateToDailyWorkouts,
        onNavigateToDailyMeals = onNavigateToDailyMeals,
        onLogWorkoutWithTitle = onLogWorkoutWithTitle,
        onLogMealWithTitle = onLogMealWithTitle,
        onNavigateToSettings = onNavigateToSettings,
        onSetUserWeightKg = { workoutViewModel.setUserWeightKg(it, selectedDate) },
        onNavigateToWeightTrend = onNavigateToWeightTrend,
        selectedChatbot = selectedChatbot,
        onUpdateMeal = { mealViewModel.updateMeal(it) },
        modifier = modifier
    )
}

@ThemePreviews
@Composable
fun DashboardScreenPreview() {
    MyApplicationTheme {
        DashboardContent(
            selectedDate = Date(),
            onDateSelected = {},
            todayWorkouts = listOf(
                ExerciseEntity(
                    exerciseName = "Bench Press",
                    sets = listOf(WorkoutSet(weight = "225", reps = "5")),
                    date = System.currentTimeMillis()
                ),
                ExerciseEntity(
                    exerciseName = "Squat",
                    sets = listOf(WorkoutSet(weight = "315", reps = "3")),
                    date = System.currentTimeMillis()
                )
            ),
            todayMeals = listOf(
                MealEntity(
                    mealName = "Breakfast",
                    calories = 600,
                    protein = 30.0,
                    carbs = 70.0,
                    fat = 20.0,
                    date = System.currentTimeMillis()
                )
            ),
            userWeightKg = 70.0,
            weightUnit = WeightUnit.KG,
            totalCalories = 600,
            targetCalories = 2400,
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
            onNavigateToSettings = {},
            onSetUserWeightKg = {},
            onNavigateToWeightTrend = {}
        )
    }
}
