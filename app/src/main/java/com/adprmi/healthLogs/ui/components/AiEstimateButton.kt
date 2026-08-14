package com.adprmi.healthLogs.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adprmi.healthLogs.model.CalorieUiState
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import com.adprmi.healthLogs.ui.theme.ThemePreviews

@Composable
fun AiEstimateButton(
    uiState: CalorieUiState,
    onClick: () -> Unit,
    onDismissError: () -> Unit,
    onShowAssumptions: (List<String>, String) -> Unit
) {
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is CalorieUiState.Error) {
            errorMessage = uiState.message
            showErrorDialog = true
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                onDismissError()
            },
            title = { Text("Estimation Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                    onDismissError()
                }) {
                    Text("OK")
                }
            }
        )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        when (uiState) {
            is CalorieUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is CalorieUiState.NotConfigured -> {
                TextButton(onClick = onClick) {
                    Text("Set up AI to estimate", style = MaterialTheme.typography.labelSmall)
                }
            }
            else -> {
                Button(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Estimate", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (uiState is CalorieUiState.Success && uiState.estimate.assumptions.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { onShowAssumptions(uiState.estimate.assumptions, uiState.prompt) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Show Assumptions",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun AiEstimateButtonPreview() {
    MyApplicationTheme {
        AiEstimateButton(
            uiState = CalorieUiState.Idle,
            onClick = {},
            onDismissError = {},
            onShowAssumptions = { _, _ -> }
        )
    }
}

@ThemePreviews
@Composable
fun AiEstimateButtonLoadingPreview() {
    MyApplicationTheme {
        AiEstimateButton(
            uiState = CalorieUiState.Loading,
            onClick = {},
            onDismissError = {},
            onShowAssumptions = { _, _ -> }
        )
    }
}

@ThemePreviews
@Composable
fun AiEstimateButtonNotConfiguredPreview() {
    MyApplicationTheme {
        AiEstimateButton(
            uiState = CalorieUiState.NotConfigured,
            onClick = {},
            onDismissError = {},
            onShowAssumptions = { _, _ -> }
        )
    }
}
