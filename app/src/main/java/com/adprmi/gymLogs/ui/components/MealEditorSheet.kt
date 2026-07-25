package com.adprmi.gymLogs.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import com.adprmi.gymLogs.ui.theme.MyApplicationTheme
import com.adprmi.gymLogs.data.MealEntity
import com.adprmi.gymLogs.util.DateUtils
import com.adprmi.gymLogs.viewmodel.MealViewModel
import java.util.*

@Composable
fun MealEditorSheet(
    viewModel: MealViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mealName by viewModel.mealName.collectAsState()
    val calories by viewModel.calories.collectAsState()
    val protein by viewModel.protein.collectAsState()
    val carbs by viewModel.carbs.collectAsState()
    val fat by viewModel.fat.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val showingSuggestions by viewModel.showingSuggestions.collectAsState()
    val suggestions by viewModel.mealSuggestions.collectAsState()
    val lastMatchingLog by viewModel.lastMatchingLog.collectAsState()
    val canSave by viewModel.canSaveLog.collectAsState()
    val editingId by viewModel.editingLogId.collectAsState()

    BackHandler {
        onDismiss()
    }

    MealEditorContent(
        mealName = mealName,
        onMealNameChange = {
            viewModel.mealName.value = it
            viewModel.showingSuggestions.value = it.trim().length >= 2
        },
        calories = calories,
        onCaloriesChange = { it -> viewModel.calories.value = it.filter { it.isDigit() } },
        protein = protein,
        onProteinChange = { input ->
            val filtered = input.filter { it.isDigit() || it == '.' }
            if (filtered.count { it == '.' } <= 1) viewModel.protein.value = filtered
        },
        carbs = carbs,
        onCarbsChange = { input ->
            val filtered = input.filter { it.isDigit() || it == '.' }
            if (filtered.count { it == '.' } <= 1) viewModel.carbs.value = filtered
        },
        fat = fat,
        onFatChange = { input ->
            val filtered = input.filter { it.isDigit() || it == '.' }
            if (filtered.count { it == '.' } <= 1) viewModel.fat.value = filtered
        },
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
fun MealEditorContent(
    mealName: String,
    onMealNameChange: (String) -> Unit,
    calories: String,
    onCaloriesChange: (String) -> Unit,
    protein: String,
    onProteinChange: (String) -> Unit,
    carbs: String,
    onCarbsChange: (String) -> Unit,
    fat: String,
    onFatChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    showingSuggestions: Boolean,
    suggestions: List<MealEntity>,
    onApplySuggestion: (MealEntity) -> Unit,
    lastMatchingLog: MealEntity?,
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
            .testTag("meal_editor_sheet")
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
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_meal_editor")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }

                Text(
                    text = if (editingId == null) "Add Meal" else "Edit Meal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = onSave,
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("save_meal_button")
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
                // Meal Name
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Meal Name",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = mealName,
                            onValueChange = onMealNameChange,
                            placeholder = { Text("e.g. Breakfast, Chicken & Rice, Protein Shake") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("meal_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Fastfood,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )

                        // Autocomplete Suggestions
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
                                                    text = suggestion.mealName,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "Last: ${
                                                        DateUtils.formatDate(
                                                            Date(
                                                                suggestion.date
                                                            ), "MMM d"
                                                        )
                                                    }",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = "${suggestion.calories} kcal",
                                                fontSize = 13.sp,
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

                // Previous Meal Log Reference Card
                lastMatchingLog?.let { prevLog ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Previous Meal (Reference)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${prevLog.calories} kcal" +
                                        (prevLog.protein?.let { " • P: ${it}g" } ?: "") +
                                        (prevLog.carbs?.let { " • C: ${it}g" } ?: "") +
                                        (prevLog.fat?.let { " • F: ${it}g" } ?: ""),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
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

                // Calories Field (Required)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Calories (Required)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = calories,
                            onValueChange = onCaloriesChange,
                            placeholder = { Text("e.g. 450") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("meal_calories_input"),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )
                    }
                }

                // Macronutrients Segmented Section (Optional)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Macronutrients (Optional)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Leave blank if you are not tracking macros",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Protein
                            OutlinedTextField(
                                value = protein,
                                onValueChange = onProteinChange,
                                label = { Text("Protein (g)") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("meal_protein_input"),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            // Carbs
                            OutlinedTextField(
                                value = carbs,
                                onValueChange = onCarbsChange,
                                label = { Text("Carbs (g)") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("meal_carbs_input"),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            // Fat
                            OutlinedTextField(
                                value = fat,
                                onValueChange = onFatChange,
                                label = { Text("Fat (g)") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("meal_fat_input"),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }
                    }
                }

                // Notes Text field
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                            placeholder = { Text("Add brand details, notes, etc.") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("meal_notes_input"),
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
fun MealEditorPreview() {
    MyApplicationTheme {
        MealEditorContent(
            mealName = "Chicken & Rice",
            onMealNameChange = {},
            calories = "650",
            onCaloriesChange = {},
            protein = "45",
            onProteinChange = {},
            carbs = "60",
            onCarbsChange = {},
            fat = "12",
            onFatChange = {},
            notes = "With broccoli and hot sauce.",
            onNotesChange = {},
            showingSuggestions = false,
            suggestions = emptyList(),
            onApplySuggestion = {},
            lastMatchingLog = MealEntity(
                mealName = "Chicken & Rice",
                calories = 600,
                protein = 40.0,
                carbs = 55.0,
                fat = 10.0,
                date = System.currentTimeMillis() - 86400000,
                notes = "Meal prep"
            ),
            canSave = true,
            editingId = null,
            onSave = {},
            onDismiss = {}
        )
    }
}
