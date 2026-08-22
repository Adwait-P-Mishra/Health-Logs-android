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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.adprmi.healthLogs.model.CalorieUiState
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import com.adprmi.healthLogs.ui.theme.ThemePreviews
import com.adprmi.healthLogs.data.MealEntity
import com.adprmi.healthLogs.util.DateUtils
import com.adprmi.healthLogs.viewmodel.MealViewModel
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
    val calorieUiState by viewModel.calorieUiState.collectAsState()
    val isEstimateLater by viewModel.isEstimateLater.collectAsState()

    BackHandler {
        onDismiss()
    }

    var showAssumptionsSheet by remember { mutableStateOf(false) }
    var assumptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentPrompt by remember { mutableStateOf("") }

    if (showAssumptionsSheet) {
        AiAssumptionsSheet(
            assumptions = assumptions,
            prompt = currentPrompt,
            onDismiss = { showAssumptionsSheet = false }
        )
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
        calorieUiState = calorieUiState,
        isEstimateLater = isEstimateLater,
        onEstimateLaterChange = { viewModel.isEstimateLater.value = it },
        onEstimateCalories = { viewModel.estimateCalories() },
        onResetCalorieUiState = { viewModel.resetCalorieUiState() },
        onShowAssumptions = { list, prompt ->
            assumptions = list
            currentPrompt = prompt
            showAssumptionsSheet = true
        },
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
    calorieUiState: CalorieUiState,
    isEstimateLater: Boolean,
    onEstimateLaterChange: (Boolean) -> Unit,
    onEstimateCalories: () -> Unit,
    onResetCalorieUiState: () -> Unit,
    onShowAssumptions: (List<String>, String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .testTag("meal_editor_sheet")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = if (editingId == null) "Add Meal" else "Edit Meal",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_meal_editor")) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSave,
                        enabled = canSave,
                        modifier = Modifier.testTag("save_meal_button_text")
                    ) {
                        Text("SAVE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
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

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
            ) {
                // Meal Name
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Text(
                            text = "MEAL NAME",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = mealName,
                            onValueChange = onMealNameChange,
                            placeholder = { Text("Enter meal name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("meal_name_input"),
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
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "PREVIOUS MEAL REFERENCE",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${prevLog.calories} kcal" +
                                        (prevLog.protein?.let { " • P: ${it}g" } ?: "") +
                                        (prevLog.carbs?.let { " • C: ${it}g" } ?: "") +
                                        (prevLog.fat?.let { " • F: ${it}g" } ?: ""),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Nutrition Facts Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nutrition Facts",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Estimate Later",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = isEstimateLater,
                                onCheckedChange = onEstimateLaterChange,
                                modifier = Modifier.scale(0.8f).testTag("estimate_later_toggle")
                            )

                            if (!isEstimateLater) {
                                Spacer(Modifier.width(8.dp))
                                AiEstimateButton(
                                    uiState = calorieUiState,
                                    onClick = onEstimateCalories,
                                    onDismissError = onResetCalorieUiState,
                                    onShowAssumptions = onShowAssumptions
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(Modifier.height(16.dp))

                        // Calories
                        Text(
                            text = "TOTAL CALORIES",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            OutlinedTextField(
                                value = if (isEstimateLater) "" else calories,
                                onValueChange = onCaloriesChange,
                                placeholder = { Text(if (isEstimateLater) "TBD" else "0") },
                                enabled = !isEstimateLater,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("meal_calories_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent
                                ),
                                textStyle = MaterialTheme.typography.displaySmall,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true
                            )
                            Text("kcal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(bottom = 8.dp))
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Spacer(Modifier.height(16.dp))

                        // Macros Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MacroInputField("PROTEIN", protein, onProteinChange, Modifier.weight(1f))
                            MacroInputField("CARBS", carbs, onCarbsChange, Modifier.weight(1f))
                            MacroInputField("FAT", fat, onFatChange, Modifier.weight(1f))
                        }
                    }
                }

                // Notes Text field
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Text(
                            text = "NOTES (OPTIONAL)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = onNotesChange,
                            placeholder = { Text("Add recipe details, ingredients, or how you felt...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("meal_notes_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedContainerColor = MaterialTheme.colorScheme.background
                            ),
                            maxLines = 4
                        )
                    }
                }
                Spacer(Modifier.height(120.dp))
            }
        }

        // Sticky Bottom Save Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
                    .testTag("save_meal_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Save Meal",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

@Composable
fun MacroInputField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background
            ),
            textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        Text("g", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp))
    }
}

@ThemePreviews
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
            calorieUiState = CalorieUiState.Idle,
            isEstimateLater = false,
            onEstimateLaterChange = {},
            onEstimateCalories = {},
            onResetCalorieUiState = {},
            onShowAssumptions = { _, _ -> },
            onSave = {},
            onDismiss = {}
        )
    }
}
