package com.example.ui.screens

import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.theme.MyApplicationTheme
import com.example.data.ExerciseEntity
import com.example.data.MealEntity
import com.example.model.SearchKind
import com.example.model.SearchResult
import com.example.model.WorkoutSet
import com.example.ui.components.ExpandableCalendarView
import com.example.ui.components.SearchBar
import com.example.ui.components.SearchResultsList
import com.example.util.DateUtils
import com.example.viewmodel.MealViewModel
import com.example.viewmodel.SearchViewModel
import com.example.viewmodel.SettingsViewModel
import com.example.viewmodel.WorkoutViewModel
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    workoutViewModel: WorkoutViewModel,
    mealViewModel: MealViewModel,
    searchViewModel: SearchViewModel,
    settingsViewModel: SettingsViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateToExerciseHistory: (String) -> Unit,
    onNavigateToMealHistory: (String) -> Unit,
    onLogWorkoutClicked: () -> Unit,
    onLogMealClicked: () -> Unit,
    onEditWorkoutClicked: (ExerciseEntity) -> Unit,
    onNavigateToDailyWorkouts: (Long) -> Unit,
    onNavigateToDailyMeals: (Long) -> Unit,
    onLogWorkoutWithTitle: (String) -> Unit,
    onLogMealWithTitle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedDate by workoutViewModel.selectedDate.collectAsState()
    val todayWorkouts by workoutViewModel.todayLogs.collectAsState()
    val todayMeals by mealViewModel.todayLogs.collectAsState()

    val totalCalories by mealViewModel.todayCaloriesSum.collectAsState()
    val totalProtein by mealViewModel.todayProteinSum.collectAsState()
    val totalCarbs by mealViewModel.todayCarbsSum.collectAsState()
    val totalFat by mealViewModel.todayFatSum.collectAsState()

    val searchQuery by searchViewModel.searchText.collectAsState()
    val searchResults by searchViewModel.results.collectAsState()
    val allWorkoutsForSearch by workoutViewModel.allLogs.collectAsState()
    val allMealsForSearch by mealViewModel.allLogs.collectAsState()

    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let { settingsViewModel.exportData(context, it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { settingsViewModel.importData(context, it) }
    }

    // Sync selected dates between ViewModels
    LaunchedEffect(selectedDate) {
        mealViewModel.changeSelectedDate(selectedDate)
    }

    DashboardContent(
        selectedDate = selectedDate,
        onDateSelected = { workoutViewModel.changeSelectedDate(it) },
        todayWorkouts = todayWorkouts,
        todayMeals = todayMeals,
        totalCalories = totalCalories,
        totalProtein = totalProtein,
        totalCarbs = totalCarbs,
        totalFat = totalFat,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
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
        onImportData = { importLauncher.launch(arrayOf("application/json", "application/octet-stream")) },
        onExportData = { exportLauncher.launch("training_nutrition_backup.json") },
        onClearData = { settingsViewModel.clearAllData() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    todayWorkouts: List<ExerciseEntity>,
    todayMeals: List<MealEntity>,
    totalCalories: Int,
    totalProtein: Double,
    totalCarbs: Double,
    totalFat: Double,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
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
    onImportData: () -> Unit,
    onExportData: () -> Unit,
    onClearData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showClearConfirm by remember { mutableStateOf(value = false) }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Data") },
            text = { Text("Are you sure you want to delete all exercise and meal logs? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearData()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Close drawer")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text(if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onToggleTheme()
                    },
                    icon = {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Import Data") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onImportData()
                    },
                    icon = { Icon(Icons.Default.FileDownload, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Export Data") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onExportData()
                    },
                    icon = { Icon(Icons.Default.FileUpload, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    label = { Text("Clear All Data", color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showClearConfirm = true
                    },
                    icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Dashboard",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Open drawer")
                            }
                        },
                        actions = {
                            Surface(
                                onClick = onLogWorkoutClicked,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                color = Color.Transparent,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = "Log Workout",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Surface(
                                onClick = onLogMealClicked,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                color = Color.Transparent,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fastfood,
                                        contentDescription = "Log Meal",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
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
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )

                        WorkoutsSection(
                            todayWorkouts = todayWorkouts,
                            onEditLog = onEditWorkoutClicked,
                        ) { onNavigateToDailyWorkouts(selectedDate.time) }

                        NutritionSection(
                            totalCalories = totalCalories,
                            totalProtein = totalProtein,
                            totalCarbs = totalCarbs,
                            totalFat = totalFat,
                            todayMeals = todayMeals,
                        ) { onNavigateToDailyMeals(selectedDate.time) }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutsSection(
    todayWorkouts: List<ExerciseEntity>,
    onEditLog: (ExerciseEntity) -> Unit,
    onHeaderClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)
            .clickable { onHeaderClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Text(
                text = "Today's Workouts",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (todayWorkouts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.3f
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No workouts logged today",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Column {
                    todayWorkouts.forEach { log ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = log.exerciseName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 200.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onEditLog(log) },
                                        modifier = Modifier.size(36.dp)
                                            .testTag("edit_workout_log_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit workout log",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "History",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionSection(
    totalCalories: Int,
    totalProtein: Double,
    totalCarbs: Double,
    totalFat: Double,
    todayMeals: List<MealEntity>,
    onHeaderClick: () -> Unit
) {
    val isProteinIncomplete = todayMeals.any { it.protein == null }
    val isCarbsIncomplete = todayMeals.any { it.carbs == null }
    val isFatIncomplete = todayMeals.any { it.fat == null }
    val isIncomplete = isProteinIncomplete || isCarbsIncomplete || isFatIncomplete
    val warningColor = Color(0xFFFBC02D) // Warning Yellow

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)
            .clickable { onHeaderClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Text(
                text = "Nutrition Tracker",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = totalCalories.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Calories",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MacroItem("Protein", totalProtein, isProteinIncomplete, warningColor)
                            MacroItem("Carbs", totalCarbs, isCarbsIncomplete, warningColor)
                            MacroItem("Fat", totalFat, isFatIncomplete, warningColor)
                        }
                    }

                    if (isIncomplete && todayMeals.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "* Some entries are missing macro data. Values are incomplete.",
                            color = warningColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (todayMeals.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No meals logged today",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MacroItem(label: String, value: Double, isIncomplete: Boolean, warningColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${value.toInt()}g",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isIncomplete) warningColor else MaterialTheme.colorScheme.onSurface
            )
            if (isIncomplete) {
                Text(
                    text = "*",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = warningColor
                )
            }
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
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
            totalCalories = 600,
            totalProtein = 30.0,
            totalCarbs = 70.0,
            totalFat = 20.0,
            isDarkTheme = false,
            onToggleTheme = {},
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
            onImportData = {},
            onExportData = {},
            onClearData = {}
        )
    }
}
