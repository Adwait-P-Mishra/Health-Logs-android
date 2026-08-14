package com.adprmi.healthLogs.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import com.adprmi.healthLogs.ui.theme.ThemePreviews
import com.adprmi.healthLogs.data.ExerciseEntity
import com.adprmi.healthLogs.model.WorkoutSet
import com.adprmi.healthLogs.util.DateUtils
import com.adprmi.healthLogs.viewmodel.WorkoutViewModel
import java.util.*

@Composable
fun ExerciseHistoryScreen(
    exerciseName: String,
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    onEditTriggered: (ExerciseEntity) -> Unit,
    onAddLogClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allLogs by viewModel.allLogs.collectAsState()

    val exerciseLogs = remember(allLogs, exerciseName) {
        allLogs.filter { it.exerciseName.trim().equals(exerciseName.trim(), ignoreCase = true) }
            .sortedByDescending { it.date }
    }

    ExerciseHistoryContent(
        exerciseName = exerciseName,
        exerciseLogs = exerciseLogs,
        onBack = onBack,
        onEditTriggered = onEditTriggered,
        onAddLogClicked = onAddLogClicked,
        onDeleteLog = { viewModel.deleteLog(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseHistoryContent(
    exerciseName: String,
    exerciseLogs: List<ExerciseEntity>,
    onBack: () -> Unit,
    onEditTriggered: (ExerciseEntity) -> Unit,
    onAddLogClicked: () -> Unit,
    onDeleteLog: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "$exerciseName History",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onAddLogClicked) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add log",
                                tint = MaterialTheme.colorScheme.secondary
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
        modifier = modifier.testTag("exercise_history_screen")
    ) { innerPadding ->
        if (exerciseLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No history logged yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .consumeWindowInsets(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp + innerPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    top = 16.dp + innerPadding.calculateTopPadding(),
                    end = 16.dp + innerPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    bottom = 16.dp + innerPadding.calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(exerciseLogs) { log ->
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
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = DateUtils.formatDate(Date(log.date), "MMM d, yyyy"),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
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

@ThemePreviews
@Composable
fun ExerciseHistoryPreview() {
    MyApplicationTheme {
        ExerciseHistoryContent(
            exerciseName = "Bench Press",
            exerciseLogs = listOf(
                ExerciseEntity(
                    exerciseName = "Bench Press",
                    sets = listOf(WorkoutSet(weight = "135", reps = "10")),
                    date = System.currentTimeMillis(),
                    notes = "Warmup"
                ),
                ExerciseEntity(
                    exerciseName = "Bench Press",
                    sets = listOf(WorkoutSet(weight = "225", reps = "5")),
                    date = System.currentTimeMillis() - 86400000 * 2,
                    notes = "Working set"
                )
            ),
            onBack = {},
            onEditTriggered = {},
            onAddLogClicked = {},
            onDeleteLog = {}
        )
    }
}
