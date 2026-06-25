package com.example.ui.components

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.R
import com.example.ui.theme.MyApplicationTheme
import com.example.data.ExerciseEntity
import com.example.model.WorkoutSet
import com.example.util.DateUtils
import com.example.viewmodel.WorkoutViewModel
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("workout_editor_sheet")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_workout_editor")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }

                Text(
                    text = if (editingId == null) "Add Exercise" else "Edit Exercise",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = onSave,
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("save_workout_button")
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Exercise Name Text field
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Exercise Name",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = exerciseName,
                            onValueChange = onExerciseNameChange,
                            placeholder = { Text("e.g. Bench Press, Squat, Deadlift") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("exercise_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null
                                )
                            },
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
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Previous Session (Reference)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            prevLog.sets.forEachIndexed { index, set ->
                                Text(
                                    text = "Set ${index + 1}: ${set.weight} lbs x ${set.reps} reps",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sets",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            TextButton(
                                onClick = onAddSet,
                                modifier = Modifier.testTag("add_set_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Set", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        draftSets.forEachIndexed { index, set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Set ${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.width(48.dp)
                                )

                                OutlinedTextField(
                                    value = set.weight,
                                    onValueChange = { input ->
                                        // Allow digits and single dot
                                        val filtered = input.filter { it.isDigit() || it == '.' }
                                        if (filtered.count { it == '.' } <= 1) {
                                            onUpdateSet(index, filtered, set.reps)
                                        }
                                    },
                                    label = { Text("Lbs") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("set_weight_input_$index"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = set.reps,
                                    onValueChange = { input ->
                                        val filtered = input.filter { it.isDigit() }
                                        onUpdateSet(index, set.weight, filtered)
                                    },
                                    label = { Text("Reps") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("set_reps_input_$index"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                IconButton(
                                    onClick = { onRemoveSet(set.id) },
                                    enabled = draftSets.size > 1,
                                    modifier = Modifier.testTag("delete_set_button_$index")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete set",
                                        tint = if (draftSets.size > 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.2f
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes Text field
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Notes (Optional)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = onNotesChange,
                        placeholder = { Text("Add any notes, form feedback, feel, etc.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("exercise_notes_input"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )
                }
            }
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
