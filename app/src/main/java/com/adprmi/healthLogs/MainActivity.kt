package com.adprmi.healthLogs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adprmi.healthLogs.data.AppDatabase
import com.adprmi.healthLogs.data.AppRepository
import com.adprmi.healthLogs.data.CalorieRepository
import com.adprmi.healthLogs.data.PreferenceRepository
import com.adprmi.healthLogs.ui.components.MealEditorSheet
import com.adprmi.healthLogs.ui.components.WorkoutEditorSheet
import com.adprmi.healthLogs.ui.screens.*
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import com.adprmi.healthLogs.viewmodel.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Moshi
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        // Initialize Local Repositories and Factories
        val database = AppDatabase.getDatabase(this)
        val appRepository = AppRepository(database.exerciseDao(), database.mealDao(), database.weightDao())
        val preferenceRepository = PreferenceRepository(this, moshi)
        val calorieRepository = CalorieRepository(preferenceRepository, moshi)
        val factory = ViewModelFactory(appRepository, preferenceRepository, calorieRepository, moshi)

        setContent {
            val isDarkMode by preferenceRepository.isDarkMode.collectAsState()
            val useDarkTheme = isDarkMode ?: isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = useDarkTheme) {
                AppNavigation(
                    factory = factory,
                    isDarkTheme = useDarkTheme,
                    onToggleTheme = {
                        preferenceRepository.setIsDarkMode(!useDarkTheme)
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
fun AppNavigation(
    factory: ViewModelFactory,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    // Instantiate ViewModels
    val workoutViewModel: WorkoutViewModel = viewModel(factory = factory)
    val mealViewModel: MealViewModel = viewModel(factory = factory)
    val searchViewModel: SearchViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

    // Form Overlay States
    var showWorkoutEditor by remember { mutableStateOf(value = false) }
    var showMealEditor by remember { mutableStateOf(value = false) }

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier,
    ) {
        // 1. Splash Screen
        composable("splash") {
            val onboardingShown by settingsViewModel.aiOnboardingShown.collectAsState()
            SplashScreen(
                onSplashFinished = {
                    val destination = if (onboardingShown) "dashboard" else "ai_welcome"
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                },
            )
        }

        // AI Onboarding Screens
        composable("ai_welcome") {
            AiOnboardingWelcomeScreen(
                onSetUpAi = { navController.navigate("ai_endpoint_choice") },
                onSkip = {
                    settingsViewModel.setAiOnboardingShown(true)
                    navController.navigate("dashboard") {
                        popUpTo("ai_welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("ai_endpoint_choice") {
            AiEndpointChoiceScreen(
                onChoice = { isHosted -> navController.navigate("ai_config/$isHosted") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "ai_config/{isHosted}",
            arguments = listOf(navArgument("isHosted") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isHosted = backStackEntry.arguments?.getBoolean("isHosted") ?: true
            AiConfigScreen(
                viewModel = settingsViewModel,
                isHosted = isHosted,
                onSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("ai_welcome") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 2. Dashboard Screen
        composable("dashboard") {
            Box(modifier = Modifier.fillMaxSize()) {
                DashboardScreen(
                    workoutViewModel = workoutViewModel,
                    mealViewModel = mealViewModel,
                    searchViewModel = searchViewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigateToExerciseHistory = { name ->
                        navController.navigate("exercise_history/$name")
                    },
                    onNavigateToMealHistory = { name ->
                        navController.navigate("meal_history/$name")
                    },
                    onLogWorkoutClicked = {
                        workoutViewModel.startNewLog()
                        showWorkoutEditor = true
                    },
                    onLogMealClicked = {
                        mealViewModel.startNewLog()
                        showMealEditor = true
                    },
                    onEditWorkoutClicked = { entity ->
                        workoutViewModel.startEditLog(entity)
                        showWorkoutEditor = true
                    },
                    onNavigateToDailyWorkouts = { dateMillis ->
                        navController.navigate("daily_workouts/$dateMillis")
                    },
                    onNavigateToDailyMeals = { dateMillis ->
                        navController.navigate("daily_meals/$dateMillis")
                    },
                    onLogWorkoutWithTitle = { title ->
                        workoutViewModel.startNewLog(title)
                        showWorkoutEditor = true
                    },
                    onLogMealWithTitle = { title ->
                        mealViewModel.startNewLog(title)
                        showMealEditor = true
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onNavigateToWeightTrend = {
                        navController.navigate("weight_trend")
                    }
                )

                // Slide-in/out form animation overlays for pristine polish
                AnimatedVisibility(
                    visible = showWorkoutEditor,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    WorkoutEditorSheet(
                        viewModel = workoutViewModel,
                        onDismiss = { showWorkoutEditor = false },
                    )
                }

                AnimatedVisibility(
                    visible = showMealEditor,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    MealEditorSheet(
                        viewModel = mealViewModel,
                        onDismiss = { showMealEditor = false },
                    )
                }
            }
        }

        composable("weight_trend") {
            WeightTrendScreen(
                viewModel = workoutViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // 3. Settings Screen
        composable("settings") {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onBack = { navController.popBackStack() },
                onNavigateToAiSetup = {
                    val config = settingsViewModel.aiProviderConfig.value
                    if (config != null) {
                        navController.navigate("ai_config/${!config.isLocal}")
                    } else {
                        navController.navigate("ai_endpoint_choice")
                    }
                },
                onResetApp = {
                    navController.navigate("splash") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 4. Exercise History Screen
        composable(
            route = "exercise_history/{exerciseName}",
            arguments = listOf(navArgument("exerciseName") { type = NavType.StringType }),
        ) { backStackEntry ->
            val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: ""
            Box(modifier = Modifier.fillMaxSize()) {
                ExerciseHistoryScreen(
                    exerciseName = exerciseName,
                    viewModel = workoutViewModel,
                    onBack = { navController.popBackStack() },
                    onEditTriggered = { entity ->
                        workoutViewModel.startEditLog(entity)
                        showWorkoutEditor = true
                    },
                    onAddLogClicked = {
                        workoutViewModel.startNewLog(exerciseName)
                        showWorkoutEditor = true
                    },
                )

                AnimatedVisibility(
                    visible = showWorkoutEditor,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    WorkoutEditorSheet(
                        viewModel = workoutViewModel,
                        onDismiss = { showWorkoutEditor = false },
                    )
                }
            }
        }

        // 5. Meal History Screen
        composable(
            route = "meal_history/{mealName}",
            arguments = listOf(navArgument("mealName") { type = NavType.StringType }),
        ) { backStackEntry ->
            val mealName = backStackEntry.arguments?.getString("mealName") ?: ""
            Box(modifier = Modifier.fillMaxSize()) {
                MealHistoryScreen(
                    mealName = mealName,
                    viewModel = mealViewModel,
                    onBack = { navController.popBackStack() },
                    onEditTriggered = { entity ->
                        mealViewModel.startEditLog(entity)
                        showMealEditor = true
                    },
                    onAddLogClicked = {
                        mealViewModel.startNewLog(mealName)
                        showMealEditor = true
                    },
                )

                AnimatedVisibility(
                    visible = showMealEditor,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    MealEditorSheet(
                        viewModel = mealViewModel,
                        onDismiss = { showMealEditor = false },
                    )
                }
            }
        }

        // 6. Daily Workouts Screen
        composable(
            route = "daily_workouts/{dateMillis}",
            arguments = listOf(navArgument("dateMillis") { type = NavType.LongType }),
        ) { backStackEntry ->
            val dateMillis = backStackEntry.arguments?.getLong("dateMillis") ?: 0L
            Box(modifier = Modifier.fillMaxSize()) {
                DailyWorkoutScreen(
                    dateMillis = dateMillis,
                    viewModel = workoutViewModel,
                    onBack = { navController.popBackStack() },
                    onEditTriggered = { entity ->
                        workoutViewModel.startEditLog(entity)
                        showWorkoutEditor = true
                    },
                    onNavigateToWeightTrend = {
                        navController.navigate("weight_trend")
                    }
                )

                AnimatedVisibility(
                    visible = showWorkoutEditor,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    WorkoutEditorSheet(
                        viewModel = workoutViewModel,
                        onDismiss = { showWorkoutEditor = false },
                    )
                }
            }
        }

        // 7. Daily Meals Screen
        composable(
            route = "daily_meals/{dateMillis}",
            arguments = listOf(navArgument("dateMillis") { type = NavType.LongType }),
        ) { backStackEntry ->
            val dateMillis = backStackEntry.arguments?.getLong("dateMillis") ?: 0L
            Box(modifier = Modifier.fillMaxSize()) {
                DailyMealScreen(
                    dateMillis = dateMillis,
                    viewModel = mealViewModel,
                    onBack = { navController.popBackStack() },
                    onEditTriggered = { entity ->
                        mealViewModel.startEditLog(entity)
                        showMealEditor = true
                    },
                )

                AnimatedVisibility(
                    visible = showMealEditor,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    MealEditorSheet(
                        viewModel = mealViewModel,
                        onDismiss = { showMealEditor = false },
                    )
                }
            }
        }
    }
}
