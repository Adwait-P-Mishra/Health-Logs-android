package com.adprmi.healthLogs.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import com.adprmi.healthLogs.ui.theme.ThemePreviews
import com.adprmi.healthLogs.model.WorkoutSet
import com.adprmi.healthLogs.model.WeightUnit
import com.adprmi.healthLogs.data.ExerciseEntity
import com.adprmi.healthLogs.util.DateUtils
import com.adprmi.healthLogs.viewmodel.WorkoutViewModel
import java.util.*

@Composable
fun DailyWorkoutScreen(
    dateMillis: Long,
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    onEditTriggered: (ExerciseEntity) -> Unit,
    onNavigateToWeightTrend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allLogs by viewModel.allLogs.collectAsState()
    val allWeights by viewModel.allWeights.collectAsState()
    val targetDate = Date(dateMillis)
    val weightUnit by viewModel.weightUnit.collectAsState()

    val dailyLogs = remember(allLogs, dateMillis) {
        allLogs.filter { DateUtils.isSameDay(Date(it.date), targetDate) }
            .sortedByDescending { it.date }
    }
    
    val weightForDay = remember(allWeights, dateMillis) {
        // Find weight for the target date, or the most recent one before it
        val endOfDay = DateUtils.getEndOfDay(targetDate).time
        allWeights.filter { it.date <= endOfDay }
            .maxByOrNull { it.date }
    }

    DailyWorkoutContent(
        date = targetDate,
        dailyLogs = dailyLogs,
        userWeightKg = weightForDay?.weightKg,
        weightUnit = weightUnit,
        onBack = onBack,
        onEditTriggered = onEditTriggered,
        onDeleteLog = { viewModel.deleteLog(it) },
        onSetUserWeightKg = { viewModel.setUserWeightKg(it, targetDate) },
        onWeightTrendClick = onNavigateToWeightTrend,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyWorkoutContent(
    date: Date,
    dailyLogs: List<ExerciseEntity>,
    userWeightKg: Double?,
    weightUnit: WeightUnit,
    onBack: () -> Unit,
    onEditTriggered: (ExerciseEntity) -> Unit,
    onDeleteLog: (String) -> Unit,
    onSetUserWeightKg: (Double) -> Unit,
    onWeightTrendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showUserWeightDialog by remember { mutableStateOf(false) }

    if (showUserWeightDialog) {
        var tempWeight by remember { mutableStateOf(userWeightKg?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { showUserWeightDialog = false },
            title = { Text("Update Current Weight") },
            text = {
                OutlinedTextField(
                    value = tempWeight,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) tempWeight = it },
                    label = { Text("Weight in ${weightUnit.displayName}") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newWeight = tempWeight.toDoubleOrNull()
                        if (newWeight != null) {
                            onSetUserWeightKg(newWeight)
                        }
                        showUserWeightDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUserWeightDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Workouts: ${DateUtils.formatDate(date, "MMM d, yyyy")}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        modifier = modifier.testTag("daily_workout_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Daily Weight Update Section
            // User Weight Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .clickable { onWeightTrendClick() },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonitorWeight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Current Weight",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = userWeightKg?.let { "$it ${weightUnit.displayName}" } ?: "Not Available",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    IconButton(onClick = { showUserWeightDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit weight",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { onWeightTrendClick() }
//                        .padding(16.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Row(
//                        modifier = Modifier.weight(1f),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.MonitorWeight,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.secondary
//                        )
//                        Spacer(Modifier.width(12.dp))
//                        Column {
//                            Text(
//                                text = "Current Weight",
//                                style = MaterialTheme.typography.labelMedium,
//                                color = MaterialTheme.colorScheme.onSecondaryContainer
//                            )
//                            Text(
//                                text = "$userWeightKg ${weightUnit.displayName}",
//                                style = MaterialTheme.typography.titleMedium,
//                                fontWeight = FontWeight.Bold,
//                                color = MaterialTheme.colorScheme.onSecondaryContainer
//                            )
//                        }
//                    }
//                    Text(
//                        text = "Update",
//                        style = MaterialTheme.typography.labelLarge,
//                        color = MaterialTheme.colorScheme.secondary,
//                        fontWeight = FontWeight.Bold,
//                        modifier = Modifier
//                            .clickable { showUserWeightDialog = true }
//                            .padding(8.dp)
//                    )
//                }
//            }

            if (dailyLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No workouts logged for this day",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dailyLogs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(4.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = log.exerciseName,
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text("Barbell • Compound", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { onEditTriggered(log) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit log",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteLog(log.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete log",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            log.sets.forEachIndexed { index, set ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "SET ${index + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.width(48.dp)
                                        )
                                        Text(
                                            text = set.weight,
                                            style = MaterialTheme.typography.displaySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(set.unit.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = set.reps,
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("reps", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (index < log.sets.size - 1) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                }
                            }

                            if (log.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(
                                    Modifier,
                                    DividerDefaults.Thickness,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Notes: ${log.notes}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

@ThemePreviews
@Composable
fun DailyWorkoutPreview() {
    MyApplicationTheme {
        val sampleLogs = listOf(
            ExerciseEntity(
                exerciseName = "Bench Press",
                sets = listOf(
                    WorkoutSet(weight = "135", reps = "10"),
                    WorkoutSet(weight = "155", reps = "8"),
                    WorkoutSet(weight = "185", reps = "5")
                ),
                date = System.currentTimeMillis(),
                notes = "Warmup"
            ),
            ExerciseEntity(
                exerciseName = "Incline Dumbbell Fly",
                sets = listOf(
                    WorkoutSet(weight = "40", reps = "12"),
                    WorkoutSet(weight = "40", reps = "12")
                ),
                date = System.currentTimeMillis()
            )
        )

        DailyWorkoutContent(
            date = Date(),
            dailyLogs = sampleLogs,
            userWeightKg = 75.0,
            weightUnit = WeightUnit.KG,
            onBack = {},
            onEditTriggered = {},
            onDeleteLog = {},
            onSetUserWeightKg = {},
            onWeightTrendClick = {}
        )
    }
}

@ThemePreviews
@Composable
fun DailyWorkoutEmptyPreview() {
    MyApplicationTheme {
        DailyWorkoutContent(
            date = Date(),
            dailyLogs = emptyList(),
            userWeightKg = null,
            weightUnit = WeightUnit.KG,
            onBack = {},
            onEditTriggered = {},
            onDeleteLog = {},
            onSetUserWeightKg = {},
            onWeightTrendClick = {}
        )
    }
}
