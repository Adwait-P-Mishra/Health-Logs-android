package com.mishrAdwait.healthLogs.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.mishrAdwait.healthLogs.ui.theme.MyApplicationTheme
import com.mishrAdwait.healthLogs.ui.theme.ThemePreviews

@Composable
fun DeleteConfirmationDialog(
    title: String = "Confirm Delete",
    message: String = "Are you sure you want to delete this? This action cannot be undone.",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@ThemePreviews
@Composable
fun DeleteConfirmationDialogPreview() {
    MyApplicationTheme {
        DeleteConfirmationDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
