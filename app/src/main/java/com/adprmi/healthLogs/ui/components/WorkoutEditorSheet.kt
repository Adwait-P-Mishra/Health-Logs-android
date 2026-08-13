package com.adprmi.healthLogs.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import com.adprmi.healthLogs.data.ExerciseEntity
import com.adprmi.healthLogs.model.WorkoutSet
import com.adprmi.healthLogs.util.DateUtils
import com.adprmi.healthLogs.viewmodel.WorkoutViewModel
import java.util.*

@Composable
fun WorkoutEditorSheet(
    viewModel: WorkoutViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val exerciseName by viewModel.exerciseName.collectAsState()
    val draftSets by viewModel.draftSets.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val showingSuggestions by viewModel.showingSuggestions.collectAsState()
    val suggestions by viewModel.exerciseSuggestions.collectAsState()
    val lastMatchingLog by viewModel.lastMatchingLog.collectAsState()
    val canSave by viewModel.canSaveLog.collectAsState()
    val editingId by viewModel.editingLogId.collectAsState()

    BackHandler {
        onDismiss()
    }

    WorkoutEditorContent(
        exerciseName = exerciseName,
        onExerciseNameChange = {
            viewModel.exerciseName.value = it
            viewModel.showingSuggestions.value = it.trim().length >= 2
        },
        draftSets = draftSets,
        onUpdateSet = { index, weight, reps -> viewModel.updateSet(index, weight, reps) },
        onAddSet = { viewModel.addSetCopyingPrevious() },
        onRemoveSet = { id -> viewModel.removeSet(id) },
        notes = notes,
        onNotesChange = { viewModel.notes.value = it },
        showingSuggestions = showingSuggestions,
        suggestions = suggestions,
        onApplySuggestion = { viewModel.applySuggestion(it) },
        lastMatchingLog = lastMatchingLog,
        canSave = canSave,
        editingId = editingId,
        onSave = {
            viewModel.saveCurrentLog()
            onDismiss()
        },
        onDismiss = onDismiss,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditorContent(
    exerciseName: String,
    onExerciseNameChange: (String) -> Unit,
    draftSets: List<WorkoutSet>,
    onUpdateSet: (Int, String, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    showingSuggestions: Boolean,
    suggestions: List<ExerciseEntity>,
    onApplySuggestion: (ExerciseEntity) -> Unit,
    lastMatchingLog: ExerciseEntity?,
    canSave: Boolean,
    editingId: String?,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("workout_editor_sheet")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = if (editingId == null) "Add Exercise" else "Edit Exercise",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_workout_editor")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                )
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Exercise Name Text field
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "EXERCISE NAME",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = exerciseName,
                            onValueChange = onExerciseNameChange,
                            placeholder = { Text("e.g. Bench Press, Squat, Deadlift") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("exercise_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )

                        // Autocomplete Suggestions popup list
                        if (showingSuggestions && suggestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(4.dp)) {
                                    suggestions.forEach { suggestion ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onApplySuggestion(suggestion) }
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = suggestion.exerciseName,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "Last: ${
                                                        DateUtils.formatDate(
                                                            Date(suggestion.date),
                                                            "MMM d"
                                                        )
                                                    }",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = "${suggestion.sets.size} sets",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Previous matching reference log
                lastMatchingLog?.let { prevLog ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "PREVIOUS SESSION",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            prevLog.sets.forEachIndexed { index, set ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Set ${index + 1}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "${set.weight} lbs x ${set.reps}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            if (prevLog.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Notes: ${prevLog.notes}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Sets List Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CURRENT SESSION",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Set", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.width(48.dp))
                            Text("Lbs", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("Reps", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Spacer(Modifier.width(48.dp))
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(Modifier.height(8.dp))

                        draftSets.forEachIndexed { index, set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(48.dp),
                                    textAlign = TextAlign.Center
                                )

                                OutlinedTextField(
                                    value = set.weight,
                                    onValueChange = { input ->
                                        val filtered = input.filter { it.isDigit() || it == '.' }
                                        if (filtered.count { it == '.' } <= 1) {
                                            onUpdateSet(index, filtered, set.reps)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("set_weight_input_$index"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                        focusedContainerColor = MaterialTheme.colorScheme.background
                                    ),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = set.reps,
                                    onValueChange = { input ->
                                        val filtered = input.filter { it.isDigit() }
                                        onUpdateSet(index, set.weight, filtered)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("set_reps_input_$index"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                        focusedContainerColor = MaterialTheme.colorScheme.background
                                    ),
                                    singleLine = true
                                )

                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        onRemoveSet(set.id)
                                    },
                                    enabled = draftSets.size > 1,
                                    modifier = Modifier.testTag("delete_set_button_$index")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete set",
                                        tint = if (draftSets.size > 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                onAddSet()
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Set", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                // Notes Text field
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "NOTES",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = onNotesChange,
                            placeholder = { Text("How did it feel?") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("exercise_notes_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            maxLines = 4
                        )
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }

        // Sticky Bottom Save Button
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
                    .testTag("save_workout_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Save Exercise",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutEditorPreview() {
    MyApplicationTheme {
        WorkoutEditorContent(
            exerciseName = "Bench Press",
            onExerciseNameChange = {},
            draftSets = listOf(
                WorkoutSet(weight = "135", reps = "10"),
                WorkoutSet(weight = "185", reps = "8"),
                WorkoutSet(weight = "225", reps = "5")
            ),
            onUpdateSet = { _, _, _ -> },
            onAddSet = {},
            onRemoveSet = {},
            notes = "Felt good, stable bar path.",
            onNotesChange = {},
            showingSuggestions = false,
            suggestions = emptyList(),
            onApplySuggestion = {},
            lastMatchingLog = ExerciseEntity(
                exerciseName = "Bench Press",
                sets = listOf(WorkoutSet(weight = "225", reps = "4")),
                date = System.currentTimeMillis() - 86400000 * 3,
                notes = "Previous best"
            ),
            canSave = true,
            editingId = null,
            onSave = {},
            onDismiss = {}
        )
    }
}
