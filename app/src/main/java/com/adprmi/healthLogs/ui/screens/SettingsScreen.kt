package com.adprmi.healthLogs.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.adprmi.healthLogs.model.AiProviderConfig
import com.adprmi.healthLogs.model.WeightUnit
import com.adprmi.healthLogs.viewmodel.SettingsViewModel

import androidx.compose.ui.tooling.preview.Preview
import com.adprmi.healthLogs.model.HeightUnit
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import com.adprmi.healthLogs.ui.theme.ThemePreviews
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    isDarkMode: Boolean?,
    onSetThemeMode: (Boolean?) -> Unit,
    onBack: () -> Unit,
    onNavigateToAiSetup: () -> Unit,
    onResetApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetCalories by settingsViewModel.targetCalories.collectAsState()
    val weightUnit by settingsViewModel.weightUnit.collectAsState()
    val userHeightCm by settingsViewModel.userHeightCm.collectAsState()
    val userGender by settingsViewModel.userGender.collectAsState()
    val heightUnit by settingsViewModel.heightUnit.collectAsState()
    val aiProviderConfig by settingsViewModel.aiProviderConfig.collectAsState()
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

    SettingsScreenContent(
        targetCalories = targetCalories,
        weightUnit = weightUnit,
        isDarkMode = isDarkMode,
        aiProviderConfig = aiProviderConfig,
        onSetThemeMode = onSetThemeMode,
        onBack = onBack,
        onSetTargetCalories = { settingsViewModel.setTargetCalories(it) },
        onSetWeightUnit = { settingsViewModel.setWeightUnit(it) },
        userHeightCm = userHeightCm,
        userGender = userGender,
        heightUnit = heightUnit,
        onSetUserHeightCm = { settingsViewModel.setUserHeightCm(it) },
        onSetUserGender = { settingsViewModel.setUserGender(it) },
        onSetHeightUnit = { settingsViewModel.setHeightUnit(it) },
        onNavigateToAiSetup = onNavigateToAiSetup,
        onImportData = { importLauncher.launch(arrayOf("application/json", "application/octet-stream")) },
        onExportData = { exportLauncher.launch("health_logs_backup.json") },
        onClearAllData = { keepSettings -> settingsViewModel.clearAllData(keepSettings, onResetApp) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    targetCalories: Int?,
    weightUnit: WeightUnit,
    isDarkMode: Boolean?,
    aiProviderConfig: AiProviderConfig?,
    userHeightCm: Double?,
    userGender: String?,
    heightUnit: com.adprmi.healthLogs.model.HeightUnit,
    onSetThemeMode: (Boolean?) -> Unit,
    onBack: () -> Unit,
    onSetTargetCalories: (Int?) -> Unit,
    onSetWeightUnit: (WeightUnit) -> Unit,
    onSetUserHeightCm: (Double?) -> Unit,
    onSetUserGender: (String?) -> Unit,
    onSetHeightUnit: (com.adprmi.healthLogs.model.HeightUnit) -> Unit,
    onNavigateToAiSetup: () -> Unit,
    onImportData: () -> Unit,
    onExportData: () -> Unit,
    onClearAllData: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearConfirm by remember { mutableStateOf(value = false) }
    var keepSettings by remember { mutableStateOf(value = true) }
    var showTargetCalorieDialog by remember { mutableStateOf(value = false) }
    var showWeightUnitDialog by remember { mutableStateOf(value = false) }
    var showProfileDialog by remember { mutableStateOf(value = false) }
    var showThemeDialog by remember { mutableStateOf(value = false) }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme Mode") },
            text = {
                Column {
                    listOf(
                        null to "System Default",
                        false to "Light Mode",
                        true to "Dark Mode"
                    ).forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetThemeMode(value)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isDarkMode == value,
                                onClick = {
                                    onSetThemeMode(value)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showProfileDialog) {
        var tempHeightCm by remember { mutableStateOf(userHeightCm?.toString() ?: "") }
        var tempGender by remember { mutableStateOf(userGender ?: "male") }
        var tempHeightUnit by remember { mutableStateOf(heightUnit) }
        
        // For ft/in
        var tempFt by remember { 
            mutableStateOf(if (heightUnit == com.adprmi.healthLogs.model.HeightUnit.FT_IN && userHeightCm != null) 
                (userHeightCm!! / 30.48).toInt().toString() else "") 
        }
        var tempIn by remember { 
            mutableStateOf(if (heightUnit == com.adprmi.healthLogs.model.HeightUnit.FT_IN && userHeightCm != null) 
                ((userHeightCm!! / 2.54) % 12).toInt().toString() else "") 
        }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Profile Information") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Gender
                    Column {
                        Text("Gender", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("male", "female", "self defined").forEach { gender ->
                                FilterChip(
                                    selected = tempGender == gender,
                                    onClick = { tempGender = gender },
                                    label = { Text(gender.replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                    }

                    // Height Unit
                    Column {
                        Text("Height Unit", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            com.adprmi.healthLogs.model.HeightUnit.entries.forEach { unit ->
                                FilterChip(
                                    selected = tempHeightUnit == unit,
                                    onClick = { tempHeightUnit = unit },
                                    label = { Text(unit.displayName) }
                                )
                            }
                        }
                    }

                    // Height Input
                    if (tempHeightUnit == com.adprmi.healthLogs.model.HeightUnit.CM) {
                        OutlinedTextField(
                            value = tempHeightCm,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) tempHeightCm = it },
                            label = { Text("Height (cm)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = tempFt,
                                onValueChange = { if (it.all { c -> c.isDigit() }) tempFt = it },
                                label = { Text("ft") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = tempIn,
                                onValueChange = { if (it.all { c -> c.isDigit() }) tempIn = it },
                                label = { Text("in") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSetUserGender(tempGender)
                        onSetHeightUnit(tempHeightUnit)
                        if (tempHeightUnit == com.adprmi.healthLogs.model.HeightUnit.CM) {
                            onSetUserHeightCm(tempHeightCm.toDoubleOrNull())
                        } else {
                            val ft = tempFt.toDoubleOrNull() ?: 0.0
                            val inch = tempIn.toDoubleOrNull() ?: 0.0
                            val totalCm = (ft * 30.48) + (inch * 2.54)
                            onSetUserHeightCm(if (totalCm > 0) totalCm else null)
                        }
                        showProfileDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTargetCalorieDialog) {
        var tempCalories by remember { mutableStateOf(targetCalories?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { showTargetCalorieDialog = false },
            title = { Text("Set Target Calories") },
            text = {
                OutlinedTextField(
                    value = tempCalories,
                    onValueChange = { if (it.all { char -> char.isDigit() }) tempCalories = it },
                    label = { Text("Calories (leave empty to clear)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newTarget = if (tempCalories.isBlank()) null else tempCalories.toIntOrNull()
                        onSetTargetCalories(newTarget)
                        showTargetCalorieDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetCalorieDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showWeightUnitDialog) {
        AlertDialog(
            onDismissRequest = { showWeightUnitDialog = false },
            title = { Text("Select Weight Unit") },
            text = {
                Column {
                    WeightUnit.entries.forEach { unit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetWeightUnit(unit)
                                    showWeightUnitDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = unit == weightUnit,
                                onClick = {
                                    onSetWeightUnit(unit)
                                    showWeightUnitDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (unit == WeightUnit.KG) "Kilograms (kg)" else "Pounds (lbs)")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWeightUnitDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Data") },
            text = {
                Column {
                    Text("This will delete all logs (workouts, meals, weights) and reset your current weight and calorie targets. This action cannot be undone.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier.clickable { keepSettings = !keepSettings }
                    ) {
                        Checkbox(
                            checked = keepSettings,
                            onCheckedChange = { keepSettings = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Keep AI configuration and theme settings", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllData(keepSettings)
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (keepSettings) "Clear Logs" else "Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                HorizontalDivider()
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ListItem(
                headlineContent = { Text("Profile Settings") },
                supportingContent = { 
                    val heightStr = if (userHeightCm != null) {
                        if (heightUnit == com.adprmi.healthLogs.model.HeightUnit.CM) {
                            "${userHeightCm!!.toInt()} cm"
                        } else {
                            val totalInches = userHeightCm!! / 2.54
                            val feet = (totalInches / 12).toInt()
                            val inches = (totalInches % 12).roundToInt()
                            "$feet' $inches\""
                        }
                    } else "Height not set"
                    Text("${userGender?.replaceFirstChar { it.uppercase() } ?: "Gender not set"} • $heightStr") 
                },
                leadingContent = { Icon(Icons.Default.Person, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showProfileDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("App Theme") },
                supportingContent = {
                    Text(
                        when (isDarkMode) {
                            null -> "System Default"
                            true -> "Dark Mode"
                            false -> "Light Mode"
                        }
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = when (isDarkMode) {
                            true -> Icons.Default.DarkMode
                            false -> Icons.Default.LightMode
                            null -> Icons.Default.SettingsSuggest
                        },
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showThemeDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Set Target Calories") },
                supportingContent = { Text(targetCalories?.let { "$it kcal" } ?: "Not set") },
                leadingContent = { Icon(Icons.Default.Adjust, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTargetCalorieDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Weight Unit") },
                supportingContent = { Text(if (weightUnit == WeightUnit.KG) "Kilograms (kg)" else "Pounds (lbs)") },
                leadingContent = { Icon(Icons.Default.Scale, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showWeightUnitDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("BYOK AI Settings") },
                supportingContent = { Text(aiProviderConfig?.let { "${it.name} (${it.model})" } ?: "Configure your own AI API details") },
                leadingContent = { Icon(Icons.Default.AutoAwesome, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAiSetup() },
                trailingContent = {
                    if (aiProviderConfig != null) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "AI configured",
                            tint = Color(0xFF4CAF50) // Green color
                        )
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Import Data") },
                supportingContent = { Text("Restore data from a JSON file") },
                leadingContent = { Icon(Icons.Default.FileDownload, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onImportData() }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Export Data") },
                supportingContent = { Text("Backup data to a JSON file") },
                leadingContent = { Icon(Icons.Default.FileUpload, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExportData() }
            )

            Spacer(modifier = Modifier.weight(1f))

            ListItem(
                headlineContent = { Text("Clear All Data", color = MaterialTheme.colorScheme.error) },
                leadingContent = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClearConfirm = true }
            )
        }
    }
}

@ThemePreviews
@Composable
fun SettingsScreenPreview() {
    MyApplicationTheme {
        SettingsScreenContent(
            targetCalories = 2000,
            weightUnit = WeightUnit.KG,
            isDarkMode = null,
            aiProviderConfig = AiProviderConfig("Gemini", "...", "key", "gemini-flash"),
            userHeightCm = 175.0,
            userGender = "male",
            heightUnit = HeightUnit.CM,
            onSetThemeMode = {},
            onBack = {},
            onSetTargetCalories = {},
            onSetWeightUnit = {},
            onSetUserHeightCm = {},
            onSetUserGender = {},
            onSetHeightUnit = {},
            onNavigateToAiSetup = {},
            onImportData = {},
            onExportData = {},
            onClearAllData = {}
        )
    }
}

