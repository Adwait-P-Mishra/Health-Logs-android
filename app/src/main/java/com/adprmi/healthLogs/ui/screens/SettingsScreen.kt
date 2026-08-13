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
import androidx.compose.ui.platform.LocalContext
import com.adprmi.healthLogs.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetCalories by settingsViewModel.targetCalories.collectAsState()
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(value = false) }
    var showTargetCalorieDialog by remember { mutableStateOf(value = false) }

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
                        settingsViewModel.setTargetCalories(newTarget)
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

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Data") },
            text = { Text("Are you sure you want to delete all exercise and meal logs? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.clearAllData()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
                headlineContent = { Text(if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode") },
                leadingContent = {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleTheme() },
                trailingContent = {
                    Switch(checked = isDarkTheme, onCheckedChange = { onToggleTheme() })
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Set Target Calories") },
                supportingContent = { Text(targetCalories?.let { "$it kcal" } ?: "Not set") },
                leadingContent = { Icon(Icons.Default.Adjust, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTargetCalorieDialog = true }
            )

            ListItem(
                headlineContent = { Text("Import Data") },
                supportingContent = { Text("Restore data from a JSON file") },
                leadingContent = { Icon(Icons.Default.FileDownload, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { importLauncher.launch(arrayOf("application/json", "application/octet-stream")) }
            )

            ListItem(
                headlineContent = { Text("Export Data") },
                supportingContent = { Text("Backup data to a JSON file") },
                leadingContent = { Icon(Icons.Default.FileUpload, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { exportLauncher.launch("health_logs_backup.json") }
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
